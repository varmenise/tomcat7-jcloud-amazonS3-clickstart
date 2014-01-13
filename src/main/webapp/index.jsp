<%@ page import="java.io.PrintWriter" %>
<html>
<head>
    <title>Tomcat 7 Hibernate JPA Amazon S3 Demo</title>
</head>
<body>
<h1>Tomcat 7 Hibernate JPA Amazon S3 Demo</h1>


<%
    if (request.getAttribute("throwable") != null) {
        Throwable t = (Throwable) request.getAttribute("throwable");
%><p>An exception occurred: <strong><code><%= t.getMessage() %></code></strong></p><%
    }
%>
<form action="${pageContext.request.contextPath}/jcloud/configuration" method="post">
    <table>
        <tr>
            <td> Amazon accessKey:</td>
            <td><input type="text" name="accessKey" value="" size="15"></td>
        </tr>

        <tr>
            <td> Amazon secretKey:</td>
            <td><input type="password" name="secretKey" value="" size="15"></td>
        </tr>

        <tr>
            <td> Amazon S3 bucket name:
            <td><input type="text" name="bucket" value="" size="15"></td>
        </tr>

        <tr>
            <td>&nbsp;</td>
            <td align="right"><input type="submit" name="S3OpenConfiguration" value="Save configuration and enter site"/></td>
        </tr>
    </table>
</form>

<h1>Amazon S3 Bucket Configuration Tip</h1>

Don't forget to allow anonymous read access to your Amazon S3 Bucket with a Bucket Policy similar to:
<code><pre>
{ "Statement" : [ { "Action" : "s3:GetObject",
        "Effect" : "Allow",
        "Principal" : { "AWS" : "*" },
        "Resource" : "arn:aws:s3:::YOUR_BUCKET_NAME*",
        "Sid" : "PublicReadGetObject"
      } ],
  "Version" : "2008-10-17"
}
</pre>
</code>

More details at <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucketPolicies.html">Amazon s3 - Using
    Bucket Policies</a>
</br>
How to reuse AWS in your applications: <a href="http://developer.cloudbees.com/bin/view/RUN/AmazonWebServices">Amazon
    Web Services on CloudBees applications</a>


<%
    if (request.getAttribute("throwable") != null) {
        Throwable t = (Throwable) request.getAttribute("throwable");
%><h1>Detailed exception</h1>
<code><pre><%
    t.printStackTrace(new PrintWriter(out));
%></pre>
</code><%
    }
%>
</body>
</html>
