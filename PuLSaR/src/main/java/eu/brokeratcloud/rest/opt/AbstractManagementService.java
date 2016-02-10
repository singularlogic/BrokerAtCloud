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
		optConfig = loadConfig(defaultConfigFile);
	}
	
	protected Properties loadConfig(String propertiesFile) {
		try {
			logger.debug("loadConfig: Loading properties file: {}", propertiesFile);
			Properties properties = eu.brokeratcloud.util.Config.getConfig(propertiesFile);
			return properties;
		} catch (Exception e) {
			logger.error("loadConfig: Exception during initialization. Using defaults: ", e);
		}
		return null;
	}
	
	protected Response createResponse(int statusCode, String mesg) {
		return Response.status(statusCode).header("Connection", "Keep-Alive").header("Keep-Alive", "timeout=600, max=99").entity(mesg).build();
	}
}