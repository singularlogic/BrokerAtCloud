<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "Service Attribute Mapping to Service Classifications"; %>
<%@ include file="../includes/head.html" %>
	<link rel="stylesheet" href="../slick/common-grid-styles.css" type="text/css"/>
  </head>
  <body style="padding:10px;">
<%@ include file="../includes/header.html" %>
	<table border="2px" cellpadding="15px" cellspacing="10px" style="border:2px solid lightgray; outline: 2px solid darkgray;" width="100%">
	  <tr>
	    <td width="300px" valign="top" style="">
			<div id="scListTitle" style="font-weight:bold;">Classification Dimensions</div>
			<div style="/*overflow:scroll;*/ background: white;">
				<div id="scList"></div>
			</div>
		</td>
        <td width="*" valign="top">
			<!-- slick grid: BEGIN -->
			<div id="grid-container" style="position:relative; width:900px;">
				<div class="grid-header" style="width:100%">
					<label id="grid-header-label">Service Classification: ...</label>
				</div>
				<div id="myGrid" style="width:100%;height:500px;"></div>
				<div id="pager" style="width:100%;height:20px;"></div>
			</div>
			<!-- slick grid: END -->
			
			<!-- slick grid Context Menu: START -->
			<ul id="contextMenu" style="display:none;position:absolute">
			  <b>Actions:</b>
			  <li data="add">New Attribute</li>
			  <li data="modify">Modify Attribute</li>
			  <li data="delete">Delete Attribute</li>
			</ul>
			<!-- slick grid Context Menu: END -->
			
			<p><hr /></p>
			<div id="formButtons"></div>
			<p><br/></p>
			<div id="res" class="alert"></div>
		</td>
	  </tr>
	</table>
	
	<!-- Attributes tree appearing in lightbox -->
	<div id="attrList-container" class="white-popup mfp-hide" style="border: 2px solid; border-radius: 25px; max-width:450px; padding-top:0px;">
		<div style="border-top-left-radius:25px; height:20px; background:grey; vertical-align:text-bootm; color:white; font-weight:bold; font-style:italics; margin:0px; margin-left:-20px; margin-right:15px; padding:5px; padding-left:35px;">Optimisation Attribute Selection</div>
		<div style="border: 2px inset darkgrey; border-radius: 20px; padding: 20px; margin-top:20px; margin-bottom:10px;">
			<div id="attrList-header" style="padding-bottom:10px;">
				<ul><li style="font-size:10pt; list-style: disc inside none;">Double click on a node to select it (this will close lightbox)</li>
					<li style="font-size:10pt; list-style: disc inside none;">For more options right click on a node</li>
					<li style="font-size:10pt; list-style: disc inside none;">Hit ESC or click anywhere outside to close lightbox</li>
				</ul>
			</div>
			<div style="width:100%; text-align: center;">
				<div id="attrList-insert-bt"><a style="cursor: pointer; cursor: hand;" onClick="doAddMultipleAttributes();">Insert</a></div>
			</div>
			<div id="attrList" style="overflow:scroll; border:1px inset lightgrey; min-height: 500px; max-height:500px; max-width:400px; background: white; bolder: 1px solid black; width:100%;"></div>
		</div>
	</div>
    
<!-- ======================================================================================================================= -->

	<!-- Attribute operation functions -->
	<script type="text/javascript" src="attribute-mgnt-node-ops.js"></script>
	
<%@ include file="../includes/js-libs.html" %>
<%@ include file="../includes/js-libs-slickgrid.html" %>

	<!-- Lightbox related files and styles -->
	<script src="../deps/magnific-popup/dist/jquery.magnific-popup.js"></script> 
	<link rel="stylesheet" href="../deps/magnific-popup/dist/magnific-popup.css"> 
	<style>
		.white-popup {
		  position: relative;
		  background: #FFF;
		  padding: 20px;
		  width: auto;
		  max-width: 500px;
		  margin: 20px auto;
		}

		/* a fix to allow jstree context menu to appear above lightbox */
		.vakata-context {
			z-index:2000; 
		}
	</style>

<!-- ======================================================================================================================= -->

<script>
  // Slick.Formatters.* : PercentComplete, PercentCompleteBar, YesNo, Checkmark
  // Slick.Editors.* : Text, Integer, Date, YesNoSelect, Checkbox, PercentComplete, LongText
  
  var columns = [
	{id: "sel", name: "#", field: "rownum", behavior: "select", cssClass: "cell-selection", width: 40, selectable: false },
	//{id: "id", name: "ID", field: "id", behavior: "select", cssClass: "cell-selection", width: 40, selectable: false },
    {id: "bppName", name: "BP property", field: "bppName", width: 140, cssClass: "cell-title"},
    //{id: "aid", name: "Attr.Id", field: "aid", width: 50, cssClass: "cell-title"},
    {id: "name", name: "Opt. Attribute", field: "name", width: 200, cssClass: "cell-title"},
    {id: "unit", name: "Unit", field: "unit", width: 60, editor: Slick.Editors.Text},
//	{id: "mandatory", name: "Mandatory", field: "mandatory", width: 60, cssClass: "cell-mandatory", formatter: Slick.Formatters.Checkmark /*, editor: Slick.Editors.Checkbox*/ },
    {id: "type", name: "Type", field: "type", width: 200, editor: TypeEditor},
    {id: "params", name: "Allowed values", width: 300, formatter: TypeRangeFormatter, editor: TypeRangeEditor},
	{id: "labelEn", name: "Label &nbsp;<img src='images/en-1.png'/>", field: "labelEn", width: 200, editor: Slick.Editors.Text},
	{id: "labelDe", name: "Label &nbsp;<img src='images/de-1.png'/>", field: "labelDe", width: 200, editor: Slick.Editors.Text},
	{id: "comment", name: "Comment", field: "comment", width: 200, editor: Slick.Editors.Text},
	{id: "measuredBy", name: "Measured By", field: "measuredBy", width: 200, editor: Slick.Editors.Text}
  ];

  var options = {
    editable: true,
    enableAddRow: false,
    enableCellNavigation: true,
    asyncEditorLoading: false,
	autoEdit: true
  };

// ================================================================================================

	var dataView;
	var grid;
	var data = [];
	var serviceCategory = new String('');
	var serviceCategoryName = '';
	
	function initGrid() {
		dataView = new Slick.Data.DataView({ inlineFilters: /*true*/false });
		grid = new Slick.Grid("#myGrid", /*data*/ dataView, columns, options);
		var pager = new Slick.Controls.Pager(dataView, grid, $("#pager"));
		//$('#grid-container').resizable();

		dataView.onRowCountChanged.subscribe(function (e, args) {
		  grid.updateRowCount();
		  grid.render();
		});

		dataView.onRowsChanged.subscribe(function (e, args) {
		  grid.invalidateRows(args.rows);
		  grid.render();
		});

		grid.onValidationError.subscribe(function (e, args) {
		  alert(args.validationResults.msg);
		});
		
		grid.onCellChange.subscribe(function (e, args) {
			saveGridRow(args.item);
		});
		
		grid.onDblClick.subscribe(function (e, args) {
			var item = args.item;
			var cols = args.grid.getColumns();
			var cell = cols[args.cell];
			if (cell.field==='name') {
				contextMenuCmd = 'modify';		// double-clicking on a grid row opens lightbox for modifying attribute
				var item = grid.getDataItem(args.row);
				updateAttribute(item);
			}
		});
	}
	
// ================================================================================================

	function saveGridRow(item, fnSuccess, fnError) {
		if (!fnSuccess) fnSuccess = function(data1, textStatus, jqXHR) {
						//alert('UPDATE OK:  '+textStatus+'\n'+data1+'\n\n\n');
						loadingIndicator.fadeOut();
						loadMappings(serviceCategory, data1);
					};
		if (!fnError) fnError = function(jqXHR, textStatus, errorThrown) {
						//setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
						loadingIndicator.fadeOut();
						alert('STATUS='+textStatus+'\nERROR='+errorThrown);
					};
		showIndicator("Saving changes...");
		$.ajax({
			async: false,
			type: 'post',
			url: '/gui/admin/category-attribute-mappings/'+serviceCategory,
			data: JSON.stringify(item),
			contentType: 'application/json',
			dataType: 'json',
			success: fnSuccess,
			error: fnError,
			//complete: function() { alert('DONE LOADING'); }
		});
	}

// ================================================================================================
	var lightbox;
	var lightboxItem;
	
	// opens lightbox for new attribute selection (and insertion in the grid)
	function addAttribute() {
		// Show lightbox with attributes hierarchy
		$('#attrList-insert-bt').show();
		$.magnificPopup.open({
		  items: {
			  src: '#attrList-container',
			  type: 'inline'
		  }
		});
		lightbox = $.magnificPopup.instance;
		
		//clear any selection
		treeAttributes.jstree("deselect_all");
		
		//prepare tree for multiple selections
		treeAttributes.jstree("show_checkboxes");
		$.jstree.defaults.core.multiple = true;
	}
	
	// opens lightbox for modifying the attribute selection an existing grid row
	function updateAttribute(item) {
		// the grid data item
		lightboxItem = item;
		
		// Show lightbox with attributes hierarchy
		$('#attrList-insert-bt').hide();
		$.magnificPopup.open({
		  items: {
			  src: '#attrList-container',
			  type: 'inline'
		  }
		});
		lightbox = $.magnificPopup.instance;
		
		//select current node
		treeAttributes.jstree("deselect_all");
		treeAttributes.jstree('open_all' /*, '#'+item.aid*/);
		treeAttributes.jstree('select_node', '#'+item.aid);
		
		//prepare tree for single selection
		treeAttributes.jstree("hide_checkboxes");
	}
	
	// adds the specified attribute in the grid using default values
	function doAddAttribute(node) {
		doAddAttributeNoClose(node);
		
		// close lightbox
		$.magnificPopup.close();
	}
	
	function doAddAttributeNoClose(node) {
		// add a new grid row
		var lines = dataView.getLength();
		dataView.addItem({
			'id': '',
			'bppName': '',
			'aid': node.id, 
			'name': node.text,
			'type': 'NUMERIC_INC',
			'mandatory' : false,
			'from': 0,	//Number.NEGATIVE_INFINITY,
			'to': 0		//Number.POSITIVE_INFINITY
		});
		var item = dataView.getItem(lines);		// Newly added data item (as the last line of the grid)
		grid.invalidateAllRows();
		grid.render();
		
		// store new attribute using default values
		saveGridRow(item);
	}
	
	// adds selected attributes in the grid using default values
	function doAddMultipleAttributes() {
		var treeAttributes = $('#attrList').jstree(true);
		var selArr = treeAttributes.get_selected(true);
		
		// add new grid rows
		for (ii in selArr) {
			var node = selArr[ii];
			doAddAttributeNoClose(node);
		}
		
		// close lightbox
		$.magnificPopup.close();
	}
	
	// saves the modified attribute selection of the current grid row
	function doUpdateAttribute(node) {
		// update grid row
		lightboxItem.aid = node.id;
		lightboxItem.name = node.text;
		grid.invalidateAllRows();
		grid.render();
		
		// store modified attribute
		saveGridRow(lightboxItem);
		
		// close lightbox
		$.magnificPopup.close();
	}
	
	// deletes the specified grid row (and removes corresponding mapping from datastore)
	function doRowDelete(row) {
		if (confirm("Delete attribute "+data[row].name+" (at row "+data[row].rownum+") ?")) {
			showIndicator("Deleting row...");
			$.ajax({
				async: false,
				type: 'delete',
				url: '/gui/admin/category-attribute-mappings/'+data[row].id,
				//contentType: 'application/json',
				//dataType: 'json',
				success: function(data1, textStatus, jqXHR) {
							//alert('UPDATE OK:  '+textStatus+'\n'+data1+'\n\n\n');
							loadingIndicator.fadeOut();
							//loadMappings(serviceCategory, data1);
			
							// update grid  (instead of loadMappings)
							data.splice(row, 1);
							grid.setData(data);
							renumberRows(data);
							grid.render();
						},
				error: function(jqXHR, textStatus, errorThrown) {
							//setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
							loadingIndicator.fadeOut();
							alert('STATUS='+textStatus+'\nERROR='+errorThrown);
						},
				//complete: function() { alert('DONE LOADING'); }
			});
		}
	}
	
// =====================================================================================================================

	// re-loads grid rows (attribute mappings) for the current category
	function loadMappings(sc_id, data1) {
		if (!sc_id) return;
		sc_id = JSON.stringify(sc_id);
		sc_id = sc_id.substring(1,sc_id.length-1);
		//if (sc_id===serviceCategory) return;

		if (!data1) {
			showIndicator("Retrieving...");
			$.ajax({
				async: false,
				type: 'get',
				url: '/gui/admin/category-attribute-mappings/'+sc_id,
				//data: valStr,
				//contentType: 'application/json',
				contentType: "application/json;charset=UTF-8",
				dataType: 'json',
				success: function(data1, textStatus, jqXHR) {
							//renderData(data, textStatus);
							loadingIndicator.fadeOut();
							data = data1;
						},
				error: function(jqXHR, textStatus, errorThrown) {
							//setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
							loadingIndicator.fadeOut();
							alert('STATUS='+textStatus+'\nERROR='+errorThrown);
						},
				//complete: function() { alert('DONE LOADING'); }
			});
		} else {
			data = data1;
		}

		// Update grid header
		serviceCategory = sc_id;
		var tree = $("#scList").jstree(true);
		var node = tree.get_node(sc_id);
		serviceCategoryName = tree.get_text(node);
		//$('#grid-header-label').html('Service Category: ('+sc_id+') '+serviceCategoryName);
		$('#grid-header-label').html('Service Category: <a alt="'+sc_id+'">'+serviceCategoryName+'</a>');
		
		// initialize the model after all the events have been hooked up
		renumberRows(data);
		grid.setData(data);
		grid.invalidateAllRows();
		grid.render();
	} // End of loadMappings
	
// =====================================================================================================================

	var treeCategories;
	var treeAttributes;
	var contextMenuCmd;
	
	function initCategoriesTree() {
		treeCategories = $('#scList').jstree({
		  "core" : {
			"animation" : 500,
			"multiple": false,
			"check_callback" : true,
			"themes" : { "stripes" : true },
			'data' : {
				'url': function (node) {
							return baseUrl+'/gui/consumer/category-list';
						},
				'data': function (node) { return ''; }
			}
		  },
		  "contextmenu" : {
			"items": function($node) {
				var tree = $("#scList").jstree(true);
				return {
					"expand_all": {
						"separator_before": false,
						"separator_after": false,
						"label": "Expand all",
						"action": function (obj) { 
							expand_all( $('#scList').jstree(true) );
						}
					},
					"collapse_all": {
						"separator_before": false,
						"separator_after": false,
						"label": "Collapse all",
						"action": function (obj) { 
							collapse_all( $('#scList').jstree(true) );
						}
					},
					"add_attr": {
						"separator_before": true,
						"separator_after": false,
						"label": "Add attribute",
						"action": function (obj) { 
							addAttribute();
						}
					},
				};
			}
		  },
		  "types" : {
			"#" : {
			  "max_children" : 1, 
			  //"max_depth" : 4, 
			  "valid_children" : ["root"]
			},
			"root" : {
			  "icon" : "/static/3.0.0/assets/images/tree_icon.png",
			  "valid_children" : ["default"]
			},
			"default" : {
			  "valid_children" : ["default","file"]
			},
			"file" : {
			  "icon" : "glyphicon glyphicon-file",
			  "valid_children" : []
			}
		  },
		  "plugins" : [
			"contextmenu", //"dnd", "search",
			"state", "types", "wholerow", "hotkeys"
		  ]
		})
		.on('changed.jstree', function (e, data) {
			$('input[name=id]').prop('readonly', true);
			if(data && data.selected && data.selected.length) {
				startTm = new Date().getTime();
				callStartTm = startTm;
				loadMappings( data.selected[0] );
			}
			else {
				$('#data .content').hide();
				$('#data .default').html('xxx').show();
			}
		});
	} // End of initCategoriesTree
	
	function initAttributesTree() {
		treeAttributes = $('#attrList').jstree({
		  "core" : {
			"animation" : 500,
			"multiple": false,
			"check_callback" : true,
			"themes" : { "stripes" : true },
			'data' : {
				'url': function (node) {
								return node.id === '#' ? 
										baseUrl+'/gui/admin/get-all-attributes' : 
										/* baseUrl+'/gui/admin/get-broker-policy-attributes' : */
										baseUrl+'/gui/admin/get-attribute/'+node.id;
						},
				'data': function (node) { return ''; }
			}
		  },
		  "contextmenu" : {
			"items": function($node) {
				var tree = $("#attrList").jstree(true);
				return {
					"expand_all": {
						"separator_before": false,
						"separator_after": false,
						"label": "Expand all",
						"action": function (obj) { 
							expand_all();
						}
					},
					"collapse_all": {
						"separator_before": false,
						"separator_after": false,
						"label": "Collapse all",
						"action": function (obj) { 
							collapse_all();
						}
					},
					"add_attribute": {
						"separator_before": true,
						"separator_after": false,
						"label": "Add Attribute",
						"action": function (obj) { 
							var tree = $("#attrList").jstree();
							var sel = tree.get_selected(true);
							if (sel.length>0) {
								var node = sel[0];
								doAddAttribute(node);
							}
						}
					},
					/*"add_attribute_and_children": {
						"separator_before": false,
						"separator_after": false,
						"label": "Add Attribute &amp; subattributes",
						"action": function (obj) { 
							alert("Not yet implemented");
						}
					},*/
					"set_attribute": {
						"separator_before": true,
						"separator_after": false,
						"label": "Change Attribute",
						"action": function (obj) { 
							var tree = $("#attrList").jstree();
							var sel = tree.get_selected(true);
							if (sel.length>0) {
								var node = sel[0];
								doUpdateAttribute(node);
							}
						}
					},
				};
			}
		  },
		  "types" : {
			"#" : {
			  "max_children" : 1, 
			  //"max_depth" : 4, 
			  "valid_children" : ["root"]
			},
			"root" : {
			  "icon" : "/static/3.0.0/assets/images/tree_icon.png",
			  "valid_children" : ["default"]
			},
			"default" : {
			  "valid_children" : ["default","file"]
			},
			"file" : {
			  "icon" : "glyphicon glyphicon-file",
			  "valid_children" : []
			}
		  },
		  "checkbox" : {
			"three_state" : false,
			"keep_selected_style" : true,
			"cascade" : ""
		  },
		  "plugins" : [
			"contextmenu", //"dnd", "search",
			"state", "types", "wholerow", "hotkeys", "checkbox"
		  ]
		});
		
		// dbl-click event handler for 'attributes tree' appearing in lightbox
		treeAttributes.bind('dblclick.jstree',function (e) {
			var tree = $("#attrList").jstree();
			/*var sel = tree.get_selected(true);
			if (sel.length==1) {
				var node = sel[0];*/
				if(contextMenuCmd) {
					if (contextMenuCmd==='add') {
						//doAddAttribute(node);
					} else 
					if (contextMenuCmd==='modify') {
						//doUpdateAttribute(node);
					}
				}
			//}
		});
		
	} // End of initAttributesTree
	
	function initGridHandlers() {
		/* Slickgrid context menu code */
		grid.onContextMenu.subscribe(function (e) {
			e.preventDefault();
			var cell = grid.getCellFromEvent(e);
			$("#contextMenu")
					.data("row", cell.row)
					.css("top", e.pageY)
					.css("left", e.pageX)
					.show();

			$("body").one("click", function () {
				$("#contextMenu").hide();
			});
		});

		// Handle double-click on mandatory field
		// i.e. toggle mandatory value
		grid.onDblClick.subscribe(function (e) {
			var cell = grid.getCellFromEvent(e);
			if (grid.getColumns()[cell.cell].id === "mandatory") {
				if (!grid.getEditorLock().commitCurrentEdit()) {
					return;
				}
				
				// save new mandatory value
				var item = data[cell.row];
				item.mandatory = ! item.mandatory;
				saveGridRow(item
					, function(data1, textStatus, jqXHR) {
						// update grid
						loadingIndicator.fadeOut();
						grid.updateRow(cell.row);
					}
					, function(jqXHR, textStatus, errorThrown) {
						//setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
						item.mandatory = ! item.mandatory;	// rollback change!!!
						loadingIndicator.fadeOut();
						alert('STATUS='+textStatus+'\nERROR='+errorThrown);
					}
				);
				
				e.stopPropagation();
			}
		});

		// set grid context menu event handler
		$("#contextMenu").click(function (e) {
			if (!$(e.target).is("li")) {
			  return;
			}
			if (!grid.getEditorLock().commitCurrentEdit()) {
			  return;
			}
			var menuCmd = e.target.attributes.data.value;
			var row = $(this).data("row");
			
			if (menuCmd==='add') {
				contextMenuCmd = menuCmd;
				addAttribute();
			}
			if (menuCmd==='modify') {
				contextMenuCmd = menuCmd;
				updateAttribute(data[row]);
			}
			if (menuCmd==='delete') doRowDelete(row);
		});
	} // End of initGridHandlers
	
// =====================================================================================================================

	$(function () {
		// Initialize mappings grid
		initGrid();
		initGridHandlers();
		
		// Initialize service category tree
		initCategoriesTree();
		
		// Initialize attributes tree
		initAttributesTree();
		
	});	// End of $(function(){...})
</script>

	<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>