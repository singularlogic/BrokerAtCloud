<!DOCTYPE html>
<html>
    <head>
        <title>Policies &mdash; ORBI</title>

        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="../assets/css/bootstrap.min.css" type="text/css" />
        <link rel="stylesheet" href="../assets/css/orbi.css" type="text/css" />
        <link href="http://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css" />
    </head>

    <body>

        <!-- Include Header -->
        <%@include file="../inc/header.jsp" %>

        <main>
            <div class="container">
                <div class="col-md-3">
                    <nav>
                        <ul>
                            <li class="title">Menu</li>
                            <li><a href="/orbibroker/dashboard">Dashboard</a></li>
                            <li><a class="active" href="/orbibroker/policies/list">Broker Policies</a></li>
                            <li><a href="/orbibroker/providers/list">Providers</a></li>
                            <li><a href="/orbibroker/clients/list">Clients</a></li>
                            <li><a href="/orbibroker/pulsar/dashboard">Active Policies</a></li>
                            <li><a href="/orbibroker/pulsar/management">Serv. Attr. Mgmt</a></li>
                            <li><a href="/orbibroker/pulsar/mapping">Attr. Mapping</a></li>
                            <li><a href="/orbibroker/configuration/list">Configuration</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <a href="/orbibroker/policies/add" class="btn btn-danger pull-right action-button">Add New</a>

                    <form class="policyUploadForm" name="policyUploadForm" action="../upload" method="POST" enctype="multipart/form-data">

                        <span class="btn btn-default fileinput-button">
                            <i class="glyphicon glyphicon-plus"></i>
                            <span>Upload Broker Policy (.ttl) </span>
                            <input id="policyUpload" type="file" name="policyUpload" onclick="uploadFile(this)" >    
                            <!--                            <input id="policyUpload" type="file" name="policyUpload" onchange="policyUploadForm.submit();">    -->
                        </span>

                    </form>

                    <h2>Broker Policies<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Broker Policies List</p></h2>

                    <div id="statusDIV" style="display: none" class="alert alert-warning alert-dismissible btn-actions" role="alert" style="text-align:center">
                        <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">�</span></button>
                        <span id="statusMessage"></span>
                    </div>

                    <div class="table-responsive wrapper col-md-12">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th class="col-md-5">Policy Name</th>
                                    <th class="col-md-2">Date Created</th>
                                    <th class="col-md-2">Date Edited</th>
                                    <th class="col-md-2"></th>
                                </tr>
                            </thead>
                            <tbody>

                                <c:forEach items="${policies}" var="policies">
                                    <tr>
                                        <td class="truncate"><strong><c:out value="${policies.name}"></c:out></strong></td>
                                        <td class="timestamp"><c:out value="${fn:substring(policies.date_created, 0, 16)}"></c:out></td>
                                        <td class="timestamp"><c:out value="${fn:substring(policies.date_edited, 0, 16)}"></c:out></td>
                                            <td class="actions">
                                                <a class="pull-right btn btn-default btn-sm" href="?remove=<c:out value="${policies.id}"></c:out>"><span class="glyphicon glyphicon-trash" title="Delete"></span></a>
                                            <a class="pull-right btn btn-default btn-sm" href="edit?id=<c:out value="${policies.id}"></c:out>"><span class="glyphicon glyphicon-edit" title="Edit"></span></a>
                                            <a class="pull-right btn btn-default btn-sm"  href="../download?id=<c:out value="${policies.id}"></c:out>&type=p"><span class="glyphicon glyphicon-download-alt" title="Download"></span></a>
                                            </td>
                                        </tr>
                                </c:forEach>

                            </tbody>
                        </table>
                    </div>

                </div>
            </div>
        </main>

        <!-- Include Footer -->
        <%@include file="../inc/footer.jsp" %>

        <script src="../assets/js/jquery-2.1.4.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>
        <script src="../assets/js/jquery.ui.widget.js"></script>
        <script src="../assets/js/jquery.iframe-transport.js"></script>
        <script src="../assets/js/jquery.fileupload.js"></script>
        <script src="../assets/js/orbi.js"></script>


        <script>


                                function uploadFile(element) {
                                    $(element).fileupload({
                                        dataType: 'json',
                                        url: '../upload',
                                        autoUpload: true,
                                        add: function (e, data) {
                                            // write code for implementing, while selecting a file. 
                                            // data represents the file data. 
                                            //below code triggers the action in mvc controller
                                            data.formData =
                                                    {
                                                        files: data.files[0]
                                                    };
                                            data.submit();
                                        },
                                        done: function (e, data) {
                                            $("#statusDIV").show();
                                            if ("OK" === data.result.status) {
                                                location.reload();
                                            } else {
                                                $("#statusMessage").text(data.result.message);
                                            }

                                            console.log(data.result.message);
                                            // after file uploaded
                                        }
                                    });

                                }


        </script>

    </body>

</html>