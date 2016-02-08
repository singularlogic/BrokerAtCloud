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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
	protected static String menuTemplateUrl = "/menu-template.html";
	protected static String menuTemplate = null;
	protected static Pattern menuTemplatePattern = null;
	protected static String patternStr = "<!--\\s*ROLE-BEGIN\\s*:\\s*(\\S+)\\s*-->(.*?)<!--\\s*ROLE-END\\s*:\\s*\\1\\s*-->";
	
	@GET
	@POST
	@Path("/menu")
	@Produces("text/html")
	@PermitAll
	@RolesAllowed({"admin","sc","sp"})
	public String getMenu(@Context HttpServletRequest request) throws IOException {
		logger.debug("Auth: getMenu: Calling getMenuStatic");
		return getMenuStatic(request);
	}
	
	public static String getMenuStatic(@Context HttpServletRequest request) throws IOException {
		logger.debug("Auth: getMenuStatic: INPUT: request={}", request);
		
		// Loading menu template from file, if it hasn't been already
		if (menuTemplate==null) {
			logger.debug("Auth: getMenuStatic: Loading menu template...");
			menuTemplate = readInputStreamAsString( Auth.class.getResourceAsStream(menuTemplateUrl) );
			logger.debug("Auth: getMenuStatic: Menu template loaded:\n"+menuTemplate);
			
			logger.trace("Auth: getMenuStatic: template processing pattern: {}", patternStr);
			menuTemplatePattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		}
		
		// Prepare menu code from template taking into consideration user roles
		String html = menuTemplate;
		try {
			// get user roles from session
			List userRoles = null;
			if (request!=null && request.getSession()!=null) userRoles = (List)request.getSession().getAttribute("user.roles");
			if (userRoles==null) userRoles = new ArrayList();
			logger.trace("Auth: getMenuStatic: User roles (from session) : {}", userRoles);
			
			// replace variables (%...%) with actual values
			if (request!=null) {
				logger.trace("Auth: getMenuStatic: Replacing variables with actual values...");
				html = html.replace("%USERNAME%", request.getRemoteUser());
			}
			
			// search for role-depended sections in menu template
			logger.trace("Auth: getMenuStatic: Processing menu template...");
			Matcher m = menuTemplatePattern.matcher(html);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				// extract part roles and check if any one of them is also contained in user roles
				String[] partRole = m.group(1).split("[,;]+");
				logger.trace("Auth: getMenuStatic: Part found: roles={}", Arrays.toString(partRole));
				boolean found = false;
				for (int i=0; i<partRole.length; i++) {
					partRole[i] = partRole[i].trim();
					if (partRole[i].isEmpty()) continue;
					if (userRoles.contains( partRole[i] )) {
						found = true;
						break;
					}
				}
				
				// if part roles don't match any of the user roles, we remove this part from menu
				if (found) {
					logger.trace("Auth: getMenuStatic: Part is retained");
					m.appendReplacement(sb, m.group(2));
				} else {
					logger.trace("Auth: getMenuStatic: Part is removed");
					m.appendReplacement(sb, "");
				}
			}
			m.appendTail(sb);
			html = sb.toString();
			logger.trace("Auth: getMenuStatic: Processing menu template... Done");
		} catch (Exception e) {
			html = "<span id=\"pulsar-menu-error\"><font color=\"red\"><b>An error occurred while processing menu template. Exception: </b>"+e+"</font></span>";
			logger.error("Auth: getMenuStatic: EXCEPTION CAUGHT: {}\n", e.getMessage(), e);
		}
		
		logger.trace("Auth: getMenuStatic: OUTPUT: menu code:\n{}", html);
		return html;
	}
	
	protected static String readInputStreamAsString(InputStream in) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while(result != -1) {
			byte b = (byte)result;
			buf.write(b);
			result = bis.read();
		}        
		return buf.toString();
	}
}