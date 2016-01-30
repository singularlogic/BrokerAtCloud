<%@page import="org.broker.orbi.models.IaaSConfiguration"%>
<!DOCTYPE html>
<html>
    <head>
        <title>App Store &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/servicedesc/list">Service Description Mgmt</a></li>
                            <li><a href="/orbibroker/iaasconfig/list">IaaS Configuration Mgmt</a></li>
                            <li><a class="active" href="/orbibroker/appstore/list">App Store</a></li>
                            <!--<li><a href="/orbibroker/flavors/list">Flavors</a></li>-->
                            <li><a href="/orbibroker/offerings/list">Offerings</a></li>
                            <li><a href="/orbibroker/pulsar/notifications">Notifications</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <a href="/orbibroker/iaasconfig/add" class="btn btn-danger pull-right action-button">Add New</a>
                    <h2>App Store<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> App Store Add</p></h2>

                    <div class="table-responsive wrapper col-md-12">
                    <form role="form" action="store" method="post" id="appstore-form" autocomplete="off">

                        <div class="droptarget-app" ondrop="loadSD(event)" ondragover="allowDrop(event)">
                            <p id="sd_load"> Drop here application thumbnail icon (500px x 500px)</p>
                        </div>
                        
                        <div class="form-group col-md-7">
                            <label for="app_name" class="sr-only">App Name</label>
                            <input type="text" name="app_name" id="app_name" class="form-control" placeholder="App Name">
                        </div>
                        <div class="form-group col-md-7">
                            <label for="app_template" class="sr-only">IaaS Configuration Endpoint</label>
                            <input type="text" name="app_template" id="app_template" class="form-control" placeholder="Template ID: 0x00">
                        </div>
                        
                        <div class="form-group col-md-7">
                            <input type="submit" id="btn-save" class="btn btn-primary btn-block" value="Save">
                        </div>
                        </form>

                    </div>

                </div>
            </div>
        </main>

        <!-- Include Footer -->
        <%@include file="../inc/footer.jsp" %>

        <script src="../assets/js/jquery-2.1.4.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>
        <script src="../assets/js/orbi.js"></script>

    </body>

</html>