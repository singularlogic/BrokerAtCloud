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

			$(args.container).append($label+': ');
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
// Various utility functions

function renumberRows(data) {
	for (i=0,n=data.length; i<n; i++) {
		data[i].rownum = i+1;
	}
}

// END OF FILE