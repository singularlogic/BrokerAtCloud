<html>
<head>
	<title>PuLSaR - Preference-based cloud service recommender</title>
	<meta charset="utf-8" />
	<link rel="stylesheet" style="text/css" href="forms/deps/opt/bootstrap.css" />
	<link rel="stylesheet" href="/forms/slick/common-grid-styles.css" type="text/css"/>
	<style>
		input[type="text"], 
		input[type="password"] {
			height: 40px;
		}
	</style>
</head>
<body style="padding:10px;">
	<h1 align="center">PuLSaR - Preference-based cloud service recommender</h1>
	<p><br/></p>
	<p align="center">
		<script>
			function setCredentials(user,pass) {
				var form = this.document.forms[0];
				for (ii in form) {
					var input = form[ii];
					if (!input || !input.name || !input.value) continue;
					if (input.name==='j_username') input.value = user;
					if (input.name==='j_password') input.value = pass;
				}
			}
		</script>
		<button onClick="setCredentials('admin','admin');"><img src="images/admin2.jpg"/><br/><font size="-1"><i>Admin</i></font></button>
		<button onClick="setCredentials('sc1','sc1');"><img src="images/consumer.png"/><br/><font size="-1"><i>Consumer 1</i></font></button>
		<button onClick="setCredentials('DE143593148','DE143593148');"><img src="images/provider.jpg"/><br/><font size="-1"><i>CAS Software AG</i></font></button>
	</p>
	<p><br/></p>
	<form action="j_security_check" method=POST enctype="application/x-www-form-urlencoded">
		<div align=center>
			<table border=0 cellpadding=0>
				<tr>
					<td><p><em>Username:</em></p>&nbsp;</td>
					<td><p><INPUT TYPE="text" MAXLENGTH="25" SIZE="12" NAME="j_username" VALUE="admin"></p></td>
				</tr>
				<tr>
					<td><p><em>Password:</em></p>&nbsp;</td>
					<td><p><INPUT TYPE="password" MAXLENGTH="25" SIZE="12" NAME="j_password" VALUE="admin"></p></td>
				</tr>
				<tr>
					<td colspan="2"><p align="center" style="text-align:center">
						<INPUT TYPE="submit" ACTION="j_security_check" VALUE="Login" METHOD="POST" NAME="submit">
					</p></td>
				</tr>
			</table>
		</div>
	</form>
	<% boolean isStandalone = true;
	%><%@ include file="forms/includes/footer.html" %>
</body>
</html>
