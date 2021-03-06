<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "Service Attribute Hierarchy Management"; %>
<%@ include file="../includes/head.html" %>
	<link rel="stylesheet" href="../slick/common-grid-styles.css" type="text/css"/>
  </head>
  <body style="padding:10px;">
<%@ include file="../includes/header.html" %>
	<table border="2px" cellpadding="15px" cellspacing="10px" style="border:2px solid lightgray; outline: 2px solid darkgray;" width="100%">
	  <tr>
	    <td width="300px" valign="top" style="">
			<div style="/*overflow:scroll;*/">
				<div id="attrList"></div>
				<div style="font:italic 10pt arial,sans-serif; border-top:black 1pt solid; margin-top:10px; padding-top:15px;">Use <strong>Del</strong>, <strong>Ins</strong> and <strong>Shift+Ins</strong> hotkeys to respectively delete selected, create sibling and create child node.</div>
			</div>
		</td>
        <td width="*" valign="top">
			<div class="alert"></div>
			<form id="formEdit"></form>
			<p><hr /></p>
			<div id="formButtons"></div>
			<p><br/></p>
			<div id="res" class="alert"></div>
		</td>
	  </tr>
	</table>
    
<%@ include file="../includes/js-libs.html" %>
	
	<script type="text/javascript">
		// Override form-commons vars to meet this form's fields
		var formFields = ['id', 'owner', 'name', 'description', 'createTimestamp', 'lastUpdateTimestamp', 'parent' /*, 'source'*/];
		var emptyData = {'id':'', 'owner':'', 'name':'', 'description':'', 'createTimestamp':'', 'lastUpdateTimestamp':'', 'parent':'' /*, 'source':''*/};
		formSelector = "#formEdit";
		statusSelector = '#res';
	</script>
	
	<!-- Main Form initialization -->
    <script type="text/javascript" src="attribute-mgnt-jsonform.js"></script>
    
	<!-- Attribute operation functions -->
	<script type="text/javascript" src="attribute-mgnt-node-ops.js"></script>
	
    <!-- jsTree and hotkeys initialization -->
	<script type="text/javascript" src="attribute-mgnt-init.js"></script>
	
	<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>