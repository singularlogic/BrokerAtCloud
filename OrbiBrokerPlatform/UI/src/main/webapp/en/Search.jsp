<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
            window.jQuery || document.write("<script src='../assets/js/jquery-2.0.3.min.js'>" + "<" + "/script>");
        </script>

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

        <!-- end: JavaScript-->

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
                                        ${username}
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

                            <li class="active">
                                <a href="search">
                                    <i class="fa fa-search" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Search
                                    </span>
                                </a>
                            </li>
                            <li>
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
                            Search
                        </li>                    
                    </ol>


                    <!-- panel 1 -->
                    <div class="row">
                        <div class="col-lg-12">

                            <div class="box">

                                <!-- PanelHeading -->
                                <div class="box-header" style="background-color: rgb(2, 185, 173); border-color: rgb(2,160,149); color: white;">
                                    <h2>
                                        <i class="fa fa-search" style="color: white;"></i>
                                        <span class="break"></span>
                                        Search Service
                                    </h2>
                                    <div class="box-icon">
                                        <a href="#" class="btn-minimize"><i class="fa fa-chevron-up" style="color: white;"></i></a>
                                        <a href="#" class="btn-close"><i class="fa fa-times" style="color: white;"></i></a>
                                    </div>
                                </div>

                                <div class="box-content clearfix">

                                    <div class="form-group" style="padding-left: 15px; padding-top: 5px;">

                                        <br><br>

                                        <div class="row">

                                            <div class="col-lg-6" style="padding-left: 0px; padding-right: 10px;">
                                                <div class="form-group" style="margin: 0px;">
                                                    <label class="control-label" id="ServicePolicy1">
                                                        Broker Policy
                                                    </label>
                                                    <br>

                                                    <div class="controls">
                                                        <select id="policy" name="policy" class="form-control">
                                                            <option value="0">
                                                                select policy...
                                                            </option>
                                                            <c:forEach items="${allPolicies}" var="policy">
                                                                <option value="${policy.id}">${policy.name}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <br>
                                        <br>


                                        <div class="row" id="variablesDIV">

                                        </div>




                                    </div>

                                    <!-- Search button -->
                                    <div class="col-lg-12">
                                        <div class="text-right">
                                            <div class="btn btn-info" id="SearchBTN" style="color: white; margin-bottom: 10px; width: 150px;">
                                                <strong>
                                                    Search
                                                </strong>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- /Search button -->

                                    <br>

                                    <!-- SearchResults-->
                                    <div class="row" id="SearchResults" style="display: none;">
                                        <div id="notificationMSG">

                                        </div>
                                        <div class="text-left" style="padding-left: 30px;">
                                            <h2>
                                                <strong>
                                                    Service Offerings
                                                </strong>
                                            </h2>
                                        </div>

                                        <br>

                                        <div class="col-lg-12" style="padding-left: 30px; padding-right: 30px;">

                                            <table class="table table-striped table-bordered bootstrap-datatable datatable">

                                                <thead>
                                                    <tr>
                                                        <th class="col-lg-5">
                                                            Service Providers
                                                        </th>
                                                        <th class="col-lg-5">
                                                            Service Offerings
                                                        </th>
                                                        <th class="col-lg-2">
                                                            Select
                                                        </th>
                                                    </tr>
                                                </thead>   

                                                <tbody id="resultsTable">


                                                </tbody>

                                            </table>
                                            <br>

                                        </div>
                                        <!-- /SearchResults -->



                                    </div>

                                </div>
                            </div>
                        </div>
                        <!-- /panel 1 -->

                    </div>

                </div>
            </div>

            <div class="clearfix"></div>

        </div>

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
        <script>
            $(document).ready(function () {

                $('#policy').change(function () {

                    var str = "";
                    $("#policy option:selected").each(function () {
                        if ($(this).val() !== null && $(this).val() > 0) {
                            str = $(this).val();
                            jQuery.ajax({
                                featureClass: "P",
                                style: "full",
                                url: "retrievePolicyVariables",
                                dataType: "jsonp",
                                contentType: "text/html; charset=UTF-8",
                                encoding: "UTF-8",
                                beforeSend: function (xhr) {
                                    xhr.setRequestHeader("Content-Type", "text/html;charset=utf-8");
                                },
                                data: {
                                    format: "json",
                                    policy: str
                                },
                                success: function (data) {

                                    // Check if the resultset contains any data 
                                    if (data.variables !== null) {
                                        $('#variablesDIV').val("");
                                        $('#variablesDIV').text("");
                                        var vars = '';
                                        for (var i = 0; i < data.variables.length; i++) {
                                            vars += '<div class="row"><div class="col-lg-4"><label>' + data.variables[i].name + ': </label></div>'
                                                    + '<div class="input-group col-sm-4"><input type="text" name="' + data.variables[i].id + '" id="' + data.variables[i].id + '"'
                                                    + 'class="form-control" placeholder=""/></div></div><br>';
                                        }

                                        $('#variablesDIV').append(vars);

                                    } else {

                                        alert("Problem Occured!");
                                    }
                                },
                                error: function () {
                                    alert("Problem Occured!");
                                }
                            });

                        }//Clear Variables DIV
                        else {
                            $('#variablesDIV').val("");
                            $('#variablesDIV').text("");
                        }
                    });

                });


                $("#SearchBTN").click(function () {

                    var policyID = $('#policy option:selected').val();

                    jQuery.ajax({
                        featureClass: "P",
                        style: "full",
                        url: "retrieveServiceOffers",
                        dataType: "jsonp",
                        contentType: "text/html; charset=UTF-8",
                        encoding: "UTF-8",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("Content-Type", "text/html;charset=utf-8");
                        },
                        data: {
                            format: "json",
                            policy: policyID
                        },
                        success: function (data) {

                            // Check if the resultset contains any data 
                            if (data.offers !== null) {

                                if (data.offers.length > 0) {
                                    $('#notificationMSG').val("");
                                    $('#notificationMSG').text("");
                                    $('#notificationMSG').removeClass("alert alert-warning");
                                    $('#resultsTable').val("");
                                    $('#resultsTable').text("");
                                    var offers = "";
                                    for (var i = 0; i < data.offers.length; i++) {
                                        offers += '<tr><td>' + data.offers[i].provider + '</td><td>' + data.offers[i].name + '</td><td>'
                                                + '<div class="text-center"><div class="btn btn-sm btn-success" id="purchaseButton" onclick="purchase(' + data.offers[i].id + ')" style="color: white; margin-bottom: 10px;"><strong>'
                                                + 'Purchase</strong></div></div></td></tr>';
                                    }

                                    $('#resultsTable').append(offers);
                                    $("#SearchResults").show();
                                }
                                else
                                {
                                    $('#resultsTable').val("");
                                    $('#resultsTable').text("");
                                    $('#notificationMSG').val("");
                                    $('#notificationMSG').text("");
                                    $('#notificationMSG').addClass("alert alert-warning");
                                    var notification = '<div class="text-center"><strong>Sorry!</strong> No Service Offerings found!<br></div>';
                                    $('#notificationMSG').append(notification);
                                    $("#SearchResults").show();
                                }
                            } else {

                                alert("Problem Occured!");
                            }
                        },
                        error: function () {
                            alert("Problem Occured!");
                        }
                    });

                });

            });


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
                    url: "purchaseSO",
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
                                    window.location.href = "runningservices";
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