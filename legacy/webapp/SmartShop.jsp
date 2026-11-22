<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>SmartShop</title>
<link rel="stylesheet" href="assets/css/bootstrap.min.css">
</head>
<body>
	<%@ include file="NavBar.jsp"%>
	<div class="container-fluid">
		<h1 class="text-center my-5">SMART SHOP</h1>
		<div class="mb-3">
			<button class="btn btn-primary" data-bs-toggle="modal"
				data-bs-target="#cartView">Voir le panier</button>
		</div>
		<div class="modal fade" id="cartView" tabindex="-1"
			aria-labelledby="exampleModalLabel" aria-hidden="true">
			<div class="modal-dialog modal-xl modal-dialog-scrolable">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title">Panier</h4>
						<button class="btn-close" data-bs-dismiss="modal"
							aria-label="Close"></button>
					</div>
					<form action="smartSale" method="post">
						<div class="modal-body" id="invoice">
							<c:set var="total" value="0"></c:set>
							<div class="card container-fluid">
								<div class="row">
									<c:forEach var="cart" items="${sessionScope.cart}">
										<c:set var="total"
											value="${total + cart.smart.smartprice * cart.cartqty}"></c:set>
										<div class="card col-md-3 text-center mb-3">
											<div class="card-header">
												<h6>${cart.smart.smartname}</h6>
											</div>
											<div class="card-body">
												<img src="data:image/jpeg;base64,${cart.smart.smartimg}">
												<hr>
												<h6>${cart.smart.smartprice}Ar</h6>
												<h6>Quantit&eacute; : ${cart.cartqty}</h6>
												<h5>Sous-total : ${cart.smart.smartprice * cart.cartqty}
													Ar</h5>
											</div>
											<div class="card-footer">
												<a class="btn btn-danger"
													href="remFromCart?smartid=${cart.smart.smartid}"
													onclick="return confirm('Vous etes sure ?')">Enlever</a>
											</div>
										</div>
									</c:forEach>
								</div>
							</div>
							<div class="card">
								<h3 class="m-3" align="right">
									Total :
									<c:out value="${total} Ar" />
								</h3>
							</div>
							<div class="row">
								<div class="col-md-7"></div>
								<div class="row col-md-5 mt-3">
									<div class="col-md-3">
										<label class="col-form-label text-primary"><b>Client
												:</b></label>
									</div>
									<div class="col-md-8">
										<select class="form-select" name="custid" required>
											<option selected hidden="true" disabled>Choisir le
												client</option>
											<c:forEach var="cust" items="${custList}">
												<option value="<c:out value="${cust.custid}"/>"><c:out
														value="${cust.custname}" /></option>
											</c:forEach>
										</select>
									</div>
									<div class="col-md-1">
										<button type="button" class="btn btn-primary"
											data-bs-toggle="modal" data-bs-target="#custForm">+</button>
									</div>
								</div>
							</div>
						</div>
						<div class="modal-footer text-center">
							<button class="btn btn-dark" onclick="printDiv('invoice')">Imprimer</button>
							<button type="submit" class="btn btn-success">Vendre</button>
							<button type="button" class="btn btn-secondary"
								data-bs-dismiss="modal">Fermer</button>
						</div>
					</form>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			function printDiv(divName) {
				var printContent = document.getElementById(divName).innerHTML;
				var originalContent = document.body.innerHTML;
				document.body.innerHTML = printContent;
				window.print();
				document.body.innerHTML = originalContent;
				window.location.reload();
			}
		</script>
		<div class="card container-fluid">
			<div class="row">
				<c:forEach var="smart" items="${smartList}">
					<div class="card col-md-2 text-center mb-5">
						<div class="card-header">
							<h6>
								<c:out value="${smart.smartname}" />
							</h6>
						</div>
						<div class="card-body">
							<img src="data:image/jpeg;base64,${smart.smartimg}">
							<hr>
							<h5>${smart.smartprice}Ar</h5>
							<h6>RAM : ${smart.smartram} Go</h6>
							<h6>Stockage : ${smart.smartstorage} Go</h6>
						</div>
						<div class="card-footer">
							<button class="btn btn-primary" data-bs-toggle="modal"
								data-bs-target="#smartAdd<c:out value="${smart.smartid}"/>">Ajouter
								au panier</button>
						</div>
					</div>
					<div class="modal fade"
						id="smartAdd<c:out value="${smart.smartid}"/>" tabindex="-1"
						aria-labelledby="exampleModalLabel" aria-hidden="true">
						<div class="modal-dialog modal-dialog-scrolable">
							<div class="modal-content">
								<div class="modal-header">
									<h4 class="modal-title" id="exampleModalLabel">
										<c:out value="${smart.smartname}" />
									</h4>
									<button class="btn-close" data-bs-dismiss="modal"
										aria-label="Close"></button>
								</div>
								<form action="addToCart" method="post">
									<div class="modal-body text-center">
										<img src="data:image/jpeg;base64,${smart.smartimg}">
										<hr>
										<h5>${smart.smartprice}Ar</h5>
										<h6>RAM : ${smart.smartram} Go</h6>
										<h6>Stockage : ${smart.smartstorage} Go</h6>
										<hr>
										<input type="hidden" name="smartid"
											value="<c:out value="${smart.smartid}"/>" />
										<div class="row">
											<div class="col-md-6 mt-1">
												<h5
													class="<c:if test="${smart.smartqty <= 3}" >text-danger</c:if>">
													En stock :
													<c:out value="${smart.smartqty}" />
												</h5>
											</div>
											<div class="col-md-3">
												<label class="col-form-label text-primary"><b>Nombre
														:</b></label>
											</div>
											<div class="col-md-3">
												<input type="number" class="form-control" name="saleqty"
													min="1" max="${smart.smartqty}" required>
											</div>
										</div>
									</div>
									<div class="modal-footer text-center">
										<button type="submit" class="btn btn-primary">Confirmer</button>
										<button type="button" class="btn btn-secondary"
											data-bs-dismiss="modal">Fermer</button>
									</div>
								</form>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>
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
					<form action="custQuiInsert" method="post">
						<div class="modal-body">
							<div class="row mb-3">
								<div class="col-md-3">
									<label>Nom :</label>
								</div>
								<div class="col-md-9">
									<input class="form-control" name="custname" maxLength="60" required>
								</div>
							</div>
							<div class="row mb-3">
								<div class="col-md-3">
									<label>T&eacute;l&eacute;phone :</label>
								</div>
								<div class="col-md-9">
									<input class="form-control" name="custpnum" maxLength="10" oninput="this.value=this.value.replace(/[^0-9]/g,'')" required>
								</div>
							</div>
							<div class="row mb-3">
								<div class="col-md-3">
									<label>Adresse :</label>
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
	</div>
</body>
</html>