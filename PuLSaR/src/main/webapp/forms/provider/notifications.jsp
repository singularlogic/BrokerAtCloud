<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "PuLSaR Notifications"; %>
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
					<div style="border: none;" id="listbox"></div>
				</div>
<!--				<div style="overflow: hidden; padding:10px;" id="ContentPanel"></div>-->
				<div style="overflow: hidden;" id="ContentPanel">
					<div style="border: none;" id="listbox_2"></div>
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
    <script type="text/javascript" src="notif-init.js"></script>
	<script>
		notificationDisplayPanel = "#ContentPanel";
		var listEmpty = true;
	</script>
	
	<script type="text/javascript">
		$(document).ready(function () {
				
			$("#splitter").jqxSplitter({  width: 800, height: 400, panels: [{ size: '30%'}] });
			$('#splitter').resizable();
			
			// prepare the data
			var url = "/gui/notifications/list-services";
			var source =
			{
				datatype: "json",
				datafields: [
					{ name: 'id' },
					{ name: 'createTimestamp' },
					{ name: 'service-name' },
					{ name: 'slp-uri' },
					{ name: 'slp-id' },
				],
				id: 'id',
				url: url
			};
			var dataAdapter = new $.jqx.dataAdapter(source, {
				loadComplete: function(data) {
					loadingIndicator.fadeOut();
					if (listEmpty) {
						$(notificationDisplayPanel).html('<br/><i>There are no service notifications so far</i>');
						//$("#listbox").html('<br/><i>There are no service notifications so far</i>');
						$("#splitter").jqxSplitter('collapse');
					}
				},
				loadError: function(xhr,status,error) {
					loadingIndicator.fadeOut();
					$(notificationDisplayPanel).html('<i>Error while retrieving notifications<br/>'+error+'</i>');
					alert('STATUS='+status+'\nERROR='+error);
				}
			});
			
			// Create a jqxListBox
			showIndicator("Retrieving notifications...");
			$('#listbox').jqxListBox({ selectedIndex: 0,  source: dataAdapter, displayMember: "service-name", valueMember: "slp-id", /*itemHeight: 70,*/ width: '100%', height: '100%',
				renderer: function (index, label, value) {
					var table = '<table style="min-width: 130px;"><tr><td><b>' + (index+1) + '.</b> ' + label + '</td></tr><tr><td><i>' + value + '</i></td></tr></table>';
					listEmpty = false;
					return table;
				}
			});
			$('#listbox').on('select', function (event) {
				if (!event || !event.args || !event.args.item) return;
				var item = event.args.item;
				var row = item.originalItem;
				if (!row) return;
				updateNotification(row);
			});
		});
	</script>
	
	<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>