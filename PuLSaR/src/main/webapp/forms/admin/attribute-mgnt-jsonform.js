/*
 *	JSONform initialization
 */
	
/*
 * Main Form specification
 */
function initAttributeEditForm() {
	var options = getBaseEditFormOptions('attribute');
	options['schema']['parent'] = {
		type: 'string',
		title: 'Parent',
		readonly: true
	};
/*	options['schema']['source'] = {
		type: 'string',
		title: 'Source',
	};*/
	options['form'].splice(1, 0,
		{	"key": "parent",
			"notitle": true,
			"prepend": "Parent: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"description": "A URI specifying the parent attribute. If empty it is a top-level attribute",
			"fieldHtmlClass": "input-xxlarge",
		}
	);
/*	options['form'].push(
		{	"key": "source",
			"notitle": true,
			"prepend": "Source: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"description": "A resource URI specifying which is the actual data source",
			"fieldHtmlClass": "input-xxlarge",
		} );*/
	options['onSubmitValid'] = function (values) {
								doSaveNode();
							};
/*		options['onSubmit'] = function (errors, values) {
							if (errors) {
								$('#res').html('<p>I beg your pardon?</p>');
							}
							else {
								onSubmitValid(values);
							}
						};*/
	$('#formEdit').jsonForm(options);
}

/*
 * Buttons Form specification
 */
function initAttributeButtonsForm() {
	options = getBaseButtonsFormOptions('attribute');
	options['form'][0]['onClick'] = function (evt) { saveNode(); };
	options['form'][1]['onClick'] = function (evt) { deleteNode(); deselectTreeNode(); };
	options['form'][2]['onClick'] = function (evt) { deselectTreeNode(); createSibling(); };
	options['form'][3]['onClick'] = function (evt) { clearForm(); deselectTreeNode(); };
	options['form'][2]['title'] = "Create sibling";
	options['form'].splice(3, 0,
		{	"title": "Create child",
			"type": "button",
			"onClick": function (evt) { deselectTreeNode(); createChild(); }
		}
	);
	$('#formButtons').jsonForm(options);
}