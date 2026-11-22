<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>SmartLogin</title>
<link rel="stylesheet" href="assets/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/dataTables.bootstrap5.min.css">
</head>
<body class="bg-dark">
	<div class="container-fluid">
		<div class="card col-md-4 text-center mx-auto mt-5">
			<div class="card-header">
				<h1>SMART SHOP</h1><hr>
				<h3 class="text-primary">CONNEXION</h3>
			</div>
			<form action="smartLogin" method="post">
			<div class="card-body">
				<div class="col-md-9 mx-auto my-3">
					<input class="form-control" name="empuname" placeholder="Nom d'utilisateur">
				</div>
				<div class="col-md-9 mx-auto mb-3">
					<input type="password" class="form-control" name="emppword" placeholder="Mot de passe">
				</div>
			</div>
			<div class="card-footer">
				<button type="submit" class="btn btn-primary">Se connecter</button>
			</div>
			</form>
		</div>
	</div>
	<script src="assets/js/jquery-3.5.1.js"></script>
	<script src="assets/js/bootstrap.bundle.min.js"></script>
	<script src="assets/js/jquery.dataTables.min.js"></script>
	<script src="assets/js/dataTables.bootstrap5.min.js"></script>
</body>
</html>