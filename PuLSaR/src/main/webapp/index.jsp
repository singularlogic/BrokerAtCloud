<%@ page session="true"%><%
	String[] allRoles = {"admin", "sc", "sp", "broker"};
	java.util.List userRoles = new java.util.ArrayList(allRoles.length);
	for(String role : allRoles) {
		if(request.isUserInRole(role)) { 
			userRoles.add(role);
		}
	}
	
	session.setAttribute("user.roles", userRoles);
	
	String standalone = request.getParameter("standalone");
	boolean isStandalone = (standalone==null || !standalone.trim().equalsIgnoreCase("no"));
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
		.success-mesg {
			color: darkgreen;
			background: lightgreen;
			font-size: medium;
			padding: 3px;
			text-align: center;
		}
		.error-mesg {
			color: red;
			background: pink;
			font-size: medium;
			font-weight: bold;
			font-style: italic;
			padding: 3px;
			text-align: center;
		}
		.choice-mesg {
			color: darkblue;
			background: lightblue;
			font-size: medium;
			padding: 3px;
			text-align: left;
			margin-bottom: 0px;
			width: *;
		}
		.choice-inner {
			text-align: left;
			width: 500px;
		}
		.choice-ul {
			font-style: italic;
			font-size: 11pt;
			padding-left: 10px;
		}
		.choice-li {
		}
		.use-it-button {
		}
		.mesg-box {
			width:90%;
			text-align: center;
		}
		.actions-line {
			font-size: 12pt;
			margin-top: 5px;
			margin-bottom: 0px;
		}
	</style>
    <script type="text/javascript" src="forms/deps/jquery-1.10.2.js"></script>
    <script type="text/javascript" src="forms/includes/util-commons.js"></script>
</head>
<body style="padding:10px;">
<% if (isStandalone) { %>
<%= eu.brokeratcloud.rest.gui.Auth.getMenuStatic(request) %>
<!--#include virtual="forms/includes/header.html" -->
	<h1 align="center">Welcome to PuLSaR</h1>
	<h3 align="center">The Preference cLoud Service Recommender</h3>
	<p><br/></p>
<% } %>
	<center>
<% if (request.isUserInRole("admin")) { %>
	<p class="dashboard-block">As an <b>admin</b> through this Dashboard you are able to manage the service
	measurement criteria and associate them with functional categories available in your Broker platform in
	order to be used by PuLSaR and considered for optimisation purposes based on the cloud consumer needs.</p>
	<div class="dashboard-block">
		<script><!--
			// On-load retrieve active broker policies
			function useItButton(bpUri) {
				return ' <button class="use-it-button" onClick="applyPolicy(\''+bpUri+'\')">Use it!</button>';
			}
			$(function () {
				console.log('Retrieving a list of active broker policies...');
				showIndicator('Retrieving active broker policies...');
				fnSuccess = function(list, textStatus, jqXHR) {
					console.log('List of active broker policies: '+textStatus+': '+JSON.stringify(list));
					loadingIndicator.hide();
					if (list.length===0) { 
						$('#bp-list').html('<div class="error-mesg">No active broker policy found !</div>');
					} else
					if (list.length===1) {
						$('#bp-list').html('<div class="success-mesg"><em><b>Active broker policy :</b></em> '+list[0]+
							useItButton(list[0])+
							'</div>');
					} else {
						var ul = '<ul class="choice-ul">'+"\n";
						for (ii in list) {
							ul+=('<li class="choice-li">'+list[ii]+useItButton(list[ii])+"</li>\n");
						}
						ul+="</ul>\n";
						msg = "Multiple active broker policies found. Please choose one : \n"+ul;
						msg = '<div class="choice-inner">'+msg+'</div>';
						$('#bp-list').html('<div class="choice-mesg"><table width="100%" border="0"><tr><td align="center">'+msg+'</td></tr></table></div>');
					}
				};
				fnError = function(jqXHR, textStatus, errorThrown) {
					console.log('Could not retrieve a list of active broker policies. Reason: '+textStatus+'\nError thrown: '+errorThrown);
					loadingIndicator.hide();
				};
				$.ajax({
					async: false,
					type: 'get',
					url: '/opt/aux/list-active-policies',
					contentType: 'application/json',
					success: fnSuccess,
					error: fnError,
				});
			});
			// On-user-action apply indicated broker policy
			function applyPolicy(bp) {
				console.log('Applying broker policy : '+bp);
				showIndicator('Applying broker policy : '+bp+'...');
				fnSuccess = function(data, textStatus, jqXHR) {
					if (data.status!=='OK') {
						fnError(null, null, data.status.replace("\n",'<br/>'));
					} else {
						msg = '<em><b>Broker policy applied :</b></em> '+data['bp-uri'];
						console.log(msg);
						loadingIndicator.hide();
						$('#bp-list').html('<div class="success-mesg">'+msg+'</div>');
					}
				}
				fnError = function(jqXHR, textStatus, errorThrown) {
					msg = 'Could not apply broker policy. Reason: '+errorThrown;
					console.log(msg);
					loadingIndicator.hide();
					$('#bp-list').html('<div class="error-mesg">'+msg+'</div>');
				};
				$.ajax({
					async: false,
					type: 'get',
					url: '/opt/aux/apply-policy/'+encodeURIComponent(bp),
					contentType: 'application/json',
					success: fnSuccess,
					error: fnError,
				});
			}
			// On-user-action flush all caches
			function flushCaches() {
				console.log('Flushing caches...');
				showIndicator('Flushing caches...');
				fnSuccess = function(data, textStatus, jqXHR) {
					msg = '<em><b>All caches flushed</b></em>';
					console.log(msg);
					loadingIndicator.hide();
					$('#bp-list').html('<div class="success-mesg">'+msg+'</div>');
				}
				fnError = function(jqXHR, textStatus, errorThrown) {
					msg = 'Could not flush caches. Reason: '+errorThrown;
					console.log(msg);
					loadingIndicator.hide();
					$('#bp-list').html('<div class="error-mesg">'+msg+'</div>');
				};
				$.ajax({
					async: false,
					type: 'get',
					url: '/opt/aux/flush-caches',
					contentType: 'application/json',
					success: fnSuccess,
					error: fnError,
				});
			}
		//--></script>
		<div id="bp-list" class="mesg-box">...</div>
		<p class="actions-line">
			<a href="javascript: applyPolicy('#');">Re-apply active broker policy</a> |
			<a href="javascript: flushCaches();">Flush all caches</a>
		</p>
	</div>
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
<% if (isStandalone) { %>
	<hr />
	<address><p align="center"><a target="_new" href="http://www.broker-cloud.eu/">Broker@Cloud FP7 Project - Continuous Quality Assurance and Optimisation for Cloud Brokers</a><br />
		&copy; 2014-2016, Institute of Communications and Computer Systems - National Technical University of Athens (ICCS-NTUA)<br/>
		<a target="_new" href="http://imu.iccs.gr">Information Management Unit (IMU)</a>
	</p></address>
<% } %>
</body>
</html>
