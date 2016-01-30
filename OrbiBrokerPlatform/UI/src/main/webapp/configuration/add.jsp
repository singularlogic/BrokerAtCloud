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
        <title>Configuration &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/policies/list">Broker Policies</a></li>
                            <li><a href="/orbibroker/providers/list">Providers</a></li>
                            <li><a href="/orbibroker/clients/list">Clients</a></li>
                            <li><a href="/orbibroker/pulsar/dashboard">Active Policies</a></li>
                            <li><a href="/orbibroker/pulsar/management">Serv. Attr. Mgmt</a></li>
                            <li><a href="/orbibroker/pulsar/mapping">Attr. Mapping</a></li>
                            <li><a class="active" href="/orbibroker/configuration/list">Configuration</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <a href="/orbibroker/configuration/list" class="btn btn-default pull-right action-button">Cancel</a>
                    <a href="/orbibroker/configuration/save" class="btn btn-danger pull-right action-button">Save</a>
                    <h2>Configuration<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Configuration Add</p></h2>
                    
                    <h1>Policies</h1>
                    <p class="lead">Policies List</p>

                    <div class="col-md-12">
                        <div class="row">
                            <div id="serviceLoadArea" class="collapse in">

                                <p>Note: You can load a valid broker policy into the editor.</p>
                                <h1> Use drag and drop functionality to load the file containing the broker policy!</h1>

                                <div class="droptarget" ondrop="loadBP(event)" ondragover="allowDrop(event)">
                                    <p id="bp"> Drop your broker policy here! </p>
                                </div>

                            </div>

                            <div id="serviceEditArea" class="collapse in">
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1>Specify your Broker Namespace </h1></div>
                                    <div class="panel-body">
                                        <!--p> Specify your broker namespace </p-->
                                        <form>
                                            <div class="form-group">
                                                <label>Broker Namespace</label>
                                                <input type="text" class="form-control" id="bp_namespace" value="http://yourbrokernamespace.com">
                                            </div>
                                        </form>
                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1> Describe your legal business entity </h1>
                                    </div>
                                    <div class='panel-body'>
                                        <form>
                                            <div class="form-group">
                                                <label>Broker acronym</label>
                                                <input class="form-control" type="text" id="bp_business_entity" value="YourBrokerCompanyAcronym">
                                            </div>
                                            <div class="form-group">
                                                <label>Broker legal name</label>
                                                <input class="form-control" type="text" id="bp_legal_name" value="Your BrokerCompany Legal Name">
                                            </div>
                                        </form>

                                    </div>
                                </div>

                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1> Describe your broker policy </h1>
                                    </div>
                                    <div class='panel-body'>
                                        <form>
                                            <div class="form-group">
                                                <label>Title</label>
                                                <input class="form-control" type="text" id="bp_instance" value="YourBrokerPolicyTitle">
                                            </div>
                                            <div class="form-group">
                                                <label>Broker policy model name</label>
                                                <input class="form-control" type="text" id="bp_model" value="YourBrokerPolicyModelName">
                                            </div>
                                        </form>



                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1> Provide classification taxonomy</h1>
                                    </div>
                                    <div class='panel-body'>
                                        <form>
                                            <div class="form-group">
                                                <label>Prefix</label>
                                                <input class="form-control" type="text" id="classtaxpref" value="fc">
                                            </div>
                                            <div class="form-group">
                                                <label>URI</label>
                                                <input class="form-control" type="text" id="classtaxURI" value="http://www.broker-cloud.eu/service-descriptions/CAS/categories">
                                            </div>
                                            <div class="form-group">
                                                <label>Root concept</label>
                                                <input class="form-control" type="text" id="classtaxroot" value="rootConcept">
                                            </div>
                                        </form>


                                        <table class="table" style="width: 95%" id="classtaxconcepts">
                                            <tr>
                                                <th></th>
                                                <th>#</th>
                                                <th>Classification tag</th>
                                                <th>Description</th>
                                                <th>Label</th>
                                            </tr>
                                        </table>
                                        <div class="bc-button4table" style="width: 95%">
                                            <button class="btn btn-default" id="classtax_table_add" onclick="addClassificationConcept()"> Add </button>
                                            <button class="btn btn-default" id="classtax_table_delete" onclick="deleteClassificationConcept()"> Delete </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1>Specify your service level profile type </h1>
                                    </div>
                                    <div class='panel-body'>
                                        <form>
                                            <label>Service Level Profile</label>
                                            <input type="text" class="form-control" id="slp_class" value="SomeServiceLevelProfile"> </p>
                                        </form>
                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1>Specify Quantitative Service Level Schemas</h1>
                                    </div>
                                    <div class='panel-body'>
                                        <table class="table" style="width: 95%" id="quantserviceleveldetails">
                                            <tr>
                                                <th></th>
                                                <th>#</th>
                                                <th>Service Level</th>
                                                <th>Description</th>
                                                <th>Unit of Measurement</th>
                                                <th>Value type</th>
                                                <th>Min Value</th>
                                                <th>Max Value</th>
                                                <th>Is Range</th>
                                                <th>Higher is better</th>
                                            </tr>
                                        </table>
                                        <div class="bc-button4table" style="width: 95%">
                                            <button class="btn btn-default" id="quant_sl_table_add" onclick="addQuantSLConcept()"> Add </button>
                                            <button class="btn btn-default" id="quant_sl_table_delete" onclick="deleteQuantSLConcept()"> Delete </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h1>Qualitative Service Levels</h1>
                                    </div>
                                    <div class='panel-body'>
                                        <h2>Specify Qualitative Service Level Schemas </h2>

                                        <table class="table" style="width: 95%" id="qualserviceleveldetails">
                                            <tr>
                                                <th></th>
                                                <th>#</th>
                                                <th>Service Level</th>
                                                <th>Description</th>
                                            </tr>
                                        </table>
                                        <div class="bc-button4table" style="width: 95%">
                                            <button class="btn btn-default" id="qual_sl_table_add" onclick="addQualSLConcept()"> Add </button>
                                            <button class="btn btn-default" id="qual_sl_table_delete" onclick="deleteQualSLConcept()"> Delete </button>
                                        </div>

                                        <div class="bc-buttongroup">
                                            <button class="btn btn-default" id="safe_schemas" onclick="save_SLSchemas()">Save the Service Level Schemas. </button>

                                            <button class="btn btn-default" id="edit_schemas" disabled="disabled" onclick="activateInputFields()">Edit the Service Level Schemas. </button>
                                        </div>
                                        <div class="panel panel-default collapse" id='qualValuePanel'>
                                            <div class="panel-heading">
                                                <h2>Qualitatve value sets </h2>
                                            </div>
                                            <div class='panel-body'>
                                                <p id="qualvalues">

                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="bc-buttongroup">


                                        <button onclick="submitBrokerPolicyForm()" class="btn btn-default" style="background-color: rgb(141, 199 , 72); color: white; margin-left: 10px; width: 150px;">Save! </button>
                                        &nbsp;&nbsp;&nbsp;
                                        <button class="btn btn-default" onclick="storeBP()">Show the business policy specification. </button>

                                        <form name="brokerPolicyForm" id="brokerPolicyForm" action="store" method="post" >
                                            <input type="hidden" id="policyID" name="policyID" value="<%= (null == request.getAttribute("policy") ? 0 : ((Policy) request.getAttribute("policy")).getId())%>" >
                                            <input type="hidden" id="brokerPolicyContent" name="brokerPolicyContent">
                                            <input type="hidden" id="brokerPolicyName" name="brokerPolicyName">
                                        </form>
                                        <br/><br/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- End Editor -->


                    </div>

                    <!--
                                        <form role="form" action="#" th:action="@{/register}" th:object="${user}" method="post" id="login-form" autocomplete="off">
                                            <div class="form-group">
                                                <label for="firstname" class="sr-only">Policy Name</label>
                                                <input type="text" th:field="*{firstname}" name="firstname" id="firstname" class="form-control" placeholder="First Name" value="" autofocus="true" />
                                            </div>
                                            <div class="form-group">
                                                <label for="lastname" class="sr-only">Variables</label>
                                                <input type="text" th:field="*{lastname}" name="lastname" id="lastname" class="form-control" placeholder="Last Name" value="" />
                                            </div>
                                            <div class="form-group">
                                                <label for="email" class="sr-only">Profiles</label>
                                                <input type="email" th:field="*{mail}" name="mail" id="email" class="form-control" placeholder="Email" value="" />
                                            </div>
                                            <hr />
                    
                                            <input type="submit" value="Save Policy" class="btn btn-danger"/>
                                        </form>-->

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
        <script src="../assets/policyeditor/policyeditor.js"></script>

        <script src="../assets/js/orbi.js"></script>

        <script>

                                            function submitBrokerPolicyForm() {
                                                $("#brokerPolicyName").val($("#bp_model").val());
                                                $("#brokerPolicyContent").val(getCurrentBP());
                                                $.ajax({
                                                    url: "../broker_policy_validation",
                                                    method: "POST",
                                                    data: {brokerPolicyContent: getCurrentBP()},
                                                    dataType: "text",
                                                    success: function (data) {
                                                        if ("OK" === data) {
                                                            $("#brokerPolicyForm").submit();
                                                        } else {
                                                            alert("Invalid Broker Policy. Reason: " + data);
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

