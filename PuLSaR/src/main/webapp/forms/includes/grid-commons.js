/*
 *	Utility functions and variables common to all grid instances
 */

// ================================================================================================
// Accepted Optimisation Attribute and criteria types

	var typeLabels = {
		'NUMERIC_INC': 'Numeric - Higher is Better',
		'NUMERIC_DEC': 'Numeric - Lower is Better',
		'NUMERIC_RANGE': 'Range',
		'BOOLEAN': 'Boolean',
		'UNORDERED_SET': 'Unordered Set',
		'FUZZY_INC': 'Fuzzy number - Higher is Better',
		'FUZZY_DEC': 'Fuzzy number - Lower is Better',
		'FUZZY_RANGE': 'Fuzzy range',
		'LINGUISTIC': 'Linguistic'
	};
	var typeSelected = "NUMERIC_INC";

// ================================================================================================
// Attribute/Criteria Types formatter and editor

	function TypeFormatter(row, cell, value, columnDef, dataContext) {
		return '<b>'+typeLabels[ dataContext.type ]+'</b>';
	}

	function TypeEditor(args) {
		var $type;
		var scope = this;

		this.init = function () {
			$html = '<SELECT required>\n';
			for (key in typeLabels) {
				$html += '<option value="'+key+'"'+(key===typeSelected ? ' selected' : '')+'>'+typeLabels[key]+'</option>\n';
			}
			$html += '</SELECT>';
			$type = $($html)
				.appendTo(args.container)
				.bind("keydown", scope.handleKeyDown);

			scope.focus();
		};

		this.handleKeyDown = function (e) {
			if (e.keyCode == $.ui.keyCode.LEFT || e.keyCode == $.ui.keyCode.RIGHT || e.keyCode == $.ui.keyCode.TAB) {
				e.stopImmediatePropagation();
			}
		};

		this.destroy = function () {
			$(args.container).empty();
		};

		this.focus = function () {
			$type.focus();
		};

		this.serializeValue = function () {
			return {type: $type.val()};
		};

		this.applyValue = function (item, state) {
			item.type = state.type;

			// reset allowed values for new type
			if (item.type==='BOOLEAN' || item.type==='UNORDERED_SET' || item.type==='LINGUISTIC') {
				item.from = '';
			} else {
				item.fromL = 0;	//Number.NEGATIVE_INFINITY;
				item.from = 0;	//Number.NEGATIVE_INFINITY;
				item.fromU = 0;	//Number.NEGATIVE_INFINITY;
				item.toL = 0;	//Number.POSITIVE_INFINITY;
				item.to = 0;	//Number.POSITIVE_INFINITY;
				item.toU = 0;	//Number.POSITIVE_INFINITY;
			}

			//alert('UPDATE ROW:\n'+JSON.stringify(item));
			showIndicator("Saving changes...");
			$.ajax({
				async: false,
				type: 'post',
				url: '/gui/admin/category-attribute-mappings/'+serviceCategory,
				data: JSON.stringify(item),
				contentType: 'application/json',
				//dataType: 'json',
				success: function(data1, textStatus, jqXHR) {
							//alert('UPDATE OK:  '+textStatus+'\n'+data1+'\n\n\n');
							loadingIndicator.fadeOut();
							loadMappings(serviceCategory, data1);
						},
				error: function(jqXHR, textStatus, errorThrown) {
							//setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
							loadingIndicator.fadeOut();
							alert('STATUS='+textStatus+'\nERROR='+errorThrown);
						},
				//complete: function() { alert('DONE LOADING'); }
			});
		};

		this.loadValue = function (item) {
			$type.val(item.type);
		};

		this.isValueChanged = function () {
			return args.item.type != $type.val();
		};

		this.validate = function () {
			var typ = $type.val();
			for (key in typeLabels) {
			if (key===typ) 
				return {valid: true, msg: null};
			}
			return {valid: false, msg: "Please select a valid type."};
		};

		this.init();
	}

// ================================================================================================
// Attribute/Criteria Type Ranges formatter and editor

	var precision = 3;
	var precisionMult = Math.pow(10,precision);

	function pf(f) {
		if (f<=Number.NEGATIVE_INFINITY) return '-&infin;';
		if (f>=Number.POSITIVE_INFINITY) return '&infin;';
		var nm = new Number(f);
		nm *= precisionMult;
		nm = Math.round(nm);
		nm /= precisionMult;
		return nm.toString();
		//return nm.toFixed(precision);
	}
	function ptfn(l,m,u) {
		return '('+pf(l)+','+pf(m)+','+pf(u)+')';
	}
	
	function isNumeric(n) {
		return !isNaN(parseFloat(n)) && isFinite(n);
	}
	
	function TypeRangeFormatter(row, cell, value, columnDef, dataContext) {
		var typ = dataContext.type;
		if (numericTypes.indexOf(typ)>-1) {
			var f = '-&infin;';
			var t = '+&infin;';
			if ((typeof dataContext.from!=='undefined') && dataContext.from!=='' && isNumeric(dataContext.from) && dataContext.from>Number.NEGATIVE_INFINITY) f = pf(dataContext.from);
			if ((typeof dataContext.to!=='undefined') && dataContext.to!=='' && isNumeric(dataContext.to) && dataContext.to<Number.POSITIVE_INFINITY) t = pf(dataContext.to);
			return  f + " - " + t;
		} else
		if (fuzzyTypes.indexOf(typ)>-1) {
			var f = '-&infin;';
			var t = '+&infin;';
			if (dataContext.fromL && dataContext.from && dataContext.fromU && isNumeric(dataContext.fromL) && dataContext.fromL>Number.NEGATIVE_INFINITY) f = ptfn(dataContext.fromL, dataContext.from, dataContext.fromU);
			if (dataContext.toL && dataContext.to && dataContext.toU&& isNumeric(dataContext.toU) && dataContext.toU<Number.POSITIVE_INFINITY) t = ptfn(dataContext.toL, dataContext.to, dataContext.toU);
			return f+ " - " + t;
		} else
		if (textTypes.indexOf(typ)>-1) {
			var f = '';
			if (dataContext.from) f = dataContext.from;
			return '[' + f + ']';
		} else {
			return 'ERROR: Unknown type '+typ+'.  VALUE: '+dataContext.from;
		}
	}

	var numericTypes = ['NUMERIC_INC', 'NUMERIC_DEC', 'NUMERIC_RANGE'];
	var fuzzyTypes = ['FUZZY_INC', 'FUZZY_DEC', 'FUZZY_RANGE'];
	var textTypes = ['BOOLEAN', 'UNORDERED_SET', 'LINGUISTIC'];

	function TypeRangeEditor(args) {
		var $typ = args.item.type;
		var $from, $to;
		var $fromL, $toL, $fromU, $toU;
		var scope = this;

		var $isNumericType = numericTypes.indexOf($typ)>-1;
		var $isFuzzyType = fuzzyTypes.indexOf($typ)>-1;
		var $isTextType = textTypes.indexOf($typ)>-1;

		// BEGIN of initialization (function 'init') definition 'if-else' structure
		if ($isNumericType) {
			this.init = function () {
				$(args.container).append("Min. value: ");
				var style = "width:40px;";
				$from = $("<INPUT type=text style='"+style+"' class='numType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);

				$(args.container).append("&nbsp; Max. value: &nbsp;");

				$to = $("<INPUT type=text style='"+style+"' class='numType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);

				scope.focus();
			};
		} else
		if ($isFuzzyType) {
			this.init = function () {
				$(args.container).append("Min. value: (");
				var style = "width:30px;";
				$fromL = $("<INPUT type=text style='"+style+"' class='fuzzyType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);
				$(args.container).append(",");
				$from = $("<INPUT type=text style='"+style+"' class='fuzzyType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);
				$(args.container).append(",");
				$fromU = $("<INPUT type=text style='"+style+"' class='fuzzyType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);

				$(args.container).append(")&nbsp; Max. value: &nbsp;(");

				$toL = $("<INPUT type=text style='"+style+"' class='fuzzyType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);
				$(args.container).append(",");
				$to = $("<INPUT type=text style='"+style+"' class='fuzzyType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);
				$(args.container).append(",");
				$toU = $("<INPUT type=text style='"+style+"' class='fuzzyType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);

				$(args.container).append(")");

				scope.focus();
			};
		} else
		if ($isTextType) {
			this.init = function () {
				$(args.container).append("Labels: ");
				$from = $("<INPUT type=text style='width:300px;' class='textType' />")
						.appendTo(args.container)
						.bind("keydown", scope.handleKeyDown);

				scope.focus();
			};
		} else {
			this.init = function () {
				$(args.container).append("ERROR: Unknown type "+$typ);
				scope.focus();
			};
		}
		// END of initialization (function 'init') definition 'if-else' structure

		this.handleKeyDown = function (e) {
			if (e.keyCode == $.ui.keyCode.LEFT || e.keyCode == $.ui.keyCode.RIGHT || e.keyCode == $.ui.keyCode.TAB) {
				e.stopImmediatePropagation();
			}
		};

		this.destroy = function () {
			$(args.container).empty();
		};

		this.focus = function () {
			if ($isFuzzyType) $fromL.focus();
			else $from.focus();
		};

		this.serializeValue = function () {
			if ($isNumericType) {
				if ($from.val().trim()==='' || isNaN($from.val().trim())) fVal = Number.NEGATIVE_INFINITY; else fVal = parseFloat($from.val(), 10);
				if ($to.val().trim()==='' || isNaN($to.val().trim())) tVal = Number.POSITIVE_INFINITY; else tVal = parseFloat($to.val(), 10);
				return {from: fVal, to: tVal};
			} else
			if ($isFuzzyType) {
				if ($fromL.val().trim()==='' || isNaN($fromL.val().trim())) fLVal = Number.NEGATIVE_INFINITY; else fLVal = parseFloat($fromL.val(), 10);
				if ($from.val().trim()===''  || isNaN($from.val().trim()))  fMVal = Number.NEGATIVE_INFINITY; else fMVal = parseFloat($from.val(), 10);
				if ($fromU.val().trim()==='' || isNaN($fromU.val().trim())) fUVal = Number.NEGATIVE_INFINITY; else fUVal = parseFloat($fromU.val(), 10);
				if ($toL.val().trim()==='' || isNaN($toL.val().trim())) tLVal = Number.POSITIVE_INFINITY; else tLVal = parseFloat($toL.val(), 10);
				if ($to.val().trim()===''  || isNaN($to.val().trim()))  tMVal = Number.POSITIVE_INFINITY; else tMVal = parseFloat($to.val(), 10);
				if ($toU.val().trim()==='' || isNaN($toU.val().trim())) tUVal = Number.POSITIVE_INFINITY; else tUVal = parseFloat($toU.val(), 10);
				return {fromL: fLVal, toL: tLVal,
						from:  fMVal, to:  tMVal,
						fromU: fUVal, toU: tUVal};
			} else
			if ($isTextType) {
				return {from: $from.val()};
			}
		};	// End of 'serializeValue'

		this._f = function (x, def) {
			var f = new String(x).trim();
			if (f==='' || isNaN(f)) f = def; else f = parseFloat(f, 10);
			return new Number(f);
		}
		
		this.applyValue = function (item, state) {
			if ($isNumericType) {
				item.from = this._f(state.from, Number.NEGATIVE_INFINITY);
				item.to = this._f(state.to, Number.POSITIVE_INFINITY);
			} else
			if ($isFuzzyType) {
				item.fromL = this._f(state.fromL, Number.NEGATIVE_INFINITY);
				item.toL = this._f(state.toL, Number.POSITIVE_INFINITY);
				item.from = this._f(state.from, Number.NEGATIVE_INFINITY);
				item.to = this._f(state.to, Number.POSITIVE_INFINITY);
				item.fromU = this._f(state.fromU, Number.NEGATIVE_INFINITY);
				item.toU = this._f(state.toU, Number.POSITIVE_INFINITY);
			} else
			if ($isTextType) {
				item.from = state.from.trim();
			}

			// copy item values to a buffer
			item['from_inf'] = false;
			item['to_inf'] = false;
			if (item['from']<=Number.NEGATIVE_INFINITY) item['from_inf'] = true;
			if (item['to']>=Number.POSITIVE_INFINITY) item['to_inf'] = true;
			
			// Push new values to server for store
			var strSubmit = JSON.stringify(item);
			//alert('UPDATE ROW:\n'+strSubmit);
			showIndicator("Saving changes...");
			$.ajax({
				async: false,
				type: 'post',
				url: '/gui/admin/category-attribute-mappings/'+serviceCategory,
				data: strSubmit,
				contentType: 'application/json',
				//dataType: 'json',
				success: function(data1, textStatus, jqXHR) {
							//alert('UPDATE OK:  '+textStatus+'\n'+data1+'\n\n\n');
							loadingIndicator.fadeOut();
							loadMappings(serviceCategory, data1);
						},
				error: function(jqXHR, textStatus, errorThrown) {
							//setStatus('<p>Status: '+textStatus+'</p>'+'<p>ERROR: '+errorThrown+'</p>');
							loadingIndicator.fadeOut();
							alert('STATUS='+textStatus+'\nERROR='+errorThrown);
						},
				//complete: function() { alert('DONE LOADING'); }
			});
		};	// End of 'applyValue'

		this.loadValue = function (item) {
			if ($isNumericType) {
				$from.val( pf(item.from) );
				$to.val( pf(item.to) );
			} else
			if ($isFuzzyType) {
				$fromL.val( pf(item.fromL) );
				$toL.val( pf(item.toL) );
				$from.val( pf(item.from) );
				$to.val( pf(item.to) );
				$fromU.val( pf(item.fromU) );
				$toU.val( pf(item.toU) );
			} else
			if ($isTextType) {
				$from.val(item.from);
			}
		};	// End of 'loadValue'

		this.isValueChanged = function () {
			if ($isNumericType) {
				var f = this._f($from.val(), Number.NEGATIVE_INFINITY);
				var t = this._f($to.val(), Number.POSITIVE_INFINITY);
				return args.item.from!==f || args.item.to!==t;
			} else
			if ($isFuzzyType) {
				var fl = this._f($fromL.val(), Number.NEGATIVE_INFINITY);
				var fm = this._f($from.val(), Number.NEGATIVE_INFINITY);
				var fu = this._f($fromU.val(), Number.NEGATIVE_INFINITY);
				var tl = this._f($toL.val(), Number.POSITIVE_INFINITY);
				var tm = this._f($to.val(), Number.POSITIVE_INFINITY);
				var tu = this._f($toU.val(), Number.POSITIVE_INFINITY);
				return args.item.fromL!==fl || args.item.toL!==tl ||
					   args.item.from !==fm || args.item.to !==tm ||
					   args.item.fromU!==fu || args.item.toU!==tu;
			} else
			if ($isTextType) {
				return args.item.from.trim() !== $from.val().trim();
			}
		};	// End of 'isValueChanged'

		this.validate = function () {
			if ($isNumericType) {
				if ($from.val().trim()!=='' && isNaN($from.val().trim()) || $to.val().trim()!=='' && isNaN($to.val().trim())) {
					return {valid: false, msg: "Please type in valid numbers."};
				}

				var f = this._f($from.val(), Number.NEGATIVE_INFINITY);
				var t = this._f($to.val(), Number.POSITIVE_INFINITY);
				if (f > t) {
					return {valid: false, msg: "'Min. value' cannot be greater than 'Max. value'"};
				}
				return {valid: true, msg: null};
			} else
			if ($isFuzzyType) {
				if ($fromL.val().trim()!=='' && isNaN($fromL.val().trim()) || $from.val()!=='' && isNaN($from.val().trim()) || $fromU.val()!=='' && isNaN($fromU.val().trim())) {
					return {valid: false, msg: "Please type in valid fuzzy number."};
				}
				if ($toL.val()!=='' && isNaN($toL.val().trim()) || $to.val()!=='' && isNaN($to.val().trim()) || $toU.val()!=='' && isNaN($toU.val().trim())) {
					return {valid: false, msg: "Please type in valid fuzzy number."};
				}

				var fl = this._f($fromL.val(), Number.NEGATIVE_INFINITY);
				var fm = this._f($from.val(), Number.NEGATIVE_INFINITY);
				var fu = this._f($fromU.val(), Number.NEGATIVE_INFINITY);
				var tl = this._f($toL.val(), Number.POSITIVE_INFINITY);
				var tm = this._f($to.val(), Number.POSITIVE_INFINITY);
				var tu = this._f($toU.val(), Number.POSITIVE_INFINITY);
				
				if (fl>fm || fl>fu || fm>fu) {
					return {valid: false, msg: "Invalid fuzzy number specified for 'Min. value' "};
				}

				if (tl>tm || tl>tu || tm>tu) {
					return {valid: false, msg: "Invalid fuzzy number specified for 'Max. value' "};
				}

				if (fl>tl || fm>tm || fu>tu) {
					return {valid: false, msg: "'Min. value' cannot be greater than 'Max. value'"};
				}
				return {valid: true, msg: null};
			} else
			if ($isTextType && $typ!=='boolean') {
				if ($from.val().trim().length==0) {
					return {valid: false, msg: "Please type in at least one unordered set item or linguistic term."};
				}
				var labels = $from.val().split(",");
				if (labels.length<1) {
					return {valid: false, msg: "Too few labels or linguistic terms. At least one is required. Labels/Terms are separated with commas."};
				}
				return {valid: true, msg: null};
			} else
			if ($typ==='boolean') {
				return {valid: true, msg: null};
			} else {
				return {valid: false, msg: "ERROR: Unknown type "+$typ};
			}
		};	// End of 'validate'

		this.init();
	}

// ================================================================================================
// Percent formatter and editor

	function PercentFormatter(row, cell, value, columnDef, dataContext) {
		if (columnDef && columnDef.displayFormat && columnDef.displayFormat!=='%') {
			return '<b><font color="red">'+parseFloat(dataContext.weight)+'</font></b>';
		} else {
			precision = (columnDef && columnDef.displayPrecision && !isNaN(columnDef.displayPrecision) && parseInt(columnDef.displayPrecision)>=0) ? parseInt(columnDef.displayPrecision) : 2;
			precision = Math.pow(10, precision);
			return '<b><font color="red">'+Math.round(parseFloat(dataContext.weight*100*precision))/precision+'%</font></b>';
		}
	}

	function PercentEditor(args) {
		var $percent;
		var $label;
		var $field;
		var $format;
		var $precision
		var scope = this;

		this.init = function () {
			$label = (args.column && args.column.name && args.column.name.trim()!=='') ? args.column.name.trim() : 'Percentage';
			$field = (args.column && args.column.field && args.column.field!=='') ? args.column.field : 'percent';
			$format = (args.column && args.column.displayFormat && args.column.displayFormat!=='%') ? args.column.displayFormat : '%';
			$precision = (args.column && args.column.displayPrecision && !isNaN(args.column.displayPrecision) && parseInt(args.column.displayPrecision)>=0) ? parseInt(args.column.displayPrecision) : 2;

			//$(args.container).append($label+': ');
			var style = "width:40px;";
			$html = $("<INPUT type=text style='"+style+"' class='numType' required />")
			$percent = $($html)
				.appendTo(args.container)
				.bind("keydown", scope.handleKeyDown);
			if ($format==='%') $(args.container).append('%');
			scope.focus();
		};

		this.handleKeyDown = function (e) {
			if (e.keyCode == $.ui.keyCode.LEFT || e.keyCode == $.ui.keyCode.RIGHT || e.keyCode == $.ui.keyCode.TAB) {
				e.stopImmediatePropagation();
			}
		};

		this.destroy = function () {
			$(args.container).empty();
		};

		this.focus = function () {
			$percent.focus();
		};

		this.serializeValue = function () {
			var ret = {};
			ret[$field] = $percent.val();
			return ret;
		};

		this.applyValue = function (item, state) {
			if ($format==='%') {
				item[$field] = parseFloat(state[$field].trim())/100;
			} else {
				item[$field] = parseFloat(state[$field].trim());
			}
		};

		this.loadValue = function (item) {
			if ($format==='%')
				$percent.val(item[$field]*100);
			else
				$percent.val(item[$field]);
		};

		this.isValueChanged = function () {
			if ($format==='%')
				return args.item[$field] != $percent.val()/100;
			else
				return args.item[$field] != $percent.val();
		};

		this.validate = function () {
			var percent = $percent.val().trim();
			if (!percent || percent==='') {
				return {valid: false, msg: "Please enter a valid "+$label.toLowerCase()+"."};
			}
			if ($format==='%') {
				if (isNaN(percent) || parseFloat(percent)<0 || parseFloat(percent)>100) {
					return {valid: false, msg: "Please enter a valid "+$label.toLowerCase()+"."};
				}
			} else {
				if (isNaN(percent) || parseFloat(percent)<0 || parseFloat(percent)>1) {
					return {valid: false, msg: "Please enter a valid "+$label.toLowerCase()+"."};
				}
			}
			return {valid: true, msg: null};
		};

		this.init();
	}

// ================================================================================================
// Constraints formatter and editor

	var orderedOps = [['=','='], ['<>','&ne;'], ['<','&lt;'], ['<=','&le;'], ['>','&gt;'], ['>=','&ge;']];
	var rangeOps = [['@','&isin;'], ['!','&notin;']];
	var constrOps = {
						'NUMERIC_INC': orderedOps,
						'NUMERIC_DEC': orderedOps,
						'NUMERIC_RANGE': rangeOps,
						'BOOLEAN': [['=','=']],
						'UNORDERED_SET': rangeOps,
						'FUZZY_INC': orderedOps,
						'FUZZY_DEC': orderedOps,
						'FUZZY_RANGE': rangeOps,
						'LINGUISTIC': orderedOps
					};
	var constrHelp = {
						'NUMERIC_INC': "Accepts a numeric value",
						'NUMERIC_DEC': "Accepts a numeric value",
						'NUMERIC_RANGE': "Accepts two numeric values <i>a, b</i> <br/>where <i>a &le; b</i>. Meaning: <i>[a, b]</i>",
						'BOOLEAN': "Use the <i>true</i> or <i>false</i> value <br/>valid for this attribute",
						'UNORDERED_SET': "Use any number of the legal <br/>list values for this attribute",
						'FUZZY_INC': "Accepts a fuzzy value. Format:<br/><b>(a ; b ; c)</b> where a &le; b &le; c",
						'FUZZY_DEC': "Accepts a fuzzy value. Format:<br/><b>(a ; b ; c)</b> where a &le; b &le; c",
						'FUZZY_RANGE': "Accepts a fuzzy range. Format:<br/><b>(a ; b ; c; d)</b> where a &le; b &le; c &le; d",
						'LINGUISTIC': "Accepts any legal linguistic value for <br/>this attribute"
					};
	
	function ConstraintsFormatter(row, cell, value, columnDef, dataContext) {
		var formatted = dataContext.constraints.trim()
							.replace('<>','&ne;')
							.replace('<=','&le;')
							.replace('>=','&ge;')
							.replace('<','&lt;')
							.replace('>','&gt;')
							.replace('@','&isin;')
							.replace('!','&notin;');
		if (formatted==='') formatted = '-'; 
		else formatted = '<font size="-1" color="blue">'+formatted+'</font>';
		return '<b>'+formatted+'</b>';
	}

	function ConstraintsEditor(args) {
		var $constrOp;
		var $constrFrom;
		var $constrTo;
		var $cbArray;
		var $clear;
		var $label;
		var $field;
		var $type;
		var scope = this;
		var canvas;

		this.init = function () {
			// get label and field name
			$label = (args.column && args.column.name && args.column.name.trim()!=='') ? args.column.name.trim() : 'Constraints';
			$field = (args.column && args.column.field && args.column.field!=='') ? args.column.field : 'constraints';
			
			// get type
			$type = args.item.type;
			var $isNumericType = numericTypes.indexOf($type)>-1;
			var $isFuzzyType = fuzzyTypes.indexOf($type)>-1;
			var $isTextType = textTypes.indexOf($type)>-1;
			var $isRange = ($type.indexOf('_RANGE')>0);
			
			// create popup constraints editor and canvas (inside editor)
			canvas = $('<div id="constraints-editor" class="ui-dialog ui-dialog-content ui-widget ui-widget-content ui-corner-all ui-front ui-draggable ui-resizable" style="width:220px; height:auto; background:#ffffcc; border-radius:10px; border:1px solid darkred; padding:10px; z-index:100;"></div>')
						.appendTo(args.container);
			
			// prepare operators html element
			var ops = constrOps[$type];
			if (ops.length==1) {
				list = ops[0][1];
			} else {
				var list = '<SELECT style="width:60px;height:23px;padding:1px !important;font-size:9pt !important;">';
				for (i in ops) {
					var op = ops[i];
					val=op[0]; txt=op[1];
					list += '<OPTION value="'+val+'">'+txt+'</OPTION>';
				}
				list += '</SELECT>';
			}
			
			// create operators list (select)
			$oplist = $(list);
			$constrOp = $($oplist)
				.appendTo(canvas)
				.bind("keydown", scope.handleKeyDown);
			
			canvas.append(' ');
			
			// create 'from' field
			if (!$isTextType) {
				// numeric or fuzzy value/range type
				var style = ($isTextType || $isFuzzyType) ? 'width:120px;' : 'width:50px;';
				$html = $('<INPUT type="text" style="'+style+';padding:1px !important;" required />');
				$constrFrom = $($html)
					.appendTo(canvas)
					.bind("keydown", scope.handleKeyDown);
			} else 
			if ($type=='BOOLEAN' || $type=='LINGUISTIC') {
				// boolean or linguistic type
				terms = args.item.from.split(',');
				$list = '<SELECT style="width:120px;height:23px;padding:1px !important;font-size:9pt !important;">';
				for (k=0; k<terms.length; k++) {
					val = terms[k].trim();
					if (val==='') continue;
					$list += '<OPTION value="'+val+'">'+val+'</OPTION>';
				}
				list += '</SELECT>';
				$constrFrom = $($list)
					.appendTo(canvas)
					.bind("keydown", scope.handleKeyDown);
			} else {
				// unordered list type
				terms = args.item.from.split(',');
				$list = '<br/>';
				$constrFrom = $('<div></div>');
				$cbArray = [];
				for (k=0; k<terms.length; k++) {
					val = terms[k].trim();
					if (val==='') continue;
					var cb = $('<INPUT type="checkbox" value="'+val+'">');
					$cbArray.push( cb );
					$constrFrom.append(cb).append(' ').append('<i>'+val+'</i>').append($('<br/>'));
				}
				$constrFrom
					.appendTo(canvas)
					.bind("keydown", scope.handleKeyDown);
			}
			
			canvas.append(' ');
			
			// create 'to' field if necessary
			if ($isRange && $isNumericType) {
				$(args.container).append(' &oline; ');
				$html = $('<INPUT type="text" style="'+style+';padding:1px !important;" required />');
				$constrTo = $($html)
					.appendTo(canvas)
					.bind("keydown", scope.handleKeyDown);
			}
			
			// create 'save' button
			$html = '<a href="#" style="color:red;vertical-align:baseline;"><img src="../../images/buttons/save-2.png" style="vertical-align:baseline;"/></a>';
			$save = $($html)
				.appendTo(canvas)
				.bind("keydown", scope.handleKeyDown)
				.click(function() {
					var container = $("#constraints-editor");
					if (container) {
						var e = jQuery.Event("keydown"); 
						e.which = 13; //choose the one you want
						e.keyCode = 13;
						container.trigger(e);
					}
				});
			
			// create 'clear' button
			$html = '<a href="#" style="color:red;vertical-align:baseline;"><img src="../../images/buttons/delete2.png" style="vertical-align:baseline;"/></a>';
			$clear = $($html)
				.appendTo(canvas)
				.bind("keydown", scope.handleKeyDown)
				.click(function() {
					$constrOp.val('');
					$constrFrom.val('');
					if ($constrTo) $constrTo.val('');
					if ($cbArray) {
						for (i=0; i<$cbArray.length; i++) $cbArray[i].prop('checked', false);
					}
				});
			
			//add help tip
			$help = '<div class="wrap" style="font-size:9pt;">'+constrHelp[$type]+'</div>';
			canvas.append($help);
			
			// set focus to this element
			scope.focus();
			
			// capture mouse click outside popup constraints editor and close it
			$(document).mouseup(function (e)
			{
				var container = $("#constraints-editor");

				if (!container.is(e.target) // if the target of the click isn't the container...
					&& container.has(e.target).length === 0) // ... nor a descendant of the container
				{
					//container.hide();
					e = jQuery.Event("keydown"); 
					e.which = 27;
					container.trigger(e);
				}
			});
		};

		this.handleKeyDown = function (e) {
			if (e.keyCode == $.ui.keyCode.LEFT || e.keyCode == $.ui.keyCode.RIGHT || e.keyCode == $.ui.keyCode.TAB) {
				e.stopImmediatePropagation();
			}
		};

		this.destroy = function () {
			$(args.container).empty();
		};

		this.focus = function () {
			$constrOp.focus();
		};

		this.serializeValue = function () {
			var values='';
			var ret = {};
			switch ($type) {
				case 'NUMERIC_INC':
				case 'NUMERIC_DEC':
					values = $constrFrom.val();
					if (!$constrOp || $constrOp.val()==='') ret[$field] = '-';
					else if ($constrFrom.val().trim()!=='' || $constrTo && $constrTo.val().trim()!=='' && $constrTo.val().trim()!==',') ret[$field] = $constrOp.val() +' '+ values;
					else ret[$field] = '-';
					break;
				case 'NUMERIC_RANGE':
					values = '['+$constrFrom.val()+','+$constrTo.val()+']';
					if (!$constrOp || $constrOp.val()==='') ret[$field] = '-';
					else if ($constrFrom.val().trim()!=='' || $constrTo && $constrTo.val().trim()!=='' && $constrTo.val().trim()!==',') ret[$field] = $constrOp.val() +' '+ values;
					else ret[$field] = '-';
					break;
				case 'FUZZY_INC':
				case 'FUZZY_DEC':
					values = '('+$constrFrom.val()+')';
					if (!$constrOp || $constrOp.val()==='') ret[$field] = '-';
					else if ($constrFrom.val().trim()!=='' || $constrTo && $constrTo.val().trim()!=='' && $constrTo.val().trim()!==',') ret[$field] = $constrOp.val() +' '+ values;
					else ret[$field] = '-';
					break;
				case 'FUZZY_RANGE':
					values = '['+$constrFrom.val()+']';
					if (!$constrOp || $constrOp.val()==='') ret[$field] = '-';
					else if ($constrFrom.val().trim()!=='' || $constrTo && $constrTo.val().trim()!=='' && $constrTo.val().trim()!==',') ret[$field] = $constrOp.val() +' '+ values;
					else ret[$field] = '-';
					break;
				case 'BOOLEAN':
				case 'LINGUISTIC':
					if (!$constrOp || $constrOp.val()==='') ret[$field] = '-';
					//else if ($constrFrom && $constrFrom.val().trim()!=='') ret[$field] = $constrOp.val() +' '+ $constrFrom.val();
					else if ($constrFrom && $constrFrom[0] && $constrFrom[0].value && $constrFrom[0].value!=='') ret[$field] = $constrOp.val() +' '+ $constrFrom[0].value;
					else ret[$field] = '-';
					break;
				case 'UNORDERED_SET':
					if (!$constrOp || $constrOp.val()==null || $constrOp.val()==='') ret[$field] = '-';
					else if ($constrFrom) {
						var str = $constrOp.val()+' [';
						first = true;
						for (i=0; i<$cbArray.length; i++) {
							var cb = $cbArray[i];
							if (cb.is(":checked")) {
								if (first) first = false; else str += ', '; 
								str += cb.val();
							}
						}
						str += ']';
						if (first) str = '-';
						ret[$field] = str;
					} else ret[$field] = '-';
					break;
			}
			return ret;
		};

		this.applyValue = function (item, state) {
			if (state[$field]) {
				item[$field] = state[$field].trim();
			} else {
				console.log('state['+$field+'] is not defined');
			}
		};

		this.loadValue = function (item) {
			var value = item[$field].trim();
			if (value==='-') value = '';
			var p = value.indexOf(' ');
			if (p>0) {
				oper = value.substring(0,p).trim();
				lim  = value.substring(p).trim();
			} else {
				oper = '=';
				lim = value;
			}
			$constrOp.val(oper);
			switch ($type) {
				case 'NUMERIC_INC':
				case 'NUMERIC_DEC':
					if (!lim) lim='';
					$constrFrom.val( lim );
					break;
				case 'NUMERIC_RANGE':
					if (lim) {
						var part = lim.substring(1, lim.length-1).split(',',2);
						$constrFrom.val( part[0].trim() );
						$constrTo.val( part[1].trim() );
					}
					break;
				case 'FUZZY_INC':
				case 'FUZZY_DEC':
					if (!lim) lim='()';
					var tfn = lim.substring(1, lim.length-1);
					$constrFrom.val( tfn );
					//var part = tfn.split(';',3);
					break;
				case 'FUZZY_RANGE':
					if (lim) {
						var tfi = lim.substring(1, lim.length-1);
						$constrFrom.val( tfi );
						//var part = tfn.split(';',4);
					}
					break;
				case 'BOOLEAN':
				case 'LINGUISTIC':
					if (!lim) lim='';
					$constrFrom.val( lim );
					break;
				case 'UNORDERED_SET':
					if (lim) {
						//clear checkbox ticks
						for (i=0; i<$cbArray.length; i++) $cbArray[i].prop('checked', false);
						//set checkbox ticks
						var part = lim.substring(1, lim.length-1).split(',');
						for (k=0; k<part.length; k++) {
							var val = part[k].trim();
							if (val==='') continue;
							// find checkbox and check it
							for (i=0; i<$cbArray.length; i++) {
								if ($cbArray[i].val().trim()===val) {
									$cbArray[i].prop('checked', true);
									break;
								}
							}
						}
					}
					break;
			}
		};

		this.isValueChanged = function () {
			r = this._isValueChanged();
			//alert('CHANGED: RESULT: '+r);
			return r;
		}
		this._isValueChanged = function () {
			var value = args.item[$field].trim();	//current (stored) value. New values in fields $constrOp/From/To
			//var part = value.split(' ',2);
			var p = value.indexOf(' ');
			var oper = value.substring(0, p);	//part[0];
			var lim  = value.substring(p+1);	//part[1];
			var cFrom='';
			var cTo='';
			switch ($type) {
				case 'NUMERIC_INC':
				case 'NUMERIC_DEC':
					if (!lim) cFrom = '';
					else cFrom = lim;
					break;
				case 'NUMERIC_RANGE':
					if (lim) {
						var part = lim.substring(1, lim.length-1).split(',',2);
						cFrom = part[0];
						cTo = part[1];
					}
					break;
				case 'FUZZY_INC':
				case 'FUZZY_DEC':
					if (!lim) cFrom='0;0;0';
					else cFrom = lim.substring(1, lim.length-1);
					break;
				case 'FUZZY_RANGE':
					if (lim) {
						lim = lim.substring(1, lim.length-1);
						cFrom = lim;
					}
					break;
				case 'BOOLEAN':
				case 'LINGUISTIC':
					if (!lim) cFrom='';
					else cFrom = lim;
					break;
				case 'UNORDERED_SET':
					if (lim) {
						arr1 = lim.split(',');
						arr2 = $constrFrom.val().split(',');
						if (arr1.length!==arr2.length) return true;
						cnt = 0;
						for (ii in arr1) {
							arr1[ii] = arr1[ii].trim();
							if (arr1[ii]==='') continue;
							for (jj in arr2) {
								arr2[jj] = arr2[jj].trim();
								if (arr2[jj]==='') continue;
								if (arr1[ii]===arr2[jj]) cnt++;
							}
						}
						if (cnt!==arr1.length) return true;
					}
					break;
			}
			if (!cTo || cTo==='') return (oper !== $constrOp.val() || cFrom !== $constrFrom.val());
			else return (oper !== $constrOp.val() || cFrom !== $constrFrom.val() || cTo !== $constrTo.val());
		};

		function isNumeric(n) {
			return !isNaN(parseFloat(n)) && isFinite(n);
		}
		
		this.validate = function () {
			r = this._validate();
			//alert('VALID: RESULT: '+JSON.stringify(r));
			return r;
		}
		this._validate = function () {
			oper = $constrOp.val();
			cFrom = $constrFrom.val();
			cTo = $constrTo ? $constrTo.val() : '';
			if (!oper || oper==null) oper = '';
			if (!cFrom || cFrom==null) cFrom = '';
			if (!cTo || cTo==null) cTo = '';
			oper = oper.trim();
			cFrom = cFrom.trim();
			cTo = cTo.trim();
			fieldName = '';	//$field;
			//alert('VALID: op: '+oper+'  from: '+cFrom+'  to: '+cTo+'  fld: '+fieldName);
			if ((oper==='' || oper==='-') && (cFrom==='' || cFrom==='-') && (cTo==='' || cTo==='-')) {
				//constraint has been cleared
				return {valid: true, msg: null};
			}
			//
			if (oper==='') {
				return {valid: false, msg: "Missing operator for "+fieldName+" constraint."};
			}
			if (cFrom==='' && $type!=='UNORDERED_SET') {
				return {valid: false, msg: "Missing value for "+fieldName+" constraint."};
			}
			//
			val = cFrom;
			switch ($type) {
				case 'NUMERIC_INC':
				case 'NUMERIC_DEC':
					if (cFrom==='' || isNaN(cFrom) || !isNumeric(cFrom)) {
						return {valid: false, msg: "Invalid numeric value for "+fieldName+" constraint."};
					} 
					break;
				case 'NUMERIC_RANGE':
					if (cFrom==='' || isNaN(cFrom) || !isNumeric(cFrom)) { return {valid: false, msg: "Invalid numeric value in the lower bound of the range specification for "+fieldName+" constraint."};	} 
					if (cTo==='' || isNaN(cTo) || !isNumeric(cTo)) { return {valid: false, msg: "Invalid numeric value in the upper bound of the range specification for "+fieldName+" constraint."}; } 
					pFrom = parseFloat(cFrom);
					pTo = parseFloat(cTo);
					if (pFrom > pTo) { return {valid: false, msg: "Lower bound must be lower or equal to upper bound of the range specification for "+fieldName+" constraint."}; } 
					break;
				case 'FUZZY_INC':
				case 'FUZZY_DEC':
					if (cFrom.startsWith('(')) cFrom = cFrom.substring(1);
					if (cFrom.endsWith(')')) cFrom = cFrom.substring(0, cFrom.length-1);
					part = cFrom.split(';');
					if (part.length!==3) {
						return {valid: false, msg: "Invalid fuzzy value for "+fieldName+" constraint."};
					}
					a = part[0].trim(); b = part[1].trim(); c = part[2].trim();
					if (a==='' || isNaN(a) || !isNumeric(a)) { return {valid: false, msg: "Invalid fuzzy value a for "+fieldName+" constraint."}; } 
					if (b==='' || isNaN(b) || !isNumeric(b)) { return {valid: false, msg: "Invalid fuzzy value b for "+fieldName+" constraint."}; } 
					if (c==='' || isNaN(c) || !isNumeric(c)) { return {valid: false, msg: "Invalid fuzzy value c for "+fieldName+" constraint."}; } 
					a = parseFloat(a); b = parseFloat(b); c = parseFloat(c); 
					if (! (a<=b && b<=c) ) { return {valid: false, msg: "Fuzzy value parameters must be a <= b <=; c for "+fieldName+" constraint."}; } 
					break;
				case 'FUZZY_RANGE':
					if (cFrom.startsWith('(')) cFrom = cFrom.substring(1);
					if (cFrom.endsWith(')')) cFrom = cFrom.substring(0, cFrom.length-1);
					if (cFrom.startsWith('[')) cFrom = cFrom.substring(1);
					if (cFrom.endsWith(']')) cFrom = cFrom.substring(0, cFrom.length-1);
					part = cFrom.split(';');
					if (part.length!==4) {
						return {valid: false, msg: "Invalid fuzzy range for "+fieldName+" constraint."};
					}
					a = part[0].trim(); b = part[1].trim(); c = part[2].trim(); d = part[3].trim();
					if (a==='' || isNaN(a) || !isNumeric(a)) { return {valid: false, msg: "Invalid fuzzy a range for "+fieldName+" constraint."}; } 
					if (b==='' || isNaN(b) || !isNumeric(b)) { return {valid: false, msg: "Invalid fuzzy b range for "+fieldName+" constraint."}; } 
					if (c==='' || isNaN(c) || !isNumeric(c)) { return {valid: false, msg: "Invalid fuzzy c range for "+fieldName+" constraint."}; } 
					if (d==='' || isNaN(d) || !isNumeric(d)) { return {valid: false, msg: "Invalid fuzzy d range for "+fieldName+" constraint."}; } 
					a = parseFloat(a); b = parseFloat(b); c = parseFloat(c); d = parseFloat(d); 
					if (! (a<=b && b<=c && c<=d) ) { return {valid: false, msg: "Fuzzy range parameters must be a <= b <= c <= d for "+fieldName+" constraint."}; } 
					break;
				case 'BOOLEAN':
				case 'LINGUISTIC':
					part = cFrom.split(',');
					if (part.length!==1) {
						return {valid: false, msg: "Multiple values in boolean or linguistic constraint for "+fieldName+"."};
					}
					break;
				case 'UNORDERED_SET':
					break;
			}
			return {valid: true, msg: null};
		};

		this.init();
	}

// ================================================================================================
// Various utility functions

function renumberRows(data) {
	for (i=0,n=data.length; i<n; i++) {
		data[i].rownum = i+1;
	}
}

// END OF FILE