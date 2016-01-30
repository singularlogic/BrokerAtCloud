<!DOCTYPE html>
<html>
    <head>
        <title>Offerings &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/appstore/list">App Store</a></li>
                            <!--<li><a href="/orbibroker/flavors/list">Flavors</a></li>-->
                            <li><a href="/orbibroker/offerings/list">Offerings</a></li>
                            <li><a href="/orbibroker/pulsar/notifications">Notifications</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Offerings<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Create a new offering</p></h2>

                    <form role="form" method="post" id="login" action="store" autocomplete="off">

                        <div class="form-group">
                            <label for="offeringName" class="sr-only">Offering Name</label>
                            <input type="text" name="offeringName" id="offeringName" class="form-control" placeholder="Offering Name">
                        </div>
                        <hr>
                        <h5 class="pull-left">Offering Details</h5><br/><br/>

                        <div class="btn-group account-type">
                            <select id="policies" name="policies" class="btn btn-default">
                                <option selected="true" value="0">Choose policy</option>
                                <c:forEach items="${policies}" var="policy">
                                    <option value="${policy.id}">${policy.name}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="btn-group account-type">
                            <select id="serviceDescs" name="serviceDescs" class="btn btn-default">
                                <option selected="true" value="0">Choose service description</option>
                                <c:forEach items="${serviceDescs}" var="serviceDesc">
                                    <option value="${serviceDesc.id}">${serviceDesc.name}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="btn-group account-type">
                            <select id="iaasProviders" name="iaasProviders" class="btn btn-default">
                                <option selected="true" value="0">Choose IaaS Provider</option>
                                <c:forEach items="${iaasProviders}" var="iaasProvider">
                                    <option value="${iaasProvider.id}">${iaasProvider.name}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="btn-group account-type">
                            <select id="images" name="images" class="btn btn-default">
                                <option selected="true" value="0">Choose Image</option>
                                <c:forEach items="${images}" var="image">
                                    <option value="${image.id}">${image.name}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!--                        <div class="btn-group account-type">
                                                    <select id="flavors" name="flavors" class="btn btn-default">
                                                        <option selected="true" value="0">Choose Flavor</option>
                        <c:forEach items="${flavors}" var="flavor">
                            <option value="${flavor.id}">${flavor.name}</option>
                        </c:forEach>
                    </select>
                </div>-->

                        <input name="flavors" type="hidden" value="2" />

                        <button type="reset" class="btn btn-default pull-right action-button">Cancel</button>
                        <button type="submit" class="btn btn-danger pull-right action-button">Save</button>
                    </form>

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