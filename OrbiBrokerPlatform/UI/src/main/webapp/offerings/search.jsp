<!DOCTYPE html>
<html>
    <head>
        <title>Search Offers &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/search/list">My Profile</a></li>
                            <li><a href="/orbibroker/recommendations/list">Recommendations</a></li>
                            <li><a href="/orbibroker/services/list">Running Services</a></li>
                            <li><a href="/orbibroker/offers/search">My Offers</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Search Offers<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Search Offers List</p></h2>

                    <div class="table-responsive wrapper col-md-12">
                        <div id="notificationMSG"></div>

                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th class="col-md-2">Offer Name</th>
                                    <th class="col-md-4">Policy Name</th>
                                    <th class="col-md-3">Service Description</th>
                                    <th class="col-md-2">Provided by</th>
                                    <th class="col-md-1">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${offers}" var="offer">

                                    <tr>
                                        <td class="truncate"><strong><c:out value="${offer.name}"></c:out></strong></td>
                                        <td class="truncate"><strong><c:out value="${offer.policy_name}"></c:out></strong></td>
                                        <td class="truncate"><strong><c:out value="${offer.service_description}"></c:out></strong></td>
                                        <td class="truncate"><strong><c:out value="${offer.username}"></c:out></strong></td>
                                            <td class="actions">
                                                <a class="pull-right btn btn-default btn-sm" id="purchaseButton" href="javascript:purchase('${offer.id}')"><span class="glyphicon glyphicon-shopping-cart" title="Purchase"></span></a>
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

        <script>

            function purchase(offerID) {

                // Make ajax call to purchase this offer

                $('#notificationMSG').val("");
                $('#notificationMSG').text("");
                $('#notificationMSG').addClass("alert alert-info");
                var notification = '<div class="text-center"><strong>Please wait...</strong><br></div>';
                $('#notificationMSG').append(notification);

                jQuery.ajax({
                    featureClass: "P",
                    style: "full",
                    url: "../offerings/purchase",
                    dataType: "jsonp",
                    contentType: "text/html; charset=UTF-8",
                    encoding: "UTF-8",
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("Content-Type", "text/html;charset=utf-8");
                    },
                    data: {
                        format: "json",
                        offer: offerID
                    },
                    success: function (data) {

                        // Check if the resultset contains any data 
                        if (data.message !== null) {

                            if (data.message === "SUCCESS") {

                                $('#notificationMSG').val("");
                                $('#notificationMSG').text("");
                                $('#notificationMSG').addClass("alert alert-success");

                                var notification = '<div class="text-center"><strong>Congratulations!</strong> You successfully purchased this offering! Please wait...<br></div>';

                                $('#notificationMSG').append(notification);

                                setTimeout(function () {
                                    window.location.href = "../services/list";
                                }, 2000);
                            }
                            else
                            {
                                $('#notificationMSG').val("");
                                $('#notificationMSG').text("");
                                $('#notificationMSG').addClass("alert alert-danger");

                                var notification = '<div class="text-center"><strong>Sorry!</strong> An error has occured. Please try again!<br></div>';

                                $('#notificationMSG').append(notification);
                            }
                        } else {

                            $('#notificationMSG').val("");
                            $('#notificationMSG').text("");
                            $('#notificationMSG').addClass("alert alert-danger");

                            var notification = '<div class="text-center"><strong>Sorry!</strong> An error has occured. Please try again!<br></div>';

                            $('#notificationMSG').append(notification);

                        }
                    },
                    error: function () {
                        $('#notificationMSG').val("");
                        $('#notificationMSG').text("");
                        $('#notificationMSG').addClass("alert alert-danger");

                        var notification = '<div class="text-center"><strong>Sorry!</strong> An error has occured. Please try again!<br></div>';

                        $('#notificationMSG').append(notification);

                    }
                });

            }


        </script>

    </body>

</html>