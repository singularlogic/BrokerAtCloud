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
                            <li><a href="/orbibroker/policies/list">Broker Policies</a></li>
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

                    <h2>Dashboard<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Administrator Dashboard</p></h2>

                    <!-- <img src="assets/img/ORBI_ARROW.png" class="arrow" width="200px" />-->

                    <div class="wrapper col-md-12">

                        <div class="dashboard-widget">
                            <img src="/orbibroker/assets/img/DASHBOARD_A1.png" class="dashboard-widget-icon"/>
                            <h4>Active Policies: <strong><c:out value="${info.numOfPolicies}"></c:out></strong><br/>
                            <small>Currently active policies list<br/><br/><a href="/orbibroker/policies/list" class="btn btn-danger">View &raquo;</a></small>
                            </h4>
                        </div>

                        <div class="dashboard-widget">
                            <img src="/orbibroker/assets/img/DASHBOARD_A2.png" class="dashboard-widget-icon"/>
                            <h4>Active Providers: <strong><c:out value="${info.numOfProviders}"></c:out></strong><br/>
                            <small>Currently active providers list<br/><br/><a href="/orbibroker/providers/list" class="btn btn-danger">View &raquo;</a></small>
                            </h4>
                        </div>

                        <div class="dashboard-widget">
                            <img src="/orbibroker/assets/img/DASHBOARD_A3.png" class="dashboard-widget-icon"/>
                            <h4>Active Clients: <strong><c:out value="${info.numOfClients}"></c:out></strong><br/>
                            <small>Currently active clients list<br/><br/><a href="/orbibroker/clients/list" class="btn btn-danger">View &raquo;</a></small>
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