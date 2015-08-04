<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "Consumer Feedback"; %>
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
				<div style="width: 100%;" id="ContentPanelContainer">
					<div style="visibility:visible; text-align:left; background:rgb(239, 239, 239); padding-top:3px; padding-bottom:3px;" id="ContentPanelButtons">
						&nbsp;&nbsp;&nbsp;<button onClick="saveFeedback();">Save</button>
						&nbsp;&nbsp;&nbsp;<button onClick="reloadFeedback();">Reload</button>
						&nbsp;&nbsp;&nbsp;<button onClick="deleteFeedback();">Delete</button>
					</div>
					<div style="overflow: scroll; width: 100%; height: 100%;" id="ContentPanelScroller">
						<div style="overflow: hidden; padding:10px; text-align:left;" id="ContentPanel"></div>
					</div>
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
    <script type="text/javascript" src="feedback-init.js"></script>
	<script>
		feedbackDisplayPanel = "#ContentPanel";
		feedbackDisplayButtons = "#ContentPanelButtons";
	</script>
	
	<script type="text/javascript">
		
		$(document).ready(function () {
				
			$("#splitter").jqxSplitter({  width: '90%', height: 400, panels: [{ size: '30%'}] });
			$('#splitter').resizable();
			
			// prepare the data
			var url = "/gui/feedback/used-services-list";
			var source =
			{
				datatype: "json",
				datafields: [
					{ name: 'id' },
					{ name: 'lastUsedTimestamp' },
					{ name: 'service-name' },
					{ name: 'service' },
					{ name: 'status' },
				],
				id: 'id',
				url: url
			};
			var dataAdapter = new $.jqx.dataAdapter(source, {
				loadComplete: function(data) {
					theData = data;
					loadingIndicator.fadeOut();
				},
				loadError: function(xhr,status,error) {
					loadingIndicator.fadeOut();
					alert('STATUS='+status+'\nERROR='+error);
				}
			});
			
			// Create a jqxListBox
			showIndicator("Retrieving used service list...");
			$('#listbox').jqxListBox({ selectedIndex: 0,  source: dataAdapter, displayMember: "service-name", valueMember: "lastUsedTimestamp", /*itemHeight: 70,*/ width: '100%', height: '100%',
				renderer: function (index, label, value) {
					// status
					var items = $('#listbox').jqxListBox('getItems');
					var item = items[index];
					if (item && item.originalItem) item = item.originalItem;
					var status = item.status;
					// last used date
					var dtStr;
					var tm = new Number(value);
					if (tm==-1) dtStr = "Currently in use";
					else dtStr = (new Date(tm)).toUTCString();
					// item html
					var itemHtml;
					if (status==='IN-USE') itemHtml = '<table style="min-width: 130px;"><tr><td rowspan="2" width="30px"><img src="/images/feedback-1.png" /></td><td><b>'+ /*'<b>' + (index+1) + '.</b> ' +*/ label + '</b></td></tr><tr><td><i>' + dtStr + '</i></td></tr></table>';
					else if (status==='NOT-USED-RECENT') itemHtml = '<table style="min-width: 130px;"><tr><td rowspan="2" width="30px"><img src="/images/feedback-2.png" /></td><td>'+label + '</td></tr><tr><td><i>' + dtStr + '</i></td></tr></table>';
					else if (status==='NOT-USED-OLD') itemHtml = '<table style="min-width: 130px; color: gray"><tr><td rowspan="2" width="30px"><img src="/images/feedback-3.png" /></td><td>'+label + '</td></tr><tr><td><i>' + dtStr + '</i></td></tr></table>';
					return itemHtml;
				}
			});
			$('#listbox').on('select', function (event) {
				if (!event || !event.args || !event.args.item) return;
				var item = event.args.item;
				var row = item.originalItem;
				if (!row) return;
				showFeedbackForm(row);
			});
		});
	</script>
	
	<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>