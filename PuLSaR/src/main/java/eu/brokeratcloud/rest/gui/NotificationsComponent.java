/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.opt.Notification;
import eu.brokeratcloud.persistence.annotations.RdfSubject;
import eu.brokeratcloud.rest.opt.NotificationManagementService;
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
	@Path("/list-services")
	@Produces("application/json")
	@RolesAllowed({"admin", "sp"})
	public String getProviderServices() throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getProviderServices: INPUT: n/a");
		
		// Call REST service in order to get services list
		long callStartTm = System.currentTimeMillis();
		ServiceDescription[] sdList = (ServiceDescription[])_callBrokerRestWS(baseUrl+"/opt/aux/offerings/sp/"+getUsername()+"/list", "GET", java.lang.reflect.Array.newInstance(ServiceDescription.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		String rowHead = "{ \"id\":\"%s\", \"createTimestamp\":%d, \"service-name\":\"%s\", \"slp-uri\":\"%s\", \"slp-id\":\"%s\"}";
		boolean first = true;
		for (int i=0; i<sdList.length; i++) {
			ServiceDescription sd = sdList[i];
			if (sd==null || sd.getId()==null || sd.getId().isEmpty()) continue;  // i.e. sd is null or empty
			
			// get service information
			String sdId = sd.getId();
			String sdName = sd.getName();
			String slpUri = (String)sd.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-URI");
			String slpId = (String)sd.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-ID");
			String consumer = sd.getOwner();	//or  (sd.getOwner!=null && !sd.getOwner().isEmpty()) ? sd.getOwner() : getUsername();
			
			// append recommendation info to string buffer
			if (first) first=false; else sb.append(",\n");
			long tm = sd.getCreateTimestamp()!=null ? sd.getCreateTimestamp().getTime() : new Date().getTime();
			sb.append( String.format(Locale.US, rowHead, sdId, tm, sdName, slpUri, slpId) );
		}
		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- getProviderServices: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
	
	@GET
	@Path("/sd/{sd_id}/list")
	@Produces("application/json")
	@RolesAllowed({"admin", "sp"})
	public String getNotificationsForService(@PathParam("sd_id") String sdId) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getNotificationsForService: INPUT: service-id: "+java.net.URLDecoder.decode(sdId, java.nio.charset.StandardCharsets.UTF_8.toString()));
		
		// Call REST service in order to get service notifications
		long callStartTm = System.currentTimeMillis();
		Notification[] notifList = (Notification[])_callBrokerRestWS(baseUrl+"/opt/notification/sd/"+java.net.URLEncoder.encode(sdId, java.nio.charset.StandardCharsets.UTF_8.toString()), "GET", java.lang.reflect.Array.newInstance(Notification.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		String rowHead = "{ \"id\":\"%s\", \"createTimestamp\":%d, \"service\":\"%s\", \"message\":\"%s\", \"type\":\"%s\"}";
		boolean first = true;
		for (int i=0; i<notifList.length; i++) {
			Notification notif = notifList[i];
			if (notif==null || notif.getId()==null || notif.getId().isEmpty()) continue;  // i.e. notif is null or empty
			
			// get service information
			String serviceId = notif.getService();
			String message = notif.getMessage();
			String consumer = notif.getOwner();  //or  (notif.getOwner!=null && !notif.getOwner().isEmpty()) ? notif.getOwner() : getUsername();
			String type = notif.getType();
			
			// append recommendation info to string buffer
			if (first) first=false; else sb.append(",\n");
			long tm = notif.getCreateTimestamp()!=null ? notif.getCreateTimestamp().getTime() : new Date().getTime();
			sb.append( String.format(Locale.US, rowHead, notif.getId(), tm, serviceId, message, type) );
		}
		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- getNotificationsForService: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
}