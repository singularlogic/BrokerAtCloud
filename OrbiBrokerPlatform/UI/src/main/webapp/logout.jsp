<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%
        session.invalidate();
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Logout</title>
<script type="text/javascript">
        function closenreload() {
                if(window.opener) {
                    window.opener.location='/orbibroker';
                    window.close();
                    window.opener.focus();
                } else {
                    window.location='/orbibroker';
                }
        }
</script>
    </head>
    <body>
<script type="text/javascript">
        window.setTimeout('closenreload()',500);
</script>
    </body>
</html>
