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

//import com.amazonaws.services.s3.model.ObjectMetadata;

import localdomain.localhost.domain.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(value = "/product/upload", loadOnStartup = 2)
@MultipartConfig
public class ProductImageUploadServlet extends HttpServlet {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	@Nonnull
	private EntityManagerFactory emf;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		emf = (EntityManagerFactory) config.getServletContext().getAttribute(
				EntityManagerFactory.class.getName());
		if (emf == null)
			throw new ServletException(
					"JPA EntityManagerFactory not found in ServletContext");

		logger.debug("Servlet initialized, JPA EntityManagerFactory loaded");
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			String itemName = request.getParameter("productName");
			String credits = request.getParameter("imageCredits");

			InputStream is = request.getPart("imageFile").getInputStream();
			Long imageSize = request.getPart("imageFile").getSize();
			String contentType = request.getPart("imageFile").getContentType();
			String imageName = getFileNameFromHeader(request.getPart(
					"imageFile").getHeader("content-disposition"));

			ServletContext context = request.getSession().getServletContext();
			ProviderResources s3Resources = (ProviderResources) context
					.getAttribute(ProviderResources.class.getName());

			String imageUrl = s3Resources.uploadImage(is, imageName, imageSize,
					contentType);

			EntityManager em = emf.createEntityManager();

			em.getTransaction().begin();
			em.persist(new Product(itemName, imageUrl, credits));
			em.getTransaction().commit();

			response.sendRedirect(request.getContextPath() + "/product/list");
		} catch (Exception e) {
			request.setAttribute("throwable", e);
			request.getRequestDispatcher("/WEB-INF/jsp/products.jsp").forward(
					request, response);
		}

	}

	@Nonnull
	private String getFileNameFromHeader(@Nonnull String header)
			throws IllegalArgumentException {
		String fileName = null;

		for (String onePiece : header.split(";")) {
			if (onePiece.contains("filename=")) {
				String myPieces[] = onePiece.split("=");
				fileName = myPieces[1].replaceAll("\"", "").trim();
			}
		}
		if (null == fileName)
			throw new IllegalArgumentException(
					"The Header provided is not valid");
		return fileName;
	}
}
