package com.stirred.packaging.Endpoint;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;


@Component
@Path("/GlobalVariables")
/**
 * Reading values of DB connection from DB config file
 * location src/main/resources/config.
 * @author Siraj
 *
 */
public class GlobalVariables {

	private static final String PROP_FILENAME = "DBConfig.properties";
	static PropertiesConfiguration config;
	
	private static Logger log = Logger.getLogger(GlobalVariables.class.getName());

	GlobalVariables () {

		try {

			String configDir = System.getProperty ("configDir", "config");
			String filePath = configDir + "/" + PROP_FILENAME;
			config = new PropertiesConfiguration(filePath);
			FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
			strategy.setRefreshDelay(30);
			config.setReloadingStrategy(strategy);

		} catch (Exception e) 
		{
			log.error("Exception :"+e.getMessage());
          // e.printStackTrace();
		}			
	}
	public String getString(String key) {

		return config.getString(key);
	}
}
