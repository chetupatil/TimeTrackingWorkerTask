
package com.stirred.packaging.Endpoint;


import java.io.FileReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.parser.JSONParser;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

@Component
@Path("/masterJson")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MasterJsonFileEndPoint 
{
	private static Logger log = Logger.getLogger(MasterJsonFileEndPoint.class.getName());
	@GET
	@Path("/JsonObjectFile")

	/**
	 * Find's the master JSON file from src/main/resources in local 
	 * and config/master.json from server.
	 *
	 * @return values with matching keys.
	 */

	public Response getJsonFile(){
		JSONParser json = new JSONParser();
		JSONObject jsonObject = null;
		// JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) json.parse(new FileReader("src/main/resources/config/masterJson.json"));
			//jsonObject = (JSONObject) json.parse(new FileReader("config/masterJson.json"));
		} catch (Exception e) {
			log.error("Exception :"+e.getMessage());
			//e.printStackTrace();
		} 

		return Response.ok(jsonObject.toString()).build();
	}


}
