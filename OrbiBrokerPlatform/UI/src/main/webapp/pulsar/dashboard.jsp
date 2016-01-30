<!DOCTYPE html>
<html>
    <head>
        <title>Active Policies &mdash; ORBI</title>

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
                            <li><a href="/orbibroker/policies/list">Broker Policies</a></li>
                            <li><a href="/orbibroker/providers/list">Providers</a></li>
                            <li><a href="/orbibroker/clients/list">Clients</a></li>
                            <li><a class="active" href="/orbibroker/pulsar/dashboard">Active Policies</a></li>
                            <li><a href="/orbibroker/pulsar/management">Serv. Attr. Mgmt</a></li>
                            <li><a href="/orbibroker/pulsar/mapping">Attr. Mapping</a></li>
                            <li><a href="/orbibroker/configuration/list">Configuration</a></li>
                        </ul>
                    </nav>
                </div>

                <div class="col-md-9">

                    <h2>Active Policies<p class="lead"><span class="glyphicon glyphicon-asterisk"></span> Manage active broker policies</p></h2>

                    <!-- 192.168.3.34 -->
                    <form id="autologinForm" target="myIframe" action="http://pulsar.euprojects.net:3353/j_security_check" method="post" >
                        <input type="hidden" name="j_username" value="admin" />
                        <input type="hidden" name="j_password" value="admin" />
                        <input type="hidden" name="submit" value="Login" />
                        <button onclick="redirectPage()" style="display:none" id="autoLoginBTN" type="submit">AutoLogin</button>
                    </form>

                    <div class="table-responsive wrapper-iframe col-md-12" style="width:1300px">
                        <iframe id="myIframe" src="" name="myIframe" width="1260" height="500" frameBorder="0"></iframe>
                    </div>

                </div>

            </div>
        </main>

        <!-- Include Footer -->
        <%@include file="../inc/footer.jsp" %>

        <script src="../assets/js/jquery-2.1.4.min.js"></script>
        <script src="../assets/js/bootstrap.min.js"></script>
        <script src="../assets/js/orbi.js"></script>
        <script>
                            $(document).ready(function () {
                                linkLocation = "http://pulsar.euprojects.net:3353/index.jsp?standalone=no";
                                console.log("1")
                                $("#autoLoginBTN").click();
                                console.log("2")
                            });

//                            login();
//                            redirectPage();

                            function redirectPage() {

                                function toRedirect() {
                                    console.log("i will redirect to: " + linkLocation);
                                    $('#myIframe').attr('src', linkLocation);
                                    $('#myIframe').fadeIn()
                                }
                                setTimeout(toRedirect, 1000);
                            }

//                            function login() {
//                                $.ajax({
//                                     url: "http://192.168.3.34:3353/j_security_check",
////                                    headers: {'Access-Control-Allow-Origin': '*'},
//                                    method: "POST",
//                                    data: {j_username: "sc1", j_password: "sc1"},
//                                    dataType: "text",
//                                    success: function (data) {
//                                        console.log("Login successful");
//                                    },
//                                    error: function (xhr, ajaxOptions, thrownError) {
////                                        alert(xhr.status);
////                                        alert(thrownError);
//                                    }
//                                });
//                            }
        </script>


    </body>

</html>