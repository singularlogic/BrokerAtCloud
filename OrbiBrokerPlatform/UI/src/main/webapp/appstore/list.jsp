<!DOCTYPE html>
<html>
    <head>
        <title>Images &mdash; ORBI</title>

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

                    <a href="/orbibroker/appstore/add" class="btn btn-danger pull-right action-button">Add New</a>
                    <h2>App Store<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> App Store List</p></h2>

                    <div class="table-responsive wrapper col-md-12">    
                        <c:forEach items="${apps}" var="app">
                            <div class="col-md-3">
                                <img title="<c:out value="${app.name}"></c:out>"  src="<c:out value="${app.thumbnail}"></c:out>" class="img-thumbnail"/>
                                <h4 title="<c:out value="${app.name}"></c:out>" class="truncate"><c:out value="${app.name}"></c:out></h4>
                                <h6 class="metadata"><c:out value="${app.metadata}"></c:out></h6>
                                </div>
                        </c:forEach>
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