package eu.brokeratcloud.rest.opt;

import eu.brokeratcloud.common.RootObject;
import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractManagementService extends RootObject {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.rest.BrokerManamentWS");

	protected static final String defaultConfigFile = "opt.properties";
	
	protected static final int HTTP_STATUS_OK = 200;
	protected static final int HTTP_STATUS_CREATED = 201;
	protected static final int HTTP_STATUS_ERROR = 500;
	
	protected static Properties optConfig;
	
	protected AbstractManagementService() {
		if (optConfig==null) loadConfig();
	}
	
	protected void loadConfig() {
		loadConfig(defaultConfigFile);
	}
	
	protected void loadConfig(String propertiesFile) {
		try {
			logger.debug("loadConfig: Loading properties file: {}", propertiesFile);
			Properties properties = new Properties();
			properties.load( getClass().getClassLoader().getResourceAsStream(propertiesFile) );
			optConfig = properties;
		} catch (Exception e) {
			logger.error("loadConfig: Exception during initialization. Using defaults: ", e);
		}
	}
	
	protected Response createResponse(int statusCode, String mesg) {
		return Response.status(statusCode).header("Connection", "Keep-Alive").header("Keep-Alive", "timeout=600, max=99").entity(mesg).build();
	}
}