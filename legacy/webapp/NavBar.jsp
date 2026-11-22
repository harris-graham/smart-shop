<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>NavBar</title>
<link rel="stylesheet" href="assets/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/dataTables.bootstrap5.min.css">
</head>
<body>
	<nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
		<div class="container-fluid">
			<a class="navbar-brand"
				href="<%=request.getContextPath()%>/smartShop">SMART SHOP</a>
			<button class="navbar-toggler" type="button"
				data-bs-toggle="collapse" data-bs-target="#content">
				<span class="navbar-toggler-icon"></span>
			</button>
			<div class="collapse navbar-collapse" id="content">
				<ul class="navbar-nav mb-2 mb-lg-0">
					<li class="nav-item"><a class="nav-link active"
						href="<%=request.getContextPath()%>/smartSelect">Smartphone</a></li>
					<li class="nav-item"><a class="nav-link active"
						href="<%=request.getContextPath()%>/custSelect">Client</a></li>
					<c:if test="${session.empname == 'Admin'}">
						<li class="nav-item"><a class="nav-link active"
							href="<%=request.getContextPath()%>/empSelect">Employ&eacute;</a></li>
						<li class="nav-item"><a class="nav-link active"
							href="<%=request.getContextPath()%>/saleSelect">Vente</a></li>
					</c:if>
				</ul>
				<ul class="navbar-nav ms-auto">
					<li class="nav-item"><a class="nav-link active" role="button"
						data-bs-toggle="modal" data-bs-target="#user">Utilisateur</a></li>
					<li class="nav-item"><a class="nav-link active" role="button"
						data-bs-toggle="modal" data-bs-target="#account">Compte</a></li>
					<li class="nav-item"><a class="nav-link active" role="button"
						data-bs-toggle="modal" data-bs-target="#logout">Deconnexion</a></li>
				</ul>
			</div>
		</div>
	</nav>
	<br>
	<br>
	<div class="modal fade" id="user" tabindex="-1"
		aria-labelledby="exampleModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-dialog-scrollable">
			<div class="modal-content">
				<div class="modal-header">
					<h3 class="modal-title text-primary" id="exampleModalLabel">Utilisateur</h3>
					<button class="btn-close" data-bs-dismiss="modal"
						aria-label="Close"></button>
				</div>
				<div class="modal-body text-center">
					<h5>Utilisateur actuel :</h5>
					<h6>
						<c:out value="${session.empname}" />
					</h6>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary"
						data-bs-dismiss="modal">Fermer</button>
				</div>
			</div>
		</div>
	</div>
	<div class="modal fade" id="account" tabindex="-1"
		aria-labelledby="exampleModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-dialog-scrollable">
			<div class="modal-content">
				<div class="modal-header">
					<h3 class="modal-title text-warning" id="exampleModalLabel">Modification
						de compte</h3>
					<button class="btn-close" data-bs-dismiss="modal"
						aria-label="Close"></button>
				</div>
				<form action="empAccUpdate" method="post">
					<div class="modal-body">
						<input type="hidden" name="empid"
							value="<c:out value="${session.empid}"/>">
						<div class="row mb-3">
							<div class="col-md-3">
								<label>Utilisateur :</label>
							</div>
							<div class="col-md-9">
								<input class="form-control" name="empuname"
									value="<c:out value="${session.empuname}"/>" maxLength="20" required>
							</div>
						</div>
						<div class="row mb-3">
							<div class="col-md-3">
								<label>Mot de passe</label>
							</div>
							<div class="col-md-9">
								<input type="password" class="form-control" name="emppword" maxLength="20">
							</div>
						</div>
					</div>
					<div class="modal-footer">
						<button type="submit" class="btn btn-warning text-light">Modifier</button>
						<button type="button" class="btn btn-secondary"
							data-bs-dismiss="modal">Fermer</button>
					</div>
				</form>
			</div>
		</div>
	</div>
	<div class="modal fade" id="logout" tabindex="-1"
		aria-labelledby="exampleModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-dialog-scrollable">
			<div class="modal-content">
				<div class="modal-header">
					<h3 class="modal-title text-primary" id="exampleModalLabel">Deconnexion</h3>
					<button class="btn-close" data-bs-dismiss="modal"
						aria-label="Close"></button>
				</div>
				<div class="modal-body text-center">
					<h5>
						Voulez-vous vraiment deconnecter votre compte,
						<c:out value="${session.empname}" />
						?
					</h5>
				</div>
				<div class="modal-footer">
					<a href="smartLogout"><button class="btn btn-primary">Se
							deconnecter</button></a>
					<button type="button" class="btn btn-secondary"
						data-bs-dismiss="modal">Fermer</button>
				</div>
			</div>
		</div>
	</div>
	<script src="assets/js/jquery-3.5.1.js"></script>
	<script src="assets/js/bootstrap.bundle.min.js"></script>
	<script src="assets/js/jquery.dataTables.min.js"></script>
	<script src="assets/js/dataTables.bootstrap5.min.js"></script>
</body>
</html>