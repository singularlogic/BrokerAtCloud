<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "PuLSaR Recommendations"; %>
<%@ include file="../includes/head.html" %>
	<link rel="stylesheet" href="../slick/common-grid-styles.css" type="text/css"/>
  </head>
  <body style="padding:10px;">
<%@ include file="../includes/header.html" %>
<!--	<table border="2px" cellpadding="15px" cellspacing="10px" style="border:2px solid lightgray; outline: 2px solid darkgray;" width="100%">
	  <tr>
        <td width="*" align="center" valign="top">--><center>
			<div id="splitter">
				<div style="overflow: hidden;">
					<div style="overflow:auto; border: none;" id="listbox"></div>
				</div>
				<div style="overflow: auto;">
					<div style="overflow: auto; padding:10px" id="ContentPanel"></div>
				</div>
			</div>
		</center><!--</td>
	  </tr>
	</table>-->
    
<%@ include file="../includes/js-libs.html" %>
    <link rel="stylesheet" href="../jqwidgets/styles/jqx.base.css" type="text/css" />
    <script type="text/javascript" src="../jqwidgets/jqxcore.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxbuttons.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxscrollbar.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxsplitter.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxlistbox.js"></script>
    <script type="text/javascript" src="../jqwidgets/jqxdata.js"></script>
    <script type="text/javascript" src="recom-init.js"></script>
	<script>
		recommendationDisplayPanel = "#ContentPanel";
		var listEmpty = true;
	</script>
	
	<script type="text/javascript">
		function postSaveResponse(itemId, response, data1) {
			var selectedItem = $("#listbox").jqxListBox('getSelectedItem');
			showIndicator("Retrieving recommendations...");
			$('#listbox').jqxListBox('refresh');
			if (selectedItem && selectedItem!=null && selectedItem!==null) $("#listbox").jqxListBox('selectItem', selectedItem); 
		}
		
		$(document).ready(function () {
				
			$("#splitter").jqxSplitter({  width: 700, height: 400, panels: [{ size: '40%'}] });
			$('#splitter').resizable({
				stop: function( event, ui ) {
					$("#splitter").jqxSplitter('collapse');
					$("#splitter").jqxSplitter('expand');
				}
			});
			
			// prepare the data
			var url = "/gui/recommendations/list";
			var source =
			{
				datatype: "json",
				datafields: [
					{ name: 'id' },
					{ name: 'createTimestamp' },
					{ name: 'profile-name' },
					{ name: 'profile' },
					{ name: 'items' },
				],
				id: 'id',
				url: url
			};
			var dataAdapter = new $.jqx.dataAdapter(source, {
				loadComplete: function(data) {
					loadingIndicator.fadeOut();
					if (listEmpty) {
						$(recommendationDisplayPanel).html('<br/><i>There are no profiles with active recommendations so far</i>');
						//$("#listbox").html('<br/><i>There are no profiles with active recommendations so far</i>');
						$("#splitter").jqxSplitter('collapse');
					}
				},
				loadError: function(xhr,status,error) {
					loadingIndicator.fadeOut();
					$(recommendationDisplayPanel).html('<i>Error while retrieving profiles<br/>'+error+'</i>');
					alert('STATUS='+status+'\nERROR='+error);
				}
			});
			
			// Create a jqxListBox
			showIndicator("Retrieving recommendations...");
			$('#listbox').jqxListBox({ selectedIndex: 0,  source: dataAdapter, displayMember: "profile-name", valueMember: "createTimestamp", /*itemHeight: 70,*/ width: '100%', height: '100%',
				renderer: function (index, label, value) {
					//var datarecord = data[index];
					var dt = new Date(new Number(value));
					var table = '<table style="min-width: 130px;"><tr><td><b>' + (index+1) + '.</b> ' + label + '</td></tr><tr><td><i>' + dt.toUTCString() + '</i></td></tr></table>';
					listEmpty = false;
					return table;
				}
			});
			$('#listbox').on('select', function (event) {
				if (!event || !event.args || !event.args.item) return;
				var item = event.args.item;
				var row = item.originalItem;
				if (!row) return;
				updateRecommendation(row);
			});
		});
	</script>
	
	<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>