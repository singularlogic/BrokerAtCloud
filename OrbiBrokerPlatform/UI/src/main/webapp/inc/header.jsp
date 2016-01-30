<%@page import="org.broker.orbi.models.Consumer"%>
<%@page import="org.broker.orbi.service.UserService"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<header>
    <div class="container">
        <div class="col-md-12">

            <div class="brand"></div>

            <div class="profile">
                <%
                    String currentUser = (String) request.getAttribute("username");
                    if (null == currentUser) {
                        out.write(" <a href=\"login\" class=\"btn btn-default btn-sm\">Login</a>");
                        out.write(" <a href=\"register\" class=\"btn btn-default btn-sm\">Create Account</a>");
                    } else {
                        Consumer user = UserService.getUser(currentUser);
                        out.write("<span style='opacity:0.5; margin-right:10px'>" + user.getName() + " " + user.getSurname() + " (" + currentUser + ")</span>");
                        out.write("<a href=\"/orbibroker/logout\" class=\"btn btn-default btn-sm\">Logout</a>");
                    }
                %>
            </div>
        </div>
    </div>
</header>