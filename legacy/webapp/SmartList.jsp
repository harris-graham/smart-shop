<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>SmartList</title>
</head>
<body>
	<%@ include file="NavBar.jsp"%>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#dataTableSmart').DataTable();
		});
	</script>
	<div class="container-fluid card pb-2">
		<h1 class="text-center my-5">Smartphone</h1>
		<hr>
		<div class="text-center mb-3">
			<button class="btn btn-primary" data-bs-toggle="modal"
				data-bs-target="#smartForm">Ajouter</button>
		</div>
		<div class="modal fade" id="smartForm" tabindex="-1"
			aria-labelledby="exampleModalLabel" aria-hidden="true">
			<div class="modal-dialog modal-dialog-scrolable">
				<div class="modal-content">
					<div class="modal-header">
						<h3 class="modal-title text-primary" id="exampleModalLabel">Ajout
							de smartphone</h3>
						<button class="btn-close" data-bs-dismiss="modal"
							aria-label="Close"></button>
					</div>
					<form action="smartInsert" method="post"
						enctype="multipart/form-data">
						<div class="modal-body">
							<div class="row mb-3">
								<div class="col-md-3">
									<label class="col-form-label">Nom :</label>
								</div>
								<div class="col-md-9">
									<input class="form-control" name="smartname" maxLength="50" required>
								</div>
							</div>
							<div class="row mb-3">
								<div class="col-md-3">
									<label class="col-form-label">Prix :</label>
								</div>
								<div class="col-md-9">
									<div class="input-group">
										<input type="number" class="form-control" name="smartprice" required> <span
											class="input-group-text">Ar</span>
									</div>
								</div>
							</div>
							<div class="row mb-3">
								<div class="col-md-3">
									<label class="col-form-label">Nombre :</label>
								</div>
								<div class="col-md-9">
									<input type="number" class="form-control" name="smartqty" required>
								</div>
							</div>
							<div class="row mb-3">
								<div class="col-md-3">
									<label class="col-form-label">RAM :</label>
								</div>
								<div class="col-md-9">
									<div class="input-group">
										<input type="number" class="form-control" name="smartram" required> <span
											class="input-group-text">Go</span>
									</div>
								</div>
							</div>
							<div class="row mb-3">
								<div class="col-md-3">
									<label class="col-form-label">Stockage :</label>
								</div>
								<div class="col-md-9">
									<div class="input-group">
										<input type="number" class="form-control" name="smartstorage" required> <span
											class="input-group-text">Go</span>
									</div>
								</div>
							</div>
							<div class="row">
								<div class="col-md-3">
									<label class="col-form-label">Image :</label>
								</div>
								<div class="col-md-9">
									<input type="file" class="form-control" name="smartimg">
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
		<table class="table table-bordered table-striped" id="dataTableSmart">
			<thead class="bg-dark text-light">
				<tr align="center">
					<th>Nom</th>
					<th>Prix</th>
					<th>Nombre</th>
					<th>RAM</th>
					<th>Stockage interne</th>
					<th>Action</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="smart" items="${smartList}">
					<tr>
						<td><c:out value="${smart.smartname}" /></td>
						<td align="right"><c:if test="${smart.smartprice != 0}">
								<c:out value="${smart.smartprice} Ar" />
							</c:if></td>
						<td align="right"><c:out value="${smart.smartqty}" /></td>
						<td align="right"><c:if test="${smart.smartram != 0}">
								<c:out value="${smart.smartram} Go" />
							</c:if></td>
						<td align="right"><c:if test="${smart.smartstorage != 0}">
								<c:out value="${smart.smartstorage} Go" />
							</c:if></td>
						<td align="center" width="auto">
							<button class="btn btn-success" data-bs-toggle="modal"
								data-bs-target="#smartMore<c:out value='${smart.smartid}'/>">Plus</button>
							<button class="btn btn-warning text-light" data-bs-toggle="modal"
								data-bs-target="#smartForm<c:out value='${smart.smartid}'/>">Modifier</button>
							<button class="btn btn-danger" data-bs-toggle="modal"
								data-bs-target="#smartDelete<c:out value='${smart.smartid}'/>">Supprimer</button>
						</td>
					</tr>
					<div class="modal fade"
						id="smartMore<c:out value='${smart.smartid}'/>" tabindex="-1"
						aria-labelledby="exampleModalLabel" aria-hidden="true">
						<div class="modal-dialog modal-dialog-scrolable">
							<div class="modal-content">
								<div class="modal-header">
									<h5 class="modal-title text-success" id="exampleModalLabel">
										Ajout de stock :
										<c:out value='${smart.smartname}' />
									</h5>
									<button class="btn-close" data-bs-dismiss="modal"
										aria-label="Close"></button>
								</div>
								<form action="smartMore" method="post">
									<div class="modal-body">
										<input type="hidden" name="smartid"
											value="<c:out value='${smart.smartid}'/>" />
										<div class="row">
											<div class="col-md-6 mt-1 text-center">
												<h5
													class="<c:if test="${smart.smartqty <= 3}" >text-danger</c:if>">
													En stock :
													<c:out value="${smart.smartqty}" />
												</h5>
											</div>
											<div class="col-md-3">
												<label class="col-form-label"><b>Nombre :</b></label>
											</div>
											<div class="col-md-3">
												<input type="number" class="form-control" name="smartqty"
													required>
											</div>
										</div>
									</div>
									<div class="modal-footer">
										<button class="btn btn-success">Ajouter</button>
										<button type="button" class="btn btn-secondary"
											data-bs-dismiss="modal">Fermer</button>
									</div>
								</form>
							</div>
						</div>
					</div>
					<div class="modal fade"
						id="smartForm<c:out value='${smart.smartid}'/>" tabindex="-1"
						aria-labelledby="exampleModalLabel" aria-hidden="true">
						<div class="modal-dialog modal-dialog-scrolable">
							<div class="modal-content">
								<div class="modal-header">
									<h3 class="modal-title text-warning" id="exampleModalLabel">Modification
										de smartphone</h3>
									<button class="btn-close" data-bs-dismiss="modal"
										aria-label="Close"></button>
								</div>
								<form action="smartUpdate" method="post"
									enctype="multipart/form-data">
									<div class="modal-body">
										<input type="hidden" name="smartid"
											value="<c:out value='${smart.smartid}'/>" />
										<div class="row mb-3">
											<div class="col-md-3">
												<label class="col-form-label">Nom :</label>
											</div>
											<div class="col-md-9">
												<input value="<c:out value='${smart.smartname}'/>"
													class="form-control" name="smartname" maxLength="50" required>
											</div>
										</div>
										<div class="row mb-3">
											<div class="col-md-3">
												<label class="col-form-label">Prix :</label>
											</div>
											<div class="col-md-9">
												<div class="input-group">
													<input type="number" value="<c:out value='${smart.smartprice}'/>"
														class="form-control" name="smartprice" required> <span
														class="input-group-text">Ar</span>
												</div>
											</div>
										</div>
										<div class="row mb-3">
											<div class="col-md-3">
												<label class="col-form-label">Nombre :</label>
											</div>
											<div class="col-md-9">
												<input type="number"
													value="<c:out value='${smart.smartqty}'/>"
													class="form-control" name="smartqty" required>
											</div>
										</div>
										<div class="row mb-3">
											<div class="col-md-3">
												<label class="col-form-label">RAM :</label>
											</div>
											<div class="col-md-9">
												<div class="input-group">
													<input type="number" value="<c:out value='${smart.smartram}'/>"
														class="form-control" name="smartram" required> <span
														class="input-group-text">Go</span>
												</div>
											</div>
										</div>
										<div class="row mb-3">
											<div class="col-md-3">
												<label class="col-form-label">Stockage :</label>
											</div>
											<div class="col-md-9">
												<div class="input-group">
													<input type="number" value="<c:out value='${smart.smartstorage}'/>"
														class="form-control" name="smartstorage" required> <span
														class="input-group-text">Go</span>
												</div>
											</div>
										</div>
										<div class="row mb-3">
											<div class="col-md-3">
												<label class="col-form-label">Image :</label>
											</div>
											<div class="col-md-9">
												<input type="file"
													value="<c:out value='${smart.smartimg}'/>"
													class="form-control" name="smartimg">
											</div>
										</div>
										<div class="text-center">
											<img src="data:image/jpg;base64,${smart.smartimg}">
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
						id="smartDelete<c:out value='${smart.smartid}'/>" tabindex="-1"
						aria-labelledby="exampleModalLabel" aria-hidden="true">
						<div class="modal-dialog modal-dialog-scrolable">
							<div class="modal-content">
								<div class="modal-header">
									<h3 class="modal-title text-danger" id="exampleModalLabel">Suppression
										de smartphone</h3>
									<button class="btn-close" data-bs-dismiss="modal"
										aria-label="Close"></button>
								</div>
								<div class="modal-body">
									<h5 class="text-center text-danger">
										Voulez-vous vraiment supprimer le smartphone "
										<c:out value='${smart.smartname}' />
										" ?
									</h5>
								</div>
								<div class="modal-footer">
									<a href="smartDelete?smartid=<c:out value='${smart.smartid}'/>">
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
</body>
</html>