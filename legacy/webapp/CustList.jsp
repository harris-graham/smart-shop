<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>CustList</title>
</head>
<body>
	<%@ include file="NavBar.jsp"%>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#dataTableCust').DataTable();
			$('#dataTablePurchNb').DataTable();
		});
	</script>
	<div class="container-fluid">
		<div class="row">
			<div class="card col-md-8 text-center pb-2">
				<h1 class="my-5">Client</h1>
				<hr>
				<div class="text-center mb-3">
					<button class="btn btn-primary" data-bs-toggle="modal"
						data-bs-target="#custForm">Ajouter</button>
				</div>
				<div class="modal fade" id="custForm" tabindex="-1"
					aria-labelledby="exampleModalLabel" aria-hidden="true">
					<div class="modal-dialog modal-dialog-scrolable">
						<div class="modal-content">
							<div class="modal-header">
								<h3 class="modal-title text-primary" id="exampleModalLabel">Ajout
									de client</h3>
								<button class="btn-close" data-bs-dismiss="modal"
									aria-label="Close"></button>
							</div>
							<form action="custInsert" method="post">
								<div class="modal-body">
									<div class="row mb-3">
										<div class="col-md-3">
											<label class="col-form-label">Nom :</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="custname" maxLength="60" required>
										</div>
									</div>
									<div class="row mb-3">
										<div class="col-md-3">
											<label class="col-form-label">T&eacute;l&eacute;phone
												:</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="custpnum" maxLength="10" oninput="this.value=this.value.replace(/[^0-9]/g,'')" required>
										</div>
									</div>
									<div class="row">
										<div class="col-md-3">
											<label class="col-form-label">Adresse :</label>
										</div>
										<div class="col-md-9">
											<input class="form-control" name="custaddress" maxLength="30">
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
				<table class="table table-bordered table-striped" id="dataTableCust">
					<thead class="bg-dark text-light">
						<tr align="center">
							<th>Nom</th>
							<th>T&eacute;l&eacute;phone</th>
							<th>Adresse</th>
							<th>Action</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="cust" items="${custList}">
							<tr>
								<td><c:out value="${cust.custname}" /></td>
								<td><c:out value="${cust.custpnum}" /></td>
								<td><c:out value="${cust.custaddress}" /></td>
								<td align="center">
									<button class="btn btn-warning text-light"
										data-bs-toggle="modal"
										data-bs-target="#custForm<c:out value='${cust.custid}'/>">Modifier</button>
									<button class="btn btn-danger" data-bs-toggle="modal"
										data-bs-target="#custDelete<c:out value='${cust.custid}'/>">Supprimer</button>
								</td>
							</tr>
							<div class="modal fade"
								id="custForm<c:out value='${cust.custid}'/>" tabindex="-1"
								aria-labelledby="exampleModalLabel" aria-hidden="true">
								<div class="modal-dialog modal-dialog-scrolable">
									<div class="modal-content">
										<div class="modal-header">
											<h3 class="modal-title text-warning" id="exampleModalLabel">Modification
												de client</h3>
											<button class="btn-close" data-bs-dismiss="modal"
												aria-label="Close"></button>
										</div>
										<form action="custUpdate" method="post">
											<div class="modal-body">
												<input type="hidden" name="custid"
													value="<c:out value='${cust.custid}'/>" />
												<div class="row mb-3">
													<div class="col-md-3">
														<label class="col-form-label">Nom :</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${cust.custname}'/>"
															class="form-control" name="custname" maxLength="60" required>
													</div>
												</div>
												<div class="row mb-3">
													<div class="col-md-3">
														<label class="col-form-label">T&eacute;l&eacute;phone
															:</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${cust.custpnum}'/>"
															class="form-control" name="custpnum" maxLength="10" oninput="this.value=this.value.replace(/[^0-9]/g,'')" required>
													</div>
												</div>
												<div class="row">
													<div class="col-md-3">
														<label class="col-form-label">Adresse :</label>
													</div>
													<div class="col-md-9">
														<input value="<c:out value='${cust.custaddress}'/>"
															class="form-control" name="custaddress" maxLength="30">
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
								id="custDelete<c:out value='${cust.custid}'/>" tabindex="-1"
								aria-labelledby="exampleModalLabel" aria-hidden="true">
								<div class="modal-dialog modal-dialog-scrolable">
									<div class="modal-content">
										<div class="modal-header">
											<h3 class="modal-title text-danger" id="exampleModalLabel">Suppression
												de client</h3>
											<button class="btn-close" data-bs-dismiss="modal"
												aria-label="Close"></button>
										</div>
										<div class="modal-body">
											<h5 class="text-center text-danger">
												Voulez-vous vraiment supprimer le client "
												<c:out value='${cust.custname}' />
												" ?
											</h5>
										</div>
										<div class="modal-footer">
											<a href="custDelete?custid=<c:out value='${cust.custid}'/>">
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
				<h3 class="text-primary my-5">Nombre d'achat</h3>
				<table class="table table-bordered table-striped"
					id="dataTablePurchNb">
					<thead class="bg-primary text-light">
						<tr align="center">
							<th>Client</th>
							<th>Achat</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="cust" items="${custList}">
							<tr>
								<td><c:out value="${cust.custname}" /></td>
								<td><c:out value="${cust.salesum}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>