package com.stirred.packaging.securityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * This is a configuration class for securityconfig.properties file located in src/main/resources folder 
 */
@Configuration
@PropertySource("classpath:securityconfig.properties")
public class SecurityConfig {
	@Autowired
	public Environment environment;
	
}
