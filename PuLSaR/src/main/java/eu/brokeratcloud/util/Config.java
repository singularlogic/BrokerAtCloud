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
package eu.brokeratcloud.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.config");
	
	protected static HashMap<String,Properties> _loadedConfigs;
	protected static HashMap<String,String> _loadedResources;
	
	public static Properties getConfig(String file) {
		if (_loadedConfigs==null) _loadedConfigs = new HashMap<String,Properties>();
		Properties p = _loadedConfigs.get(file);
		if (p==null) {
			for (String properFileName : getProperFileNames(file)) {
				logger.debug("getConfig: Trying to load Config from file: {}", properFileName);
				p = _loadSettings(properFileName);
				if (p!=null) break;
			}
			String outcome = p!=null ? "found" : "not found";
			logger.debug("getConfig: Config {} for file: {}", outcome, file);
			_loadedConfigs.put(file, p);
		} else {
			logger.debug("getConfig: Config found in cache: file={}", file);
		}
		return p!=null ? (Properties)p.clone() : null;
	}
	
	public static Iterable<String> getProperFileNames(String file) {
		String pfile = file;
		file = file.trim();
		if (file.isEmpty()) throw new IllegalArgumentException("getProperFileNames: Empty file argument");
		if (file.charAt(0)!='/' && file.charAt(0)!='\\') file = "/"+file;
		
		Vector<String> names = new Vector<String>();
		// Get environment-variable-based config. file names
		addProperFileNameFromEnvVar("PULSAR_CONFIG_BASE", file, names);
		addProperFileNameFromEnvVar("PULSAR_CFG_BASE", file, names);
		addProperFileNameFromEnvVar("PULSAR_CONFIG", file, names);
		addProperFileNameFromEnvVar("PULSAR_CFG", file, names);
		// Get default-paths-based config. file names
		names.add( "/pulsar-config"+file );
		names.add( "/pulsar-cfg"+file );
		names.add( "/pulsar"+file );
		names.add( "/config"+file );
		names.add( "/cfg"+file );
		names.add( pfile );
		names.add( file );
		return names;
	}
	
	protected static boolean addProperFileNameFromEnvVar(String envVar, String file, Vector<String> names) {
		String base = System.getenv( envVar );
		if (base!=null && !(base=base.trim()).isEmpty()) {
			if (base.endsWith("/") || base.endsWith("\\")) {
				base = base.substring(0,base.length()-1).trim();
			}
			if (!base.isEmpty()) {
				names.add( base + file );
				return true;
			}
		}
		return false;
	}
	
	protected static Properties _loadSettings(String file) {
		java.io.InputStream is = null;
		try {
			Properties p = new Properties();
			logger.trace("Reading properties from file: {}...", file);
			is = Config.class.getResourceAsStream(file);
			if (is==null) {
				logger.trace("Reading properties from file: {}... Not found", file);
				return null;
			}
			p.load( is );
			logger.trace("Reading properties from file: {}... done", file);
			return p;
		} catch (Exception e) {
			logger.trace("Reading properties from file: {}... EXCEPTION: {}", file, e);
			return null;
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (Exception e) {
					logger.error("Reading properties from file: {}... Error while closing stream. EXCEPTION: {}", e);
				}
			}
		}
	}
	
	public static String getResourceAsString(String file) {
		if (_loadedResources==null) _loadedResources = new HashMap<String,String>();
		String contents = _loadedResources.get(file);
		if (contents==null) {
			for (String properFileName : getProperFileNames(file)) {
				logger.debug("getResourceAsString: Trying to load file: {}", properFileName);
				contents = _loadResource(properFileName);
				if (contents!=null) break;
			}
			String outcome = contents!=null ? "found" : "not found";
			logger.debug("getResourceAsString: File {}: {}", outcome, file);
			_loadedResources.put(file, contents);
		} else {
			logger.debug("getResourceAsString: File contents in cache: file={}", file);
		}
		return contents;
	}
	
	protected static String _loadResource(String file) {
		java.io.InputStream is = null;
		try {
			logger.trace("Reading file: {}...", file);
			is = Config.class.getResourceAsStream(file);
			if (is==null) {
				logger.trace("Reading file: {}... Not found", file);
				return null;
			}
			String contents = _readInputStreamAsString(is);
			logger.trace("Reading properties from file: {}... done\n{}", file, contents);
			return contents;
		} catch (Exception e) {
			logger.trace("Reading file: {}... EXCEPTION: {}", file, e);
			return null;
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (Exception e) {
					logger.error("Reading file: {}... Error occurred while closing stream. EXCEPTION: {}", e);
				}
			}
		}
	}
	
	public static String _readInputStreamAsString(InputStream in) throws IOException {
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
	
	public static void clearCache() {
		_loadedConfigs.clear();
		_loadedResources.clear();
	}
}