/*
 *	Notifications display functions
 */

var notificationDisplayPanel = "#NotificationsPanel";

function updateNotification(service) {
	var container = $('<div style="margin: 5px;"></div>')
	
	if (service && service.id && service.id!=='' && service.id!==null) {
		// prepare the data
		var url = "/gui/notifications/sd/"+encodeURIComponent(service.id)+"/list";
		//console.log("URL:  "+url);
		var source =
		{
			datatype: "json",
			datafields: [
				{ name: 'id' },
				{ name: 'createTimestamp' },
				{ name: 'service-name' },
				{ name: 'message' },
				{ name: 'type' },
			],
			id: 'id',
			url: url
		};
		var dataAdapter = new $.jqx.dataAdapter(source, {
			loadComplete: function(data) {
				loadingIndicator.fadeOut();
			},
			loadError: function(xhr,status,error) {
				loadingIndicator.fadeOut();
				alert('STATUS='+status+'\nERROR='+error);
			}
		});
		
		// Create a jqxListBox
		showIndicator("Retrieving notifications...");
		$('#listbox_2').jqxListBox({ selectedIndex: 0,  source: dataAdapter, displayMember: "createTimestamp", valueMember: "message", /*itemHeight: 70,*/ width: '100%', height: '100%',
			renderer: function (index, label, value) {
				var row = dataAdapter.records[index];
				var type = row.type;
				var dt = (new Date(new Number(label))).toUTCString();
				
				var typeStr = '';
				if (type==='RECOMMENDATION_NOTIFICATION') typeStr = '<span style="background:red; color:yellow; font-weight:bold;">&nbsp;RECOM&nbsp;</span>';
				else if (type==='FEEDBACK_NOTIFICATION') typeStr = '<span style="background:darkgreen; color:yellow; font-weight:bold;">&nbsp;FEEDBACK&nbsp;</span>';
				var bgcolor = index%2==0 ? 'rgb(230,230,230)' : 'rgb(255,255,255)';
				
				var itemHtml = '<div style="padding: 2px; min-width: 130px; background:'+bgcolor+';"><b>' + (index+1) + '.</b> ' + typeStr + ' ' + dt + '<br/><i>' + value + '</i></div>';
				return itemHtml;
			}
		});
		
		return;
	}
	else {
		container.append("<i>Service not found: "+service.id+"</i><br/>JSON: "+JSON.stringify(service));
	}
	
	if (!notificationDisplayPanel) notificationDisplayPanel = "#NotificationsPanel";
	$(notificationDisplayPanel).html(container.html());
}

// EOF