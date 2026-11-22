<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@page import="java.sql.ResultSet, smart.dao.SmartDao"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>SaleList</title>
<link rel="stylesheet" href="assets/css/bootstrap.min.css">
</head>
<body>
	<%@ include file="NavBar.jsp"%>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#dataTableSale').DataTable();
		});
	</script>
	<div class="container-fluid card pb-2">
		<h1 class="text-center my-5">Vente</h1>
		<hr>
		<table class="table table-bordered table-striped" id="dataTableSale">
			<thead class="bg-dark text-light">
				<tr align="center">
					<th>Date</th>
					<th>Smartphone</th>
					<th>Prix</th>
					<th>Quantit&eacute;</th>
					<th>Totale</th>
					<th>Client</th>
					<th>Employe</th>
					<th>Action</th>
				</tr>
			</thead>
			<tbody>
				<%
				SmartDao sd = new SmartDao();
				ResultSet rs = sd.saleSelect();
				while (rs.next()) {
				%>
				<tr>
					<td><%=rs.getString("SaleDate")%></td>
					<td><%=rs.getString("SmartName")%></td>
					<td align="right"><%=rs.getInt("SmartPrice")%> Ar</td>
					<td align="right"><%=rs.getInt("SaleQty")%></td>
					<td align="right"><%=rs.getInt("SaleTotal")%> Ar</td>
					<td><%=rs.getString("CustName")%></td>
					<td><%=rs.getString("EmpName")%></td>
					<td align="center">
						<button class="btn btn-primary" data-bs-toggle="modal"
							data-bs-target="#saleView<%=rs.getInt("SaleID")%>">Afficher</button>
					</td>
				</tr>
				<div class="modal fade" id="saleView<%=rs.getInt("SaleID")%>"
					tabindex="-1" aria-labelledby="exampleModalLabel"
					aria-hidden="true">
					<div class="modal-dialog modal-dialog-scrolable">
						<div class="modal-content">
							<div class="modal-header">
								<h3 class="modal-title" id="exampleModalLabel">Facture</h3>
								<button class="btn-close" data-bs-dismiss="modal"
									aria-label="Close"></button>
							</div>
							<div class="modal-body" id="invoice">
								<h3 class="text-center mb-5">SMART SHOP</h3>
								<div class="row mb-5">
									<div class="col-md-3">
										<label class="col-form-label">Date et heure</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getString("SaleDate")%>"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3">
										<label class="col-form-label">Smartphone :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getString("SmartName")%>"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3">
										<label class="col-form-label">Prix :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getInt("SmartPrice")%> Ar"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3">
										<label class="col-form-label">Quantite :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getInt("SaleQty")%>"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3">
										<label class="col-form-label">Total :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getInt("SaleTotal")%> Ar"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3">
										<label class="col-form-label">Client :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getString("CustName")%>"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row mb-5">
									<div class="col-md-3">
										<label class="col-form-label">Employ&eacute; :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getString("EmpName")%>"
											class="form-control-plaintext" disabled>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3">
										<label class="col-form-label">Net &agrave; payer :</label>
									</div>
									<div class="col-md-9">
										<input value="<%=rs.getInt("SaleTotal")%> Ar"
											class="form-control-plaintext" disabled>
									</div>
								</div>
							</div>
							<div class="modal-footer">
								<button class="btn btn-dark" onclick="printDiv('invoice')">Imprimer</button>
								<button type="button" class="btn btn-secondary"
									data-bs-dismiss="modal">Fermer</button>
							</div>
						</div>
					</div>
				</div>
				<%
				}
				%>
			</tbody>
		</table>
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
	</div>
</body>
</html>