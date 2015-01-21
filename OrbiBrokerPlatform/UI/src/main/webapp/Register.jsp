
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Broker@Cloud - Register</title>

        <link rel="shortcut icon" type="image/png" href="favicon.png"/>

        <!-- start: CSS -->
        <link href="assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="assets/css/style.min.css" rel="stylesheet">
        <link href="assets/css/retina.min.css" rel="stylesheet">
        <link href="assets/css/print.css" rel="stylesheet" type="text/css" media="print"/>
        <!-- end: CSS -->

        <!-- start: JavaScript-->
        <!--[if !IE]>-->

        <script src="assets/js/jquery-2.0.3.min.js"></script>

        <!--<![endif]-->

        <!--[if IE]>
        
                <script src="assets/js/jquery-1.10.2.min.js"></script>
        
        <![endif]-->

        <!--[if !IE]>-->

        <script type="text/javascript">
            window.jQuery || document.write("<script src='assets/js/jquery-2.0.3.min.js'>" + "<" + "/script>");
        </script>

        <!--<![endif]-->

        <!--[if IE]>
        
                <script type="text/javascript">
                window.jQuery || document.write("<script src='../assets/js/jquery-1.10.2.min.js'>"+"<"+"/script>");
                </script>
                
        <![endif]-->
        <script src="assets/js/jquery-migrate-1.2.1.min.js"></script>
        <script src="assets/js/bootstrap.min.js"></script>


        <!-- page scripts -->
        <script src="assets/js/jquery-ui-1.10.3.custom.min.js"></script>
        <script src="assets/js/jquery.sparkline.min.js"></script>
        <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="assets/js/excanvas.min.js"></script><![endif]-->
        <script src="assets/js/jquery.knob.modified.min.js"></script>
        <script src="assets/js/jquery.easy-pie-chart.min.js"></script>
        <script src="assets/js/raphael.min.js"></script>
        <script src="assets/js/justgage.1.0.1.min.js"></script>

        <!-- theme scripts -->
        <script src="assets/js/custom.min.js"></script>
        <script src="assets/js/core.min.js"></script>
        <!-- inline scripts related to this page -->
        <script src="assets/js/pages/ui-elements.js"></script>
        <!-- inline scripts related to this page -->
        <script src="assets/js/pages/charts-other.js"></script>

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
                    <img src="Orbi - Logo - transp 2.png" height="40" alt="">
                </a>		
                <a class="navbar-brand col-md-2 col-sm-1 col-xs-2" href="">
                    <span>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        Orbi - Broker
                    </span>
                </a>
                <div id="search" class="col-sm-5 col-xs-8 col-lg-3" style="margin:8px 0 0px 15%">
                    <strong>
                        <a href="http://portal.singularlogic.eu/">
                            <em>powered by</em> SingularLogic
                        </a>
                    </strong>
                </div>

                <!-- HeaderMenu -->
                <div class="nav-no-collapse header-nav">
                    <ul class="nav navbar-nav pull-right">

                        <!-- Englinsh - -
                        <li class="dropdown hidden-xs">
                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                                <img src="English-Language.png" height="20">
                            </a>
                        </li>
                        <!- - /English - -

                        <!- - Spanish - -
                        <li class="dropdown hidden-xs">
                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="index.html#">
                                <img src="spanish.png" height="25">
                            </a>
                        </li>
                        <!- - /Spanish -->

                    </ul>
                </div>
                <!-- /HeaderMenu -->

            </div>	
        </header>
        <!-- /HeaderBar -->


        <div id="content" class="col-sm-12 full">
            <div class="row">

                <div class="col-lg-4">&nbsp;</div>

                <!-- Log In 1-->
                <div class="row">	
                    <div class="col-lg-4">
                        <div class="box">
                            <div class="box-header" style="background-color: rgb(2, 185, 173); color: white;">
                                <h2><i class="fa fa-list" style="color: white;"></i>User Registration</h2>
                            </div>
                            <div class="box-content clearfix">

                                <div class="text-center">

                                    <h2>
                                        <em>
                                            Welcome to Orbi Broker
                                        </em>
                                    </h2>

                                    Powered by&nbsp;
                                    <a href="http://portal.singularlogic.eu/">
                                        <img src="logo_singularlogic.jpg"  alt="" height="20">
                                    </a>

                                    <p>&nbsp;</p>

                                    <form class="form-horizontal login" action="registerNewUser" method="post">
                                        <fieldset class="col-sm-12">
                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="input-group col-sm-12">	
                                                        <input type="text" class="form-control" name="username" placeholder="Username"/>
                                                        <span class="input-group-addon"><i class="fa fa-user"></i></span>
                                                    </div>	
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="input-group col-sm-6">	
                                                        <input type="text" class="form-control" name="surname" placeholder="Surname"/>
                                                        <span class="input-group-addon"><i class="fa fa-user"></i></span>
                                                    </div>	
                                                    <div class="input-group col-sm-6">	
                                                        <input type="text" class="form-control" name="name" placeholder="Name"/>
                                                        <span class="input-group-addon"><i class="fa fa-user"></i></span>
                                                    </div>	
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="input-group col-sm-12">	
                                                        <input type="text" class="form-control" name="email" placeholder="E-mail"/>
                                                        <span class="input-group-addon"><strong>@</strong></span>
                                                    </div>	
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="input-group col-sm-12">	
                                                        <input type="password" class="form-control" name="password" placeholder="Password"/>
                                                        <span class="input-group-addon"><i class="fa fa-key"></i></span>
                                                    </div>	
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <div class="controls row">
                                                    <div class="input-group col-sm-12">
                                                        <select id="userRole" name="userRole" class="form-control">
                                                            <option value ="0">
                                                                select user role...
                                                            </option>
                                                            <option value ="1">
                                                                Service Provider
                                                            </option>
                                                            <option value ="2">
                                                                End User
                                                            </option>
                                                        </select>
                                                    </div>	
                                                </div>
                                            </div>	
                                            <br>

                                            <div class="row">
                                                <button type="submit" class="btn btn-lg btn-primary col-xs-12">Register</button>
                                            </div>
                                            <div class="row">&nbsp;</div>
                                            <%

                                                if (null != request.getParameter("msg")) {
                                                    if (request.getParameter("msg").equalsIgnoreCase("MA")) {

                                            %>

                                            <div class="row">
                                                <div class="alert alert-dismissable alert-danger" style="">
                                                    <strong>
                                                        Sorry!
                                                    </strong>
                                                    <a href="" class="alert-link" style="cursor: initial">
                                                        You have to fill all the fields.
                                                    </a>
                                                    <em>

                                                    </em>
                                                </div>
                                            </div>
                                            <%                                            } else if (request.getParameter("msg").equalsIgnoreCase("ER")) {
                                            %>
                                            <div class="row">
                                                <div class="alert alert-dismissable alert-danger" style="">
                                                    <strong>
                                                        Sorry!
                                                    </strong>
                                                    <a href="" class="alert-link" style="cursor: initial">
                                                        An Error has occured. Try again!
                                                    </a>
                                                    <em>

                                                    </em>
                                                </div>
                                            </div>
                                            <%
                                            } else if (request.getParameter("msg").equalsIgnoreCase("OK")) {
                                            %>
                                            <div class="row">
                                                <div class="alert alert-dismissable alert-success" style="">
                                                    <strong>
                                                        Congratulations!
                                                    </strong>
                                                    <a href="/orbibroker/" class="alert-link" >
                                                        User created successfully! Click to Login!
                                                    </a>
                                                    <em>
                                                    </em>
                                                </div>
                                            </div>
                                            <%
                                                    }
                                                }
                                            %>
                                        </fieldset>	

                                    </form>


                                    <div class="clearfix"></div>


                                </div>

                            </div>	
                        </div><!--/col-->

                    </div>
                    <!-- /Log In 1-->

                    <!--  - - - - - - - - - - - - - -->


                </div>
            </div>


            <div class="clearfix">&nbsp;</div>

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

    </body>
</html>