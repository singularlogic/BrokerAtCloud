<%@ page session="true"%>
<html>
<head>
	<title>PuLSaR - Preference-based cloud service recommender</title>
	<meta charset="utf-8" />
	<link rel="stylesheet" style="text/css" href="forms/deps/opt/bootstrap.css" />
	<link rel="stylesheet" href="/forms/slick/common-grid-styles.css" type="text/css"/>
	<META HTTP-EQUIV="refresh" CONTENT="2;URL=/logon.jsp" />
</head>
<body style="padding:10px;">
	<h1 align="center">PuLSaR - Preference-based cloud service recommender</h1>
	<p><br/></p>
	<p align="center">
		User <em>'<%=request.getRemoteUser()%>'</em> has been logged out.

<% session.invalidate(); %>

		<br/><br/>
		<a href="/logon.jsp">Click here to go to logon page</a>
	</p>

	<!--#include virtual="forms/includes/footer.html" -->
</body>
</html>
