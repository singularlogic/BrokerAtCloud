/*
 *  Attribute operations
 */
function reloadTree() {
	$('#profileList').jstree("refresh");
}
function saveNode() {
	submitForm('#formEditProfile');
}
function doSaveNode() {
	var urlStr;
	if ($('input[name=id]').prop('readonly')===true) {  // save
		urlStr = baseUrl+"/gui/consumer/profile-save";
	} else {  // create
		$('input[name=id]').prop('readonly', true);
		urlStr = baseUrl+"/gui/consumer/profile-create";
	}
	var methodStr = "POST";
	var values = getFormValues();
	prepareValuesForSubmit(values);
	retrieveData(methodStr, urlStr, values, "Saving...");
	reloadTree();
}
function deleteNode() {
	if ($('input[name=id]').prop('readonly')===true) {	// if 'id' field is readonly then it's delete of an existing profile, not a new (unsaved) one
		var tree = $("#profileList").jstree();
		var sel = selectedTreeNode(tree);
		if (sel && sel!=null) {
			// ok, go on...
		} else {
			alert('Select a profile to delete');
			return;
		}
		
		var id = getField('id');
		var name = getField('name');
		if (confirm('Delete profile '+name+' ?')) {
			// prepare WS url
			var urlStr = baseUrl+"/gui/consumer/profile-delete/"+id;

			clearForm();
			setStatus('<p>Waiting for DELETE... <img src="../ajax-loader.gif" /></p>');
			showIndicator("Deleting...");
			$.ajax({
				async: false,
				type: 'GET',
				url: urlStr,
				success: function(data, textStatus, jqXHR) {
							loadingIndicator.fadeOut();
							setStatus('<p>Profile '+name+' DELETED</p><p>'+textStatus+'</p>');
							reloadTree();
							deselectTreeNode();
						},
				error: function(jqXHR, textStatus, errorThrown) {
							loadingIndicator.fadeOut();
							setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
							alert('STATUS='+textStatus+'\nERROR='+errorThrown);
						},
			});
		}
	} else {	// if 'id' field is NOT readonly then it's delete of a new and unsaved profile. So we can simply clear the form (no actual delete occurs in profiles store)
		$('input[name=id]').prop('readonly', false);
		clearForm();
		deselectTreeNode();
	}
}
function createNode() {
	clearForm();
	setField('id', ' ');
	deselectTreeNode();
	//$('input[name=id]').prop('readonly', false);
}
function selectedTreeNode(tree) {
	if (!tree) tree = $("#profileList").jstree();
	var sel = tree.get_selected(true);
	if (sel.length>0) {
		return sel[0];
	} else {
		return null;
	}
}
function deselectTreeNode(tree) {
	if (!tree) tree = "#profileList";
	$(tree).jstree('deselect_all', true);
}
