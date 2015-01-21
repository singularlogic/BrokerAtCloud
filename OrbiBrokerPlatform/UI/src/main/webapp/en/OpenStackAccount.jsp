<%@page import="org.broker.orbi.ui.IaaSProvider"%>
<%@page import="org.broker.orbi.ui.ImageTemplate"%>
<%@page import="org.broker.orbi.ui.Flavor"%>
<%@page import="org.broker.orbi.ui.SPOffer"%>
<%@page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Broker@Cloud - IaaS Account</title>

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
        <script src="http://code.jquery.com/jquery-1.10.2.js"></script>

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
                                                                    <a href="aHomePage.jsp">
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

                            <!--li>
                                <a href="#">
                                    <i class="fa fa-home" style="margin-right: 2px;"></i>
                                    <span class="hidden-sm text">
                                        Home Page
                                    </span>
                                </a>
                            </li-->
                            <li >
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

                            <li class="active">
                                <a href="modifyIaaSProvider">
                                    <i class="fa fa-user" style="margin-right: 2px;"></i>
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
                        <li class="active" >
                            View Offerings
                        </li>                    
                    </ol>


                    <!-- panel 1 -->
                    <div class="row">
                        <div class="col-sm-12">

                            <div class="box">

                                <!-- PanelHeading -->
                                <div class="box-header" style="background-color: rgb(2, 185, 173); border-color: rgb(2,160,149); color: white;">
                                    <h2>
                                        <i class="fa fa-cog" style="color: white;"></i>
                                        <span class="break"></span>
                                        <%= request.getUserPrincipal().getName()%>'s IaaS Details
                                    </h2>
                                    <div class="box-icon">
                                        <a href="#" class="btn-minimize"><i class="fa fa-chevron-up" style="color: white;"></i></a>
                                        <a href="#" class="btn-close"><i class="fa fa-times" style="color: white;"></i></a>
                                    </div>
                                </div>

                                <br />


                                <% if (null != request.getAttribute("updateIAAS")) {
                                        if (request.getAttribute("updateIAAS").toString().equalsIgnoreCase("SUCCESS")) {

                                            out.write("<div class=\"text-center alert alert-success\"><strong>Congratulations!</strong> IaaS Details has been successfully updated!<br></div>");

                                        } else {
                                            out.write("<div class=\"text-center alert alert-danger\"><strong>Sorry!</strong> IaaS Details could not be updated!<br></div>");
                                        }
                                    }
                                %>

                                <div class="box-content clearfix">
                                    <% IaaSProvider iassProvider = new IaaSProvider();

                                        if (null != request.getAttribute("iaasProvider")) {
                                            iassProvider = (IaaSProvider) request.getAttribute("iaasProvider");
                                        }
                                    %>
                                    <form class="form-horizontal login" action="#" method="post">
                                        <fieldset class="col-sm-12">
                                            <input type="text" class="form-control" id="iaas_id" name ="iaas_id"  value="<%= iassProvider.getId()%>"  style="visibility:hidden;display:none">
                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="col-lg-4"><label>Username: </label></div>
                                                    <div class="input-group col-sm-12">	
                                                        <input type="text" class="form-control" id="username" name ="username" placeholder="Username" value="<%= iassProvider.getUsername()%>">
                                                        <span class="input-group-addon"><i class="fa fa-user"></i></span>
                                                    </div>	
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="col-lg-4"><label>Password: </label></div>
                                                    <div class="input-group col-sm-12">	
                                                        <input type="password" class="form-control" id="password" name="password" placeholder="Password" value="<%= iassProvider.getPassword()%>">
                                                        <span class="input-group-addon"><i class="fa fa-key"></i></span>
                                                    </div>	
                                                </div>
                                            </div>


                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="col-lg-4"><label>Tenant Name: </label></div>
                                                    <div class="input-group col-sm-12">	
                                                        <input type="text" class="form-control" id="tenant_name" name="tenant_name" placeholder="Tenant Name" value="<%= iassProvider.getTenant_name()%>">
                                                        <span class="input-group-addon"><i class="fa fa-users"></i></span>
                                                    </div>	
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="col-lg-4"><label>Endpoint: </label></div>
                                                    <div class="input-group col-sm-12">	
                                                        <input type="text" class="form-control" id="end_point" name="end_point" placeholder="End-Point" value="<%= iassProvider.getEnd_point()%>">
                                                        <span class="input-group-addon"><i class="fa fa-building-o"></i></span>
                                                    </div>	
                                                </div>
                                            </div>
                                                        
                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="col-lg-4"><label>Failure Prevention Endpoint: </label></div>
                                                    <div class="input-group col-sm-12">	
                                                        <input type="text" class="form-control" id="end_point" name="failure_end_point" placeholder="Failure Prevention End-Point" value="<%= iassProvider.getPrevention_mechanism_endpoint()%>">
                                                        <span class="input-group-addon"><i class="fa fa-building-o"></i></span>
                                                    </div>	
                                                </div>
                                            </div>            


                                            <br>

                                            <div class="row">
                                                <button type="submit" class="btn btn-lg btn-primary col-xs-12">Update</button>
                                            </div>
                                            <div class="row">&nbsp;</div>

                                            <div class="row" hidden="true">
                                                <div class="alert alert-dismissable alert-danger" style="">
                                                    <button type="button" class="close" data-dismiss="alert">
                                                        ×
                                                    </button>
                                                    <strong>
                                                        Sorry!
                                                    </strong>
                                                    <a href="#" class="alert-link">
                                                        You have to fill all mandatory fields.
                                                    </a>
                                                    <em>

                                                    </em>
                                                </div>
                                            </div>

                                        </fieldset>	
                                    </form>

                                </div>
                            </div>
                        </div>
                        <!-- /panel 1 -->

                    </div><!-- /row (line_1037)-->

                </div>
            </div>
            <div class="row">&nbsp;</div>
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


<script>
            $(document).ready(function () {

                $("select").change(function () {
                    var selectValue = $(this).val();
                    var selectID = $(this).attr('id');
                    if (typeof selectID !== "undefined") {
                        if ((selectID.indexOf('flavor_') == 0) || (selectID.indexOf('it_') == 0)) {
                            var actualID = selectID.split("_")[1];

                            jQuery.ajax({
                                featureClass: "P",
                                style: "full",
                                url: "modifySPOffer",
                                dataType: "jsonp",
                                contentType: "text/html; charset=UTF-8",
                                encoding: "UTF-8",
                                beforeSend: function (xhr) {
                                    xhr.setRequestHeader("Content-Type", "text/html;charset=utf-8");
                                },
                                data: {
                                    format: "json",
                                    offer_id: actualID,
                                    select_value: selectValue,
                                    select_type: selectID.split("_")[0]
                                },
                                success: function (data) {

                                    // Check if the resultset contains any data 
                                    if (data == true) {
                                        alert("Modifcations of current offering were saved!");
                                    } else {

                                        alert("Problem Occured!");
                                    }
                                },
                                error: function () {
                                    alert("Problem Occured!");
                                }
                            });
                        }

                    }

                });

            });
</script>

