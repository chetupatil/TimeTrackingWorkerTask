package com.stirred.packaging.Endpoint;

import java.sql.*;
import java.util.*;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/externalError")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class ExternalErrorEndPoint
{
	Connection conn = null;
	
	private static Logger log = Logger.getLogger(ExternalErrorEndPoint.class.getName());

	GlobalVariables gv=new GlobalVariables();

	final String DBuser=gv.getString("db.user");
	final String DBport=gv.getString("db.port");
	final String DBpass=gv.getString("db.password");
	final String DBhost=gv.getString("db.hostname");
	final String DBname=gv.getString("db.database");
	final String driver = "com.mysql.jdbc.Driver";

	final String ConnURL="jdbc:mysql://"+DBhost+":"+DBport+"/"+DBname;
	


	@POST
	@Path("/addErrorInfo")
	/**
	 * Adding Error data into particular
	 * task using task-id and tr number as primary key.
	 * @param error
	 * @return
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Response addErrorDetails(String error) throws JSONException, ClassNotFoundException, SQLException
	{
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			JSONObject externalError = new JSONObject(error);
			String sql =addExternalError(externalError);
			PreparedStatement ps= conn.prepareStatement(sql);
			int addCount = ps.executeUpdate();
				if((addCount==0))
				{
					log.error("Failed to add Error data into External Error Table");
					return Response.ok("Failed to add Error data ").build();
				}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception:" +ex.getMessage());
		//	ex.printStackTrace();
		}
		return Response.ok().build();	
	}


	@GET
	@Path("/readAll")
	/**
	 * Reading all jobs from External Error table 
	 * using input as taskId.
	 * 
	 * @param taskId
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 */
	public Response readAllError(@HeaderParam("taskId") String taskId) throws SQLException, JSONException, ClassNotFoundException
	{
		JSONArray readErrorBasedTaskid = new JSONArray();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String readError = "select * from external_error where task_id ='"+taskId+"'";
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(readError);

			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();
				JSONObject rowWiseData = new JSONObject();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					rowWiseData.put(taskName, taskValue.toString());	
				}
				readErrorBasedTaskid.put(rowWiseData);

			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(readErrorBasedTaskid.toString()).build();

	}

	@GET
	@Path("/readBasedOnId")
	/**
	 * Reading data from external error input as ID.
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 */
	public Response readId(@HeaderParam("id") int id) throws SQLException, JSONException, ClassNotFoundException
	{
		JSONObject rowWiseData = new JSONObject();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String readErrorId = "select * from external_error where id = "+id;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(readErrorId);

			while(rs.next())
			{
				ResultSetMetaData rsmd =rs.getMetaData();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					rowWiseData.put(taskName, taskValue);	
				}

			}
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(rowWiseData.toString()).build();

	}

	@GET
	@Path("/readTrNumber")
	/**
	 * Fetching tr number from packaging table.
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 */
	public Response readTRNumbers() throws SQLException, JSONException, ClassNotFoundException
	{
		JSONArray trNumberArray = new JSONArray();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String query = "select tr_number from packagingtask_part1";
			ResultSet rs = stmt.executeQuery(query);

			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();
				JSONObject rowWiseData = new JSONObject();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					rowWiseData.put(taskName, taskValue.toString());	
				}
				trNumberArray.put(rowWiseData);

			}
		}catch(Exception ex) {
			log.error("Exception : "+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return Response.ok(trNumberArray.toString()).build();

	}

	@POST
	@Path("/updateErrorDetail")
	/**
	 * Modifying External Error  details.
	 * @param updateError
	 * @return
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Response updateError(String updateError) throws JSONException, ClassNotFoundException, SQLException
	{

		JSONObject externalError = new JSONObject(updateError);
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql =updateQueryInTable(externalError);
			PreparedStatement ps= conn.prepareStatement(sql);
			int addCount = ps.executeUpdate();
			if(addCount==0)
			{
				log.error("Failed to Update Error data into External Error Table");
				return Response.ok("Failed to update Information").build();
			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}
		return Response.ok("Update").build();

	}

	public String addExternalError(JSONObject taskInfoJson) throws JSONException {

		StringBuilder dataKeys = new StringBuilder();
		StringBuilder dataValues = new StringBuilder();
		Iterator<String> iter = taskInfoJson.keys();

		while (iter.hasNext()) {
			String key = iter.next();
			dataKeys.append("'"+key+"\"");	
			String value = taskInfoJson.getString(key);
			dataValues.append("'"+value+"'");
		}		

		String finalDataKeys = dataKeys.toString().replaceAll("\"'", ",").replaceAll("\"", "").replaceAll("'", "");
		String finalDataValues = dataValues.toString().replaceAll("''", "','");
		String SqlQuery = "INSERT INTO external_error ("+finalDataKeys+") VALUES("+finalDataValues+")";
		return SqlQuery;
	}
	
	public String updateQueryInTable(JSONObject jsonObj) throws JSONException
	{
		StringBuilder dataKeys = new StringBuilder();
		StringBuilder dataValues = new StringBuilder();
		String sqlQuery = null;
		StringBuilder data = new StringBuilder();
		Iterator<String> itr = jsonObj.keys();
		int id = Integer.parseInt(jsonObj.getString("id"));
		Map<String, Object> map = new HashMap<String, Object>();
		while(itr.hasNext())
		{
			String key = itr.next();
			dataKeys.append("'"+key+"\"");

			String values = jsonObj.getString(key);
			dataValues.append("'"+values+"'");
			map.put(key, values);
		}

		for (Entry<String, Object> entry :map.entrySet()) {
			sqlQuery = entry.getKey()+" = '"+entry.getValue()+"'";
			data.append(sqlQuery+",");
		}

		String newData = data.substring(0, (data.length()-1));

		sqlQuery = "UPDATE external_error SET "+newData +" where id = "+id;

		return sqlQuery;

	}
}
