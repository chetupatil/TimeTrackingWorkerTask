package com.stirred.packaging.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.UUID.*;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Utilities {
	
	private static Logger log = Logger.getLogger(Utilities.class.getName());

	/**
	 * This method is used to encrypt the password. Encryption is done using MD5.
	 * 
	 * @param passwordToHash: String value that need to encrypt.
	 * @return the encrypted value in string.
	 * @exception NoSuchAlgorithmException for encryption algorithm MD5.
	 */
	public static String getSecurePassword(String passwordToHash)     {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } 
        catch (NoSuchAlgorithmException e) {
        	log.error("Exception :" +e.getMessage());
          //  e.printStackTrace();
        }
        return generatedPassword;
    }

	/*public static String generatePK() {
		return UUID.randomUUID().toString();
	}*/
	
	
	/**
	 * This method helps to validate url whether the url can be accessed without authentication token or not.
	 * @param allowedUrls: List of allowed urls without token. Allowed urls are configured in property file.
	 * @param requestUrl: Requested url
	 * @return a boolean value true or false. If url is allowed without token then true else false.
	 * @throws JSONException
	 */
	public static Boolean validateUrls(JSONArray allowedUrls, String requestUrl) throws JSONException {
		String[] requestUrlArr = requestUrl.split("/");
		for(int i = 0; i < allowedUrls.length(); i++) {
			String[] allowedUrl = allowedUrls.getString(i).split("/");
			if(allowedUrl[0].equals(requestUrlArr[0])) {
				if(allowedUrl[1].equals("*") || allowedUrl[1].equals(requestUrlArr[1])) {
					return true;
				}
			}
		}
		return false;
	}
}
	 

