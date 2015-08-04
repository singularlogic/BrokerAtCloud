/*
 *	Utility functions and variables common to all pages
 */

// ================================================================================================
// Various utility functions and variables (loading indicator and row renumbering)

var loadingIndicator = null;
var loadingIndicatorElement = null;

function showIndicator(mesg, elem) {
	if (!loadingIndicator) {
		loadingIndicator = $("<span class='loading-indicator'><label>"+mesg+"</label></span>").appendTo(document.body);
		if (!elem) elem = document.body;
		var $g = $(elem);
		loadingIndicator
			.css("position", "absolute")
			.css("top", $g.position().top + $g.height() / 2 - loadingIndicator.height() / 2)
			.css("left", $g.position().left + $g.width() / 2 - loadingIndicator.width() / 2);
	} else {
		$('.loading-indicator').html('<label>'+mesg+'</label>');
	}
	loadingIndicator.show();
}

var statusSelector = '#res';

// Status functions
function setStatus(mesg) {
}

function statusContacting(url, valStr) {
}

//EOF