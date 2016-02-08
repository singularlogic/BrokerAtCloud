/*
 *	Recommendation display functions
 */

var recommendationDisplayPanel = "#RecommendationsPanel";
var recommendationAcceptButtonVisible = "false";

function requestRecommendation(profileId, requestNewRecom) {
	if (!profileId || profileId===null || profileId.trim()==='') {
		if (!wizardGlobalData['profile']) profileId = null;
		else profileId = wizardGlobalData['profile'];
	}
	if (!profileId || profileId===null || profileId.trim()==='') {
		alert('Please select an existing Profile or create a new one');
		return;
	}
	
	if (requestNewRecom && requestNewRecom===true) {
		url = '/gui/recommendations/'+profileId+'/request';
		showIndicator("Requesting a new recommendation...");
	} else {
		url = '/gui/recommendations/'+profileId+'/list';
		showIndicator("Retrieving recommendation...");
	}
	$.ajax({
		async: false,
		type: 'get',
		url: url,
		contentType: 'application/json',
		success: function(data1, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					if(data1) {
						// Show only the first recommendation
						if (data1[0]) data1 = data1[0];
						// update recommendation items panel
						updateRecommendation(data1);
					}
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
				},
	});
}

function updateRecommendation(recom) {
	var container = $('<div style="margin: 5px;"></div>')
	var noRecom = true;
	
	if (recom && recom.id && recom.id!=='' && recom.id!==null) {
		noRecom = false;
		var rowtpl = '<tr style="border-top: 1px solid darkgrey;"><td colspan="2">%SUGGESTION%<td></tr><tr style="border-bottom: 1px solid darkgrey;"><td nowrap><font color="blue"><i>Score: %REL%%</i></font> %VALUES_BUTTON%</td><td align="right" width="100px">%ACCEPT_BUTTON%</td></tr>';
		var rowtpl_accepted = '<tr style="background:yellow; border-top: 1px solid darkgrey;"><td colspan="2">%SUGGESTION%<td></tr><tr style="background:yellow; border-bottom: 1px solid darkgrey;"><td nowrap><font color="blue"><i>Score: %REL%%</i></font> %VALUES_BUTTON%</td><td align="right" width="150px"><b>Accepted!</b> %ACCEPT_BUTTON%</td></tr>';
		var acctpl = '<a href="#" onClick="saveResponse(this, \'%ITEM-ID%\', \'ACCEPT\');">Accept</a>';
		var acctpl_accepted = '<a href="#" onClick="saveResponse(this, \'%ITEM-ID%\', \'UNKNOWN\');">Clear</a>';
		var valtpl = '<a href="#" onClick="showExtra(this, \'%EXTRA%\');">[+]</a>';
		
		if (recom.items && recom.items.length>0) {
			var recomItems = recom.items;
			var items = '<table width="100%">';
			for (ii in recomItems) {
				var rit = recomItems[ii];
				var iid = rit.id;
				var suggestion = rit.suggestion;
				var relevance = rit.relevance;
				var order = rit.order;
				var response = rit.response;
				var extra = rit.extra;
				
				// prepare attribute id/value pairs in extra for display
				var str = '';
				for (ii in extra) {
					pair = extra[ii];
					at = pair.attribute;
					val = pair.value;
					str += at+' = '+val+'\\n';
				}
				extra = str;
				
				if (response.trim().toUpperCase()==='ACCEPT') tpl = rowtpl_accepted;
				else tpl = rowtpl;
				if (recommendationAcceptButtonVisible==='true') {
					tpl2 = (response.trim().toUpperCase()==='ACCEPT') ? acctpl_accepted : acctpl;
					tpl3 = '';		// don't show in recommendations form
				} else {
					tpl2 = '';
					tpl3 = valtpl;	// show in profile mgnt form
				}
				acc = tpl2.replace('%ITEM-ID%',iid);
				val = tpl3.replace('%EXTRA%',extra);
				
				items += tpl.replace('%SUGGESTION%',suggestion).replace('%REL%',relevance).replace('%ITEM-ID%',iid).replace('%ACCEPT_BUTTON%',acc).replace('%VALUES_BUTTON%',val);
			}
			items += '</table>';
		}
		else {
			items = '<i>No suggestions</i>';
		}
		
		var tbl = '<table>';
		tbl += '<tr><td><b>Id:</b></td><td colspan="3">' + recom.id + '</td></tr>';
		tbl += '<tr><td><b>Creation:</b></td><td>' + new Date(recom.createTimestamp).toUTCString() + '</td>';
		tbl += '</tr>';
		tbl += '<tr><td><b>Profile:</b></td><td>' + recom['profile-name'] + '</td>';
		tbl += '</tr>';
		tbl += '<tr><td colspan="4"><br/><b>Suggestions:</b></td></tr>';
		tbl += '<tr><td colspan="4">' + items + '</td></tr>';
		tbl += '</table>';
		
		container.append(tbl);
	}
	else {
		container.append("<i>No recommendations</i>");
	}
	
	if (!recommendationDisplayPanel) recommendationDisplayPanel = "#RecommendationsPanel";
	$(recommendationDisplayPanel).html(container.html());
	
	// Create 'Save' link
	if (!noRecom && $('#recomSave').length>0) {
		if (!wizardGlobalData['profile']) return;
		profileId = wizardGlobalData['profile'];
		var saveUrl = "/gui/recommendations/profile/"+profileId+"/make-permenent/"+recom.id;
		$('#recomSave').html('&nbsp;&nbsp;&nbsp;&nbsp;<a style="cursor:pointer" onClick="updateSave(\''+saveUrl+'\');">Save</a>');
	} else 
	if ($('#recomSave').length>0) $('#recomSave').html('');
}

function updateSave(url) {
	showIndicator("Saving recommendation...");
	$.ajax({
		async: false,
		type: 'get',
		url: url,
		success: function(data1, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					$('#recomSave').html('&nbsp;&nbsp;&nbsp;&nbsp;'+data1);
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
				},
	});
}

function saveResponse(elem, itemId, response) {
	url = '/gui/recommendations/'+itemId+'/'+response;
	showIndicator("Saving response...");
	$.ajax({
		async: false,
		type: 'post',
		url: url,
		contentType: 'application/json',
		success: function(data1, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					if (postSaveResponse) postSaveResponse(itemId, response, data1);
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
				},
	});
}

function clearRecommendation() {
	var container = $('<div style="margin: 5px;"></div>')
	if (!recommendationDisplayPanel) recommendationDisplayPanel = "#RecommendationsPanel";
	$(recommendationDisplayPanel).html(container.html());
	
	// Clean 'Save' link
	if ($('#recomSave').length>0) $('#recomSave').html('');
}

// Show extra service details
//
function showExtra(elem, extra) {
	//showExtraPopupDialog(elem, extra);
	showExtraExpandable(elem, extra);
}

var dialogServDetails;

function showExtraPopupDialog(elem, extra) {
	//console.log('Recommendation item: extra info = \n'+extra);
	
	if ($( "#dialog-message" ).length>0) $( "#dialog-message" ).empty();
	if (dialogServDetails && dialogServDetails!=='') { dialogServDetails.empty(); dialogServDetails = ''; }
	
	tblHtml = '';
	cnt = 0;
	rows = extra.split('\n');
	for (i=0; i<rows.length; i++) {
		row = rows[i].trim();
		if (row==='') continue;
		p = row.indexOf('=');
		attr = row.substring(0,p).trim().replace('#','');
		val = row.substring(p+1).trim();
		tblHtml += '	  <div class="row"><div class="cell" style="text-align: right;"><b>'+attr+'</b></div><div class="cell"><i>'+val+'</i></div></div>\n';
		cnt++;
	}
	
	html = 
		'<link rel="stylesheet" href="extra.css" type="text/css">'+
		'<div id="dialog-message" title="Service Details">'+
		'  <p>'+
		'	<div class="wrapper"><div class="table">'+
		'	  <div class="row header blue"><div class="cell" style="text-align:center">Attribute</div><div class="cell" style="text-align:center">Value</div></div>'+
		tblHtml+
		'	</div></div>'+
		'  </p>'+
		'</div>';
	dialogServDetails = $(html);
	dialogServDetails.appendTo(document.body);

	$( "#dialog-message" ).dialog({
		modal: true,
		width: 800,
		closeOnEscape: false,
		buttons: {
			Ok: function() {
				$( this ).dialog( "close" );
				dialogServDetails.remove();
				dialogServDetails = '';
			}
		}
	});
}

function showExtraExpandable(elem, extra) {
	//console.log('Recommendation item: extra info = \n'+extra);
	
	var txt = $(elem).eq(0).html();
	if (txt!=='[+]') {
		// details are shown... collapsing div
		$(elem).empty();
		$(elem).append( '[+]' );
		return ;
	}
	//else :  details are hidden... expand div
	
	// prepare service details for display
	tblHtml = '';
	cnt = 0;
	rows = extra.split('\n');
	for (i=0; i<rows.length; i++) {
		row = rows[i].trim();
		if (row==='') continue;
		p = row.indexOf('=');
		attr = row.substring(0,p).trim().replace('#','');
		val = row.substring(p+1).trim();
		tblHtml += '	  <div class="row"><div class="cell" style="text-align: right;"><b>'+attr+'</b></div><div class="cell"><i>'+val+'</i></div></div>\n';
		cnt++;
	}
	
	html = 
		'<link rel="stylesheet" href="extra.css" type="text/css">'+
		'<div id="dialog-message" title="Service Details">'+
		'  <p>'+
		'	<div class="wrapper"><div class="table">'+
		'	  <div class="row header blue"><div class="cell" style="text-align:center">Attribute</div><div class="cell" style="text-align:center">Value</div></div>'+
		tblHtml+
		'	</div></div>'+
		'  </p>'+
		'</div>';
	
	// replace <a> contents with service details (including the [-] collapse button)
	$(elem).empty();
	details = $(html);
	$(elem).html( '[&#8211;]' ).append( details );
}

// EOF