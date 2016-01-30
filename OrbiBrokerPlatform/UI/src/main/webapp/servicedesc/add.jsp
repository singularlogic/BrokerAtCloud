<%@page import="org.broker.orbi.models.ServiceDescription"%>
<%@page import="org.broker.orbi.models.Policy"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html>
    <head>
        <title>Policies &mdash; ORBI</title>

        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="../assets/css/bootstrap.min.css" type="text/css" />
        <link rel="stylesheet" href="../assets/css/orbi.css" type="text/css" />
        <link href="http://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css" />

        <link rel="stylesheet" href="../assets/policyeditor/jquery/css/smoothness/jquery-ui-1.8.6.custom.css" />
        <link rel="stylesheet" href="../assets/policyeditor/css/jquery-ui-timepicker-addon.css" />

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
                            <li><a class="active" href="/orbibroker/servicedesc/list">Service Description Mgmt</a></li>
                            <li><a href="/orbibroker/iaasconfig/list">IaaS Configuration Mgmt</a></li>
                            <li><a href="/orbibroker/appstore/list">App Store</a></li>
                            <!--<li><a href="/orbibroker/flavors/list">Flavors</a></li>-->
                            <li><a href="/orbibroker/offerings/list">Offerings</a></li>
                            <li><a href="/orbibroker/pulsar/notifications">Notifications</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <!--
                    <a href="/orbibroker/policies/list" class="btn btn-default pull-right action-button">Cancel</a>
                    <a href="/orbibroker/policies/save" class="btn btn-danger pull-right action-button">Save</a>
                    -->
                    <h2>Service Descriptions<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Service Description Add</p></h2>

                    <div class="table-responsive wrapper col-md-12">


                        <!-- Select Broker Policy in order to create a Service Description -->
                        <div class="col-lg-4" style="padding-left: 0px; padding-right: 10px;">
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
                            <br/>
                        </div>  

                        <div class="row" style="display:none" id="sdEditor">




                            <div id="serviceLoadArea" class="collapse in">
                                <div class="droptarget" ondrop="loadBP(event)" ondragover="allowDrop(event)">
                                    <p id="bp"> 1. Drop your broker policy here! </p>
                                </div>
                            </div>
                            <div id="serviceDescriptionLoadArea" class="collapse in">
                                <div class="droptarget" ondrop="loadSD(event)" ondragover="allowDrop(event)">
                                    <p id="sd_load"> To edit existing service description, drop it here! </p>
                                </div>
                            </div>



                            <div id="serviceEditArea" class="collapse in">


                                <div>
                                    <div class="row panel1col">
                                        <div class="col-sm-6">
                                            <div class="panel panel-default panel2col">
                                                <div class="panel-heading">
                                                    <h1> Broker Policy </h1>
                                                </div>
                                                <div class="panel-body">

                                                    <form class="form-horizontal">
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="bp_namespace">Broker namespace</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="bp_namespace" placeholder="http://yourbrokernamespace.com"> </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="bp_business_entity">Broker acronym</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="bp_business_entity" placeholder="YourBrokerCompanyAcronym">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="bp_legal_name">Broker legal name</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="bp_legal_name" placeholder="Your BrokerCompany Legal Name">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="bp_instance">Broker policy title</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="bp_instance" placeholder="YourBrokerPolicyTitle">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="bp_model">Broker policy model name</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="bp_model" placeholder="YourBrokerPolicyModelName">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="valid_from">Valid from</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="valid_from" value="">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="valid_through">Valid through</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="valid_through" value="">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="successor_of_bp">Successor of</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="successor_of_bp" value="">
                                                            </div>
                                                        </div> 
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="onboarding_deprecated_from">Onboarding deprecated from</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="onboarding_deprecated_from" value="">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="recommendation_deprecated_from">Recommendation deprecated from</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="recommendation_deprecated_from" value="">
                                                            </div>
                                                        </div>

                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="slp_class">Service Level Profile Type</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="slp_class" placeholder="SomeServiceLevelProfile">
                                                            </div>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="col-sm-6">
                                            <div class="panel panel-default panel2col">
                                                <div class="panel-heading">
                                                    <h1>Service and Service Provider Description</h1>
                                                </div>
                                                <div class="panel-body">
                                                    <form class="form-horizontal">
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sp_namespace">Service provider namespace</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sp_namespace" value="http://yournamespace.com">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sp_business_entity">Provider acronym</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sp_business_entity" value="YourCompanyAcronym">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sp_legal_name">Provider legal name</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sp_legal_name" value="Your Company Legal Name">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sd_namespace">Service namespace</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sd_namespace" value="http://yournamespace.com/services/yourservicenamespace">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sd">Service acronym</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sd" value="YourServiceDescriptionAcronym">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sd_title">Service title</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sd_title" value="YourServiceDescriptionTitle">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sd_description">Service description</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sd_description" value="YourServiceDescription">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="sd_model">Service model name</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="sd_model" value="YourServiceModelName">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="valid_from_sd">Valid from</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="valid_from_sd" value="YYYY-MM-DD">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="valid_through_sd">Valid through (optional)</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="valid_through_sd" value="YYYY-MM-DD">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="successor_of_sd">Successor of (URI to the predecessor service description, optional)</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="successor_of_sd" value="">
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="deprecation_recommendation_sd">Recommendation deprecated from (optional)</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="deprecation_recommendation_sd" value="YYYY-MM-DD">
                                                            </div>
                                                        </div>

                                                        <div class="form-group">
                                                            <label class="col-sm-4 control-label" for="slp">Service Level Profile Title</label>
                                                            <div class="col-sm-8">
                                                                <input class="form-control" type="text" id="slp" value="YourServiceLevelProfileTitle">
                                                            </div>
                                                        </div>
                                                        <input type="text" id="servicemodel_class" value="Broker Policy Class" disabled="disabled" hidden="hidden">
                                                        <input type="text" id="servicemodel" value="Broker Policy Instance" disabled="disabled" hidden="hidden">
                                                    </form>

                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="panel panel-default panel1col">

                                    <div class="panel-heading">
                                        <h1> Classification taxonomy of the Broker Policy</h1>
                                    </div>
                                    <div class="panel-body">
                                        <form class="form-horizontal">
                                            <div class="form-group">
                                                <label class="col-sm-2 control-label" for="classtaxpref">Prefix</label>
                                                <div class="col-sm-9">
                                                    <input class="form-control" type="text" id="classtaxpref" value="fc">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-sm-2 control-label" for="classtaxURI">URI</label>
                                                <div class="col-sm-9">
                                                    <input class="form-control" type="text" id="classtaxURI" value="http://www.broker-cloud.eu/service-descriptions/CAS/categories">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-sm-2 control-label" for="classtaxroot">Root concept</label>
                                                <div class="col-sm-9">
                                                    <input class="form-control" type="text" id="classtaxroot" value="rootConcept">
                                                </div>
                                            </div>
                                        </form>

                                        <table class="table innerTable" id="classtaxconcepts">
                                            <tr>
                                                <th class='col-sm-2'>Classification tag</th>
                                                <th class='col-sm-8'>Description</th>
                                                <th class='col-sm-2'>Label</th>
                                            </tr>
                                        </table>
                                    </div>


                                    <div class="panel panel-default innerPanel">
                                        <div class="panel-heading">
                                            <h2> Classify your service</h2>
                                        </div>
                                        <div class="panel-body">
                                            <p>Available service classification concepts:
                                                <select name="classifications" id="classtaxonomy"> </select>
                                                <button class="btn btn-default" onclick="addConcept()"> Add </button>
                                                <button class="btn btn-default" onclick="deleteConcept()"> Delete </button>
                                            <table id="class_table">
                                            </table>
                                            </p>

                                        </div>
                                    </div>
                                </div>
                                <div class="panel panel-default panel1col">

                                    <div class="panel-heading">
                                        <h1> Specify your quantitative service levels</h1>
                                    </div>
                                    <div class="panel-body">
                                        <div id="slQuantitative"></div>
                                    </div>
                                </div>


                                <div class="panel panel-default panel1col">

                                    <div class="panel-heading">
                                        <h1>Specify your qualitative service levels </h1>
                                    </div>
                                    <div class="panel-body">
                                        <h1>Overview Qualitative Service Level Schemas </h1>

                                        <table class="table" style="width: 95%" id="qualserviceleveldetails">
                                            <tr>
                                                <th class='col-sm-3'>Service Level</th>
                                                <th class='col-sm-9'>Description</th>
                                            </tr>
                                        </table>

                                        <h1>Specify Qualitative values</h1>
                                        <p id="qualvalues">
                                        </p>
                                    </div>
                                </div>

                                <div class="bc-buttongroup" style="margin:20px">
                                    <button onclick="submitServiceDescriptionForm()" class="btn btn-default  btn-success">Save</button>
                                    &nbsp;&nbsp;&nbsp;
                                    <button class="btn btn-default" onclick="storeSD()">Show the service description specification. </button>

                                    <form name="serviceDescriptionForm" id="serviceDescriptionForm" action="store" method="post" >
                                        <input type="hidden" id="selectedPolicyID" name="selectedPolicyID" value="<%= (null == request.getAttribute("policy") ? 0 : ((Policy) request.getAttribute("policy")).getId())%>" >
                                        <input type="hidden" id="serviceDescriptionID"  value="<%= (null == request.getAttribute("serviceDescription") ? 0 : ((ServiceDescription) request.getAttribute("serviceDescription")).getId())%>" name="serviceDescriptionID">
                                        <input type="hidden" id="serviceDescriptionContent" name="serviceDescriptionContent">
                                        <input type="hidden" id="serviceDescriptionName" name="serviceDescriptionName">
                                        <input type="hidden" id="serviceDescriptionAcronym" name="serviceDescriptionAcronym">
                                    </form>
                                    <br/><br/>
                                </div>
                            </div>
                        </div>



                    </div>

                </div>

        </main>

        <!-- Include Footer -->
        <%@include file="../inc/footer.jsp" %>

        <script src="../assets/js/jquery-2.1.4.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>

        <script src="../assets/policyeditor/bower_components/jquery/dist/jquery.min.js"></script>
        <script src="../assets/policyeditor/bower_components/jquery-ui/jquery-ui.min.js"></script>
        <script src="../assets/policyeditor/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>
        <script src="../assets/policyeditor/js/jquery-ui-timepicker-addon.js"></script>
        <script src="../assets/policyeditor/js/jquery.editable-1.3.3.js"></script>
        <script src="../assets/policyeditor/js/jquery.livequery.js"></script>
        <script src="../assets/policyeditor/js/jquery.clicknscroll.v1.0.js"></script>
        <script src="../assets/policyeditor/js/jquery.sa.js"></script>
        <script src="../assets/policyeditor/js/jquery.cookie.js"></script>
        <script src="../assets/policyeditor/js/mime.js"></script>
        <script src="../assets/policyeditor/js/slidedeck.js"></script>
        <script src="../assets/policyeditor/js/jquery.jgrowl_minimized.js"></script>
        <script src="../assets/policyeditor/js/Math.uuid.js"></script>
        <script src="../assets/policyeditor/js/base64.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.json.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.uri.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.xmlns.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.curie.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.datatype.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.rdf.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.rdfa.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.rdf.json.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.rdf.xml.js"></script>
        <script src="../assets/policyeditor/rdfquery/jquery.rdf.turtle.js"></script>
        <!-- Broker Policy Editor-->
        <script src="../assets/policyeditor/sdeditor.js"></script>

        <script src="../assets/js/orbi.js"></script>

        <script>

                                        $(document).ready(function () {
                                            var defaultSDeditorDiv = $("#sdEditor").clone();

                                            $('#policy').change(function () {
                                                $("#policy option:selected").each(function () {
                                                    if ($(this).val() !== null && $(this).val() > 0) {
                                                        var policyID = $(this).val();
                                                        console.log("Policy selected with ID: " + policyID);
                                                        $.ajax({
                                                            method: "POST",
                                                            url: "../fetchpolicy",
                                                            dataType: "text",
                                                            data: {policyID: policyID},
                                                            success: function (data) {
                                                                if (data.length == 0) {
                                                                    alert("Error: Corrupted Broker Policy.");
                                                                } else {
                                                                    $("#sdEditor").replaceWith(defaultSDeditorDiv.clone());
                                                                    $("#sdEditor").show();
                                                                    customLoadBP(data);
                                                                    //customLoadSD(data);
                                                                }
                                                            },
                                                            error: function () {
                                                                alert("Problem Occured!");
                                                            }
                                                        });
                                                    } else {
                                                        $("#sdEditor").replaceWith(defaultSDeditorDiv.clone());
                                                        $("#sdEditor").hide();
                                                    }
                                                });
                                            });
                                        });


                                        function submitServiceDescriptionForm() {
                                            $("#serviceDescriptionAcronym").val($("#sd").val());
                                            $("#serviceDescriptionName").val($("#sd_model").val());
                                            $("#serviceDescriptionContent").val(getCurrentSD());
                                            $("#selectedPolicyID").val($("#policy").val())
                                            $.ajax({
                                                url: "../broker_servicedesc_validation",
                                                method: "POST",
                                                data: {brokerPolicyContent: getCurrentSD()},
                                                dataType: "text",
                                                success: function (data) {
//                                                    Validation Service is not working properly so validation process is disabled...
                                                    if ("OK" === data) {
                                                        $("#serviceDescriptionForm").submit();
                                                    } else {
                                                        alert("Invalid Service Description. Reason: " + data);
                                                    }
                                                },
                                                error: function (xhr, ajaxOptions, thrownError) {
                                                    alert(xhr.status);
                                                    alert(thrownError);
                                                }
                                            });
                                        }

                                        var brokerPolicy = "<%=  (null == request.getAttribute("policy") ? "" : ((Policy) request.getAttribute("policy")).getContent().toString())%>";
        </script>

    </body>

</html>

