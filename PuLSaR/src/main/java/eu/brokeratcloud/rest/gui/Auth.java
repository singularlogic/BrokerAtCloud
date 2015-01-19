package eu.brokeratcloud.rest.gui;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/auth")
public class Auth {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.rest.Auth");
	protected static String redirectUrl = "/logon.html";

	@GET
	@POST
	@Path("/menu")
	@Produces("text/html")
	@PermitAll
	public String getMenu(@Context HttpServletRequest request) {
		return getMenuStatic(request);
	}
	
	public static String getMenuStatic(@Context HttpServletRequest request) {
		String str = 
			"	<!-- SSI HEADER - BEGIN --> \n" +
			"	<style> \n" +
			"	#menu-grad { \n" +
			"		background: -webkit-linear-gradient(left,rgba(200,200,200,0.2),rgba(200,200,200,1)); /*Safari 5.1-6*/ \n" +
			"		background: -o-linear-gradient(right,rgba(200,200,200,0.2),rgba(200,200,200,1)); /*Opera 11.1-12*/ \n" +
			"		background: -moz-linear-gradient(right,rgba(200,200,200,0.2),rgba(200,200,200,1)); /*Fx 3.6-15*/ \n" +
			"		background: linear-gradient(to right, rgba(200,200,200,0.2), rgba(200,200,200,1)); /*Standard*/ \n" +
			"		padding: 5px; \n" +
			"		margin: 2px; \n" +
			"	}  \n" +
			"	</style> \n" +
			"	<div id=\"menu-grad\"> \n" +
			"		&nbsp;&nbsp;&nbsp;<font size=\"+1\">Welcome <i>"+request.getRemoteUser()+"</i></font> &nbsp;&nbsp;&nbsp;\n" +
			"		[&nbsp; \n" +
			"		<a href=\"/\">Dashboard</a> \n";
		
		if (request.isUserInRole("admin")) {
			str += 
			"		&nbsp;|&nbsp; \n" +
			"		<a href=\"/forms/admin/attribute-mgnt.html\">Admin: Serv. Attr. Mgnt</a> \n" +
			"		&nbsp;-&nbsp; \n" +
			"		<a href=\"/forms/admin/attribute-mapping.html\">Attr. Mapping</a> \n" +
			"		&nbsp;-&nbsp; \n" +
			"		<a href=\"/gui/admin/exportBrokerPolicy\">Export</a> \n";
		}
		if (request.isUserInRole("sc")) {
			str += 
			"		&nbsp;|&nbsp; \n" +
			"		<a href=\"/forms/consumer/pref-profile-mgnt.html\">Consumer: Pref. Profiles</a> \n" +
			"		&nbsp;-&nbsp; \n" +
			"		<a href=\"/forms/consumer/recoms.html\">Recommendations</a> \n";
		}
		
		str += 
			"		&nbsp;] \n" +
			"		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/logout.jsp\" onClick=\"return confirm('Logout?');\">Logout</a> \n" +
			"	</div> \n" +
			"	<!-- SSI HEADER - END --> \n";
		return str;
	}
	
/*	@GET
	@POST
	@Path("/logout")
	public Response logout(@Context HttpServletRequest request) {
		logger.debug("Auth: logging out...");
		if (request==null) return createResponse(500, "Could not retrieve session instance. Request object is null");
		HttpSession session = request.getSession(false);
		logger.debug("Auth: Session: {}", session);
		if (session==null) return createResponse(500, "Session is null");
		String user = request.getRemoteUser();
		session.invalidate();
		logger.info("Auth: Logged out user "+user+". Redirecting browser to loggin page");
		try {
			return Response.seeOther(new java.net.URI(redirectUrl)).build();
		} catch (Exception e) {
			return Response.ok().entity("<META http-equiv=\"refresh\" content=\"0;URL="+redirectUrl+"\" />").build();
		}
	}
	
	protected Response createResponse(int statusCode, String mesg) {
		if (statusCode>299) logger.warn("Auth: Error: Returning message: {}", mesg);
		return Response.status(statusCode).header("Connection", "Keep-Alive").header("Keep-Alive", "timeout=600, max=99").entity(mesg).build();
	}*/
}