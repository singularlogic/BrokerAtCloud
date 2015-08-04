package eu.brokeratcloud.opt.engine;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 *	A class managing all major configuration-dependent system components
 */
public class Configuration {
	protected Properties configuration;
	
	public Configuration(String configFile, String[] args) {
	}
	
	public String toString() {
		return "Configuration: ";
	}
}
