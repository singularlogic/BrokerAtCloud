/* 
 *	jsTree, hotkeys and jsonform initializations
 */
var profileList;
var categoryList;
var availAttribList;
var wizardGlobalData = {};
var isPage1saved = true;
var isPage2saved = true;
var isPage3saved = true;
var isPage4saved = true;

function initProfileList() {
	// Initialize profile tree
	profileList = $('#profileList').jstree({
	  "core" : {
		"animation" : 500,
		"multiple": false,
		"check_callback" : true,
		"themes" : { "stripes" : true },
		'data' : {
			'url': function (node) {
						return node.id === '#' ? 
								baseUrl+'/gui/consumer/profile-list' : 
								baseUrl+'/gui/consumer/profile-data/'+node.id;
					},
			'data': function (node) { return ''; }
		}
	  },
	  "contextmenu" : {
		"items": function($node) {
			var tree = $("#profileList").jstree(true);
			return {
				"create": {
					"separator_before": false,
					"separator_after": false,
					"label": "Create Profile",
					"action": function (obj) { 
						createNode();
					}
				},
				"remove": {
					"separator_before": false,
					"separator_after": false,
					"label": "Delete",
					"action": function (obj) { 
						deleteNode();
					}
				},
			};
		}
	  },
	  "types" : {
		"#" : {
		  "max_children" : 1, 
		  "max_depth" : 1, 
		  "icon" : "/forms/consumer/profile-tree-node.jpg",
		  "valid_children" : ["profile"]
		},
		"profile" : {
		  "icon" : "/forms/consumer/profile-tree-node.jpg",
		  "valid_children" : []
		},
	  },
	  "plugins" : [
		"state", "types", "wholerow", "hotkeys",
		//"contextmenu", //"dnd", "search",
	  ]
	})
	.on('ready.jstree', function() {
		console.log('Calling clearFormAndLists from #profileList ready.jstree');
		clearFormAndLists();
	})
	.on('changed.jstree', function (e, data) {
		$('input[name=id]').prop('readonly', true);
		if(data && data.selected && data.selected.length) {
			if (data.selected.length>2) {
				alert('BUG!!  multiple selections >2');
				clearForm();
				profileList.jstree('deselect_all');
				categoryList.jstree('deselect_all');
				return;
			} else
			if (data.selected.length==2) {
				//alert('BUG!!  multiple selections ==2');
				for (ii in data.selected) {
					if (data.selected[ii]!=='__root__') {
						profileList.jstree('deselect_all');
						profileList.jstree('select_node', data.selected[ii]);
						selectedProfile = data.selected[ii];
					}
				}
			} else {
				selectedProfile = data.selected[0];
			}

			if (data.selected[0]==="__root__") {
				clearForm();
				profileList.jstree('deselect_all');
				categoryList.jstree('deselect_all');
				return;
			}
			
			startTm = new Date().getTime();
			var url = baseUrl+'/gui/consumer/profile-data/'+selectedProfile;
			statusContacting(url, 'n/a');
			clearForm();
			categoryList.jstree('deselect_all');
			clearRecommendation();
			
			showIndicator("Retrieving profile...");
			callStartTm = new Date().getTime();
			$.get(url, function (d, status) {
				loadingIndicator.fadeOut();
				console.log('Retrieving profile: '+status);
				if(d) {
					renderData(d, 'obviously OK');
					var clsfArr = getField('serviceClassifications').split(",");
					categoryList.jstree('deselect_all');
					for (ii in clsfArr) {
						categoryList.jstree('select_node', clsfArr[ii]);
					}
					
					// initialize global wizard data
					var prId = getField('id');
					var clsfStr = getField('serviceClassifications');
					wizardGlobalData = { 'profile': prId, 'category': clsfStr };
				}
			});
		}
		else {
			$('#data .content').hide();
			$('#data .default').html('xxx').show();
		}
	})
	;
};

/*
 *	Hotkeys initialization
 */
function initHotkeys() {
	//var tree = $("#profileList");
	var tree = $(document);
	tree.bind('keydown', 'insert', function (evt){ createNode(); return false; } );
	tree.bind('keydown', 'del', function (evt){ deleteNode(); return false; } );
}

/*
 *	Smart wizard initialization
 */
function initWizard() {
	$('#wizard').smartWizard(
		{
			transitionEffect: 'slideleft',
			noForwardJumping: false,
			includeFinishButton : false,
			keyNavigation : false,
			enableAllSteps: true,
			onLeaveStep: function (obj, context) {
							console.log('ON-LEAVE-STEP');
							var prId = getField('id');
							//var catId = getField('serviceCategory');
							var clsfStr = getField('serviceClassifications');
							wizardGlobalData = { 'profile': prId, 'category': clsfStr };
							console.log('context: '+JSON.stringify(context));
							console.log('extra: '+JSON.stringify(wizardGlobalData));
							console.log('\n ===================================================================\n');
							
							// Do any necessary validations (e.g. is form saved, is a profile and a service category selected)
							if (!prId || prId==='') {
								alert('Please select a profile');
								return false;
							}
							
							var step = context.fromStep+'';
							var success = true;
							
							if (step) {
								// check if changes have been saved
								if (!wizardGlobalData['profile']) return false;
								//if (step==='1') success = savePage1Grid();
								if (step==='2') success = savePage2Grid();
								if (step==='3') success = savePage3Grid();
								if (step==='4') success = savePage4Grid();
							}
							
							return success;
						},
			onShowStep: function (obj, context) {
							console.log('ON-SHOW-STEP');
							console.log('context: '+JSON.stringify(context));
							console.log('extra: '+JSON.stringify(wizardGlobalData));
							console.log('\n ===================================================================\n');
							
							var step = context.toStep+'';
							
							// load available attributes
							if (step && step==='2') {
								if (!wizardGlobalData['category']) return false;
								//var catId = wizardGlobalData['category'];
								var clsfStr = wizardGlobalData['category'];
								var profileId = wizardGlobalData['profile'];
								//var url = '/gui/consumer/preference-attributes/category/'+catId+'/profile/'+profileId;
								var url = '/gui/consumer/preference-attributes/category/'+clsfStr+'/profile/'+profileId;
								//
								showIndicator("Retrieving...");
								$.ajax({
									async: false,
									type: 'get',
									url: url,
									contentType: 'application/json',
									success: function(data1, textStatus, jqXHR) {
												loadingIndicator.fadeOut();
												if(data1) {
													// Initialize available attributes/criteria grid
													initPage2Grid(data1);
												}
											},
									error: function(jqXHR, textStatus, errorThrown) {
												loadingIndicator.fadeOut();
												alert('STATUS='+textStatus+'\nERROR='+errorThrown);
											},
								});
							} else
							if (step && step==='4') {
								if (!wizardGlobalData['category']) return false;
								//var catId = wizardGlobalData['category'];
								var clsfStr = wizardGlobalData['category'];
								var profileId = wizardGlobalData['profile'];
								//var url = '/gui/consumer/preference-attributes/category/'+catId+'/profile/'+profileId+'/selected';
								var url = '/gui/consumer/preference-attributes/category/'+clsfStr+'/profile/'+profileId+'/selected';
								//
								showIndicator("Retrieving...");
								$.ajax({
									async: false,
									type: 'get',
									url: url,
									contentType: 'application/json',
									success: function(data1, textStatus, jqXHR) {
												loadingIndicator.fadeOut();
												if(data1) {
													// Initialize available attributes/criteria grid
													initPage4Grid(data1);
												}
											},
									error: function(jqXHR, textStatus, errorThrown) {
												loadingIndicator.fadeOut();
												alert('STATUS='+textStatus+'\nERROR='+errorThrown);
											},
								});
							} else
							// load criteria pairs
							if (step && step==='3') {
								if (!wizardGlobalData['profile']) return false;
								var profileId = wizardGlobalData['profile'];
								var url = '/gui/consumer/preference-attributes/pairs/profile/'+profileId;
								//
								showIndicator("Retrieving...");
								$.ajax({
									async: false,
									type: 'get',
									url: url,
									contentType: 'application/json',
									success: function(data1, textStatus, jqXHR) {
												loadingIndicator.fadeOut();
												if(data1) {
													// Initialize available attributes/criteria grid
													initPage3Grid(data1);
												}
											},
									error: function(jqXHR, textStatus, errorThrown) {
												loadingIndicator.fadeOut();
												alert('STATUS='+textStatus+'\nERROR='+errorThrown);
											},
								});
							} else
							
							// adjust wizard contents height
							$('#wizard').smartWizard('fixHeight');
							
							return true;
						},
		}
	);
	$('#wizard').resizable( {alsoResize:".stepContainer", /*animate:true*/} );
};

// ================================================================================================
// Setup and Functions of PAGE-1

/*
 * Main Form specification
 */
function initProfileEditForm() {
	var options = getBaseEditFormOptions('profile');
	options['schema']['selectionPolicy'] = {
		type: 'text',
		readonly: false,
	};
	options['schema']['serviceClassifications'] = {
		type: 'string',
		title: 'Service Classifications',
		readonly: true,
	};
	options['form'][0]['description'] = "A URI uniquely identifying this profile. If you specify a unique key it will be automatically converted to URI upon save. If left empty a GUID will be generated for key.";
	options['form'][1]['description'] = "The owner of the profile (normally you)";
	options['form'][2]['description'] = "The display name of the profile";
	options['form'][3]['description'] = "An explanatory description of the profiles's purpose";
	options['form'].push(
		{	"key": "selectionPolicy",
			"notitle": true,
			"prepend": "Selection Policy: &nbsp;&nbsp;&nbsp;",
			"description": "Selection policy to be used, e.g. TOP 10 (the 10 top ranked items are recommended), TOP 20% (the top 20% ranked items are recommended), OVER 0.7 (items with relevance above 0.7 are recommended), OVER 30% (items with relevance more than 30% of best item's relevance)",
			"fieldHtmlClass": "input-xxlarge",
		} );
	options['form'].push(
		{	"key": "serviceClassifications",
			"notitle": true,
			"prepend": "Service Classifications: ",
			"description": "Selected service classifications. Select service classifications from the tree below.",
			"fieldHtmlClass": "input-xxlarge",
		} );
	options['onSubmitValid'] = function (values) {
								doSaveNode();
							};
	$('#formEditProfile').jsonForm(options);
}

/*
 * Buttons Form specification
 */
function initProfileButtonsForm() {
	var options = getBaseButtonsFormOptions('Profile');
	options['form'][0]['onClick'] = function (evt) { savePage1Grid(); };		// Save button
	options['form'][1]['onClick'] = function (evt) { deleteNode(); };			// Delete button
	options['form'][2]['onClick'] = function (evt) { createNode(); };			// Create button
	options['form'][3]['onClick'] = function (evt) { clearFormAndLists(); };	// Clear button
	$('#formButtonsProfile').jsonForm(options);
}

/*
 *	Clear form, lists and recommendations
 */
function clearFormAndLists() {
	if (isPage1saved===false && confirm('Discard changes?')) {
		console.log('clearFormAndLists: BEGIN');
		try { clearForm(); } catch (e) { console.log('clearFormAndLists: clearForm: EXCEPTION: '+e); }
		try { deselectTreeNode("#profileList"); } catch (e) { console.log('clearFormAndLists: clear #profileList: EXCEPTION: '+e); }
		try { deselectTreeNode('#categoryList'); } catch (e) { console.log('clearFormAndLists: clear #categoryList: EXCEPTION: '+e); }
		try { clearRecommendation(); } catch (e) { console.log('clearFormAndLists: clearRecommendation: EXCEPTION: '+e); }
		console.log('clearFormAndLists: END');
	}
}

/*
 *	Service Categories jstree initialization
 */
function initCategoryList() {
	// Initialize service category tree
	categoryList = $('#categoryList').jstree({
	  "core" : {
		"animation" : 500,
		"multiple": true,
		"check_callback" : true,
		"themes" : { "stripes" : true },
		'data' : {
			'url': function (node) {
						return '/gui/consumer/category-list';
					},
			'data': function (node) { return ''; }
		}
	  },
	  "types" : {
		"#" : {
		  "max_children" : 1, 
		  "valid_children" : ["category"]
		},
		"category" : {
		  "valid_children" : ["category"]
		},
	  },
	  "checkbox" : {
		"three_state" : false,
		"keep_selected_style" : true,
		"cascade" : "down"
	  },
	  "plugins" : [
		"state", "types", "wholerow", "hotkeys",
		//"contextmenu", //"dnd", "search",
		"checkbox"
	  ]
	})
	.on('ready.jstree', function() {
		console.log('Calling clearFormAndLists from #categoryList ready.jstree');
		clearFormAndLists();
	})
	.on('changed.jstree', function (e, data) {
		if(data && data.selected && data.selected.length && e && data.node && data.node.text) {
			setField('serviceClassifications', data.selected);
		}
	});
};

function savePage1Grid() {
	var profileId = wizardGlobalData['profile'];
	saveNode();
	isPage1saved = true;
}

// ================================================================================================
// Setup and Functions of PAGE-2 grid

// Slick.Formatters.* : PercentComplete, PercentCompleteBar, YesNo, Checkmark
// Slick.Editors.* : Text, Integer, Date, YesNoSelect, Checkbox, PercentComplete, LongText
  
// Page 2 grid columns
var page2grid_columns = [
	{id: "sel", name: "#", field: "rownum", behavior: "select", cssClass: "cell-selection", width: 40, selectable: false },
//	{id: "id", name: "ID", field: "id", behavior: "select", cssClass: "cell-selection", width: 10, selectable: false },
//	{id: "aid", name: "Attr.Id", field: "aid", width: 10, cssClass: "cell-title"},
    {id: "selected", name: "Sel.", field: "selected", width: 60, cssClass: "cell-mandatory", formatter: Slick.Formatters.Checkmark /*, editor: Slick.Editors.Checkbox*/},
    {id: "name", name: "Opt. Attribute", field: "name", width: 200, cssClass: "cell-title"},
//    {id: "mandatory", name: "Mandatory", field: "mandatory", width: 60, cssClass: "cell-mandatory", formatter: Slick.Formatters.Checkmark, editor: Slick.Editors.Checkbox},
    {id: "type", name: "Type", width: 200, formatter: TypeFormatter},
    {id: "params", name: "Allowed values", width: 400, formatter: TypeRangeFormatter},
];

// Page 2 grid options
var page2grid_options = {
	editable: true,
	enableAddRow: false,
	enableCellNavigation: true,
	asyncEditorLoading: false,
	autoEdit: true
};

/*
 *	Initialize available attributes/criteria grid (page 2)
 */
var page2grid_dataView;
var page2grid;
var page2grid_data = [];

function initPage2Grid(data) {
	// renumber data
	for (rr=0,n=data.length;rr<n;rr++) data[rr]['rownum'] = rr+1;
	
	// create dataview and grid
	var groupItemMetadataProvider = new Slick.Data.GroupItemMetadataProvider();

	page2grid_dataView = new Slick.Data.DataView({
		inlineFilters: /*true*/false,
		groupItemMetadataProvider: groupItemMetadataProvider
	});
	page2grid = new Slick.Grid("#page2-grid", page2grid_dataView, page2grid_columns, page2grid_options);
	page2grid.registerPlugin(groupItemMetadataProvider);
	var page2grid_pager = new Slick.Controls.Pager(page2grid_dataView, page2grid, $("#page2-grid-pager"));
	//$('#page2-grid-container').resizable();

	// set event handlers
	page2grid_dataView.onRowCountChanged.subscribe(function (e, args) {
	  page2grid.updateRowCount();
	  page2grid.render();
	});

	page2grid_dataView.onRowsChanged.subscribe(function (e, args) {
	  page2grid.invalidateRows(args.rows);
	  page2grid.render();
	});

	page2grid.onValidationError.subscribe(function (e, args) {
	  alert(args.validationResults.msg);
	});
	
	// Handle click on selected field (i.e. toggle selected value)
	page2grid.onClick.subscribe(function (e) {
		var cell = page2grid.getCellFromEvent(e);
		if (page2grid.getColumns()[cell.cell].id === "selected") {
			if (!page2grid.getEditorLock().commitCurrentEdit()) {
				return;
			}
			
			// change selected value
			var item = page2grid_dataView.getItem( cell.row );
			item.selected = ! item.selected;
			page2grid_dataView.updateItem(item.id, item);
			
			e.stopPropagation();
		}
	});
	
	// set data and grouping
	page2grid_dataView.beginUpdate();
	page2grid_data = data;
	page2grid_dataView.setItems(page2grid_data);
	page2grid_dataView.setGrouping({
		getter: "category",
		formatter: function (g) {
		  return "<b>Service Category:</b> " + g.value + " &nbsp; <span style='color:green'>(" + g.count + " attributes)</span>";
		},
		aggregators: [
		],
		aggregateCollapsed: false,
		lazyTotalsCalculation: true
	});
	page2grid_dataView.endUpdate();
}

function savePage2Grid() {
	var profileId = wizardGlobalData['profile'];
	// get selected attribute id's
	var selectedAttrs = [];
	for (i in page2grid_data) {
		var item = page2grid_data[i];
		if (item.selected && item.selected===true) {
			if (item.id && item.id!=='') {
				selectedAttrs.push(item.id);
			}
		}
	}
	
	// save selected attributes to profile
	var url = '/gui/consumer/preference-attributes/profile/'+profileId+'/save';
	showIndicator("Saving...");
	var success = false;
	$.ajax({
		async: false,
		type: 'post',
		url: url,
		data: JSON.stringify(selectedAttrs),
		contentType: 'application/json',
		dataType: 'json',
		success: function(data1, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					isPage2saved = true;
					success = true;
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
					success = false;
				},
	});
	return success;
}

// ================================================================================================
// Setup and Functions of PAGE-4 grid

// Slick.Formatters.* : PercentComplete, PercentCompleteBar, YesNo, Checkmark
// Slick.Editors.* : Text, Integer, Date, YesNoSelect, Checkbox, PercentComplete, LongText
  
// Page 4 grid columns
var page4grid_columns = [
	{id: "sel", name: "#", field: "rownum", behavior: "select", cssClass: "cell-selection", width: 40, selectable: false },
//	{id: "id", name: "ID", field: "id", behavior: "select", cssClass: "cell-selection", width: 10, selectable: false },
//	{id: "aid", name: "Attr.Id", field: "aid", width: 10, cssClass: "cell-title"},
//	{id: "cpid", name: "CP id", field: "cpid", width: 100, cssClass: "cell-title"},
    {id: "name", name: "Opt. Attribute", field: "name", width: 150, cssClass: "cell-title"},
    {id: "mandatory", name: "Mandatory", field: "mandatory", width: 60, cssClass: "cell-mandatory", formatter: Slick.Formatters.Checkmark /*, editor: Slick.Editors.Checkbox*/},
    {id: "weight", name: "Weight", field: "weight", width: 120, groupTotalsFormatter: sumTotalsFormatter, formatter: PercentFormatter, editor: PercentEditor, displayFormat:"%", displayPrecision:2},
    {id: "type", name: "Type", width: 100, formatter: TypeFormatter},
    {id: "params", name: "Allowed values", width: 150, formatter: TypeRangeFormatter},
    {id: "constraints", name: "Constraints", field: "constraints", width: 200, formatter: ConstraintsFormatter, editor: Slick.Editors.LongText},
];

// Weight sum, formatter and editor
function sumTotalsFormatter(totals, columnDef) {
	var val = totals.sum && totals.sum[columnDef.field];
	if (val != null) {
		if (columnDef && columnDef.displayFormat && columnDef.displayFormat!=='%') {
			return ""+parseFloat(val);
		} else {
			precision = (columnDef && columnDef.displayPrecision && !isNaN(columnDef.displayPrecision) && parseInt(columnDef.displayPrecision)>=0) ? parseInt(columnDef.displayPrecision) : 2;
			precision = Math.pow(10, precision);
			return (Math.round(parseFloat(val)*100*precision)/precision) + "%";
		}
	}
	return "";
}

// Constraints formatter and editor
function ConstraintsFormatter(row, cell, value, columnDef, dataContext) {
	return '<b>'+dataContext.constraints+'</b>';
}

// Page 4 grid options
var page4grid_options = {
	editable: true,
	enableAddRow: false,
	enableCellNavigation: true,
	showHeaderRow: true,
	explicitInitialization: true,
	asyncEditorLoading: false,
	autoEdit: true
};

/*
 *	Initialize available attributes/criteria grid (page 4)
 */
var page4grid_dataView;
var page4grid;
var page4grid_data = [];

function isNumeric(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
}

function _round(f) {
	if (f && f!=='' && !isNaN(f) && isNumeric(f) && f>Number.NEGATIVE_INFINITY && f<Number.POSITIVE_INFINITY) {
		return Math.round(f*1000000000)/1000000000;
	}
	return f;
}

function initPage4Grid(data) {
	// renumber data
	for (rr=0,n=data.length;rr<n;rr++) {
		data[rr]['rownum'] = rr+1;
		data[rr]['from'] = _round(data[rr]['from']);
		data[rr]['fromL'] = _round(data[rr]['fromL']);
		data[rr]['fromU'] = _round(data[rr]['fromU']);
		data[rr]['to'] = _round(data[rr]['to']);
		data[rr]['toL'] = _round(data[rr]['toL']);
		data[rr]['toU'] = _round(data[rr]['toU']);
	}
	
	// create dataview and grid
	var groupItemMetadataProvider = new Slick.Data.GroupItemMetadataProvider();

	page4grid_dataView = new Slick.Data.DataView({
		inlineFilters: /*true*/false,
		groupItemMetadataProvider: groupItemMetadataProvider
	});
	page4grid = new Slick.Grid("#page4-grid", page4grid_dataView, page4grid_columns, page4grid_options);
	page4grid.registerPlugin(groupItemMetadataProvider);
	var page4grid_pager = new Slick.Controls.Pager(page4grid_dataView, page4grid, $("#page4-grid-pager"));
	//$('#page4-grid-container').resizable();

	// set event handlers
	page4grid_dataView.onRowCountChanged.subscribe(function (e, args) {
	  page4grid.updateRowCount();
	  page4grid.render();
	});

	page4grid_dataView.onRowsChanged.subscribe(function (e, args) {
	  page4grid.invalidateRows(args.rows);
	  page4grid.render();
	});

	page4grid.onValidationError.subscribe(function (e, args) {
	  alert(args.validationResults.msg);
	});
	
	page4grid.onCellChange.subscribe(function (e, args) {
	  var grid = args.grid;
	  var cell = args.cell;
	  if (grid.getColumns()[args.cell].id === "weight") {
	    page4grid_dataView.refresh();
		refreshGrandTotals();
		e.stopPropagation();
	  }
	});
	
	// Handle click on mandatory field (i.e. toggle mandatory value)
	page4grid.onClick.subscribe(function (e) {
		var cell = page4grid.getCellFromEvent(e);
		if (page4grid.getColumns()[cell.cell].id === "mandatory") {
			if (!page4grid.getEditorLock().commitCurrentEdit()) {
				return;
			}
			
			// change mandatory value
			var item = page4grid_dataView.getItem( cell.row );
			item.mandatory = ! item.mandatory;
			page4grid_dataView.updateItem(item.id, item);
			
			e.stopPropagation();
		}
	});
	
	// weights sum
	page4grid.onHeaderRowCellRendered.subscribe(function(e, args) {
		$(args.node).empty();
		if (args.column.id==="weight") {
			//$('<input id="page4grid_header_total_weight" type="text" name="page4grid_header_total_weight" value="0" size="5" readonly />')
			$('<span id="page4grid_header_total_weight">100%</span>')
				.appendTo(args.node);
		}
	});

	page4grid.init();

	// set data and grouping
	page4grid_dataView.beginUpdate();
	page4grid_data = data;
	page4grid_dataView.setItems(page4grid_data);
	page4grid_dataView.setGrouping({
		getter: "category",
		formatter: function (g) {
		  return "<b>Service Category:</b> " + g.value + " &nbsp; <span style='color:green'>(" + g.count + " attributes)</span>";
		},
		aggregators: [
			 new Slick.Data.Aggregators.Sum("weight")		// adds a row after each group with weights sum
		],
		aggregateCollapsed: false,
		lazyTotalsCalculation: false
	});
	page4grid_dataView.endUpdate();
	
	// normalize weights
	normalizeWeights(); normalizeWeights(); normalizeWeights();
	refreshGrandTotals();
}

function refreshGrandTotals() {
	sum = 0;
	for (rowId in page4grid_data) {
		var row = page4grid_data[rowId];
		sum += row.weight;
	}
	
	console.log('refreshGrandTotals: '+sum);
	$('#page4grid_header_total_weight').html( _round(100*sum)+'%' );
}

function savePage4Grid() {
	if (!checkNormalized()) {
		alert('Weights are not normalized');
		return;
	}
	
	// save preference parameter
	var profileId = wizardGlobalData['profile'];
	var url = '/gui/consumer/preference-attributes/profile/'+profileId+'/preferences/save';
	showIndicator("Saving...");
	var success = false;
	$.ajax({
		async: false,
		type: 'post',
		url: url,
		data: JSON.stringify(page4grid_data),
		contentType: 'application/json',
		dataType: 'json',
		success: function(data1, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					isPage4saved = true;
					success = true;
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
					success = false;
				},
	});

	return success;
}

function checkNormalized() {
	var sum = 0;
	for (i in page4grid_data) {
		var item = page4grid_data[i];
		if (!item.weight) continue;
		sum += item.weight;
	}
	return (sum==1);
}

function normalizeWeights() {
	var sum = 0;
	for (i in page4grid_data) {
		var item = page4grid_data[i];
		if (!item.weight) continue;
		sum += item.weight;
	}
	if (sum<=0) return;
	for (i in page4grid_data) {
		var item = page4grid_data[i];
		if (!item.weight) continue;
		item.weight /= sum;
	}
	page4grid.invalidateAllRows();
	page4grid.render();
	
	refreshGrandTotals();
}

// ================================================================================================
// Setup and Functions of PAGE-3 grid

// Slick.Formatters.* : PercentComplete, PercentCompleteBar, YesNo, Checkmark
// Slick.Editors.* : Text, Integer, Date, YesNoSelect, Checkbox, PercentComplete, LongText
  
// Page 3 grid columns
var page3grid_columns = [
	{id: "sel", name: "#", field: "rownum", behavior: "select", cssClass: "cell-selection", width: 40, selectable: false },
//	{id: "id", name: "ID", field: "id", behavior: "select", cssClass: "cell-selection", width: 10, selectable: false },
//	{id: "parent", name: "Parent id", field: "parent", width: 100, cssClass: "cell-title"},
//    {id: "attr1_id", name: "Attr #1", field: "attr1_id", width: 150, cssClass: "cell-title"},
//    {id: "attr2_id", name: "Attr #2", field: "attr2_id", width: 150, cssClass: "cell-title"},
    {id: "attr1_name", name: "Attribute #1", field: "attr1_name", width: 150, cssClass: "cell-title", formatter: HtmlCellFormatter},
    {id: "value", name: "Rel. Importance", field: "value", width: 500, cssClass: "cell-title", formatter: SliderFormatter},
    {id: "attr2_name", name: "Attribute #2", field: "attr2_name", width: 150, cssClass: "cell-title", formatter: HtmlCellFormatter},
];

function HtmlCellFormatter(row, cell, value, columnDef, dataContext) {
	var tooltip = null;
	if (cell===1) tooltip = dataContext.attr1_reason;
	if (cell===3) tooltip = dataContext.attr2_reason;
	return (tooltip==null) ? value : '<span tooltip="<p class=tooltip-pair-reason>This attribute appears in comparison pairs because it is an ancestor of :<br/><span class=tooltip-pair-reason-attr>'+tooltip+'</span><br/>For more information view attribute hierarchy at Attribute Management page</p>"><span class="reason-indicator"></span>'+value+'</span>';
}

function updatePairValue(elem) {
	alert(JSON.stringify(elem));
}

function SliderFormatter(row, cell, value, columnDef, dataContext) {
	return '<div style="min-height:100px; white-space: nowrap;"><input name="'+dataContext.id+'" class="page3-grid-slider" type="slider" value="'+dataContext.value+'" /></div>';
}

// Page 3 grid options
var page3grid_options = {
	editable: true,
	enableAddRow: false,
	enableCellNavigation: true,
	asyncEditorLoading: false,
	autoEdit: true,
	rowHeight: 50
};

/*
 *	Initialize available attributes/criteria grid (page 3)
 */
var page3grid_dataView;
var page3grid;
var page3grid_data = [];

function initPage3Grid(data) {
	// renumber data
	for (rr=0,n=data.length;rr<n;rr++) data[rr]['rownum'] = rr+1;
	
	// create dataview and grid
	var groupItemMetadataProvider = new Slick.Data.GroupItemMetadataProvider();

	page3grid_dataView = new Slick.Data.DataView({
		inlineFilters: /*true*/false,
		groupItemMetadataProvider: groupItemMetadataProvider
	});
	page3grid = new Slick.Grid("#page3-grid", page3grid_dataView, page3grid_columns, page3grid_options);
	page3grid.registerPlugin(groupItemMetadataProvider);
	var page3grid_pager = new Slick.Controls.Pager(page3grid_dataView, page3grid, $("#page3-grid-pager"));
	//$('#page3-grid-container').resizable();
	
//	var tooltipPlugin = new Slick.AutoTooltips({ enableForHeaderCells: true });
//	page3grid.registerPlugin(tooltipPlugin);
//	page3grid.render();
	
	// set event handlers
	page3grid_dataView.onRowCountChanged.subscribe(function (e, args) {
	  page3grid.updateRowCount();
	  page3grid.render();
	});

	page3grid_dataView.onRowsChanged.subscribe(function (e, args) {
	  page3grid.invalidateRows(args.rows);
	  page3grid.render();
	});

	page3grid.onValidationError.subscribe(function (e, args) {
	  alert(args.validationResults.msg);
	});
	
	// set data and grouping
	page3grid_dataView.beginUpdate();
	page3grid_data = data;
	page3grid_dataView.setItems(page3grid_data);
	page3grid_dataView.setGrouping({
		getter: "parent",
		formatter: function (g) {
		  return "<b>Parent attribute:</b> <span style='color:blue'>" + g.value + "</span> &nbsp; <span style='color:green'>(" + g.count + " pairs)</span>";
		},
		aggregators: [
		],
		aggregateCollapsed: false,
		lazyTotalsCalculation: true
	});
	page3grid_dataView.endUpdate();
	
	// Initialize sliders
	initSliders();
	
	$(".slick-viewport").scroll(function(){
		initSliders();
	}); 
}

function initSliders() {
	jQuery(".page3-grid-slider").slider({
		from: -8,
		to: 8,
		step: 1,
		round: 1,
		scale: ['&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Extreme', 'Very<br/>Strong', 'Strong', 'Moderate', 'Equal', 'Moderate', 'Strong', 'Very<br/>Strong', 'Extreme&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'],
		limits: false,
		dimension: '',
		callback: function (value) {
			var inp = this.inputNode[0];
			var id = inp.name;
			var val = inp.value;
			if (val<-8) val=-8;
			if (val>8) val=8;
			for (ii in page3grid_data) {
				var pair = page3grid_data[ii];
				if (pair.id===id) {
					pair.value = val;
					break;
				}
			}
		},
	});
}

function savePage3Grid() {
	// save comparisons
	var profileId = wizardGlobalData['profile'];
	var url = '/gui/consumer/preference-attributes/profile/'+profileId+'/calculate-weights';
	showIndicator("Saving...");
	var success = false;
	$.ajax({
		async: false,
		type: 'post',
		url: url,
		data: JSON.stringify(page3grid_data),
		contentType: 'application/json',
		dataType: 'json',
		success: function(data1, textStatus, jqXHR) {
					loadingIndicator.fadeOut();
					isPage3saved = true;
					success = true;
				},
		error: function(jqXHR, textStatus, errorThrown) {
					loadingIndicator.fadeOut();
					alert('STATUS='+textStatus+'\nERROR='+errorThrown);
					success = false;
				},
	});
	return success;
}

//EOF