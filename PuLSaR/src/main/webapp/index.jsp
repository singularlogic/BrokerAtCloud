<%@ page session="true"%><%
	String[] allRoles = {"admin", "sc", "sp", "broker"};
	java.util.List userRoles = new java.util.ArrayList(allRoles.length);
	for(String role : allRoles) {
		if(request.isUserInRole(role)) { 
			userRoles.add(role);
		}
	}
	
	session.setAttribute("user.roles", userRoles);
	
%><html>
<head>
	<title>PuLSaR Dashboard</title>
	<meta charset="utf-8" />
	<link rel="stylesheet" style="text/css" href="/forms/deps/opt/bootstrap.css" />
	<link rel="stylesheet" href="/forms/slick/common-grid-styles.css" type="text/css"/>
	<style>
		.dashboard-block {
			background: lightgrey;
			border : black bevel 2px;
			padding: 5px;
			margin : 5px;
			font-size: 1.3em;
			line-height: 1.5em;
			width: 75%;
			align: left;
		}
	</style>
</head>
<body style="padding:10px;">
<%= eu.brokeratcloud.rest.gui.Auth.getMenuStatic(request) %>
<!--#include virtual="forms/includes/header.html" -->
	<h1 align="center">Welcome to PuLSaR</h1>
	<h3 align="center">The Preference cLoud Service Recommender</h3>
	<p><br/></p>
	<center>
<% if (request.isUserInRole("admin")) { %>
	<p class="dashboard-block">As an <b>admin</b> through this Dashboard you are able to manage the service
	measurement criteria and associate them with functional categories available in your Broker platform in
	order to be used by PuLSaR and considered for optimisation purposes based on the cloud consumer needs.</p>
<% } %>
<% if (request.isUserInRole("sc")) { %>
	<p class="dashboard-block">As a <b>service consumer</b> through this Dashboard you can create your Preference
	profiles, indicate what criteria are relevant or important and receive valuable recommendations about the best
	cloud services to use.</p>
<% } %>
<% if (request.isUserInRole("sp")) { %>
	<p class="dashboard-block">As a <b>service provider</b> through this Dashboard you can receive 
	notifications about the usage of your cloud services and offers.</p>
<% } %>
	</center>
<!--#include virtual="forms/includes/footer.html" -->
	<hr />
	<address><p align="center"><a target="_new" href="http://www.broker-cloud.eu/">Broker@Cloud FP7 Project - Continuous Quality Assurance and Optimisation for Cloud Brokers</a><br />
		&copy; 2014-2015, Institute of Communications and Computer Systems - National Technical University of Athens (ICCS-NTUA)<br/>
		<a target="_new" href="http://imu.iccs.gr">Information Management Unit (IMU)</a>
	</p></address>
</body>
</html>
