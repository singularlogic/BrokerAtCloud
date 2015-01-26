/*
 *	Menu
 */

function renderMenu() {
	var valStr = '';
	$.ajax({
		async: false,
		type: 'get',
		url: '/auth/menu',
		contentType: 'text/html',
		dataType: 'html',
		success: function(data, textStatus, jqXHR) {
					document.write(data);
				},
		error: function(jqXHR, textStatus, errorThrown) {
					document.write('<font color="red"><i>Failed to rerieve menu</i></font>');
				},
	});
}

renderMenu();