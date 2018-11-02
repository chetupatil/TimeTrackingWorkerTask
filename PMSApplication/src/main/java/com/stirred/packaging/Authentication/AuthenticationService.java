package com.stirred.packaging.Authentication;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.stirred.packaging.securityConfig.SecurityConfig;

/**
 * Service for authentication 
 *
 */
@Service
public class AuthenticationService {
 	
    private static Logger log = Logger.getLogger(AuthenticationService.class.getName());
	
	@Autowired
	SecurityConfig sec;
	
	/**
	 * This method helps to verify the given token whether it is an invalid token or not.
	 * 
	 * @param token: Token that need to be verified
	 * @return boolean value true if it is a valid token. else false.
	 */
	public boolean authenticate(String token) {
		if (token == null || "".equals(token)) {
			return false;
		}
		JWTVerifier verifier = null;
		try {
			verifier = JWT.require(Algorithm.HMAC256(sec.environment.getProperty("jwt.sign"))).withIssuer(sec.environment.getProperty("jwt.issuer")).withJWTId("1").build();
		} catch (IllegalArgumentException e) {
			log.error("Exception :"+e.getMessage());
		//	e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			log.error("Exception :"+e.getMessage());
		//	e.printStackTrace();
			return false;
		}
		DecodedJWT decodedJWT = verifier.verify(token);
		if(decodedJWT.getExpiresAt().before(new Date())){
			return false;
		}
		return true;
	}
}
