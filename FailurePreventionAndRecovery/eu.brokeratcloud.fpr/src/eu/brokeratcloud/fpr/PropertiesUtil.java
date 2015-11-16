package eu.brokeratcloud.fpr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class PropertiesUtil {
	Properties properties = null;

	public static PropertiesUtil INSTANCE = new PropertiesUtil();

	protected PropertiesUtil() {
		File propertiesFile = new File("./plugin.properties");
		if (!propertiesFile.exists()) {
			Bundle bundle = Platform.getBundle("eu.brokeratcloud.fpr");
			URL fileURL = bundle.getEntry("plugin.properties");
			try {
				propertiesFile = new File(FileLocator.resolve(fileURL).toURI());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFile));
			this.properties = properties;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String get(String key) {
		return properties.getProperty(key);
	}

}
