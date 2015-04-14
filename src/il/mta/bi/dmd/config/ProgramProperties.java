package il.mta.bi.dmd.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ProgramProperties {
	static Logger logger = Logger.getLogger(ProgramProperties.class);
	
	private static ProgramProperties theProperties;
	private Properties globalProperties;
	
	public static ProgramProperties getProperties() {
		if (theProperties == null) {
			theProperties = new ProgramProperties();
		}
		return theProperties;
	}

	private ProgramProperties() {
	}
	
	/**
     * Get property value from global properties repository
     */

    public String getProperty(String propertyName) {
    	if (globalProperties == null) {
    		
    		try {
    			String propertiesFileName = "conf\\config.properties";
    			InputStream input = new FileInputStream(propertiesFileName);
    			globalProperties = new Properties();
    			
				globalProperties.load(input);
			} catch (Exception e) {
				logger.warn("caught exception ", e);
				return null;
			} 
    	}
    	
    	return globalProperties.getProperty(propertyName);
    }
}
