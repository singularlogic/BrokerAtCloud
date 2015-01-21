<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
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

        <script src="../assets/js/jquery-2.1.0.min.js"></script>

        <!--<![endif]-->

        <!--[if IE]>
        
                <script src="../assets/js/jquery-1.11.0.min.js"></script>
        
        <![endif]-->

        <!--[if !IE]>-->

        <script type="text/javascript">
            window.jQuery || document.write("<script src='../assets/js/jquery-2.1.0.min.js'>" + "<" + "/script>");
        </script>

        <!--<![endif]-->

        <!--[if IE]>
        
                <script type="text/javascript">
                window.jQuery || document.write("<script src='../assets/js/jquery-1.11.0.min.js'>"+"<"+"/script>");
                </script>
                
        <![endif]-->
        <script src="../assets/js/jquery-migrate-1.2.1.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>

        <!-- page scripts -->
        <script src="../assets/js/jquery-ui-1.10.3.custom.min.js"></script>
        <script src="../assets/js/jquery.sparkline.min.js"></script>
        <script src="../assets/js/jquery.dataTables.min.js"></script>
        <script src="../assets/js/dataTables.bootstrap.min.js"></script>
        <script src="../assets/js/jquery.placeholder.min.js"></script>
        <script src="../assets/js/wizard.min.js"></script>

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
                                        <%= request.getUserPrincipal().getName()%>
                                    </span>
                                </div>
                            </a>
                            <ul class="dropdown-menu">
                                <!--                                <li>
                                                                    <a href="index.html#">
                                                                        <i class="fa fa-cog"></i>
                                                                        Profile Settings
                                                                    </a>
                                                                </li>-->
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
                                <a href="services">
                                    <i class="fa fa-tag" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Create Offering
                                    </span>
                                </a>
                            </li>
                            <li>
                                <a href="spoffers">
                                    <i class="fa fa-list-ul" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        View Offerings
                                    </span>
                                </a>
                            </li>

                            <li>
                                <a href="modifyIaaSProvider">
                                    <i class="fa fa-suitcase" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        IaaS Details
                                    </span>
                                </a>
                            </li>

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
                            <a href="#">
                                &nbsp;Orbi
                            </a>
                        </li>
                        <li>
                            Service Provider
                        </li>
                        <li class="active">
                            Create Offering
                        </li>                    
                    </ol>


                    <!-- panel 1 -->
                    <div class="row">
                        <div class="col-lg-12">

                            <div class="box">

                                <!-- PanelHeading -->
                                <div class="box-header" style="background-color: rgb(2, 185, 173); border-color: rgb(2,160,149); color: white;">
                                    <h2>
                                        <i class="fa fa-tags" style="color: white;"></i>
                                        <span class="break"></span>
                                        Service Offering
                                    </h2>
                                    <div class="box-icon">
                                        <a href="#" class="btn-minimize"><i class="fa fa-chevron-up" style="color: white;"></i></a>
                                        <a href="#" class="btn-close"><i class="fa fa-times" style="color: white;"></i></a>
                                    </div>
                                </div>
                                <!-- /PanelHeading -->

                                <!-- PanelBody -->
                                <div class="box-content clearfix">

                                    <div class="col-lg-12">
                                        <div id="MyWizard" class="wizard">
                                            <ul class="steps">
                                                <li data-target="#step1" id="step1Button" class="active"><span class="badge badge-info">1</span><span class="chevron"></span>Step 1</li>
                                                <li data-target="#step2" id="step2Button"><span class="badge">2</span><span class="chevron"></span>Step 2</li>
                                                <li data-target="#step3" id="step3Button"><span class="badge">3</span><span class="chevron"></span>Step 3</li>
                                            </ul>
                                            <div class="actions" style="padding: 0px;">
                                                <button id="previousButton" type="button" class="btn btn-info" onclick="previousStep();" disabled> <i class="fa fa-arrow-left"></i> Prev</button>
                                                <button id="nextButton" type="button" class="btn btn-info" onclick="nextStep();" data-last="Finish">Next <i class="fa fa-arrow-right"></i></button>
                                                <input type="hidden" value="1" id="activeStep"/>
                                                <input type="hidden" value="" id="currentPolicy"/>
                                                <input type="hidden" value="" id="currentSLP"/>
                                                <input type="hidden" value="" id="currentVars"/>
                                            </div>
                                        </div>

                                        <div class="step-content">

                                            <!-- S T E P  (1)  -->
                                            <div class="step-pane active" id="step1">
                                                <form class="form-horizontal register" action="#" method="post">
                                                    <fieldset class="col-lg-12">

                                                        <div class="text-center">
                                                            <h2><b></b></h2>
                                                        </div>
                                                        <br>

                                                        <div id="notificationMSG1">

                                                        </div>	

                                                        <div class="row">

                                                            <div class="col-lg-6" style="padding-left: 0px; padding-right: 10px;">
                                                                <div class="form-group" style="margin: 0px;">
                                                                    <label class="control-label" id="ServicePolicy1">
                                                                        Broker Policy
                                                                        <span style="color:red">
                                                                            &nbsp;*
                                                                        </span>
                                                                    </label>
                                                                    <br>
                                                                    <div class="controls">
                                                                        <select id="policy" name="policy" class="form-control">
                                                                            <option value="0">
                                                                                select policy...
                                                                            </option>
                                                                            <c:forEach items="${policies}" var="policy">
                                                                                <option value="${policy.id}">${policy.name}</option>
                                                                            </c:forEach>
                                                                        </select>
                                                                    </div>
                                                                </div>
                                                            </div>

                                                            <div class="col-lg-6" style="padding-left: 10px; padding-right: 0px;">
                                                                <div class="form-group" style="margin: 0px;">
                                                                    <label class="control-label" id="ServiceLevelProfile1">
                                                                        Service Level Profile
                                                                        <span style="color:red">
                                                                            &nbsp;*
                                                                        </span>
                                                                    </label>
                                                                    <br>
                                                                    <div class="controls">
                                                                        <select id="serviceLevelProfile" name="serviceLevelProfile" class="form-control">
                                                                            <option value ="0">
                                                                                select service level profile...
                                                                            </option>
                                                                        </select>
                                                                    </div>
                                                                </div>
                                                            </div>

                                                        </div>

                                                        <div class="row">&nbsp;</div>

                                                    </fieldset>	
                                            </div>
                                            <!-- / S T E P  (1)  -->

                                            <!-- S T E P  (2)  -->
                                            <div class="step-pane" id="step2">
                                                <form class="form-horizontal">
                                                    <fieldset class="col-sm-12">

                                                        <div class="text-center">
                                                            <h2><b></b></h2>
                                                        </div>

                                                        <br>

                                                        <div id="notificationMSG2">

                                                        </div>	

                                                        <div class="row">

                                                            <div class="col-lg-3">&nbsp;</div>

                                                            <div class="col-lg-6" style="padding-left: 0px; padding-right: 0px;">
                                                                <div id="variablesDIV" class="form-group" style="margin: 0px;">


                                                                </div>
                                                            </div>

                                                            <div class="col-gl-3">&nbsp;</div>

                                                        </div>

                                                    </fieldset>	
                                                </form>
                                            </div>
                                            <!-- / S T E P  (2)  -->

                                            <!-- S T E P  (3)  -->                                                            
                                            <div class="step-pane" id="step3">
                                                <fieldset class="col-sm-12">
                                                    <form class="form-horizontal">

                                                        <div id="notificationMSG3">

                                                        </div>	

                                                        <div class="col-lg-12" style="padding-left: 400px; padding-right: 400px;">

                                                            <div class="row">
                                                                <div class="col-lg-4" >
                                                                    <label>Offering name: </label>
                                                                </div>
                                                                <div class="input-group col-sm-8">
                                                                    <input type="text" size="30" name="offerName" id="offerName" class="form-control" placeholder=""/>
                                                                </div>
                                                            </div>
                                                            <br>

                                                        </div>
                                                        <!--  Create Button  -->
                                                        <div class="col-lg-12">
                                                            <div class="text-center">
                                                                <!--div class="btn" style="background-color: rgb(238, 44 , 67); color: white; margin-bottom: 10px; width: 150px;">
                                                                    <strong>
                                                                        Cancel
                                                                    </strong>
                                                                </div>
                                                                &nbsp;-->
                                                                <div id="validationButton" class="btn btn-primary btn-setting" style="background-color: rgb(141, 199 , 72); color: white; margin-bottom: 10px;">
                                                                    <strong>
                                                                        Validate &AMP; Create Service Offering
                                                                    </strong>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <!--  / Create Button  -->

                                                    </form>	
                                                </fieldset>	
                                            </div>
                                            <!-- /S T E P  (3)  -->                                         


                                        </div>

                                    </div>




                                    <br>
                                    <div class="row">&nbsp;</div>
                                    <br>


                                </div>

                            </div>
                        </div>
                    </div>
                    <!-- /panel 1 -->

                </div><!-- /row -->

            </div>
        </div>

        <div class="modal fade" id="myModal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true" id="closeButton">&times;</button>
                        <h4 class="modal-title">Please wait...</h4>
                    </div>
                    <div class="modal-body">
                        <br>
                        <div class="progress progress-striped active">
                            <div class="progress-bar"  role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 45%">
                                <span class="sr-only">45% Complete</span>
                            </div>
                        </div>
                        <br>
                    </div>
                </div>
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->

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

        <script>

            $(document).ready(function () {

                var variableIDs = [];

                $('#policy').change(function () {

                    var str = "";
                    $("#policy option:selected").each(function () {
                        if ($(this).val() !== null && $(this).val() > 0) {
                            str = $(this).val();
                            jQuery.ajax({
                                featureClass: "P",
                                style: "full",
                                url: "retrieveServiceProfiles",
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
                                    if (data.serviceLevels !== null) {
                                        $('#serviceLevelProfile').val("");
                                        $('#serviceLevelProfile').text("");
                                        $('#serviceLevelProfile').append($('<option/>', {
                                            text: 'select service level profile...',
                                            value: '0'
                                        }));
                                        for (var i = 0; i < data.serviceLevels.length; i++) {


                                            $('#serviceLevelProfile').append($('<option/>', {
                                                value: data.serviceLevels[i].id,
                                                text: data.serviceLevels[i].name
                                            }));
                                        }
                                    } else {

                                        alert("Problem Occured!");
                                    }
                                },
                                error: function () {
                                    alert("Problem Occured!");
                                }
                            });

                        }//Clear Profiles Select Box
                        else {
                            $('#serviceLevelProfile').val("");
                            $('#serviceLevelProfile').text("");
                            $('#serviceLevelProfile').append($('<option/>', {
                                text: 'select service level profile...'
                            }));
                        }
                    });


                });

                $('#serviceLevelProfile').change(function () {

                    var policyID = $("#policy option:selected").val();
                    $('#currentPolicy').val(policyID);
                    var profileID = "";

                    $("#serviceLevelProfile option:selected").each(function () {
                        if ($(this).val() !== null && $(this).val() > 0) {
                            profileID = $(this).val();
                            $('#currentSLP').val(profileID);

                            jQuery.ajax({
                                featureClass: "P",
                                style: "full",
                                url: "retrieveVariables",
                                dataType: "jsonp",
                                contentType: "text/html; charset=UTF-8",
                                encoding: "UTF-8",
                                data: {
                                    format: "json",
                                    policy: policyID,
                                    slp: profileID
                                },
                                success: function (data) {
                                    if (data.variables !== null) {

                                        var variablesSTR = "";
                                        var varsSTR = "";

                                        variableIDs.slice(0, variableIDs.length);

                                        for (var i = 0; i < data.variables.length; i++) {

                                            variablesSTR += '<div class="row"><div class="col-lg-4"><label>' + data.variables[i].name + ': </label></div><div class="input-group col-sm-8">' +
                                                    '<input type="text" size="30" name="' + data.variables[i].id + '" id="' + data.variables[i].id + '" class="form-control" placeholder="use ' + data.variables[i].type + ' (' + data.variables[i].metric + ')"/></div></div><br>';

                                            varsSTR += data.variables[i].id + ',';
                                            variableIDs.push(data.variables[i].id);
                                        }

                                        $('#currentVars').val(varsSTR);

                                        $("#variablesDIV").val("");
                                        $("#variablesDIV").text("");
                                        $("#variablesDIV").append(variablesSTR);

                                    } else {

                                        alert("Problem Occured!");
                                    }

                                },
                                error: function () {
                                    alert("Problem Occured!");
                                }
                            });

                        }

                    });


                });

                $('#validationButton').click(function () {
                    var offer = $('#offerName').val();

                    if (offer !== "") {
                        var varIDs = "";

                        for (var i = 0; i < variableIDs.length; i++) {
                            var id = "";
                            id = variableIDs[i];
                            var tempVar = $('#' + id).val();

                            varIDs += variableIDs[i] + '|' + tempVar + '^';

                        }

                        var currentPolicy = $('#currentPolicy').val();
                        var currrentSLP = $('#currentSLP').val();

                        jQuery.ajax({
                            featureClass: "P",
                            style: "full",
                            url: "validateAndCreateSO",
                            dataType: "jsonp",
                            contentType: "text/html; charset=UTF-8",
                            encoding: "UTF-8",
                            data: {
                                format: "json",
                                policy: currentPolicy,
                                slp: currrentSLP,
                                variables: varIDs,
                                offerName: offer
                            },
                            success: function (data) {
                                $("#closeButton").click();
                                if (data.message !== null) {

                                    if (data.message === "SUCCESS") {

                                        $("#variablesDIV").val("");
                                        $("#variablesDIV").text("");

                                        $('#serviceLevelProfile').val("");
                                        $('#serviceLevelProfile').text("");
                                        $('#serviceLevelProfile').append($('<option/>', {
                                            text: 'select service level profile...'
                                        }));

                                        $('#notificationMSG3').val("");
                                        $('#notificationMSG3').text("");
                                        $('#notificationMSG3').addClass("alert alert-success");

                                        var notification = '<div class="text-center"><strong>Congratulations!</strong> Your Service Offering has been successfully created! Please wait...<br></div>';

                                        $('#notificationMSG3').append(notification);

                                        setTimeout(function () {
                                            window.location.href = "spoffers";
                                        }, 3000);

                                    } else {
                                        $("#variablesDIV").val("");
                                        $("#variablesDIV").text("");

                                        $('#serviceLevelProfile').val("");
                                        $('#serviceLevelProfile').text("");
                                        $('#serviceLevelProfile').append($('<option/>', {
                                            text: 'select service level profile...'
                                        }));

                                        $('#notificationMSG3').val("");
                                        $('#notificationMSG3').text("");
                                        $('#notificationMSG3').addClass("alert alert-danger");

                                        var notification = '<div class="text-center"><strong>Sorry!</strong> An error occured! Please wait and try again...<br></div>';

                                        $('#notificationMSG3').append(notification);

                                        setTimeout(function () {
                                            window.location.href = "spoffers";
                                        }, 3000);

                                    }

                                } else {

                                    $('#notificationMSG3').val("");
                                    $('#notificationMSG3').text("");
                                    $('#notificationMSG3').addClass("alert alert-danger");

                                    var notification = '<div class="text-center"><strong>Sorry!</strong> An error occured! Please try again...<br></div>';

                                    $('#notificationMSG3').append(notification);

                                    setTimeout(function () {
                                        $('#notificationMSG3').val("");
                                        $('#notificationMSG3').text("");
                                        $('#notificationMSG3').removeClass("alert alert-danger");
                                    }, 2000);
                                }

                            },
                            error: function () {
                                $("#closeButton").click();
                                $('#notificationMSG3').val("");
                                $('#notificationMSG3').text("");
                                $('#notificationMSG3').addClass("alert alert-danger");

                                var notification = '<div class="text-center"><strong>Sorry!</strong> An error occured! Please try again...<br></div>';

                                $('#notificationMSG3').append(notification);

                                setTimeout(function () {
                                    $('#notificationMSG3').val("");
                                    $('#notificationMSG3').text("");
                                    $('#notificationMSG3').removeClass("alert alert-danger");
                                }, 2000);
                            }
                        });
                    } else {
                        $("#closeButton").click();
                        $('#notificationMSG3').val("");
                        $('#notificationMSG3').text("");
                        $('#notificationMSG3').addClass("alert alert-danger");

                        var notification = '<div class="text-center"><strong>Sorry!</strong> An Offering Name is required. Please provide one!<br></div>';

                        $('#notificationMSG3').append(notification);

                        setTimeout(function () {
                            $('#notificationMSG3').val("");
                            $('#notificationMSG3').text("");
                            $('#notificationMSG3').removeClass("alert alert-danger");
                        }, 3000);
                    }

                });

            });

            function nextStep() {
                var activeStep = $("#activeStep").val();
                if (activeStep === "1") {

                    var policy = $('#policy').val();
                    var slp = $('#serviceLevelProfile').val();

                    if (policy === "0") {
                        $('#notificationMSG1').val("");
                        $('#notificationMSG1').text("");
                        $('#notificationMSG1').addClass("alert alert-danger");

                        var notification = '<div class="text-center"><strong>Sorry!</strong> You have to choose a Broker Policy and a Service Level Profile!<br></div>';

                        $('#notificationMSG1').append(notification);

                        setTimeout(function () {
                            $('#notificationMSG1').val("");
                            $('#notificationMSG1').text("");
                            $('#notificationMSG1').removeClass("alert alert-danger");
                        }, 4000);

                    } else {
                        if (slp === "0") {
                            $('#notificationMSG1').val("");
                            $('#notificationMSG1').text("");
                            $('#notificationMSG1').addClass("alert alert-danger");

                            var notification = '<div class="text-center"><strong>Sorry!</strong> You have to choose a Broker Policy and a Service Level Profile!<br></div>';

                            $('#notificationMSG1').append(notification);

                            setTimeout(function () {
                                $('#notificationMSG1').val("");
                                $('#notificationMSG1').text("");
                                $('#notificationMSG1').removeClass("alert alert-danger");
                            }, 4000);
                        } else {

                            $("#step1Button").removeClass("active");
                            $("#step2Button").addClass("active");
                            $("#previousButton").removeAttr('disabled');
                            $("#step1").removeClass("active");
                            $("#step2").addClass("active");
                            $("#activeStep").val("2");
                        }
                    }
                } else if (activeStep === "2") {
                    var validationObject = "";
                    var variables = $('#currentVars').val();
                    var allOK = true;

                    var varArray = variables.split(",");

                    for (var i = 0; i < varArray.length; i++) {
                        var id = varArray[i];
                        if (id !== "") {
                            var tempVar = $('#' + id).val();
                            var tempPlaceholder = $('#' + id).attr('placeholder');
                            if (tempVar === "") {
                                allOK = false;
                                break;
                            } else {
                                validationObject += varArray[i] + '|' + tempVar + '|' + tempPlaceholder + '^';
                            }

                        }
                    }

                    if (allOK) {

                        jQuery.ajax({
                            featureClass: "P",
                            style: "full",
                            url: "validateVariables",
                            dataType: "jsonp",
                            contentType: "text/html; charset=UTF-8",
                            encoding: "UTF-8",
                            data: {
                                format: "json",
                                validationObject: validationObject
                            },
                            success: function (data) {
                                if (data.message !== null) {
                                    if (data.message === "SUCCESS") {
                                        $("#step2Button").removeClass("active");
                                        $("#step3Button").addClass("active");
                                        $("#step2").removeClass("active");
                                        $("#step3").addClass("active");
                                        $("#nextButton").prop('disabled', true);
                                        $("#activeStep").val("3");
                                    } else {

                                        $('#notificationMSG2').val("");
                                        $('#notificationMSG2').text("");
                                        $('#notificationMSG2').addClass("alert alert-danger");

                                        var notification = '<div class="text-center"><strong>Sorry!</strong> Validation Error! Please correct the variable values!<br></div>';

                                        $('#notificationMSG2').append(notification);

                                        setTimeout(function () {
                                            $('#notificationMSG2').val("");
                                            $('#notificationMSG2').text("");
                                            $('#notificationMSG2').removeClass("alert alert-danger");
                                        }, 4000);
                                    }
                                } else {

                                    $('#notificationMSG2').val("");
                                    $('#notificationMSG2').text("");
                                    $('#notificationMSG2').addClass("alert alert-danger");

                                    var notification = '<div class="text-center"><strong>Sorry!</strong> A problem has occured! Please try again...<br></div>';

                                    $('#notificationMSG2').append(notification);

                                    setTimeout(function () {
                                        $('#notificationMSG2').val("");
                                        $('#notificationMSG2').text("");
                                        $('#notificationMSG2').removeClass("alert alert-danger");
                                    }, 4000);
                                }
                            },
                            error: function () {

                                $('#notificationMSG2').val("");
                                $('#notificationMSG2').text("");
                                $('#notificationMSG2').addClass("alert alert-danger");

                                var notification = '<div class="text-center"><strong>Sorry!</strong> A problem has occured! Please try again...<br></div>';

                                $('#notificationMSG2').append(notification);

                                setTimeout(function () {
                                    $('#notificationMSG2').val("");
                                    $('#notificationMSG2').text("");
                                    $('#notificationMSG2').removeClass("alert alert-danger");
                                }, 4000);
                            }
                        });

                    } else {

                        $('#notificationMSG2').val("");
                        $('#notificationMSG2').text("");
                        $('#notificationMSG2').addClass("alert alert-danger");

                        var notification = '<div class="text-center"><strong>Sorry!</strong> Please complete all the Variables!<br></div>';

                        $('#notificationMSG2').append(notification);

                        setTimeout(function () {
                            $('#notificationMSG2').val("");
                            $('#notificationMSG2').text("");
                            $('#notificationMSG2').removeClass("alert alert-danger");
                        }, 4000);
                    }

                } else {

                }
            }
            ;

            function previousStep() {
                var activeStep = $("#activeStep").val();
                if (activeStep === "2") {
                    $("#step2Button").removeClass("active");
                    $("#step1Button").addClass("active");
                    $("#step2").removeClass("active");
                    $("#step1").addClass("active");
                    $("#previousButton").prop('disabled', true);
                    $("#activeStep").val("1");
                } else if (activeStep === "3") {
                    $("#step3Button").removeClass("active");
                    $("#step2Button").addClass("active");
                    $("#step3").removeClass("active");
                    $("#step2").addClass("active");
                    $("#nextButton").removeAttr('disabled');
                    $("#activeStep").val("2");
                }
            }
            ;

        </script>

    </body>
</html>