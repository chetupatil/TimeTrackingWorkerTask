package com.stirred.packaging.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Main Application Starts from this point
 * Component Scan will scan all packages.
 * 
 * @author Siraj
 *
 */
@SpringBootApplication
@ComponentScan({"com.stirred.packaging.common","com.stirred.packaging.EndPoint","com.stirred.packaging.config","com.stirred.packaging.Authentication",
	"con.stirred.packaging.DAO","com.stirred.packaging.securityConfig"})
@ServletComponentScan("com.stirred.packaging.EndPointfilter")
//public class PMSApplication extends SpringBootServletInitializer{
public class PMSApplication{
	private static Logger 	log = LogManager.getLogger(PMSApplication.class.getName());
	public static void main(String[] args )
	{
		log.debug("----------------Start PMS Application------------------");
//		new PMSApplication().configure(new SpringApplicationBuilder(PMSApplication.class)).run(args);
		 SpringApplication.run(PMSApplication.class, args);
		
	}
}
