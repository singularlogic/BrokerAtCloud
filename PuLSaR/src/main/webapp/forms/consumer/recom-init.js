/*
 *	Recommendation display functions
 */

var recommendationDisplayPanel = "#RecommendationsPanel";
var recommendationAcceptButtonVisible = "true";

function requestRecommendation(profileId, requestNewRecom) {
	if (!profileId || profileId===null || profileId.trim()==='') {
		if (!wizardGlobalData['profile']) return;
		profileId = wizardGlobalData['profile'];
	}
	if (!profileId || profileId===null || profileId.trim()==='') return;
	
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
		var rowtpl = '<tr style="border-top: 1px solid darkgrey;"><td colspan="2">%SUGGESTION%<td></tr><tr style="border-bottom: 1px solid darkgrey;"><td nowrap><font color="blue"><i>Score: %REL%%</i></font> <a href="#" onClick="showExtra(this, \'%EXTRA%\');">[+]</a></td><td align="right" width="100px">%ACCEPT_BUTTON%</td></tr>';
		var rowtpl_accepted = '<tr style="background:yellow; border-top: 1px solid darkgrey;"><td colspan="2">%SUGGESTION%<td></tr><tr style="background:yellow; border-bottom: 1px solid darkgrey;"><td nowrap><font color="blue"><i>Score: %REL%%</i></font> <a href="#" onClick="showExtra(this, \'%EXTRA%\');">[+]</a></td><td align="right" width="150px"><b>Accepted!</b> %ACCEPT_BUTTON%</td></tr>';
		var acctpl = '<a href="#" onClick="saveResponse(this, \'%ITEM-ID%\', \'ACCEPT\');">Accept</a>';
		var acctpl_accepted = '<a href="#" onClick="saveResponse(this, \'%ITEM-ID%\', \'UNKNOWN\');">Clear</a>';
		
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
					if (response.trim().toUpperCase()==='ACCEPT') tpl2 = acctpl_accepted;
					else tpl2 = acctpl;
				} else tpl2 = '';
				acc = tpl2.replace('%ITEM-ID%',iid);
				items += tpl.replace('%SUGGESTION%',suggestion).replace('%REL%',relevance).replace('%ITEM-ID%',iid).replace('%EXTRA%',extra).replace('%ACCEPT_BUTTON%',acc);
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

function showExtra(elem, extra) {
	console.log('Recommendation item: extra info = \n'+extra);
	alert(extra);
}

// EOF