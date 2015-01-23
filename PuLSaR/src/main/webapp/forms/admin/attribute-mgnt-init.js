/* 
 *	jsTree and hotkeys initialization
 */
		/*
		 * Initialize jstree and load initial data
		 */
		// Attributes jsTree
		var treeAttributes;
		
		$( document ).ready(function() {
			// Initialize json forms
			initAttributeEditForm();
			initAttributeButtonsForm();
			
			// Initialize attribute tree
			//$.jstree.defaults.core.data = true;
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
						"create_sibling": {
							"separator_before": true,
							"separator_after": false,
							"label": "Create Sibling",
							"action": function (obj) { 
								//$node = tree.create_node($node);
								//tree.edit($node);
								createSibling();
							}
						},
						"create_child": {
							"separator_before": false,
							"separator_after": true,
							"label": "Create Child",
							"action": function (obj) { 
								//$node = tree.create_node($node);
								//tree.edit($node);
								createChild();
							}
						},
/*						"rename": {
							"separator_before": false,
							"separator_after": false,
							"label": "Rename",
							"action": function (obj) { 
								tree.edit($node);
							}
						},*/
						"remove": {
							"separator_before": false,
							"separator_after": false,
							"label": "Delete",
							"action": function (obj) { 
								//tree.delete_node($node);
								deleteNode();
							}
						},
/*						"remove_cascade": {
							"separator_before": true,
							"separator_after": false,
							"label": "Delete Cascade",
							"action": function (obj) { 
								//tree.delete_node($node);
								deleteNodeCascade();
							}
						}*/
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
					var url = baseUrl+'/gui/admin/get-attribute/'+data.selected;
//console.log('Calling service at: '+url);
					statusContacting(url, 'n/a');
					clearForm();
					callStartTm = new Date().getTime();
					$.get(url, function (d) {
						if(d) {
//console.log('Data received: '+JSON.stringify(d));
							renderData(d, 'obviously OK');
						}
					});
				}
				else {
					$('#data .content').hide();
					$('#data .default').html('xxx').show();
				}
			})
/*			.on('move_node.jstree', function (e, data) {
				var id = data.node.id;
				alert('move_node '+id);
//var m='';
//var o=data.node.id;
//for (i in o) m+=i+": "+o[i]+" \n ";
//alert(m);
				var pNew = data.parent;
				var pOld = data.old_parent;
				if (pNew!==pOld) {
					alert('moving node from : '+pOld+' to '+pNew);
					$('#parent').value
				}
			})*/
			;
			
			initTreeHotkeys();
		});
		
		/*
		 *	Hotkeys initialization
		 */
		function initTreeHotkeys() {
			var tree = $("#attrList");	//jQuery(document);
/*			tree.bind('keydown', 'esc',function (evt){clearForm(); return false; });
			tree.bind('keydown', 'tab',function (evt){ return false; });
			tree.bind('keydown', 'space',function (evt){ return false; });
			tree.bind('keydown', 'return',function (evt){ return false; });
			tree.bind('keydown', 'backspace',function (evt){ return false; });*/
			tree.bind('keydown', 'insert',function (evt){ createSibling(); return false; });
			tree.bind('keydown', 'Shift+insert',function (evt){ createChild(); return false; });
			tree.bind('keydown', 'del',function (evt){ deleteNode(); return false; });
/*			tree.bind('keydown', 'home',function (evt){ return false; });
			tree.bind('keydown', 'end',function (evt){ return false; });*/
		}
