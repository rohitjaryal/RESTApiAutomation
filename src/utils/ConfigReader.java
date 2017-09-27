package utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class ConfigReader {
	HashMap<String, String> globalConfig = new HashMap<String, String>();

	public ConfigReader(String filename) {
		Properties globalProp = loadPropertyFile(filename);
		globalConfig = loadConfig(globalProp);
	}

	private Properties loadPropertyFile(String pathToPropertyLocation) {
		Properties pageProp = new Properties();
		try {
			File f = new File(pathToPropertyLocation);
			FileInputStream fis = new FileInputStream(f);
			pageProp.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pageProp;
	}

	public HashMap<String, String> loadConfig(Properties prop) {
		HashMap<String, String> propMap = new HashMap<String, String>();
		try {
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String val = prop.getProperty(key);
				propMap.put(key, val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return propMap;
	}

	public HashMap<String, String> getConfig() {
		return globalConfig;
	}
}
