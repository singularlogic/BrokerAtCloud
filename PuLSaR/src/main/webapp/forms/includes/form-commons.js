/*
 *	Utility Function common to all forms
 */
// User-provided variables
var baseUrl = '';
var formFields = [];
var emptyData = {};
var formSelector = "";
var attrParent;
var attrNode;

// Timer variables
var startTm = null;
var callStartTm = null;

/*
 * Common form functions
 */
// Initialization and auxiliary functions
function makeEmpty(fields) {
	if (!fields) fields = formFields;
	emptyData = {};
	for (f in fields) emptyData[f] = '';
}
function getBaseEditFormOptions(entityName) {
	var entity;
	if (!entityName) entity = 'node'; else entity = entityName;
	return {
		schema: {
		  id: {
			type: 'string',
			title: 'Id',
			readonly: true,
			required: true
		  },
		  owner: {
			type: 'string',
			title: 'Owner',
			readonly: true,
			required: false
		  },
		  name: {
			type: 'string',
			title: 'Name',
			required: true
		  },
		  description: {
			type: 'string',
			title: 'Description',
		  },
		  createTimestamp: {
			//type: 'date',
			type: 'string',
			title: 'Creation',
			readonly: true,
		  },
		  lastUpdateTimestamp: {
			//type: 'date',
			type: 'string',
			title: 'Last Update',
			readonly: true,
		  },
		},
		"form": [
			{	"key": "id",
				"notitle": true,
				"prepend": "Id: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
				"append": "<font color=red><i>required</i></font>",
				"description": "A URI uniquely identifying this "+entity+". If you specify a unique id it will be automatically converted to URI upon save. If left empty a GUID will be generated for Id.",
				"fieldHtmlClass": "input-xxlarge",
			},
			{	"key": "owner",
				"notitle": true,
				"prepend": "Owner: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
				"append": "<font color=red><i>required</i></font>",
				"description": "The owner of the "+entity+" (normally you)",
				"fieldHtmlClass": "input-xxlarge",
			},
			{	"key": "name", 
				"notitle": true,
				"prepend": "Name: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
				"append": "<font color=red><i>required</i></font>",
				"description": "The display name of the "+entity,
				"fieldHtmlClass": "input-xxlarge",
			},
			{	"key": "description",
				"notitle": true,
				"prepend": "Description: ",
				"description": "An explanatory description of the "+entity+"'s purpose",
				"fieldHtmlClass": "input-xxlarge",
			},
			{	"key": "createTimestamp", 
				//"type": "datetime",
				"notitle": true,
				"prepend": "Creation Date/Time: &nbsp;&nbsp;&nbsp;&nbsp;",
			},
			{	"key": "lastUpdateTimestamp",
				//"type": "datetime",
				"notitle": true,
				"prepend": "Last Update Date/Time: ",
			},
		]
	};
}
function getBaseButtonsFormOptions(entityName) {
	var entity;
	if (entityName) entity = entityName; else entity = 'Node';
	return {
		schema: {
		},
		"form": [
			{	"title": "Save Changes",
				"type": "button",
				"onClick": function (evt) {
					alert('NOT IMPLEMENTED');
				}
			},
			{	"title": "Delete "+entity,
				"type": "button",
				"onClick": function (evt) {
					alert('NOT IMPLEMENTED');
				}
			},
			{	"title": "Create "+entity,
				"type": "button",
				"onClick": function (evt) {
					alert('NOT IMPLEMENTED');
				}
			},
			{	"title": "Clear form",
				"type": "button",
				"onClick": function (evt) {
					alert('NOT IMPLEMENTED');
				}
			},
		],
	}
}

// Form and form field functions
function setField(name, value) {
	$('input[name='+name+']').val(value);
}
function getField(name) {
	return $('input[name='+name+']').val();
}
function getFormValues() {
	var values = {};
	for (ii in formFields) {
		var fld = formFields[ii];
		values[fld] = getField(fld);
	}
	return values;
}
function setFormValues(values) {
	attrNode = values;
	for (ii in formFields) {
		var fld = formFields[ii];
		var val = values[fld];
		if (fld==='parent' && val && val.id) {
			attrParent = val;
			val = attrParent.id;
		}
		setField(fld, val);
	}
}
function setFormData(data) {
	setFormValues(data);
}
function submitForm(frmSel) {
	if (frmSel && frmSel.trim()!=='')  $(frmSel).submit();
	else $(formSelector).submit();
}
function clearForm(frmSel) {
	setFormValues(emptyData);
	for (f in formFields) $('input[name='+f+']').val('');
	$('input[name=id]').prop('readonly',true);
}

// Data exchange functions
function retrieveData(methodStr, urlStr, values, mesg) {
	var valStr = '';
	//for (i in values) if (values[i]==='-') values[i]='';
	var _hasParent = ( values && values['parent'] && attrParent && values['parent']===attrParent.id );
	if (_hasParent) values['parent'] = attrParent;
	else delete values['parent'];
	if (values) valStr = JSON.stringify(values);
	if (_hasParent) values['parent'] = attrParent.id;
	statusContacting(methodStr+' '+urlStr, valStr);
	//alert('METHOD: '+methodStr+'\nURL: '+urlStr+'\nSENDING: ' + JSON.stringify(values)+'\n\n');
	//clearForm();
	setStatus('<p>Waiting for reply... <img src="../ajax-loader.gif" /></p>');
	if (mesg) showIndicator(mesg); else showIndicator("Contacting server...");
	callStartTm = new Date().getTime();
	$.ajax({
		async: false,
		type: methodStr,
		url: urlStr,
		data: valStr,
		contentType: 'application/json',
		dataType: 'json',
		success: function(data, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					renderData(data, textStatus);
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
				},
		//complete: function() { alert('ALWAYS'); }
	});
}

// Rendering functions
function renderData(data, textStatus) {
	var callEndTm = new Date().getTime();
	var dataStr = JSON.stringify(data);
	prepareValuesForRender(data);
	clearForm();
	setFormData(data);
	var endTm = new Date().getTime();
	setStatus('<p>Status: '+textStatus+'</p>'+
					'<p>Duration: '+(endTm-startTm)+'ms   contacting server: '+(callEndTm-callStartTm)+'ms</p>'+
					'<p>JSON: '+dataStr+'</p>');
}
function renderDate(tm) {
	if (tm) {
		try {
			if ($.isNumeric(tm)) { return new Date(tm).toISOString(); }
		} catch (e) { alert(tm+'\n'+e); }
	}
	return tm;
}
function prepareValuesForSubmit(data) {
//XXX: TODO: Convert date/times from 'd/m/Y H:i:s' format to W3C standard format 'YYYY-mm-dd'T'HH:mi:ss.000+TZ'
}
function prepareValuesForRender(data) {
//XXX: TODO: Convert date/times from W3C standard format 'YYYY-mm-dd'T'HH:mi:ss.000+TZ'  to  'd/m/Y H:i:s'
	data.lastUpdateTimestamp = renderDate(data.lastUpdateTimestamp);
	data.createTimestamp = renderDate(data.createTimestamp);
}

// Define custom date/time picker input field
JSONForm.fieldTypes['datetime'] = {
	'template': '<input type="text" ' +
	  '<%= (fieldHtmlClass ? "class=\'" + fieldHtmlClass + "\' " : "") %>' +
	  'name="<%= node.name %>" value="<%= escape(value) %>" id="<%= id %>"' +
	  '<%= (node.disabled? " disabled" : "")%>' +
	  '<%= (node.readOnly ? " readonly=\'readonly\'" : "") %>' +
	  '<%= (node.schemaElement && node.schemaElement.maxLength ? " maxlength=\'" + node.schemaElement.maxLength + "\'" : "") %>' +
	  '<%= (node.schemaElement && node.schemaElement.required && (node.schemaElement.type !== "boolean") ? " required=\'required\'" : "") %>' +
	  ' />\n' +
	  ' <script type="text\/javascript">$("#<%= id %>").datetimepicker({format:"d/m/Y H:i:s",mask:"9999-19-39T29:59:59.999Z"});<\/script>\n',
	'fieldtemplate': true,
	'inputfield': true
}
