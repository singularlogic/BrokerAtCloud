/*
 *	Consumer Feedback display functions
 */

var feedbackDisplayPanel = "#FeedbackPanel";
var feedbackDisplayButtons = "#FeedbackPanelButtons";

function showFeedbackForm(feedback) {
	var container = $('<div style="margin: 5px;"></div>')
	//if (feedbackDisplayButtons) $(feedbackDisplayButtons).hide();
	
	var status = feedback.status;
	if (status==='NOT-USED-OLD') {
		var tm = feedback.lastUsedTimestamp;
		if (tm && tm!=='' && !isNaN(tm)) tm = new Date(tm).toUTCString();
		$(feedbackDisplayPanel).html('<i><b>Feedback is not allowed for this service:</b><br/>'+feedback['service-name']+'<br/><b>Last used:</b> '+tm+'</i>');
		return;
	}
	
	if (feedback && feedback.service && feedback.service!=='' && feedback.service!==null) {
		// retrieve and process feedback form specs
		retrieveFeedbackFormSpec(feedback.service);
	}
	else {
		container.append("<i>No feedback for service: "+feedback.service+"</i><br/>JSON: "+JSON.stringify(feedback));
		if (!feedbackDisplayPanel) feedbackDisplayPanel = "#FeedbackPanel";
		$(feedbackDisplayPanel).html(container.html());
	}
}

function retrieveFeedbackFormSpec(srvUri) {
	// Check if a service has been selected
	if (!srvUri || srvUri.trim()==='') {
		alert('You must select first a service!');
		return;
	}
	
	// prepare the data
	var url = "/gui/feedback/sd/"+encodeURIComponent(srvUri);
	console.log("Retrieving feedback form specs: URL: "+url);
	//if (feedbackDisplayButtons) $(feedbackDisplayButtons).hide();
	
	showIndicator("Retrieving feedback...");
	$.get( url )
	.done( function(data) {
		loadingIndicator.fadeOut();
		//console.log("Feedback jsonform specification: \n"+JSON.stringify(data));
		$(feedbackDisplayPanel).html('');
		$(feedbackDisplayPanel).jsonForm(data);
		//if (feedbackDisplayButtons) $(feedbackDisplayButtons).show();
	} )
	.fail( function(jqXHR, status, error) {
		loadingIndicator.fadeOut();
		alert('STATUS='+status+'\nERROR='+error);
		//if (feedbackDisplayButtons) $(feedbackDisplayButtons).hide();
	} );
}

function reloadFeedback() {
	var srvUri = $("input[name=_SERVICE_URI]").val();
	//console.log('Reloading feedback form for service: '+srvUri);
	if (confirm('If you reload feedback form, any unsaved changes will be lost.\nDo you want to proceed?')) {
		retrieveFeedbackFormSpec(srvUri);
	}
}

function saveFeedback() {
	// Get service URI
	var data = {};
	var srvUri = $("input[name=_SERVICE_URI]").val();
	data['_SERVICE_URI'] = srvUri;
	//console.log('Service URI: '+srvUri);
	
	// Check if a service has been selected
	if (!srvUri || srvUri.trim()==='') {
		alert('You must select first a service!');
		return;
	}
	
	// Get attribute URIs (used as id's)
	var attrUri = [];
	var ii = 0;
	do { ii++; var sel = $('input[name=ATTR-URI-'+ii+']'); if (sel.length==0) break; var uri = sel.val(); if (uri && uri.trim()!=='') attrUri[ii] = uri; } while (true);
	//console.log('Attribute URIs found:\n'+JSON.stringify(attrUri));
	
	// Get feedback per attribute
	var attrCnt = ii-1;
	//console.log('Count of Attributes: '+attrCnt);
	for (ii=1; ii<=attrCnt; ii++) {
		//console.log('Checking for ATTR-ID'+ii);
		var sel = $('input[name=ATTR-ID-'+ii+']');
		//console.log('\tATTR-ID'+ii+' : input? : '+sel.length);
		if (sel.length==0) sel = $('select[name=ATTR-ID-'+ii+']');
		//console.log('\tATTR-ID'+ii+' : select? : '+sel.length);
		if (sel.length==0) {
			console.long('ERROR: unknown or non-existing form control: ATTR-ID-'+ii);
			continue;
		}
		var val = sel.val();
		//console.log('\tATTR-ID'+ii+' : value='+val);
		if (val) {
			var uri = attrUri[ii];
			//console.log('\tATTR-ID'+ii+' : uri='+uri);
			data[ uri ] = val;
		}
	}
	//console.log('Feedback form data to submit for save:\n'+JSON.stringify(data));
	
	if (Object.keys(data).length<=1) {
		alert('Your feedback is empty!\nIf you want to remove feedback press "Delete" button');
		return;
	}
	
	// Post feedback form data to server for storing
	var url = "/gui/feedback/sd/"+encodeURIComponent(srvUri);
	//console.log("Saving feedback form data: URL: "+url);
	
	showIndicator("Saving feedback...");
	$.ajax({
		async: false,
		type: 'post',
		url: url,
		data: JSON.stringify(data),
		contentType: 'application/json',
		dataType: 'json',
	})
	.done( function(data) {
		loadingIndicator.fadeOut();
		console.log("Feedback was saved: Server Response: "+JSON.stringify(data));
	} )
	.fail( function(jqXHR, status, error) {
		loadingIndicator.fadeOut();
		alert('STATUS='+status+'\nERROR='+error);
	} );
}

function deleteFeedback() {
	// Check if a service has been selected
	var srvUri = $("input[name=_SERVICE_URI]").val();
	if (!srvUri || srvUri.trim()==='') {
		alert('You must select first a service!');
		return;
	}
	
	// Confirm user action
	if (!confirm('Are you sure you want to delete this feedback?')) {
		return;
	}
	
	// Instruct server to delete feedback
	var url = "/gui/feedback/sd/"+encodeURIComponent(srvUri);
	console.log("Deleting feedback data: URL: "+url);
	
	showIndicator("Deleting feedback...");
	$.ajax({
		async: false,
		type: 'delete',
		url: url,
		contentType: 'application/json',
	})
	.done( function(data) {
		loadingIndicator.fadeOut();
		console.log("Feedback was deleted: Server Response: "+JSON.stringify(data));
		clearFeedbackForm();
	} )
	.fail( function(jqXHR, status, error) {
		loadingIndicator.fadeOut();
		alert('STATUS='+status+'\nERROR='+error);
	} );
}

function clearFeedbackForm() {
	// Clear service URI
	//$("input[name=_SERVICE_URI]").val('');
	
	// Count attribute URIs (used as id's) and clear their values
	var ii = 0;
	do { ii++; var sel = $('input[name=ATTR-URI-'+ii+']'); if (sel.length==0) break; sel.val(''); } while (true);
	
	// Clear feedback per attribute
	var attrCnt = ii-1;
	//console.log('Count of Attributes: '+attrCnt);
	for (ii=1; ii<=attrCnt; ii++) {
		//console.log('Checking for ATTR-ID'+ii);
		var sel = $('input[name=ATTR-ID-'+ii+']');
		//console.log('\tATTR-ID'+ii+' : input? : '+sel.length);
		if (sel.length==0) sel = $('select[name=ATTR-ID-'+ii+']');
		//console.log('\tATTR-ID'+ii+' : select? : '+sel.length);
		if (sel.length==0) {
			console.long('ERROR: unknown or non-existing form control: ATTR-ID-'+ii);
			continue;
		}
		sel.val('');
	}
}

// EOF