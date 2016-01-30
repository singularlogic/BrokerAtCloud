<%@page import="org.broker.orbi.models.IaaSConfiguration"%>
<!DOCTYPE html>
<html>
    <head>
        <title>IaaS Configuration &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/servicedesc/list">Service Descriptions</a></li>
                            <li><a class="active" href="/orbibroker/iaasconfig/list">IaaS Configurations</a></li>
                            <li><a href="/orbibroker/appstore/list">App Store</a></li>
                            <!--<li><a href="/orbibroker/flavors/list">Flavors</a></li>-->
                            <li><a href="/orbibroker/offerings/list">Offerings</a></li>
                            <li><a href="/orbibroker/pulsar/notifications">Pulsar Notifications</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <a href="/orbibroker/iaasconfig/add" class="btn btn-danger pull-right action-button disabled">Add New</a>
                    <h2>IaaS Configuration<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> IaaS Configuration Add</p></h2>

                    <div class="table-responsive wrapper col-md-12">
                    <form role="form" action="store" method="post" id="iaas-form" autocomplete="off">

                        <input type="hidden" id="policyID" name="iaas_id" value="<%= (null == request.getAttribute("iaas_config") ? 0 : ((IaaSConfiguration) request.getAttribute("iaas_config")).getId())%>" >
                        

                        <div class="form-group col-md-7">
                            <label for="iaas_name" class="sr-only">IaaS Configuration Name</label>
                            <input type="text" name="iaas_name" id="iaas_name" class="form-control" placeholder="IaaS Configuration Name"  value="<%= (null == request.getAttribute("iaas_config") ? "" : ((IaaSConfiguration) request.getAttribute("iaas_config")).getName())%>" >
                        </div>
                        <div class="form-group col-md-7">
                            <label for="iaas_url" class="sr-only">IaaS Configuration Endpoint</label>
                            <input type="text" name="iaas_url" id="iaas_url" class="form-control" placeholder="http://" value="<%= (null == request.getAttribute("iaas_config") ? "" : ((IaaSConfiguration) request.getAttribute("iaas_config")).getEnd_point())%>">
                        </div>
                        <div class="form-group col-md-7">
                            <label for="iaas_tenant" class="sr-only">IaaS Tenant Name</label>
                            <input type="text" name="iaas_tenant" id="iaas_tenant" class="form-control" placeholder="Tenant Name" value="<%= (null == request.getAttribute("iaas_config") ? "" : ((IaaSConfiguration) request.getAttribute("iaas_config")).getTenant_name())%>">
                        </div>
                        <div class="form-group col-md-7">
                            <label for="iaas_username" class="sr-only">Username</label>
                            <input type="text" name="iaas_username" id="iaas_username" class="form-control" placeholder="Username" value="<%= (null == request.getAttribute("iaas_config") ? "" : ((IaaSConfiguration) request.getAttribute("iaas_config")).getUsername())%>">
                        </div>
                        <div class="form-group col-md-7">
                            <label for="iaas_paassword" class="sr-only">Paasword</label>
                            <input type="password" name="iaas_password" id="iaas_password" class="form-control" placeholder="Password" value="<%= (null == request.getAttribute("iaas_config") ? "" : ((IaaSConfiguration) request.getAttribute("iaas_config")).getPassword())%>">
                        </div>
                        <div class="form-group col-md-7">
                            <input type="submit" id="btn-save" class="btn btn-primary btn-block" value="Save">
                        </div>
                        </form>

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