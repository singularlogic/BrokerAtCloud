<!DOCTYPE html>
<html>
    <head>
        <title>Error &mdash; ORBI</title>

        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="/orbibroker/assets/css/bootstrap.min.css" type="text/css" />
        <link rel="stylesheet" href="/orbibroker/assets/css/orbi.css" type="text/css" />
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
                                    <img src="/orbibroker/assets/img/ORBI_LOGO_ERROR.png" width="200px">
                                    <div class="alert alert-danger" role="alert" >
                                        <h4><strong>ERROR!!1</strong></h4>
                                        <span>Page not found</span><br/>
                                        <span>(redirecting to dashboard...)</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </main>

        <footer>
            <div class="container">
                <div class="col-md-12">
                    <div>ORBI Dashboard v0.1</div>
                </div>
            </div>
        </footer>

        <script src="/orbibroker/assets/js/jquery-2.1.4.min.js"></script>
        <script src="/orbibroker/assets/js/bootstrap.min.js"></script>
        <script src="/orbibroker/assets/js/orbi.js"></script>


        <script>
            $(document).ready(function () {
                setTimeout(function () {
                    document.location.href = '/orbibroker';
                }, 4000);
            });
        </script>
    </body>

</html>