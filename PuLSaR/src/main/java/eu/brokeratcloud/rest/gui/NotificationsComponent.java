package eu.brokeratcloud.rest.gui;

import java.io.*;
import java.util.*;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import javax.ws.rs.client.Entity;

import eu.brokeratcloud.opt.Notification;
import eu.brokeratcloud.persistence.annotations.RdfSubject;
import eu.brokeratcloud.rest.opt.ProfileManagementService;


@Path("/gui/notifications")
public class NotificationsComponent extends AbstractFacingComponent {

	public NotificationsComponent(@Context HttpServletRequest request) throws IOException {
		super(request);
		loadConfig();
	}
	
	public NotificationsComponent(String propertiesFile) throws IOException {
		loadConfig(propertiesFile);
	}
	
	// =====================================================================================================
	
	@GET
	@Path("/list")
	@Produces("application/json")
	@RolesAllowed({"admin", "sp"})
	public Notification[] getNotifications() throws IOException {
		return null;
	}
	
	@GET
	@Path("/{service_id}/list")
	@Produces("application/json")
	@RolesAllowed({"admin", "sp"})
	public Notification[] getNotificationsForService(@PathParam("service_id") String serviceId) throws IOException {
		return null;
	}
	
	@GET
	@Path("/{notif_id}")
	@Produces("application/json")
	@RolesAllowed({"admin", "sp"})
	public Notification getNotification(@PathParam("notif_id") String notifId) throws IOException {
		return null;
	}
}