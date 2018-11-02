package com.stirred.packaging.EndPointfilter;

import java.io.IOException;
import java.security.AllPermission;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.stirred.packaging.Authentication.AuthenticationService;
import com.stirred.packaging.common.Utilities;
import com.stirred.packaging.securityConfig.SecurityConfig;


@Provider
public class CORSFilter implements ContainerRequestFilter,ContainerResponseFilter
{

	private final String AUTHENTICATION_NAME = "Authorization";
	private static Logger log = Logger.getLogger(CORSFilter.class.getName());

	@Autowired
	AuthenticationService auth;

	@Autowired
	SecurityConfig securityConfig;


	@Override
	public void filter(ContainerRequestContext creq,ContainerResponseContext cres)
	{
		cres.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
		cres.getHeaders().putSingle("Access-Control-Allow-Credentials", true);
		cres.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
		cres.getHeaders().putSingle("Access-Control-Allow-Headers", "origin, content-type, authorization,accept, X-Requested-With,userType,Password,index,RecordperPage,taskId,wfId,userId,userName,search,year,month,jobtype,brand,id,category,QcPerson,clientName,barCodeNumber,Year,client_name,PdPerson");


	}

	/**
	 * This method is used to validate the request.
	 * This method will validate whether the url can be accessed without authorization token or not. If no then the authorization token will be validated to give access permission.
	 */
	@Override
	public void filter(ContainerRequestContext creqc) throws IOException {
		Boolean isAllowUrl = false;
		try {
			JSONArray allowedUrls = new JSONArray(securityConfig.environment.getProperty("jwt.allowedUrls"));
			isAllowUrl = Utilities.validateUrls(allowedUrls, creqc.getUriInfo().getPath());
		} catch (JSONException e) {
			log.error("Exception :" +e.getMessage());
			//e.printStackTrace();
		}

		if (!creqc.getMethod().equals("OPTIONS") && !isAllowUrl) {
			boolean authenticationStatus = true;
			authenticationStatus = auth.authenticate(creqc.getHeaderString(AUTHENTICATION_NAME));
//			System.out.println("hello am unauthorized");
			if (!authenticationStatus) {
				throw new WebApplicationException(Status.UNAUTHORIZED);
			}
		}
	}
  }
