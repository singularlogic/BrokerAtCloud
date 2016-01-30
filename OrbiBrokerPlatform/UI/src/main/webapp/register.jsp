<!DOCTYPE html>
<html>
    <head>
        <title>Welcome &mdash; Orbi Broker</title>

        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="assets/css/bootstrap.min.css" type="text/css" />
        <link rel="stylesheet" href="assets/css/orbi.css" type="text/css" />
        <link href="http://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css" />
    </head>

    <body>

        <!-- Include Header -->
        <%@include file="inc/header.jsp" %>

        <main>
            <div class="container">

                <section id="login">
                    <div class="container">
                        <div class="row">
                            <div class="col-xs-12">
                                <div class="form-wrap">
                                    <img src="assets/img/ORBI_LOGO_ICON.png" width="200px">

                                    <h4>Create a new account</h4>

                                    <%                                        if (null != request.getParameter("msg")) {
                                            String msg = request.getParameter("msg");
                                            if (msg.equalsIgnoreCase("OK")) {
                                    %>

                                    <div class="alert alert-success" role="alert" >
                                        <h4><strong>SUCCESS!!!</strong></h4>
                                        <span>You are registered!</span>
                                    </div>
                                    <% } else if (msg.equalsIgnoreCase("MA")) {
                                    %>

                                    <div class="alert alert-danger" role="alert" >
                                        <h4><strong>ERROR!!!</strong></h4>
                                        <span>Please fill in all the required fields.</span>
                                    </div>

                                    <% } else if (msg.equalsIgnoreCase("ER")) {
                                    %>

                                    <div class="alert alert-danger" role="alert" >
                                        <h4><strong>ERROR!!!</strong></h4>
                                        <span>Please fill in all the required fields.</span>
                                    </div>

                                    <%                }
                                        }
                                    %>

                                    
                                    <form role="form" method="post" id="login-form" action="clients/register" autocomplete="off">
                                        <h5 class="pull-left">Account Details</h5>

                                        <div class="form-group">
                                            <label for="firstname" class="sr-only">First Name</label>
                                            <input type="text" name="firstname" id="firstname" class="form-control" placeholder="First Name">
                                        </div>
                                        <div class="form-group">
                                            <label for="lastname" class="sr-only">Last Name</label>
                                            <input type="text" name="lastname" id="lastname" class="form-control" placeholder="Last Name">
                                        </div>
                                        <div class="form-group">
                                            <label for="email" class="sr-only">Email</label>
                                            <input type="email" name="email" id="email" class="form-control" placeholder="Email">
                                        </div>
                                        <hr>
                                        <h5 class="pull-left">User Credentials</h5>

                                        <div class="form-group">
                                            <label for="username" class="sr-only">Username</label>
                                            <input type="text" name="username" id="username" class="form-control" placeholder="Username">
                                        </div>
                                        <div class="form-group">
                                            <label for="key" class="sr-only">Password</label>
                                            <input type="password" name="password" id="password" class="form-control" placeholder="Password">
                                        </div>
                                        <hr>
                                        <h5 class="pull-left">Account Type</h5><br/><br/>
                                        <div class="btn-group account-type">
                                            <select id="userRole" name="userRole" class="btn btn-default">
                                                <option selected="true" value="0">Choose account type</option>
                                                <option value="sp">Service Provider</option>
                                                <option value="eu">End User</option>
                                            </select>
                                        </div>

                                        <button type="submit" class="btn btn-primary btn-block">Create Account</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>

                </section>

            </div>
        </main>

        <!-- Include Footer -->
        <%@include file="inc/footer.jsp" %>

        <script src="assets/js/jquery-2.1.4.min.js"></script>
        <script src="assets/js/bootstrap.min.js"></script>
        <script src="assets/js/orbi.js"></script>

    </body>

</html>