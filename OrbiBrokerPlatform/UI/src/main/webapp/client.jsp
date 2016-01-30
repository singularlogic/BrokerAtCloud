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
                            <li><a href="/orbibroker/profile/list">Profile</a></li>
                            <li><a href="/orbibroker/recommendations/list">Recommendations</a></li>
                            <li><a href="/orbibroker/services/list">Running Services</a></li>
                            <li><a href="/orbibroker/feedback/list">Feedback</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Dashboard<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Client Dashboard</p></h2>

                    <!-- <img src="assets/img/ORBI_ARROW.png" class="arrow" width="200px" />-->

                    <div class="wrapper col-md-12">

                        <div class="dashboard-widget">
                            <img src="/orbibroker/assets/img/DASHBOARD_U1.png" class="dashboard-widget-icon"/>
                            <h4>Purchased Services: <strong><c:out value="${numOfServices}"></c:out></strong><br/>
                                    <small>Currently purchased services list<br/><br/><a href="/orbibroker/services/list" class="btn btn-danger">View &raquo;</a></small>
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