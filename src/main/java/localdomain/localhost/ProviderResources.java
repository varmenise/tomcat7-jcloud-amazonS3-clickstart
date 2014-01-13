/*
 * Copyright 2010-2013, the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package localdomain.localhost;
//
//import com.amazonaws.AmazonServiceException;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.ObjectMetadata;

import javax.annotation.Nonnull;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Logger;

public class ProviderResources {

	protected final Logger logger = Logger.getLogger(getClass().getName());
	private final Random random = new Random();
	private String bucketName;
	private BlobStore blobstore;

	/**
	 * @param accessKey
	 * @param secretKey
	 * @param bucketName
	 * @throws AmazonServiceException
	 */
	public ProviderResources(String accessKey, String secretKey,
			String bucketName) {
		//AWSCredentials credentials;

		// init
		BlobStoreContext context = ContextBuilder.newBuilder("aws-s3")
				.credentials(accessKey, secretKey)
				.buildView(BlobStoreContext.class);

		blobstore = context.getBlobStore();

		//Location is a region, provider or another scope in which a container can be created to ensure data locality. If you don't have a location concern, pass null to accept the default.
		boolean created = blobstore.createContainerInLocation(null, bucketName);
		if (created) {
			logger.info("the container didn't exist, but does now");
		} else {
			logger.info("the container already existed");
		}

		this.bucketName = bucketName;
	}

	/**
	 * Upload an image to Amazon S3
	 * 
	 * @param in
	 * @param objectMetadata
	 * @param fileName
	 * @return uploaded image URL
	 * @throws AmazonServiceException
	 * @throws IllegalArgumentException
	 */
	@Nonnull
	public String uploadImage(@Nonnull InputStream in,
			@Nonnull String fileName, Long imageSize, String contentType)
			throws IllegalArgumentException {

		int idx = fileName.lastIndexOf(".");
		if (idx == -1)
			throw new IllegalArgumentException(
					"Invalid filename without extension: " + fileName);

		String uploadedFileExtension = fileName.substring(idx + 1,
				fileName.length());

		HashSet<String> permittedFileExtensions = new HashSet<String>();
		permittedFileExtensions.add("jpg");
		permittedFileExtensions.add("png");
		permittedFileExtensions.add("gif");

		uploadedFileExtension = uploadedFileExtension.toLowerCase();

		if (!permittedFileExtensions.contains(uploadedFileExtension))
			throw new IllegalArgumentException("Invalid file extension '"
					+ uploadedFileExtension + "' in " + fileName);

		Long randomImageName = Math.abs(random.nextLong());
		String randomImageNameWithFileExtension = randomImageName + "."
				+ uploadedFileExtension;

		// add blob
		Blob blob = blobstore.blobBuilder(randomImageNameWithFileExtension)
				.payload(in).contentLength(imageSize).contentType(contentType)
				.build();

		blobstore.putBlob(bucketName, blob);

		String imageUrl = "https://s3.amazonaws.com/" + bucketName + "/"
				+ randomImageNameWithFileExtension;
		logger.info("Image uploaded to Amazon S3 " + imageUrl);

		return imageUrl;
	}
}
