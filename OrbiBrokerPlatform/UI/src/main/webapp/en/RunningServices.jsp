<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Broker@Cloud - Home Page</title>

        <link rel="shortcut icon" type="image/png" href="../favicon.png"/>

        <!-- start: CSS -->
        <link href="../assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="../assets/css/style.min.css" rel="stylesheet">
        <link href="../assets/css/retina.min.css" rel="stylesheet">
        <link href="../assets/css/print.css" rel="stylesheet" type="text/css" media="print"/>
        <!-- end: CSS -->

        <!-- start: JavaScript-->
        <!--[if !IE]>-->

        <script src="../assets/js/jquery-2.0.3.min.js"></script>


        <!--<![endif]-->

        <!--[if IE]>
        
                <script src="../assets/js/jquery-1.10.2.min.js"></script>
        
        <![endif]-->

        <!--[if !IE]>-->

        <script type="text/javascript">
            window.jQuery || document.write("<script src='../assets/js/jquery-2.0.3.min.js'>" + "<" + "/script>");</script>

        <!--<![endif]-->

        <!--[if IE]>
        
                <script type="text/javascript">
                window.jQuery || document.write("<script src='../assets/js/jquery-1.10.2.min.js'>"+"<"+"/script>");
                </script>
                
        <![endif]-->
        <script src="../assets/js/jquery-migrate-1.2.1.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>


        <!-- page scripts -->
        <script src="../assets/js/jquery-ui-1.10.3.custom.min.js"></script>
        <script src="../assets/js/jquery.sparkline.min.js"></script>
        <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../assets/js/excanvas.min.js"></script><![endif]-->
        <script src="../assets/js/jquery.knob.modified.min.js"></script>
        <script src="../assets/js/jquery.easy-pie-chart.min.js"></script>
        <script src="../assets/js/raphael.min.js"></script>
        <script src="../assets/js/justgage.1.0.1.min.js"></script>
        <script src="../assets/js/jquery.dataTables.min.js"></script>
        <script src="../assets/js/dataTables.bootstrap.min.js"></script>

        <!-- theme scripts -->
        <script src="../assets/js/custom.min.js"></script>
        <script src="../assets/js/core.min.js"></script>

        <!-- inline scripts related to this page -->
        <script src="../assets/js/pages/table.js"></script>

    </head>
    <body>


        <!-- HeaderBar -->
        <header class="navbar">
            <div class="container">
                <button class="navbar-toggle" type="button" data-toggle="collapse" data-target=".sidebar-nav.nav-collapse">
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a id="main-menu-toggle" class="hidden-xs open" style="padding: 0px;">
                    <img src="../Orbi - Logo - transp 2.png" height="40" alt="">
                </a>		
                <a class="navbar-brand col-md-2 col-sm-1 col-xs-2" href="#">
                    <span>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        Orbi - Broker
                    </span>
                </a>
                <div id="search" class="col-sm-5 col-xs-8 col-lg-3" style="margin:8px 0 0px 15%">
                    <strong>
                        <a href="http://portal.singularlogic.eu/">
                            <em>powered by</em>&nbsp;&nbsp;SingularLogic
                        </a>
                    </strong>
                </div>


                <!-- HeaderMenu -->
                <div class="nav-no-collapse header-nav">
                    <ul class="nav navbar-nav pull-right">


                        <!-- HeaderUserDropdown -->
                        <li class="dropdown">
                            <a class="btn account dropdown-toggle" data-toggle="dropdown" href="#">
                                <div class="avatar">
                                </div>
                                <div class="user">
                                    <span class="hello">
                                        Welcome!
                                    </span>
                                    <span class="name">
                                        <%= request.getUserPrincipal().getName()%>
                                    </span>
                                </div>
                            </a>
                            <ul class="dropdown-menu">
                                <!--li>
                                    <a href="index.html#">
                                        <i class="fa fa-cog"></i>
                                        Profile Settings
                                    </a>
                                </li-->
                                <li>
                                    <a href="../logout.jsp">
                                        <i class="fa fa-sign-out"></i> 
                                        Log Out
                                    </a>
                                </li>
                            </ul>
                        </li>
                        <!-- HeaderUserDropdown -->
                    </ul>
                </div>
                <!-- /HeaderMenu -->

            </div>
        </header>
        <!-- /HeaderBar -->


        <div class="container" style="font-family: arial">
            <div class="row">


                <!-- start: Main Menu -->
                <div id="sidebar-left" class="col-lg-2 col-sm-1">

                    <div class="sidebar-nav nav-collapse collapse navbar-collapse">

                        <ul class="nav main-menu">

                            <li>
                                <a href="search">
                                    <i class="fa fa-search" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Search
                                    </span>
                                </a>
                            </li>
                            <li class="active">
                                <a href="runningservices">
                                    <i class="fa fa-flash" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Running Services
                                    </span>
                                </a>
                            </li>
                            <!--li>
                                <a href="Notification.jsp">
                                    <i class="fa fa-globe" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Notifications
                                    </span>
                                </a>
                            </li-->

                        </ul>
                    </div>

                    <a href="#" id="main-menu-min" class="full visible-md visible-lg">
                        <i class="fa fa-angle-double-left"></i>
                    </a>

                </div>
                <!-- end: Main Menu -->


                <!-- start: Content -->
                <div id="content" class="col-lg-10 col-sm-11 ">


                    <ol class="breadcrumb">
                        <li>
                            <a href="uHomePage.jsp">
                                &nbsp;Orbi
                            </a>
                        </li>
                        <li class="active" >
                            Running Services
                        </li>                    
                    </ol>


                    <!-- panel 1 -->
                    <div class="row">
                        <div class="col-lg-12">

                            <div class="box">

                                <!-- PanelHeading -->
                                <div class="box-header" style="background-color: rgb(2, 185, 173); border-color: rgb(2,160,149); color: white;">
                                    <h2>
                                        <i class="fa fa-flash" style="color: white;"></i>
                                        <span class="break"></span>
                                        Running Services
                                    </h2>
                                    <div class="box-icon">
                                        <a href="#" class="btn-minimize"><i class="fa fa-chevron-up" style="color: white;"></i></a>
                                        <a href="#" class="btn-close"><i class="fa fa-times" style="color: white;"></i></a>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${numOfpurchases == '0'}">
                                        <br />
                                        <div class="text-center alert alert-warning"><strong>Sorry!</strong> You haven't purchased any Service Offering!<br></div>
                                        </c:when>
                                        <c:otherwise>


                                        <div class="box-content clearfix">

                                            <div class="row">

                                                <div class="col-lg-12">

                                                    <table class="table table-striped table-bordered bootstrap-datatable datatable">

                                                        <thead>
                                                            <tr>
                                                                <th align="center">
                                                                    Service Description
                                                                </th>
                                                                <th align="center">
                                                                    Service Profile
                                                                </th>
                                                                <th align="center">
                                                                    Broker Policy
                                                                </th>
                                                                <th align="center">
                                                                    Date Purchased
                                                                </th>
                                                                <th align="center">
                                                                    Offered by
                                                                </th>
                                                                <th align="center">
                                                                    Variables
                                                                </th>
                                                                <th align="center">
                                                                    PublicIP
                                                                </th>
                                                                <th align="center">
                                                                    Actions
                                                                </th>
                                                            </tr>
                                                        </thead>   

                                                        <tbody>
                                                            <c:forEach items="${purchases}" var="purchase">
                                                                <tr>
                                                                    <td align="center">
                                                                        ${purchase.name}
                                                                    </td>
                                                                    <td align="center">
                                                                        ${purchase.profileName}
                                                                    </td>
                                                                    <td align="center">
                                                                        ${purchase.policyName}
                                                                    </td>
                                                                    <td align="center">
                                                                        ${fn:substring(purchase.datePurchased, 0, 19)}
                                                                    </td>
                                                                    <td align="center"> 
                                                                        ${purchase.providerName}
                                                                    </td>
                                                                    <td align="center"> 
                                                                        <select id="variables${purchase.purchaseID}" name="variables${purchase.purchaseID}">
                                                                            <c:forEach items="${purchase.variables}" var="var">
                                                                                <option value="${var.id}">${var.name}</option>
                                                                            </c:forEach>
                                                                        </select>
                                                                    </td>
                                                                    <td align="center"> 
                                                                        ${purchase.publicIP}
                                                                    </td>
                                                                    <td>
                                                                        <div class="text-center">
                                                                            <a  onClick="showGraph('<c:out value="${purchase.purchaseID}"></c:out>', true);" title="View Graph" data-rel="tooltip" id="ViewBtn1" class="btn btn-xs btn-info" style="margin-top: 5px;">
                                                                                    <i class="fa fa-bar-chart-o"></i>
                                                                                </a>    
                                                                                <a title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" href="runningservices?remove=<c:out value="${purchase.purchaseID}"></c:out>"> <i class="fa fa-trash-o"></i></a>        


                                                                            </div>
                                                                        </td>
                                                                    </tr>
                                                            </c:forEach>
                                                        </tbody>

                                                    </table>
                                                </div>

                                                <div class="col-lg-2">&nbsp;</div>
                                            </div>

                                            <br><br>
                                            <input type ="hidden" name="purchaseID" id="purchaseID" />
                                            <div id="refreshDIV" class = "controls">

                                            </div>
                                            <div class="box-content">
                                                <div id="flotchart" class="center" style="height:300px;width:100%;"></div>
                                            </div>

                                        </div>

                                    </c:otherwise>
                                </c:choose>



                            </div>
                        </div>
                    </div>
                    <!-- /panel 1 -->

                </div>

            </div>
        </div>


        <div class="clearfix"></div>


        <!-- Footer -->
        <footer>
            <p>
                <span style="text-align:left;float:left">
                    &copy; Orbi
                    <a href="#">
                        Broker@Cloud
                    </a>
                </span>
                <span class="hidden-phone" style="text-align:right;float:right">
                    Powered by: 
                    <a href="http://portal.singularlogic.eu/">
                        SingularLogic
                    </a>
                </span>
            </p>

        </footer>
        <!-- /Footer -->

        <script type="text/javascript" src="../assets/js/raphael.min.js"></script>
        <script type="text/javascript" src="../assets/js/morris.min.js"></script>

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


