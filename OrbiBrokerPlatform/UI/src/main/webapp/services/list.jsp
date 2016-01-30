<!DOCTYPE html>
<html>
    <head>
        <title>Services &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/profile/list">Profile</a></li>
                            <li><a href="/orbibroker/recommendations/list">Recommendations</a></li>
                            <li><a class="active" href="/orbibroker/services/list">Running Services</a></li>
                            <li><a href="/orbibroker/feedback/list">Feedback</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Services<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Services List</p></h2>

                    <div class="table-responsive wrapper col-md-12">

                        <c:choose>
                            <c:when test="${numOfpurchases == '0'}">
                                <br />
                                <div class="text-center alert alert-warning"><strong>Sorry!</strong> You haven't purchased any Service Offering!<br></div>
                                </c:when>
                                <c:otherwise>

                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th class="col-md-7">Offering Details</th>
                                            <th class="col-md-2">Date Purchased</th>
                                            <th class="col-md-2"></th>
                                        </tr>
                                    </thead>
                                    <tbody>

                                        <c:forEach items="${purchases}" var="purchase">
                                            <tr>
                                                <td class="truncate">
                                                    <strong><c:out value="${purchase.name}"></c:out></strong> (<c:out value="${purchase.providerName}"></c:out>)<br/>
                                                    <c:out value="${purchase.policyName}"></c:out><br/><c:out value="${purchase.profileName}"></c:out><br/>
                                                    <a  target="_blank" href="http://<c:out value="${purchase.publicIP}"></c:out>"> <c:out value="${purchase.publicIP}"></c:out></a> &nbsp;
                                                    <a style="color:red" target="_blank" href="http://<c:out value="${purchase.publicIP}"></c:out>/stressmeout.php"> Stress Test</a><br/>
                                                    <!-- CPU: <c:out value="${purchase.cpuUtil}"></c:out>&nbsp;MEM: <c:out value="${purchase.ramUtil}"></c:out>&nbsp; -->
                                                    Status: <c:out value="${purchase.vmStatus}"></c:out>
                                                    </td>
                                                        <td class="timestamp"><c:out value="${fn:substring(purchase.datePurchased, 0, 16)}"></c:out></td>
                                                    <td class="actions">

                                                        <div class="btn-group account-type">
                                                            <select id="variables${purchase.purchaseID}" name="variables${purchase.purchaseID}" class="btn btn-default">

                                                            <c:forEach items="${purchase.variables}" var="var">
                                                                <option value="${var.id}">${var.name}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>

                                                    <a class="pull-right btn btn-default btn-sm" href="?remove=<c:out value="${purchase.purchaseID}"></c:out>"><span class="glyphicon glyphicon-trash" title="Delete"></span></a>
                                                    <a onClick="showGraph('<c:out value="${purchase.purchaseID}"></c:out>', true);" title="View Graph" data-rel="tooltip" id="ViewBtn1" class="pull-right btn btn-default btn-sm" style="margin-top: 5px;">
                                                            <span class="glyphicon glyphicon-signal" title="View Graph"></span>
                                                        </a>   
                                                    </td>
                                                </tr>
                                        </c:forEach>

                                    </tbody>
                                </table>

                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>
            </div>
            <div class="row">
                <div class="col-md-3"></div>
                <div class="col-md-9">
                    <div id="flotchart" class="center" style="height:300px;width:80%;"></div>
                </div>
            </div>
        </main>

        <!-- Include Footer -->
        <%@include file="../inc/footer.jsp" %>

        <script src="../assets/js/jquery-2.1.4.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>
        <script src="../assets/js/orbi.js"></script>
        <script src="../assets/js/raphael.min.js" type="text/javascript" ></script>
        <script src="../assets/js/morris.min.js" type="text/javascript" ></script>
        <style type="text/css">
            .morris-hover{position:absolute;z-index:1000}.morris-hover.morris-default-style{border-radius:10px;padding:6px;color:#666;background:rgba(255,255,255,0.8);border:solid 2px rgba(230,230,230,0.8);font-family:sans-serif;font-size:12px;text-align:center}.morris-hover.morris-default-style .morris-hover-row-label{font-weight:bold;margin:0.25em 0}
            .morris-hover.morris-default-style .morris-hover-point{white-space:nowrap;margin:0.1em 0}
        </style>
        <script>


                                                        $(document).ready(function () {
                                                            //Hide Chart Diagram
                                                            $("#flotchart").hide();
                                                        });
                                                        function drawGraph(chartData) {

                                                            $("#refreshDIV").val('');
                                                            $("#refreshDIV").text('');

                                                            var purchaseID = $("#purchaseID").val();

                                                            var buttonHTML = '<button type="button" class="btn btn-lg btn-primary col-xs-12" onclick="showGraph(' + purchaseID + ', false);" style="float:center">Refresh</button>';

                                                            $("#refreshDIV").append(buttonHTML);

                                                            chartData.sort();
                                                            Morris.Line({
                                                                element: 'flotchart',
                                                                data: chartData,
                                                                xkey: 'time',
                                                                ykeys: ['value'],
                                                                labels: ['Value'],
                                                                parseTime: false,
                                                                ymin: 'auto'
                                                            });
                                                        }

                                                        //Is called when the user clicks the show graph icon located on the Action column
                                                        function showGraph(purchaseID, isNew) {

                                                            $("#purchaseID").val(purchaseID);

                                                            var variableType = $("#variables" + purchaseID).val();

                                                            if ($("#flotchart").is(":visible") && isNew) {

                                                                $("#refreshDIV").val('');
                                                                $("#refreshDIV").text('');

                                                                $("#flotchart").val("");
                                                                $("#flotchart").text("");
                                                                $("#flotchart").hide();

                                                            } else {

                                                                $("#flotchart").val("");
                                                                $("#flotchart").text("");

                                                                var chartData = [];

                                                                jQuery.ajax({
                                                                    featureClass: "P",
                                                                    style: "full",
                                                                    url: "retrieveHistory",
                                                                    dataType: "jsonp",
                                                                    contentType: "text/html; charset=UTF-8",
                                                                    encoding: "UTF-8",
                                                                    data: {
                                                                        format: "json",
                                                                        varType: variableType,
                                                                        id: purchaseID
                                                                    },
                                                                    success: function (data) {
                                                                        if (data.history !== null) {

                                                                            for (var i = 0; i < data.history.length; i++) {

                                                                                chartData.push({time: data.history[i].timestamp, value: data.history[i].metricValue});
                                                                            }

                                                                            chartData.sort();

                                                                            drawGraph(chartData);
                                                                            //Show Chart Diagram
                                                                            $("#flotchart").show();
                                                                            //

                                                                        } else {

                                                                            $('#flotchart').val("");
                                                                            $('#flotchart').text("");
                                                                            $('#flotchart').addClass("alert alert-danger");
                                                                            var notification = '<div class="text-center"><strong>Sorry!</strong> A problem has occured! Please try again...<br></div>';
                                                                            $('#flotchart').append(notification);
                                                                            $("#flotchart").show();
                                                                            setTimeout(function () {
                                                                                $('#flotchart').val("");
                                                                                $('#flotchart').text("");
                                                                                $('#flotchart').removeClass("alert alert-danger");
                                                                            }, 4000);
                                                                        }



                                                                    },
                                                                    error: function () {

                                                                        $('#flotchart').val("");
                                                                        $('#flotchart').text("");
                                                                        $('#flotchart').addClass("alert alert-danger");
                                                                        var notification = '<div class="text-center"><strong>Sorry!</strong> A problem has occured! Please try again...<br></div>';
                                                                        $('#flotchart').append(notification);
                                                                        $("#flotchart").show();
                                                                        setTimeout(function () {
                                                                            $('#flotchart').val("");
                                                                            $('#flotchart').text("");
                                                                            $('#flotchart').removeClass("alert alert-danger");
                                                                        }, 4000);
                                                                    }
                                                                });
                                                            }


                                                        }

        </script>

    </body>

</html>