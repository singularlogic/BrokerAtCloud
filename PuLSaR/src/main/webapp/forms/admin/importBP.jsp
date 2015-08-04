<!DOCTYPE html>
<%@ include file="../includes/prelude.html" %>
<html>
  <head>
<% pageTitle = "Import Broker Policy"; %>
<%@ include file="../includes/head.html" %>
	<link rel="stylesheet" href="../slick/common-grid-styles.css" type="text/css"/>
	<script src="dropzone.js"></script>
	<link rel="stylesheet" href="dropzone.css">

	<script>
		Dropzone.options.ttlUploadDropzone = {
			paramName: "file", // The name that will be used to transfer the file
			//maxFilesize: 2, // MB
			accept: function(file, done) {
				if (!file.name.toLowerCase().endsWith(".ttl")) {
					if (confirm("File "+file.name+" doesn't look like a TTL file.\nContinue with upload?")) { done(); }
					else { done("User cancelled upload"); this.removeFile(file); }
				}
				else { done(); }
			},
			uploadMultiple: false,
			addRemoveLinks: true,
			clickable: true,
			dictCancelUpload: "cancel",
			//dictCancelUploadConfirmation: "Are you sure?",
			dictRemoveFile: "remove"
		};
	</script>
	
  </head>
  <body style="padding:10px;">
<%@ include file="../includes/header.html" %>
<%@ include file="../includes/js-libs.html" %>
	
	<div style="width:80%; margin-left:auto; margin-right:auto;">
		<form action="/gui/admin/importBrokerPolicy" method="POST" enctype="multipart/form-data" accept-charset="UTF-8" class="dropzone" id="ttl-upload-dropzone">
			<p>
				<b>Import mode:</b>&nbsp;&nbsp;&nbsp;
				<input type="radio" name="import_mode" value="append" checked> Append to current content&nbsp;&nbsp;&nbsp;
				<input type="radio" name="import_mode" value="replace"> Replace current content
			</p>
			<p class="dz-message" style="color:blue;">Drag and drop broker policy TTL file in this area...</p>
			<div class="fallback">
				<input name="file" type="file" multiple />
				<input type="submit" value="Send It!" />
			</div>
		</form>
	</div>
	
	<%@ include file="../includes/footer.html" %>
  </body>
</html>
<%@ include file="../includes/debug.html" %>
<%@ include file="../includes/trail.html" %>