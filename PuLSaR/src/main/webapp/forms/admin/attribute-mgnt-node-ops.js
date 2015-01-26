/*
 *  Attribute operations
 */
		function reloadTree() {
			$('#attrList').jstree("refresh");
		}
		function saveNode() {
			submitForm();
		}
		function doSaveNode() {
			var urlStr;
			if ($('input[name=id]').prop('readonly')===true) {  // save
				urlStr = baseUrl+"/gui/admin/save-attribute";
			} else {  // create
				$('input[name=id]').prop('readonly', true);
				urlStr = baseUrl+"/gui/admin/create-attribute";
			}
			var methodStr = "POST";
			var values = getFormValues();
			prepareValuesForSubmit(values);
			retrieveData(methodStr, urlStr, values);
			reloadTree();
		}
		function deleteNode(cascade) {
			if ($('input[name=id]').prop('readonly')===true) {	// if 'id' field is readonly then it's delete of an existing attribute, not a new (unsaved) one
				var tree = $("#attrList").jstree();
				var sel = selectedTreeNode(tree);
				if (sel && sel!=null) {
					if (tree.is_parent(sel)) {
						alert('Attribute cannot be deleted!\n\nThis attribute has child attributes. You must first delete them.');
						return;
					}
				} else {
					alert('Select an attribute to delete');
					return;
				}
				
				var id = getField('id');
				if (confirm('Delete attribute '+id+' ?')) {
					// check if cascade delete
					if (cascade && cascade===true) cascade = '/cascade'; else cascade = '';
					// prepare WS url
					var urlStr = baseUrl+"/gui/admin/delete-attribute/"+id+cascade;

					clearForm();
					setStatus('<p>Waiting for DELETE... <img src="../ajax-loader.gif" /></p>');
					$.ajax({
						async: false,
						type: 'GET',
						url: urlStr,
						success: function(data, textStatus, jqXHR) {
									setStatus('<p>Attribute '+id+' DELETED</p><p>'+textStatus+'</p>');
									reloadTree();
									deselectTreeNode();
								},
						error: function(jqXHR, textStatus, errorThrown) {
									alert('STATUS='+textStatus+'\nERROR='+errorThrown);
									setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
								},
					});
				}
			} else {	// if 'id' field is NOT readonly then it's delete of a new and unsaved attribute. So we can simply clear the form (no actual delete occurs in attributes store)
				$('input[name=id]').prop('readonly', false);
				clearForm();
				deselectTreeNode();
			}
		}
/*		function deleteNodeCascade() {
			deleteNode(true);
		}*/
		function createSibling() {
			var newPar = getField('parent');
			var tmp = attrParent;
			clearForm();
			setField('parent', newPar);
			setField('id', newPar+'-');
			$('input[name=id]').prop('readonly', false);
			attrParent = tmp;
		}
		function createChild() {
			var newPar = getField('id');
			var tmp = attrNode;
			clearForm();
			setField('parent', newPar);
			setField('id', newPar+'-');
			$('input[name=id]').prop('readonly', false);
			attrParent = tmp;
		}
		function selectedTreeNode(tree) {
			if (!tree) tree = $("#attrList").jstree();
			var sel = tree.get_selected(true);
			if (sel.length>0) {
				return sel[0];
			} else {
				return null;
			}
		}
		function deselectTreeNode(tree) {
			if (!tree) tree = $("#attrList").jstree();
			var sel1 = tree.get_selected(true);
			if (sel1.length>0) {
				tree.deselect_node(sel1[0]);
			}
		}
		function expand_all(tree) {
			if (!tree) tree = $("#attrList").jstree();
			var node = selectedTreeNode(tree);
			if (node && node!=null) tree.open_all(node);
		}
		function collapse_all(tree) {
			if (!tree) tree = $("#attrList").jstree();
			var node = selectedTreeNode(tree);
			if (node && node!=null) tree.close_all(node);
		}
