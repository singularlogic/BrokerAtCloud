<!DOCTYPE html>
<html>
    <head>
        <title>Welcome &mdash; ORBI</title>

        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="assets/css/bootstrap.min.css" type="text/css" />
        <link rel="stylesheet" href="assets/css/orbi.css" type="text/css" />
        <link href="http://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css" />
    </head>

    <body>
        
        <!-- Include Header Bar -->
        <%@include file="inc/header.jsp" %>
        
        <main>
            <div class="container">

                <div class="jumbotron">
                    <h1><img src="assets/img/ORBI_LOGO_WHITE.png" width="300px">
                        <p class="lead">
                            <span class="glyphicon glyphicon-ok"></span> Enterprise IT environment is progressively transformed into a matrix of interwoven infrastructure.<br>
                            <span class="glyphicon glyphicon-ok"></span> Services are delivered from diverse service providers.<br>
                            <span class="glyphicon glyphicon-ok"></span> It is time to deal with the overwhelming complexity.<br>
                        </p>
                        <a href="/orbibroker/register" class="btn btn-danger" role="button">Join the Orbi Broker platform &raquo;</a>
                </div>

                <!-- Example row of columns -->
                <div class="row">
                    <div class="col-md-4">
                        <h3>What is Orbi Broker?</h3>                        
                        <iframe class= scrolling="no" allowTransparency="true" frameborder="0" width="330" height="200" src="https://goanimate.com/player/embed/0hrIbCTJD4cM?utm_source=social&utm_medium=tumblr&utm_campaign=usercontent"></iframe>
                    </div>
                    <div class="col-md-4">
                        <h3>Our Competitive Advantages</h3>
                        <p><span class="glyphicon glyphicon-ok"></span> Broker Policy Management</p>
                        <p><span class="glyphicon glyphicon-ok"></span> Complete Service Lifecycle Management</p>
                        <p><span class="glyphicon glyphicon-ok"></span> Advanced Matchmaking algorithms</p>
                        <p><span class="glyphicon glyphicon-ok"></span> Integration with public cloud infrastructure</p>
                    </div>

                    <div class="col-md-4">
                        <h3>Benefits</h3>
                        <p><span class="glyphicon glyphicon-ok"></span> Find the best-fit service for you</p>
                        <p><span class="glyphicon glyphicon-ok"></span> Monitoring</p>
                        <p><span class="glyphicon glyphicon-ok"></span> Failure Prevention Analysis</p>
                    </div>
                </div>

            </div>
        </main>

        <!-- Include Footer -->
        <%@include file="inc/footer.jsp" %>
        
        <script src="assets/js/jquery-2.1.4.min.js"></script>
        <script src="assets/js/bootstrap.min.js"></script>
        <script src="assets/js/orbi.js"></script>

    </body>

</html>