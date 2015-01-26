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
		//$('#listbox_2').html('');
		$('#listbox_2').jqxListBox({ selectedIndex: 0,  source: dataAdapter, displayMember: "createTimestamp", valueMember: "message", /*itemHeight: 70,*/ width: '100%', height: '100%',
			renderer: function (index, label, value) {
				var dt = (new Date(new Number(label))).toUTCString();
				var table = '<table style="min-width: 130px;"><tr><td><b>' + (index+1) + '.</b> ' + dt + '</td></tr><tr><td><i>' + value + '</i></td></tr></table>';
				return table;
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