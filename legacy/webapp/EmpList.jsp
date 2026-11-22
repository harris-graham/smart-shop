<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>EmpList</title>
</head>
<body>
	<%@ include file="NavBar.jsp"%>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#dataTableEmp').DataTable();
			$('#dataTableSaleNb').DataTable();
		});
	</script>
	<div class="container-fluid">
		<div class="row">
			<div class="card col-md-8 text-center pb-2">
				<h1 class="my-5">Employ&eacute;</h1>
				<hr>
				<div class="mb-3">
					<button class="btn btn-primary" data-bs-toggle="modal"
						data-bs-target="#empForm">Ajouter</button>
				</div>
				<div class="modal fade" id="empForm" tabindex="-1"
					aria-labelledby="exampleModalLabel" aria-hidden="true">
					<div class="modal-dialog modal-dialog-scrolable">
						<div class="modal-content">
							<div class="modal-header">
								<h3 class="modal-title text-primary" id="exampleModalLabel">Ajout
									d'employ&eacute;</h3>
								<button class="btn-close" data-bs-dismiss="modal"
									aria-label="Close"></button>
							</div>
							<form action="empInsert" method="post">
								<div class="modal-body">
									<div class="row mb-3">
										<div class="col-md-3">
											<label class="col-form-label">Nom :</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="empname" maxLength="60" required>
										</div>
									</div>
									<div class="row mb-3">
										<div class="col-md-3">
											<label class="col-form-label">T&eacute;l&eacute;phone
												:</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="emppnum" maxLength="10" oninput="this.value=this.value.replace(/[^0-9]/g,'')" required>
										</div>
									</div>
									<div class="row mb-3">
										<div class="col-md-3">
											<label class="col-form-label">Adresse :</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="empaddress" maxLength="30">
										</div>
									</div>
									<div class="row mb-3">
										<div class="col-md-3">
											<label class="col-form-label">Utilisateur :</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="empuname" maxLength="20" required>
										</div>
									</div>
									<div class="row">
										<div class="col-md-3">
											<label class="col-form-label">Mot de passe</label>
										</div>
										<div class="col-md-9">
											<input type="password" class="form-control" name="emppword" maxLength="20" required>
										</div>
									</div>
								</div>
								<div class="modal-footer">
									<button type="submit" class="btn btn-primary">Ajouter</button>
									<button type="button" class="btn btn-secondary"
										data-bs-dismiss="modal">Fermer</button>
								</div>
							</form>
						</div>
					</div>
				</div>
				<table class="table table-bordered table-striped" id="dataTableEmp">
					<thead class="bg-dark text-light">
						<tr align="center">
							<th>Nom</th>
							<th>T&eacute;l&eacute;phone</th>
							<th>Adresse</th>
							<th>Utilisateur</th>
							<th>Action</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="emp" items="${empList}">
							<tr>
								<td><c:out value="${emp.empname}" /></td>
								<td><c:out value="${emp.emppnum}" /></td>
								<td><c:out value="${emp.empaddress}" /></td>
								<td><c:out value="${emp.empuname}" /></td>
								<td align="center">
									<button class="btn btn-warning text-light"
										data-bs-toggle="modal"
										data-bs-target="#empForm<c:out value='${emp.empid}'/>">Modifier</button>
									<button class="btn btn-danger" data-bs-toggle="modal"
										data-bs-target="#empDelete<c:out value='${emp.empid}'/>">Supprimer</button>
								</td>
							</tr>
							<div class="modal fade" id="empForm<c:out value='${emp.empid}'/>"
								tabindex="-1" aria-labelledby="exampleModalLabel"
								aria-hidden="true">
								<div class="modal-dialog modal-dialog-scrolable">
									<div class="modal-content">
										<div class="modal-header">
											<h3 class="modal-title text-warning" id="exampleModalLabel">Modification
												d'employ&eacute;</h3>
											<button class="btn-close" data-bs-dismiss="modal"
												aria-label="Close"></button>
										</div>
										<form action="empUpdate" method="post">
											<div class="modal-body">
												<input type="hidden" name="empid"
													value="<c:out value='${emp.empid}'/>" />
												<div class="row mb-3">
													<div class="col-md-3">
														<label class="col-form-label">Nom :</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${emp.empname}'/>"
															class="form-control" name="empname" maxLength="60" required>
													</div>
												</div>
												<div class="row mb-3">
													<div class="col-md-3">
														<label class="col-form-label">T&eacute;l&eacute;phone
															:</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${emp.emppnum}'/>"
															class="form-control" name="emppnum" maxLength="10" oninput="this.value=this.value.replace(/[^0-9]/g,'')" required>
													</div>
												</div>
												<div class="row mb-3">
													<div class="col-md-3">
														<label class="col-form-label">Adresse :</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${emp.empaddress}'/>"
															class="form-control" name="empaddress" maxLength="30">
													</div>
												</div>
												<div class="row mb-3">
													<div class="col-md-3">
														<label class="col-form-label">Utilisateur :</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${emp.empuname}'/>"
															class="form-control" name="empuname" maxLength="20" required>
													</div>
												</div>
												<div class="row">
													<div class="col-md-3">
														<label class="col-form-label">Mot de passe</label>
													</div>
													<div class="col-md-9">
														<input type="password" class="form-control"
															name="emppword" maxLength="20">
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
							<div class="modal fade"
								id="empDelete<c:out value='${emp.empid}'/>" tabindex="-1"
								aria-labelledby="exampleModalLabel" aria-hidden="true">
								<div class="modal-dialog modal-dialog-scrolable">
									<div class="modal-content">
										<div class="modal-header">
											<h3 class="modal-title text-danger" id="exampleModalLabel">Renvoi
												d'employ&eacute;</h3>
											<button class="btn-close" data-bs-dismiss="modal"
												aria-label="Close"></button>
										</div>
										<div class="modal-body text-center">
											<h5 class="text-danger">
												Voulez-vous vraiment renvoyer l'employ&eacute; "
												<c:out value='${emp.empname}' />
												" ?
											</h5>
										</div>
										<div class="modal-footer">
											<a href="empDelete?empid=<c:out value='${emp.empid}'/>">
												<button class="btn btn-danger">Supprimer</button>
											</a>
											<button type="button" class="btn btn-secondary"
												data-bs-dismiss="modal">Fermer</button>
										</div>
									</div>
								</div>
							</div>
						</c:forEach>
					</tbody>
				</table>
			</div>
			<div class="card col-md-4 text-center pb-2">
				<h3 class="text-primary my-5">Nombre de vente</h3>
				<table class="table table-bordered table-striped" id="dataTableSaleNb">
					<thead class="bg-primary text-light">
						<tr align="center">
							<th>Employ&eacute;</th>
							<th>Vente</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="emp" items="${empList}">
							<tr>
								<td><c:out value="${emp.empname}" /></td>
								<td><c:out value="${emp.salesum}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>