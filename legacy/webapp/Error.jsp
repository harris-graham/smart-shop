<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isErrorPage="true"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Error</title>
<link rel="stylesheet" href="assets/css/bootstrap.css">
</head>
<body>
	<div class="text-center">
		<h1>ERREUR</h1>
		<h3><%=exception.getMessage()%></h3>
	</div>
</body>
</html>