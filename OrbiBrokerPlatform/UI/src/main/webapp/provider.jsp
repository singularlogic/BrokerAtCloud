<!DOCTYPE html>
<html>
    <head>
        <title>Welcome &mdash; ORBI</title>

        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="assets/css/bootstrap.min.css" type="text/css" />
        <link rel="stylesheet" href="assets/css/orbi.css" type="text/css" />
        <link href="http://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css" />
    </head>

    <body>

        <!-- Include Header -->
        <%@include file="inc/header.jsp" %>

        <main>
            <div class="container">
                <div class="col-md-3">
                    <nav>
                        <ul>
                            <li class="title">Menu</li>
                            <li><a class="active" href="/orbibroker/dashboard">Dashboard</a></li>
                            <li><a href="/orbibroker/servicedesc/list">Service Description Mgmt</a></li>
                            <li><a href="/orbibroker/iaasconfig/list">IaaS Configuration Mgmt</a></li>
                            <li><a href="/orbibroker/appstore/list">App Store</a></li>
                            <!--<li><a href="/orbibroker/flavors/list">Flavors</a></li>-->
                            <li><a href="/orbibroker/offerings/list">Offerings</a></li>
                            <li><a href="/orbibroker/pulsar/notifications">Notifications</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Dashboard<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Provider Dashboard</p></h2>

                    <!-- <img src="assets/img/ORBI_ARROW.png" class="arrow" width="200px" />-->

                    <div class="wrapper col-md-12">

                        <div class="dashboard-widget">
                            <img src="/orbibroker/assets/img/DASHBOARD_P1.png" class="dashboard-widget-icon"/>
                            <h4>Available Service Descriptions: <strong><c:out value="${info.numOfServicedesc}"></c:out></strong><br/>
                                    <small>Currently available service descriptions list<br/><br/><a href="/orbibroker/servicedesc/list" class="btn btn-danger">View &raquo;</a></small>
                                </h4>
                            </div>

                            <div class="dashboard-widget">
                                <img src="/orbibroker/assets/img/DASHBOARD_P2.png" class="dashboard-widget-icon"/>
                                <h4>Available Images: <strong><c:out value="${info.numOfImages}"></c:out></strong><br/>
                                    <small>Currently available images list<br/><br/><a href="/orbibroker/images/list" class="btn btn-danger">View &raquo;</a></small>
                                </h4>
                            </div>

<!--                            <div class="dashboard-widget">
                                <img src="/orbibroker/assets/img/DASHBOARD_P3.png" class="dashboard-widget-icon"/>
                                <h4>Available Flavors: <strong><c:out value="${info.numOfFlavors}"></c:out></strong><br/>
                                    <small>Currently available flavors list<br/><br/><a href="/orbibroker/flavors/list" class="btn btn-danger">View &raquo;</a></small>
                                </h4>
                            </div>-->

                            <div class="dashboard-widget">
                                <img src="/orbibroker/assets/img/DASHBOARD_P4.png" class="dashboard-widget-icon"/>
                                <h4>Available Offerings: <strong><c:out value="${info.numOfOfferings}"></c:out></strong><br/>
                                    <small>Currently available offerings list<br/><br/><a href="/orbibroker/offerings/list" class="btn btn-danger">View &raquo;</a></small>
                                </h4>
                            </div>



                        </div>

                    </div>

                </div>
            </main>

            <!-- Include Footer -->
        <%@include file="inc/footer.jsp" %>

        <script src="assets/js/jquery-2.1.4.min.js"></script>
        <script src="assets/js/bootstrap.min.js"></script>
        <script src="assets/js/orbi.js"></script>

    </body>

</html>