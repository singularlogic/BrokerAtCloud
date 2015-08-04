package eu.brokeratcloud.rest.gui;

import eu.brokeratcloud.common.RootObject;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import javax.ws.rs.client.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractFacingComponent extends RootObject {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.rest.UserFacingWS");

	protected static final String defaultConfigFile = "ws.properties";
	protected Properties configProperties;
	protected String baseUrl = "";
	protected HttpServletRequest request;
	
	protected AbstractFacingComponent() { this.request = null; }
	protected AbstractFacingComponent(HttpServletRequest request) { this.request = request; }
	
	public void loadConfig() throws IOException {
		loadConfig(defaultConfigFile);
	}
	
	public void loadConfig(String propertiesFile) throws IOException {
		try {
			logger.debug("Loading properties file: {}", propertiesFile);
			Properties properties = new Properties();
			properties.load( getClass().getClassLoader().getResourceAsStream(propertiesFile) );
			baseUrl = properties.getProperty("ws-base");
			this.configProperties = properties;
			logger.debug("WS base: {}", baseUrl);
		} catch (Exception e) {
			logger.error("Exception during initialization. Using defaults: ", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Object _callBrokerRestWS(String url, String method, Class clss, Object entity) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		if (!url.toLowerCase().startsWith("http:") && baseUrl!=null) url = baseUrl+url;
		logger.trace("_callBrokerRestWS: url={}", url);
		ResteasyWebTarget target = client.target(url);
		
		Response response = null;
		method = method.trim().toLowerCase();
		if (method.equals("get")) response = target.request().get();
		else if (method.equals("put")) response = target.request().put( Entity.json(entity) );
		else if (method.equals("post")) response = target.request().post( Entity.json(entity) );
		else if (method.equals("delete")) response = target.request().delete();
		int status = response.getStatus();
		logger.debug("--> Response: {}", status);
		if (status>299) throw new RuntimeException("Operation failed: Status="+status+", URL="+url);
		
		Object obj = null;
		if (clss!=null) {
			obj = response.readEntity( clss );
		} else {
			obj = response.getEntity();
		}
		response.close();
		return obj;
	}
	
	protected String getUsername() {
		return request!=null ? request.getRemoteUser() : null;
	}
}