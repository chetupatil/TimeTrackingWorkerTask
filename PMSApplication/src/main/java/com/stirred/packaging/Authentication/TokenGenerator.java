package com.stirred.packaging.Authentication;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.stirred.packaging.securityConfig.SecurityConfig;

/**
 * This class is used to generate token while login in.
 * 
 */

@Component
@Singleton
public class TokenGenerator {
	
 private static Logger log = Logger.getLogger(TokenGenerator.class.getName());
	
	@Autowired
	SecurityConfig sec;

	/**
	 * This method is used to generate token while login in.
	 * @param uniqKey: This parameter is not used.
	 * @return Token object with token in string.
	 */
	public Token generateToken(String uniqKey) {
		// String s = null;
		Token token = new Token();
		try {
			Builder buildJwtTocken = JWT.create();
			buildJwtTocken.withIssuer(sec.environment.getProperty("jwt.issuer")).withJWTId("1");
			//          TODO changed minute from 30 to 300 for developing purpose. After this minutes token will expire. 
			LocalDateTime ldt = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).plusMinutes(300);
			//			Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			buildJwtTocken.withExpiresAt(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
			if ("HMAC256".equals(sec.environment.getProperty("jwt.algorithem")))
				token.setToken(buildJwtTocken.sign(Algorithm.HMAC256(sec.environment.getProperty("jwt.sign"))));
		} catch (IllegalArgumentException | JWTCreationException | UnsupportedEncodingException e) {
			//e.printStackTrace();
			log.error("Exception :" +e.getMessage());
		}
		//System.out.println("Here the token is :"+token);
		log.debug(token);
		return token;
	}

}
