<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "Preference Profile Management"; %>
<%@ include file="../includes/head.html" %>
<%@ include file="../includes/js-libs.html" %>
<%@ include file="../includes/js-libs-slickgrid.html" %>
	<link href="smart_wizard.css" rel="stylesheet" type="text/css">
	<script type="text/javascript" src="jquery.smartWizard.js"></script>
<!-- Slider staff : BEGIN -->
<link rel="stylesheet" href="../slider/css/jslider.css" type="text/css">
<link rel="stylesheet" href="../slider/css/jslider.blue.css" type="text/css">
<link rel="stylesheet" href="../slider/css/jslider.plastic.css" type="text/css">
<link rel="stylesheet" href="../slider/css/jslider.round.css" type="text/css">
<link rel="stylesheet" href="../slider/css/jslider.round.plastic.css" type="text/css">
<script type="text/javascript" src="../slider/js/jshashtable-2.1_src.js"></script>
<script type="text/javascript" src="../slider/js/jquery.numberformatter-1.2.3.js"></script>
<script type="text/javascript" src="../slider/js/tmpl.js"></script>
<script type="text/javascript" src="../slider/js/jquery.dependClass-0.1.js"></script>
<script type="text/javascript" src="../slider/js/draggable-0.1.js"></script>
<script type="text/javascript" src="../slider/js/jquery.slider.js"></script>
<!-- Slider staff : END -->
	<style>
		.step-description {
			font: 10pt plain Arial, sans-serif;
			padding: 5px;
			margin-top: 5px;
			margin-bottom: 5px;
			background: lightgrey;
		}
		.tree-container {
			border: 1px inset grey;
			margin: 2px;
			margin-bottom: 10px;
			padding: 3px;
			overflow-x: auto;
			overflow-y: auto;
			max-width: 200px;
			min-height: 350px;
			margin-right: 5px;
		}
		#categoryList-container {
			border: 1px inset grey;
			margin: 2px;
			margin-bottom: 10px;
			padding: 3px;
			overflow: visible;	/*auto;*/
			/*max-height: 150px;*/
		}
		.recom-container {
			width: 90%;
			height: 100%;
			border: 1px inset darkgrey;
			display: block;
			font: 10pt plain Arial,sans-serif;
			padding: 15px;
			margin: 0px;
			background: -webkit-linear-gradient(bottom, rgba(200,200,200,0), rgba(200,200,200,1)); /*Safari 5.1-6*/
			background:      -o-linear-gradient(bottom, rgba(200,200,200,0), rgba(200,200,200,1)); /*Opera 11.1-12*/
			background:    -moz-linear-gradient(bottom, rgba(200,200,200,0), rgba(200,200,200,1)); /*Fx 3.6-15*/
			background:      linear-gradient(to bottom, rgba(200,200,200,0), rgba(200,200,200,1)); /*Standard*/
		}
		
		/* Slider staff */
		.layout { padding: 50px; font-family: Georgia, serif; }
		.layout-slider { margin-bottom: 60px; width: 50%; }
		.layout-slider-settings { font-size: 12px; padding-bottom: 10px; }
		.layout-slider-settings pre { font-family: Courier; }
		
		.reason-indicator {
			color: blue;
			vertical-align: super;
			font-size: smaller;
		}
		.reason-indicator:before {
			content: url(/images/info-2-smaller.png);
		}
		
		.tooltip-pair-reason, .tooltip-pair-reason-attr {
			font: normal 8pt sans-serif !important ;
		}
		
		.tooltip-pair-reason-attr {
			font-style: italic ! important ;
			color: blue;
		}
		
		.ui-tooltip {
			background: lightyellow ! important;
			border: 1px solid darkgrey;
			padding: 2px 2px;
			/*color: darkgrey ! important;*/
			border-radius: 5px;
			font: bold 8px "Helvetica";
			/*text-transform: uppercase;*/
			box-shadow: 0 0 7px black;
		}
	</style>
	
	<link rel="stylesheet" href="../slick/common-grid-styles.css" type="text/css"/>
  </head>
  <body style="padding:10px;">
<%@ include file="../includes/header.html" %>
<!--	<hr />-->

	<table width="100%"><tr><td valign="top" width="1020px">
	
		<!-- WIZARD AREA - BEGIN -->
		<div id="wizard" class="swMain ui-widget-content" style="border: 1px inset grey; padding: 5px; width:1010px /*85%*/; margin-right:10px;">
			<!-- WIZARD TABS - BEGIN -->
		  <ul>
			<li><a href="#step-1">
				  <label class="stepNumber">1</label>
				  <span class="stepDesc">
					 Profile Mgnt<br />
					 <small>Organize your profiles</small>
				  </span>
			  </a></li>
			<li><a href="#step-2">
				  <label class="stepNumber">2</label>
				  <span class="stepDesc">
					 Criteria<br />
					 <small>Add selection criteria</small>
				  </span>
			  </a></li>
			<li><a href="#step-3">
				  <label class="stepNumber">3</label>
				  <span class="stepDesc">
					 Weights calc.<br />
					 <small>with crit. comparisons</small>
				  </span>                   
			   </a></li>
			<li><a href="#step-4">
				  <label class="stepNumber">4</label>
				  <span class="stepDesc">
					 Constraints<br />
					 <small>Add constraints</small>
				  </span>                   
			  </a></li>
		  </ul>
		<!-- WIZARD TABS - END -->
		<!-- WIZARD PAGES - BEGIN -->
		  <div id="step-1">   
			  <h2 class="StepTitle">Step 1 - Profile Management</h2>
			   <!-- step content -->
					<!-- PAGE 1 : BEGIN -->
							<table cellpadding="5px" cellspacing="10px" width="*">
							  <tr>
								<td width="*" align="right" valign="top" colspan="2">
									<div id="formButtonsProfile"></div>
								</td>
							  </tr>
							  <tr>
								<td width="200px" valign="top">
									<div id="profileList" class="tree-container"></div>
									<div style="font:italic 10pt arial,sans-serif; border-top:black 1pt solid; margin-top:10px; padding-top:15px;">Use <strong>Ins</strong> and <strong>Del</strong> hotkeys to create new and delete existing profiles respectively.</div>
								</td>
								<td width="*" valign="top">
									<form id="formEditProfile"></form>
									<div id="categoryList-container"><div id="categoryList"></div></div>
								</td>
							  </tr>
							</table>
					<!-- PAGE 1 : END -->
		  </div>
		  <div id="step-2">
			  <h2 class="StepTitle">Step 2 - Profile criteria</h2> 
			   <!-- step content -->
					<!-- PAGE 2 : BEGIN -->
							<table cellpadding="5px" cellspacing="10px" width="*">
							  <tr>
								<td width="*" valign="top">
									<p align="right">
										<button onClick="savePage2Grid();">Save selections</button>
									</p>
									<!-- slick grid: BEGIN -->
									<div id="page2-grid-container" style="position:relative; width:900px;">
										<div class="grid-header" style="width:100%">
											<label id="page2-grid-header-label">Criteria</label>
										</div>
										<div id="page2-grid" style="width:100%;height:500px;"></div>
										<div id="page2-grid-pager" style="width:100%;height:20px;"></div>
									</div>
									<!-- slick grid: END -->
									<!-- slick grid Context Menu: START -->
									<ul id="page2-grid-contextMenu" style="display:none;position:absolute">
									  <li data="dummy">Dummy 1</li>
									  <li data="dummy">Dummy 2</li>
									</ul>
									<!-- slick grid Context Menu: END -->
								</td>
							  </tr>
							</table>
					<!-- PAGE 2 : END -->
		  </div>                      
		  <div id="step-3">
			  <h2 class="StepTitle">Step 3 - Weight calculation</h2>   
			   <!-- step content -->
					<!-- PAGE 3 : BEGIN -->
							<table cellpadding="5px" cellspacing="10px" width="*">
							  <tr>
								<td width="*" valign="top">
									<p align="right">
										<button onClick="$('#wizard').smartWizard('goForward');">Skip this step, i'll give criteria weights</button>
										<button onClick="savePage3Grid();">Save and Calculate weights</button>
									</p>
									<!-- slick grid: BEGIN -->
									<div id="page3-grid-container" style="position:relative; width:900px;">
										<div class="grid-header" style="width:100%">
											<label id="page3-grid-header-label">Criteria</label>
										</div>
										<div id="page3-grid" style="width:100%;height:500px;"></div>
										<div id="page3-grid-pager" style="width:100%;height:20px;"></div>
									</div>
									<!-- slick grid: END -->
								</td>
							  </tr>
							</table>
					<!-- PAGE 3 : END -->
		  </div>
		  <div id="step-4">
			  <h2 class="StepTitle">Step 4 - Criteria Constraints</h2>   
			   <!-- step content -->                         
					<!-- PAGE 4 : BEGIN -->
							<table cellpadding="5px" cellspacing="10px" width="*">
							  <tr>
								<td width="*" valign="top">
									<p align="right">
										<button onClick="normalizeWeights();">Normalize weights</button>
										<button onClick="savePage4Grid();">Save changes</button>
									</p>
									<!-- slick grid: BEGIN -->
									<div id="page4-grid-container" style="position:relative; width:900px;">
										<div class="grid-header" style="width:100%">
											<label id="page4-grid-header-label">Criteria</label>
										</div>
										<div id="page4-grid" style="width:100%;height:500px;"></div>
										<div id="page4-grid-pager" style="width:100%;height:20px;"></div>
									</div>
									<!-- slick grid: END -->
									<!-- slick grid Context Menu: START -->
									<ul id="page4-grid-contextMenu" style="display:none;position:absolute">
									  <li data="dummy">Dummy 1</li>
									  <li data="dummy">Dummy 2</li>
									</ul>
									<!-- slick grid Context Menu: END -->
								</td>
							  </tr>
							</table>
					<!-- PAGE 4 : END -->
		  </div>
		  <!-- WIZARD PAGES - END -->
		</div>
		<!-- WIZARD AREA - END -->
	</td>
	<td valign="top" width="*">
		<!-- RECOMMENDATIONS - BEGIN -->
		<div class="recom-container">
			<h4 align="center">PuLSaR Recommendations</h4>
			<center>
				<p align="center"><a style="cursor:pointer" onClick="requestRecommendation(null, true);">Get recommendation</a><span id="recomSave"></span></p>
				<div style="overflow: hidden;" id="RecommendationsPanel"></div>
			</center>
		</div>
		<!-- RECOMMENDATIONS - END -->
	</td></tr></table>
	
	<!-- MESSAGES & ERRORS - BEGIN -->
		<!-- Display messages -->
		<!--<div id="res" class="alert" style="margin-top: 10px; min-height: 50px;"></div>-->
	<!-- MESSAGES & ERRORS - END -->

<!-- ================================================================================================== -->
	
	<!-- Override form-commons vars to meet this form's fields -->
	<script type="text/javascript">
		// Override form-commons vars to meet this form's fields
		var formFields = ['id', 'owner', 'name', 'description', 'createTimestamp', 'lastUpdateTimestamp', /*'serviceCategory', 'serviceCategoryText',*/ 'selectionPolicy', 'serviceClassifications'];
		var emptyData = {'id':'', 'owner':'', 'name':'', 'description':'', 'createTimestamp':'', 'lastUpdateTimestamp':'', /*'serviceCategory':'', 'serviceCategoryText':'',*/ 'selectionPolicy':'', 'serviceClassifications':''};
		formSelector = "#formEditProfile";
		statusSelector = '#res';
	</script>
	
	<!-- Profiles specific JS code -->
	<script type="text/javascript" src="profile-node-ops.js"></script>
	<script type="text/javascript" src="profile-init.js"></script>
	<script type="text/javascript" src="../includes/grid-commons.js"></script>
	<script src="../slick/slick.groupitemmetadataprovider.js"></script>
    
	<!-- Recommendations-related JS code -->
	<link rel="stylesheet" href="../jqwidgets/styles/jqx.base.css" type="text/css" />
    <script type="text/javascript" src="../jqwidgets/jqxcore.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxbuttons.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxscrollbar.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxsplitter.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxlistbox.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxdata.js"></script>
    <script type="text/javascript" src="recom-init.js"></script>
	<script>
		recommendationDisplayPanel = "#RecommendationsPanel";
		recommendationAcceptButtonVisible = "false";		// Not used in profile management form
		
		function postSaveResponse(itemId, response, data1) {
			requestRecommendation();
		}
	</script>
	
	<script>
// ==================================================================================================
// Initialization...

		$(document).ready(function() {
			// Initialize Smart Wizard
			initWizard();
			
			/*****  PAGE-1 initialization  *****/
			// Initialize profiles list (tree actually)
			initProfileList();
			// Initialize hotkeys
			initHotkeys();
			// Initialize service categories tree
			initCategoryList();
			// Initialize json forms
			initProfileEditForm();
			initProfileButtonsForm();
			
			updateProfileFormButtonsState(false);
			
			/*****  PAGE-2 initialization  *****/
			// Page 2 initialization occurs when page 2 is displayed.
			
			/*****  PAGE-4 initialization  *****/
			// Page 4 initialization occurs when page 4 is displayed.
			
			// Initialize tooltips (for page 3 attribute reasons)
			$( document ).tooltip({
				items: "[title], [tooltip]",
				content: function() {
							var element = $( this );
							if ( element.is("[title]") )   { return element.attr( "title" ); }
							if ( element.is("[tooltip]") ) { return element.attr( "tooltip" ); }
						}
			});
		});
	</script>
	
<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>