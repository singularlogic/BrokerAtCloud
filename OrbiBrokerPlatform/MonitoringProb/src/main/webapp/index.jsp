<%@page import="org.broker.orbi.scheduler.MonitoringScheduler"%>
<%@page import="org.broker.orbi.quartz.BrokerScheduler"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!--meta http-equiv="refresh" CONTENT="30; URL=index.jsp"-->
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="icon" href="resources/img/broker@cloud.png">
        <title>Broker@Cloud</title>

        <link href="resources/css/bootstrap.min.css" rel="stylesheet">
        <link href="resources/css/broker.css" rel="stylesheet">
    </head>
    <body>

        <div class="container-fluid">
            <div class="row">
                <div class="col-sm-9 col-md-12 main">
                    <h1 class="page-header"><center><img src="resources/img/broker@cloud.png"></center></h1>

                    <div class="row placeholders">
                        <div class="col-xs-6 col-sm-12 placeholder">
                            <h4></h4>

                            <span class="text-muted">Monitoring status: <%= MonitoringScheduler.INSTANCE.getMonitoringSchedulerState()%></span>
                            <br>
                            <br>
                            <center>
                            <form class="form-horizontal" action="changeStatusOfMonitoringScheduler" method="post">

                                
                                    <% if (MonitoringScheduler.INSTANCE.getMonitoringSchedulerStateAsString().equalsIgnoreCase("RUNNING")) {
                                    %>

                                    <button type="submit" class="btn btn-lg btn-primary col-xs-4" style="float:center">STOP</button>

                                    <%
                                    } else {
                                    %>

                                    <button type="submit" class="btn btn-lg btn-primary col-xs-4" style="float:center">START</button>

                                    <%
                                        }
                                    %>
                            </form>
                            </center>
                        </div>
                    </div>
                </div>
            </div>
        </div>


        <script src="resources/js/jquery.min.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>
        <script src="resources/js/docs.min.js"></script>
    </body>
</html>
