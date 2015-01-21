<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Broker@Cloud - Service Profiles</title>

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
        <script src="jquery.tabelizer-master/jquery-ui-1.10.4.custom.min.js"></script>

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

        <script src="jquery.tabelizer-master/jquery.tabelizer.js"></script>
        <link href="jquery.tabelizer-master/tabelizer.min.css" media="all" rel="stylesheet" type="text/css" />

        <script>
            $(document).ready(function () {
                var table1 = $('#table1').tabelize({
                    /*onRowClick : function(){
                     alert('test');
                     }*/
                    fullRowClickable: true,
                    onReady: function () {
                        console.log('ready');
                    },
                    onBeforeRowClick: function () {
                        console.log('onBeforeRowClick');
                    },
                    onAfterRowClick: function () {
                        console.log('onAfterRowClick');
                    }
                });
            });

        </script>

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

                            <li>
                                <a href="policies">
                                    <i class="fa fa-wrench"></i>
                                    <span class="hidden-sm text">
                                        Manage Policies
                                    </span>
                                </a>
                            </li>
                            <li class="active">
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
                            <a href="aHomePage.jsp">
                                &nbsp;Orbi
                            </a>
                        </li>
                        <li class="active" >
                            Manage Service Providers
                        </li>                    
                    </ol>


                    <!-- panel 1 -->
                    <div class="row">
                        <div class="col-lg-12">

                            <div class="box">

                                <!-- PanelHeading -->
                                <div class="box-header" style="background-color: rgb(2, 185, 173); border-color: rgb(2,160,149); color: white;">
                                    <h2>
                                        <i class="fa fa-cog" style="color: white;"></i>
                                        <span class="break"></span>
                                        Service Providers
                                    </h2>
                                    <div class="box-icon">
                                        <a href="#" class="btn-minimize"><i class="fa fa-chevron-up" style="color: white;"></i></a>
                                        <a href="#" class="btn-close"><i class="fa fa-times" style="color: white;"></i></a>
                                    </div>
                                </div>

                                <div class="box-content clearfix">


                                    <table class="table table-striped table-bordered bootstrap-datatable datatable">

                                        <thead>
                                            <tr>
                                                <th>
                                                    Service Provider
                                                </th>
                                                <th>
                                                    Policy
                                                </th>
                                                <th>
                                                    Offering Name
                                                </th>
                                                <th>
                                                    Date Created
                                                </th>
                                                <th>
                                                    Actions
                                                </th>
                                            </tr>
                                        </thead>   



                                        <tbody>
                                            <c:forEach items="${spoffers}" var="spoffers">
                                                <tr>
                                                    <td><c:out value="${spoffers.username}"></c:out></td>
                                                    <td><c:out value="${spoffers.policy_name}"></c:out></td>
                                                    <td><c:out value="${spoffers.name}"></c:out></td>
                                                    <td><c:out value="${fn:substring(spoffers.date_created, 0, 19)}"></c:out></td>
                                                        <td><!--                                                                <a title="Edit" data-rel="tooltip" class="btn btn-xs btn-info" href="#">
                                                                    <i class="fa fa-edit"></i>  
                                                                </a>-->
                                                            <a title="Delete" data-rel="tooltip" class="btn btn-xs btn-danger" href="manageserviceproviders?remove=<c:out value="${spoffers.id}"></c:out>">
                                                                <i class="fa fa-trash-o"></i> 
                                                            </a>
                                                        </td>

                                                    </tr>
                                            </c:forEach>
                                        </tbody>   
                                    </table>


                                    <div class="row">&nbsp;</div>
                                    <br>

                                 


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