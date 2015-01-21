<%@page import="org.broker.orbi.ui.Policy"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.broker.orbi.ui.PolicyVariable"%>
<%@page import="org.broker.orbi.ui.ServiceLevelProfile"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Broker@Cloud - Manage Policies</title>

        <link rel="shortcut icon" type="image/png" href="../favicon.png"/>

        <!-- start: CSS -->

        <link href="../assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="../assets/css/style.min.css" rel="stylesheet">
        <link href="../assets/css/retina.min.css" rel="stylesheet">
        <link href="../assets/css/print.css" rel="stylesheet" type="text/css" media="print"/>


        <!-- end: CSS -->

        <!-- start: JavaScript-->
        <!--[if !IE]>-->

        <!--        <script src="../assets/js/jquery-2.1.0.min.js"></script>-->

        <script src="../assets/js/jquerytest.js"></script>



        <!--<![endif]-->

        <!--[if IE]>
        
                <script src="../assets/js/jquery-1.11.0.min.js"></script>
        
        <![endif]-->

        <!--[if !IE]>-->

        <script type="text/javascript">
            window.jQuery || document.write("<script src='../assets/js/jquery-2.1.0.min.js'>" + "<" + "/script>");</script>

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

        <!--        <script src="../assets/js/jquery.dataTables.min.js"></script>-->




        <script type="text/javascript" language="javascript" src="../assets/js/jquerydatatablestest.js"></script>



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
                                        <%= request.getUserPrincipal().getName()%>
                                    </span>
                                </div>
                            </a>
                            <ul class="dropdown-menu">
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
                                <a href="policies">
                                    <i class="fa fa-wrench" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Manage Policies
                                    </span>
                                </a>
                            </li>
                            <li>
                                <a href="manageserviceproviders">
                                    <i class="fa fa-cogs" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Manage Providers
                                    </span>
                                </a>
                            </li>
                            <li>
                                <a href="consumers">
                                    <i class="fa fa-users" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Manage Consumers
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
                            <a href="">
                                &nbsp;Orbi
                            </a>
                        </li>
                        <li class="active" >
                            Manage Policies
                        </li>                    
                    </ol>


                    <!-- panel 1 -->
                    <div class="row">
                        <div class="col-lg-12">

                            <div class="box">
                                <form name="brokerPolicyForm" id="brokerPolicyForm" action="storePolicy" method="post" >
                                    <!-- PanelHeading -->
                                    <div class="box-header" style="background-color: rgb(2, 185, 173); border-color: rgb(2,160,149); color: white;">
                                        <h2>
                                            <i class="fa fa-pencil" style="color: white;"></i>
                                            <span class="break"></span>
                                            Create Policy
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

                                            <div class="row">
                                                <div class="col-lg-4">
                                                    <label class="control-label">
                                                        Broker Policy Name
                                                        <span style="color:red">
                                                            &nbsp;*
                                                        </span>
                                                    </label>
                                                    <div class="controls row">
                                                        <div class="input-group col-sm-12">	
                                                            <input class="form-control"  value="${policy.name}" id="policyName" name="policyName" placeholder="Enter Policy Name"/>
                                                            <span class="input-group-addon">
                                                                <i class="fa fa-tag"></i>
                                                            </span>
                                                        </div>	
                                                    </div>
                                                    <input class="form-control" id="policyID" name="policyID" value="${policy.id}"  style="visibility:hidden;display:none"/>
                                                    <input class="form-control" id="policyVariables" name="policyVariables"   style="visibility:hidden;display:none"/>
                                                    <input class="form-control" id="policyProfiles" name="policyProfiles" style="visibility:hidden;display:none" />
                                                    <input class="form-control" id="valuesPerProfile" name="valuesPerProfile" style="visibility:hidden;display:none"/>
                                                    <input class="form-control" id="relatedPolicies" name="relatedPolicies" style="visibility:hidden;display:none"/>



                                                </div>

                                                <div class="col-lg-8">
                                                    &nbsp;
                                                </div>
                                            </div>
                                            <br>
                                            <hr width="97%">
                                            <!-- Related to Primitive Policies -->
                                            <div style="" id="RelatedToPolicy" class="row">
                                                <div class="col-lg-2">&nbsp;</div>
                                                <div class="col-lg-3">
                                                    <div class="text-center"><label>Available Service Level Profiles</label><br>
                                                        <select style="width: 400px;" size="7" multiple="multiple" id="availablePolicies">

                                                            <%
                                                                String tmpOut = "";
                                                                List<ServiceLevelProfile> primitiveSLP = (List<ServiceLevelProfile>) request.getAttribute("primitiveSLP");
                                                                if (null != request.getAttribute("relatedSLP")) {

                                                                    List<ServiceLevelProfile> relatedSLPs = (List<ServiceLevelProfile>) request.getAttribute("relatedSLP");

                                                                    for (ServiceLevelProfile slp : primitiveSLP) {
                                                                        boolean isFound = false;
                                                                        for (ServiceLevelProfile relatedSLP : relatedSLPs) {
                                                                            if (relatedSLP.getId() == slp.getId()) {
                                                                                isFound = true;
                                                                                break;
                                                                            }
                                                                        }
                                                                        if (!isFound) {
                                                                            tmpOut += "<option value='" + slp.getId() + "'>" + slp.getPolicy_name() + "." + slp.getName() + "</option>";
                                                                        }
                                                                    }
                                                                } else {
                                                                    for (ServiceLevelProfile slp : primitiveSLP) {
                                                                        tmpOut += "<option value='" + slp.getId() + "'>" + slp.getPolicy_name() + "." + slp.getName() + "</option>";
                                                                    }
                                                                }
                                                                out.write(tmpOut);
                                                            %>



                                                        </select>
                                                    </div> 
                                                </div>
                                                <div class="col-lg-2">
                                                    <div class="text-center">
                                                        <input type="button" value="&lt;" onclick="swapElement('realtedToPolicies', 'availablePolicies')" name="&amp;lt;">
                                                        <input type="button" value="&gt;" onclick="swapElement('availablePolicies', 'realtedToPolicies')" name="&amp;gt;">
                                                    </div>
                                                </div>
                                                <div class="col-lg-3">
                                                    <div class="text-center"><label>Depends On</label><br>
                                                        <select style="width: 400px;" size="7" multiple="multiple" id="realtedToPolicies">

                                                            <c:forEach items="${relatedSLP}" var="relatedSLP">
                                                                <option value="<c:out value="${relatedSLP.id}"></c:out> ">
                                                                    <c:out value="${relatedSLP.policy_name}"></c:out>.<c:out value="${relatedSLP.name}"></c:out >
                                                                    </option>
                                                            </c:forEach>


                                                        </select>
                                                    </div>
                                                </div>
                                                <div class="col-lg-2">&nbsp;</div>                                            
                                            </div>
                                            <br>
                                        </div>


                                        <br>
                                        <hr width="97%">


                                        <div class="col-lg-12">
                                            <label>
                                                Variables of Broker Policy
                                            </label>


                                            <table   id="policyVariableTable" class="table table-striped table-bordered bootstrap-datatable datatable">
                                                <thead>
                                                    <tr>
                                                        <th>
                                                            Variable Name
                                                        </th>
                                                        <th>
                                                            Variable Type
                                                        </th>
                                                        <th>
                                                            Metric Unit
                                                        </th>
                                                        <th>
                                                            Date Created
                                                        </th>
                                                        <th>
                                                            Actions
                                                        </th>
                                                    </tr>
                                                </thead>   
                                                <c:if test="${ null != policyVariables}">
                                                    <tbody>
                                                        <c:forEach items="${policyVariables}" var="policyVariables">
                                                            <tr>
                                                                <td><c:out value="${policyVariables.name}"></c:out></td>
                                                                <td><c:out value="${policyVariables.type_id}"></c:out></td>
                                                                <td><c:out value="${policyVariables.metric_unit}"></c:out></td>
                                                                <td><c:out value="${fn:substring(policyVariables.date_created, 0, 19)}"></c:out></td>
                                                                    <td><!--                                                                <a title="Edit" data-rel="tooltip" class="btn btn-xs btn-info" href="#">
                                                                                <i class="fa fa-edit"></i>  
                                                                            </a>-->
                                                                        <a onClick="deletePolicyVariableRow('<c:out value="${policyVariables.name}"></c:out>');" title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" > <i class="fa fa-trash-o"></i> </a>
                                                                    </td>

                                                                </tr>
                                                        </c:forEach>
                                                    </tbody>   


                                                </c:if>
                                            </table>



                                            <!-- add Variable btn -->
                                            <div class="col-lg-12">
                                                <a title="add Variable" data-rel="tooltip" id="addvariable1" class="btn btn-xs btn-primary" data-toggle="collapse" data-parent="#accordion1" href="#collapseOne" style="margin-top: 5px;">
                                                    Add Variable&nbsp;&nbsp;<i class="fa fa-plus-circle"></i>
                                                </a>
                                            </div>
                                            <!-- /add Variable /btn -->

                                            <!--  - - - - - - - - - Add Variable 1 - - - - - - - - - -  - -->
                                            <div class="row" style="margin: 0px;">
                                                <div id="collapseOne" class="accordion-body collapse">
                                                    <div class="accordion-inner">

                                                        <div class="text-left">

                                                            <div class="row">&nbsp;</div>

                                                            <div class="col-lg-4">
                                                                <label class="control-label">
                                                                    Variable Name
                                                                    <span style="color:red">
                                                                        &nbsp;*
                                                                    </span>
                                                                </label>
                                                                <div class="controls row">
                                                                    <div class="input-group col-sm-12">	
                                                                        <input id="variableName" class="form-control" id="VarName1" placeholder="Enter Variable Name"/>
                                                                        <span class="input-group-addon">
                                                                            <i class="fa fa-bars"></i>
                                                                        </span>
                                                                    </div>	
                                                                </div>
                                                            </div>

                                                            <div class="col-lg-4">
                                                                <div class="form-group">
                                                                    <label class="control-label" id="VarType1">
                                                                        Variable Type
                                                                        <span style="color:red">
                                                                            &nbsp;*
                                                                        </span>
                                                                    </label>
                                                                    <br>
                                                                    <div class="controls">
                                                                        <select id="variableTypeSelect" class="form-control">
                                                                            <option value="">
                                                                                choose variabe type...
                                                                            </option>
                                                                            <c:forEach items="${variableTypes}" var="vt">
                                                                                <option value="<c:out value="${vt.id}"> </c:out> ">
                                                                                    <c:out value="${vt.name}"> </c:out >
                                                                                    </option>
                                                                            </c:forEach>
                                                                        </select>
                                                                    </div>
                                                                </div>
                                                            </div>


                                                            <div class="col-lg-4">
                                                                <label class="control-label">
                                                                    Metric Unit

                                                                </label>
                                                                <div class="controls row">
                                                                    <div class="input-group col-sm-12">	
                                                                        <input id="variableMU" class="form-control" id="VarName1" placeholder="Enter Metric Unit"/>
                                                                        <span class="input-group-addon">
                                                                            <i class="fa fa-bars"></i>
                                                                        </span>
                                                                    </div>	
                                                                </div>
                                                            </div>




                                                            <div class="col-lg-1">
                                                                <label class="control-label">
                                                                    &nbsp;
                                                                </label>
                                                            </div>

                                                            <div class="col-lg-3">
                                                                <label class="control-label">
                                                                    &nbsp;
                                                                </label>
                                                                <div class="text-right">
                                                                    <div id="addPolicyVariable" class="btn btn-success" style="margin-bottom: 10px; width: 150px;">
                                                                        <strong>
                                                                            Add Variable
                                                                        </strong>
                                                                    </div>

                                                                </div>
                                                            </div>
                                                            <br>


                                                            <!--  - - - - - - - - - Add Variable 2 - - - - - - - - - -  - -->
                                                            <div id="collapseTow" class="accordion-body collapse">
                                                                <div class="accordion-inner">

                                                                    <div class="text-left">

                                                                        <div class="row">&nbsp;</div>

                                                                        <div class="col-lg-4">
                                                                            <label class="control-label">
                                                                                Variable Name
                                                                                <span style="color:red">
                                                                                    &nbsp;*
                                                                                </span>
                                                                            </label>
                                                                            <div class="controls row">
                                                                                <div class="input-group col-sm-12">	
                                                                                    <input class="form-control" id="VarName2" placeholder="Enter Variable Names"/>
                                                                                    <span class="input-group-addon">
                                                                                        <i class="fa fa-bars"></i>
                                                                                    </span>
                                                                                </div>	
                                                                            </div>
                                                                        </div>



                                                                        <div class="col-lg-1">
                                                                            <label class="control-label">
                                                                                &nbsp;
                                                                            </label>
                                                                            <div class="text-left">
                                                                                <a title="add another Variable" data-rel="tooltip" id="addvariable3" class="btn btn-xs btn-primary" data-toggle="collapse" data-parent="#accordion2" href="#collapseThree" style="margin-top: 5px;">
                                                                                    <i class="fa fa-plus-circle"></i>
                                                                                </a>
                                                                            </div>
                                                                        </div>

                                                                        <div class="col-lg-3">
                                                                            <label class="control-label">
                                                                                &nbsp;
                                                                            </label>
                                                                            <div class="text-right">
                                                                                <div class="btn btn-success" style="margin-bottom: 10px; width: 150px;">
                                                                                    <strong>
                                                                                        Add Variable
                                                                                    </strong>
                                                                                </div>
                                                                            </div>
                                                                        </div>

                                                                        <div class="row">&nbsp;</div>
                                                                        <!--  - - - - - - - - - Add Variable 2 - - - - - - - - - -  - -->
                                                                    </div>

                                                                </div>
                                                            </div>

                                                            <div class="row">&nbsp;</div>

                                                        </div><!--  /text-left  -->

                                                    </div><!-- /accordion-inner -->

                                                    <div class="row">&nbsp;</div>


                                                </div><!-- /accordion-body -->

                                            </div><!--  /row  -->
                                            <!--  /- - - - - - - /Add Variable 1 - - - - - - - - -  -->


                                            <br>


                                            <hr width="97%">
                                            <div class="row">&nbsp;</div>

                                            <div class="col-lg-12">
                                                <label>
                                                    Service Level Profiles of Broker Policy
                                                </label>

                                                <table  id="profilesTable" class="table table-striped table-bordered bootstrap-datatable datatable">

                                                    <thead>
                                                        <tr>
                                                            <th>
                                                                Service Level Profile
                                                            </th>
                                                            <th>
                                                                Date Created
                                                            </th>
                                                            <th>
                                                                Actions
                                                            </th>
                                                        </tr>
                                                    </thead>   

                                                    <c:if test="${ null != policyProfiles}">
                                                        <tbody>
                                                            <c:forEach items="${policyProfiles}" var="policyProfiles">
                                                                <tr>
                                                                    <td><c:out value="${policyProfiles.name}"></c:out></td>
                                                                    <td><c:out value="${fn:substring(policyProfiles.date_created, 0, 19)}"></c:out></td>
                                                                        <td>
                                                                            <a onClick="toggleSLP('<c:out value="${policyProfiles.name}"></c:out>');" class="btn btn-xs btn-info" data-rel="tooltip" title="Edit"> <i class="fa fa-edit"></i></a>
                                                                        <a onClick="deleteProfileRow('<c:out value="${policyProfiles.name}"></c:out>');" title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" > <i class="fa fa-trash-o"></i></a>
                                                                        </td>
                                                                    </tr>
                                                            </c:forEach>
                                                        </tbody>   
                                                    </c:if>






                                                </table>
                                            </div>

                                            <br>



                                            <!-- add ServiceLevelProfile btn -->
                                            <div class="col-lg-12">
                                                <a title="add Service Level Profile" data-rel="tooltip" id="addSLP1" class="btn btn-xs btn-primary" data-toggle="collapse" data-parent="#accordion11" href="#collapseEleven" style="margin-top: 5px;">
                                                    Add Service Level Profile&nbsp;&nbsp;<i class="fa fa-plus-circle"></i>
                                                </a>
                                            </div>
                                            <!-- /add ServiceLevelProfile /btn -->

                                            <!--  - - - - - - - - - Add ServiceLevelProfile 1 - - - - - - - - - -  - -->
                                            <div class="row" style="margin: 0px;">
                                                <div id="collapseEleven" class="accordion-body collapse">
                                                    <div class="accordion-inner">

                                                        <div class="text-left">

                                                            <div class="row">&nbsp;</div>

                                                            <div class="col-lg-4">
                                                                <label class="control-label">
                                                                    Service Level Profile Name
                                                                    <span style="color:red">
                                                                        &nbsp;*
                                                                    </span>
                                                                </label>
                                                                <div class="controls row">
                                                                    <div class="input-group col-sm-12">	
                                                                        <input id="profileName" class="form-control" id="SLPName1" placeholder="Enter Service Level Profile Name"/>
                                                                        <span class="input-group-addon">
                                                                            <i class="fa fa-bars"></i>
                                                                        </span>
                                                                    </div>	
                                                                </div>
                                                            </div>

                                                            <div class="col-lg-1">
                                                                <label class="control-label">
                                                                    &nbsp;
                                                                </label>
                                                            </div>

                                                            <div class="col-lg-3">
                                                                <label class="control-label">
                                                                    &nbsp;
                                                                </label>
                                                                <div class="text-right">
                                                                    <div id="addProfile" class="btn btn-success" style="margin-bottom: 10px;">
                                                                        <strong>
                                                                            Add Service Level Profile
                                                                        </strong>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <br>

                                                            <div class="row">&nbsp;</div>

                                                        </div><!--  /text-left  -->

                                                    </div><!-- /accordion-inner -->

                                                    <div class="row">&nbsp;</div>


                                                </div><!-- /accordion-body -->

                                            </div><!--  /row  -->
                                            <!--  /- - - - - - - /Add ServiceLevelProfile 1 - - - - - - - - -  -->




                                            <br>

                                            <script>
                                                function swapElement(fromList, toList) {
                                                    var selectOptions = document.getElementById(fromList);
                                                    for (var i = 0; i < selectOptions.length; i++) {
                                                        var opt = selectOptions[i];
                                                        if (opt.selected) {
                                                            document.getElementById(fromList).removeChild(opt);
                                                            document.getElementById(toList).appendChild(opt);
                                                            i--;
                                                        }
                                                    }
                                                }
                                            </script>



                                            <!-- Dynamically add all SLPedits-->

                                            <div id="slpEdits">

                                                <%                                                    if (null != request.getAttribute("policy")) {

                                                        List<ServiceLevelProfile> slp = (List<ServiceLevelProfile>) request.getAttribute("policyProfiles");
                                                        List<PolicyVariable> policyVariables = (List<PolicyVariable>) request.getAttribute("policyVariables");
                                                        //slpVariables
                                                        Map<String, ArrayList<String>> slpVariables = (Map<String, ArrayList<String>>) request.getAttribute("slpVariables");
                                                        for (String profile_name : slpVariables.keySet()) {
                                                            String leftSLPColumn = "";
                                                            String rightSLPColumn = "";

                                                            //Iterate all Policy Variables
                                                            for (PolicyVariable policyVariable : policyVariables) {
                                                                //Put to Right Column
                                                                if (slpVariables.get(profile_name).contains(policyVariable.getName())) {
                                                                    rightSLPColumn += "<option value='" + policyVariable.getType_id() + "'>" + policyVariable.getName() + "</option>";
                                                                } //Put to Left Column
                                                                else {
                                                                    leftSLPColumn += "<option value='" + policyVariable.getType_id() + "'>" + policyVariable.getName() + "</option>";
                                                                }

                                                            }

                                                            String slpEdit = "<div class=\"row\" id=\"" + profile_name + "\" style=\"display: none;\">"
                                                                    + "<div class=\"col-lg-2\">&nbsp;</div>"
                                                                    + "<div class=\"col-lg-3\">"
                                                                    + "<div class=\"text-center\">"
                                                                    + "<label>All</label><br>"
                                                                    + "<select id=\"left" + profile_name + "\" multiple=\"multiple\" size=\"7\" style=\"width: 220px;\">"
                                                                    + leftSLPColumn
                                                                    + "</select>"
                                                                    + "</div>"
                                                                    + " </div>"
                                                                    + "<div class=\"col-lg-2\">"
                                                                    + "<div class=\"text-center\">"
                                                                    + "<input name=\"&amp;lt;\" onclick=\"swapElement('right" + profile_name + "', 'left" + profile_name + "')\" type=\"button\" value=\"&lt;\" />"
                                                                    + "<input name=\"&amp;gt;\" onclick=\"swapElement('left" + profile_name + "', 'right" + profile_name + "')\" type=\"button\" value=\"&gt;\" />"
                                                                    + "</div>"
                                                                    + "</div>"
                                                                    + "<div class=\"col-lg-3\">"
                                                                    + "<div class=\"text-center\">"
                                                                    + "<label>" + profile_name + " Profile</label><br>"
                                                                    + "<select id=\"right" + profile_name + "\" multiple=\"multiple\" size=\"7\" style=\"width: 220px;\">"
                                                                    + rightSLPColumn
                                                                    + "</select>"
                                                                    + "</div>"
                                                                    + "</div>"
                                                                    + "<div class=\"col-lg-2\">&nbsp;"
                                                                    + "</div>"
                                                                    + "</div>";

                                                            out.write(slpEdit);

                                                        }

                                                    }


                                                %>


                                            </div>
                                            <br>

                                            <!--  Publish Button  -->
                                            <div class="col-lg-12">
                                                <div class="text-right">
                                                    <button class="btn" style="background-color: rgb(238, 44 , 67); color: white; margin-bottom: 10px; width: 150px;" onclick="resetForm()">
                                                        <strong>
                                                            Reset
                                                        </strong>
                                                    </button>
                                                    &nbsp;
                                                    <button onclick="submitBrokerPolicyForm()" class="btn" style="background-color: rgb(141, 199 , 72); color: white; margin-bottom: 10px; width: 150px;">
                                                        <strong>
                                                            <%= (null != request.getAttribute("policy") ? "Update Policy" : "Publish New Policy")%>
                                                        </strong>
                                                    </button>
                                                </div>
                                            </div>
                                            <!--  / Publish Button  -->
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>


                    </div>
                </div>
                <!-- /panel 1 -->

            </div><!-- /row -->

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

    </body>
</html>


<script type="text/javascript">

    var tempPolicyVariableTable;
    var tempProfilesTable;
//When WebPage is loaded do the following
    $(document).ready(function () {
        var formAction = "";

//==> Functions for #policyVariableTable
        var policyVariableTable = $('#policyVariableTable').DataTable();
        tempPolicyVariableTable = policyVariableTable;

        $('#addPolicyVariable').on('click', function () {
            errorMessage = "";
            if ($("#variableName").val().trim().length < 1)
                errorMessage = "Please enter a valid Variable Name";
            else if ($("#variableTypeSelect").val().length > 0) {
                var actionButtons = '<a onClick="deletePolicyVariableRow(\'' + $("#variableName").val() + '\');" title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" > <i class="fa fa-trash-o"></i> </a>';
                var today = new Date();
                var dd = today.getDate();
                var mm = today.getMonth() + 1; //January is 0!
                var yyyy = today.getFullYear();
                if (dd < 10) {
                    dd = '0' + dd
                }

                if (mm < 10) {
                    mm = '0' + mm
                }

                today = yyyy + '-' + mm + '-' + dd;
                metric_unit = "N/A";
                if (!(typeof $("#variableMU").val() === "undefined") && $("#variableMU").val().length > 0)
                    metric_unit = $("#variableMU").val();
                policyVariableTable.row.add([
                    $("#variableName").val(),
                    $("#variableTypeSelect").val(),
                    metric_unit,
                    today,
                    actionButtons
                ]).draw();
                //Store Variables to Hidden Input #policyVariables
                storePolicyVariables();
                //Add new option to all SLP
                addNewVariableToAllSLP($("#variableName").val(), $("#variableTypeSelect").val());
            }
            else {
                errorMessage = "Please select Variable Type";
            }

            if (errorMessage.length > 0)
                alert(errorMessage);
        });
        $('#policyVariableTable tbody').on('click', 'tr', function () {
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
            }
            else {
                policyVariableTable.$('tr.selected').removeClass('selected');
                $(this).addClass('selected');
            }
        });
        $('#removePolicyVariable').click(function () {
            policyVariableTable.row('.selected').remove().draw(false);
        });
//==> Functions for  #profilesTable


        var profilesTable = $('#profilesTable').DataTable();
        tempProfilesTable = profilesTable;
        $('#addProfile').on('click', function () {
            //            var actionButtons2 = '<a onClick="deleteProfileRow();" title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" > <i class="fa fa-trash-o"></i> </a>';
            if ($("#profileName").val().trim().length < 1)
                alert("Please enter a valid  Service Level Profile Name");
            else {

                var SLPID = $("#profileName").val().trim();
                var actionButtons2 = '<a onClick="deleteProfileRow(\'' + SLPID + '\');" title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" > <i class="fa fa-trash-o"></i> </a>';
                var edit = '<a  onClick="toggleSLP(\'' + SLPID + '\');" class="btn btn-xs btn-info" data-rel="tooltip" title="Edit"> <i class="fa fa-edit"></i></a>';
                actionButtons2 = edit + actionButtons2;
                //Create SLP object
                SLPEditFactory(SLPID);
                var today = new Date();
                var dd = today.getDate();
                var mm = today.getMonth() + 1; //January is 0!
                var yyyy = today.getFullYear();
                if (dd < 10) {
                    dd = '0' + dd
                }

                if (mm < 10) {
                    mm = '0' + mm
                }

                today = yyyy + '-' + mm + '-' + dd;
                profilesTable.row.add([
                    $("#profileName").val(),
                    today,
                    actionButtons2
                ]).draw();
                //Store Profiles to Hidden Input #Profiles
                storeProfiles();
            }

        });
        $('#profilesTable tbody').on('click', 'tr', function () {
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
            }
            else {
                policyVariableTable.$('tr.selected').removeClass('selected');
                $(this).addClass('selected');
            }
        });

        //Init tables values
        storePolicyVariables();
        storeProfiles();

    });
//==> Util Functions 

    function storePolicyVariables() {
        $("#policyVariables").val("");
        var arrayVariables = tempPolicyVariableTable.rows().data();
        var policyVariables = "";
        for (var i = 0; i < arrayVariables.length; i++) {
            policyVariables = policyVariables + arrayVariables[i][0] + "," + arrayVariables[i][1].trim() + "," + arrayVariables[i][2] + "|";
        }
        $("#policyVariables").val(policyVariables);
    }
    function storeProfiles() {
        $("#policyProfiles").val("");
        var arrayProfiles = tempProfilesTable.rows().data();
        var profiles = "";
        for (var i = 0; i < arrayProfiles.length; i++) {
            profiles = profiles + arrayProfiles[i][0] + "," + arrayProfiles[i][1].trim() + "|";
        }
        $("#policyProfiles").val(profiles);
    }


    function deletePolicyVariableRow(variable_name) {
        setTimeout(function () {
            tempPolicyVariableTable.row('.selected').remove().draw(false);
            storePolicyVariables();
            removeVariableFromAllSLP(variable_name);
        }, 400);
    }


    function deleteProfileRow(SLPID) {
        console.log("Remove SPL with ID: " + SLPID);
        $("#" + SLPID).remove();
        setTimeout(function () {
            tempProfilesTable.row('.selected').remove().draw(false);
            storeProfiles();
        }, 400);
    }


    function getSelectBoxValues() {
        var options = $('#right2 option');
        var values = $.map(options, function (option) {
            return option.value;
        });
        alert(values);
    }

    //TODO: Hold all values of Profiles

    function toggleSLP(id) {
        $("#" + id).toggle();
    }

    function createSLPLeftColumnValues() {
        var arrayVariables = tempPolicyVariableTable.rows().data();
        var columnOptions = "";
        for (var i = 0; i < arrayVariables.length; i++) {
            columnOptions = columnOptions + '<option value="' + arrayVariables[i][1].trim() + '">' + arrayVariables[i][0] + '</option>'
        }
        return columnOptions;
    }


    function SLPEditFactory(name) {
        console.log('SLPEditFactory was called');
        var htmlCode = '';
        var SLPID = name;
        console.log('Generated SLPID: ' + SLPID);
        htmlCode = '<div class="row" id="' + name + '" style="display: none;">' + '<div class="col-lg-2">&nbsp; </div>'
                + '<div class="col-lg-3"> <div class="text-center">'
                + '<label>'
                + name + ' Profile'
                + '</label><br>'
                + '<select id="left' + SLPID + '" multiple="multiple" size="7" style="width: 220px;">'
                + createSLPLeftColumnValues()
                + '</select> </div></div><div class="col-lg-2"><div class="text-center">'
                + '<input name="&amp;lt;" onclick="swapElement(\'right' + SLPID + '\', \'left' + SLPID + '\')" type="button" value="&lt;" />'
                + '<input name="&amp;gt;" onclick="swapElement(\'left' + SLPID + '\', \'right' + SLPID + '\')" type="button" value="&gt;" />'
                + '</div></div><div class="col-lg-3"><div class="text-center"><label> All</label><br>'
                + '<select id="right' + SLPID + '" multiple="multiple" size="7" style="width: 220px;">'
                + '</select>' + '</div>';         //Append the new object
        $('#slpEdits').append(htmlCode);
        getAllProfilesValues();
    }

    function getAllProfilesValues() {
        var profileValues = "";
        var selects = document.getElementsByTagName("select");
        for (var i = 0; i < selects.length; i++) {
            if (selects[i].id.indexOf('right') == 0) {
                var SLPName = selects[i].id.substring(5);
                var SLPValues = $.map(selects[i], function (option) {
                    return option.text;
                });
                profileValues = SLPName + ":" + SLPValues + "|" + profileValues;
            }
        }

        return profileValues;
    }


    function removeVariableFromAllSLP(variable_name) {
        var selects = document.getElementsByTagName("select");
        for (var i = 0; i < selects.length; i++) {

            var match = 0;
            if (selects[i].id.indexOf('left') === 0)
                match = 1;

            if (selects[i].id.indexOf('right') === 0)
                match = 1;

            if (match === 1) {
                var SLPName = selects[i].id;
                console.log("SLP Name: " + SLPName);
                var SLPTexts = [];
                $('#' + SLPName + ' option').each(function () {
                    if (this.text === variable_name) {
                        this.remove();
                        console.log("Removed : " + variable_name)
                    }
                    SLPTexts.push(this.text);
                });
            }
        }



        //Check Left SLP


        //Check Right SLP

    }

    function addNewVariableToAllSLP(variable_name, variable_type_id) {
        var selects = document.getElementsByTagName("select");
        for (var i = 0; i < selects.length; i++) {
            if (selects[i].id.indexOf('left') == 0) {
                var SLPName = selects[i].id;
                console.log("Append option: " + variable_name + " to SLP: " + SLPName);
                $("#" + SLPName).append($("<option></option>").attr("value", variable_type_id).text(variable_name));
            }
        }

    }



    function  submitBrokerPolicyForm() {
        formAction = "submit";
        $("#valuesPerProfile").val(getAllProfilesValues());
    }

    function resetForm() {
        formAction = "reset";
    }




    $("#brokerPolicyForm").submit(function (e) {

        if (formAction === "reset") {
            e.preventDefault();
            $('#brokerPolicyForm').trigger("reset");
        }
        else {
            if (typeof $("#policyName").val() === "undefined" || $("#policyName").val().trim().length < 1) {
                alert("BrokerPolicy name cannot be empty!");
                e.preventDefault();
                return;
            }
            else {
                $("#valuesPerProfile").val(getAllProfilesValues());

                var relatedSelect = document.getElementById("realtedToPolicies");
                var relatedPoliciesValues = $.map(relatedSelect, function (option) {
                    return option.value;
                });
                $("#relatedPolicies").val(relatedPoliciesValues);
            }
        }

    })

</script>