<!DOCTYPE html>
<html>
    <head>
        <title>Providers &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/policies/list">Broker Policies</a></li>
                            <li><a href="/orbibroker/providers/list">Providers</a></li>
                            <li><a class="active" href="/orbibroker/clients/list">Clients</a></li>
                            <li><a href="/orbibroker/pulsar/dashboard">Active Policies</a></li>
                            <li><a href="/orbibroker/pulsar/management">Serv. Attr. Mgmt</a></li>
                            <li><a href="/orbibroker/pulsar/mapping">Attr. Mapping</a></li>
                            <li><a href="/orbibroker/configuration/list">Configuration</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Clients<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Client List</p></h2>
                    
                    <div class="table-responsive wrapper col-md-12">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th class="col-md-5">Client Details</th>
                                    <th class="col-md-2">Date Created</th>
                                    <th class="col-md-2">Date Edited</th>
                                    <th class="col-md-2"></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${consumers}" var="consumers">
                                    <tr>
                                        <td class="truncate"><strong><c:out value="${consumers.name}"></c:out> <c:out value="${consumers.surname}"></c:out></strong> (@<c:out value="${consumers.username}"></c:out>)<br/><c:out value="${consumers.email}"></c:out></td>
                                        <td class="timestamp"><c:out value="${fn:substring(consumers.date_created, 0, 16)}"></c:out></td>
                                        <td class="timestamp"><c:out value="${fn:substring(consumers.date_created, 0, 16)}"></c:out></td>
                                            <td class="actions">
                                                <a class="pull-right btn btn-default btn-sm" href="?remove=<c:out value="${consumers.id}"></c:out>"><span class="glyphicon glyphicon-trash" title="Delete"></span></a>
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
        <script src="../assets/js/orbi.js"></script>

    </body>

</html>