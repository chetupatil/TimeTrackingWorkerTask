package com.stirred.packaging.Endpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

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
import org.springframework.stereotype.Component;

@Component
@Path("/historyView")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
/**
 * Shows the history of particular job.
 * based on task id and tr number.
 * calculating time has worked on a particular task.
 * @author Siraj
 *
 */
public class HistoryViewEndPoint {

	Connection conn = null;
	private static Logger log = Logger.getLogger(HistoryViewEndPoint.class.getName());

	GlobalVariables gv=new GlobalVariables();

	final String DBuser=gv.getString("db.user");
	final String DBport=gv.getString("db.port");
	final String DBpass=gv.getString("db.password");
	final String DBhost=gv.getString("db.hostname");
	final String DBname=gv.getString("db.database");
	final String driver = "com.mysql.jdbc.Driver";

	final String ConnURL="jdbc:mysql://"+DBhost+":"+DBport+"/"+DBname;

	@GET
	@Path("/readHistory")
	/**
	 * Reading History..
	 * @return
	 */
	public Response getHistory() 
	{
		JSONArray history=new JSONArray();
		try {
			history = getTaskInfo();

		} catch (Exception e) {
			log.error("Exception :" +e.getMessage());
			//e.printStackTrace();
		}
		return Response.ok(history.toString()).build();

	}

	@GET
	@Path("/readTaskHistory")
	/**
	 * Fetching data of a particular task.
	 * time taken to complete the job.
	 * @param taskId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public Response getTaskHistory(@HeaderParam("taskId") int taskId) throws ClassNotFoundException, SQLException, JSONException
	{

		JSONArray taskHistory =getTaskHistoryInfo(taskId);
		JSONArray historyArray = new JSONArray();
		for(int i=0;i<taskHistory.length();i++){
			JSONObject historyObj = taskHistory.getJSONObject(i);
			JSONObject jobHistory = new JSONObject();
			if(!historyObj.isNull("status"))
			{
				jobHistory.put("nameTo", historyObj.getString("assign_to"));
				jobHistory.put("name", historyObj.getString("assign_by"));

				if(!historyObj.isNull("start_timer_wf")){

					jobHistory.put("startTime",historyObj.getString("start_timer_wf"));
				}
				if(!historyObj.isNull("stop_timer_wf")){

					jobHistory.put("endTime", historyObj.getString("stop_timer_wf"));
				}
				jobHistory.put("status", historyObj.getString("status"));
				if(!historyObj.isNull("duration")){

					jobHistory.put("duration", historyObj.getString("duration")+"min");
				}
			}else
			{
				continue;
			}
			int duration = totalDuration(taskId);
			jobHistory.put("totalDuration", duration +" min");
			historyArray.put(jobHistory);				
		}
		return Response.ok(historyArray.toString()).build();

	}

	/**
	 * getting duration time of a particular job using 
	 * task id as input. 
	 * @param taskId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public JSONObject getTotalDuration(int taskId) throws ClassNotFoundException, SQLException, JSONException 
	{
		JSONArray taskInfo =getTaskHistoryInfo(taskId);
		JSONObject jobObj = new JSONObject();
		int totalDuration = 0;
		for(int i=0;i<taskInfo.length();i++)
		{
			JSONObject task = taskInfo.getJSONObject(i);
			if(!task.isNull("duration"))
			{
				int addDuration = Integer.parseInt(task.getString("duration"));
				totalDuration = totalDuration+addDuration;
				jobObj.put("duration", totalDuration);

			}else
			{
				continue;
			}
			if(task.getString("status").equals("Approved"))
			{
				jobObj.put("dateOfDelivered", task.getString("endTime"));
			}else
			{
				continue;
			}
		}

		return jobObj;

	}

	@GET
	@Path("/search")
	/**
	 * Searching any job based on category,brand,job type,iteam name,sku-type. It matches search string with all of these fields
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Response searchItem(@HeaderParam("search") String search) throws Exception
	{
		//System.out.println(search);
		JSONArray responseSearch = new JSONArray();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sqlQuery ="SELECT * FROM packagingtask_part1 WHERE (packagingtask_part1.category LIKE '%"+search+"%'"
					+ "OR packagingtask_part1.brand LIKE '%"+search+"%' OR packagingtask_part1.tr_number LIKE '%"+search+"%' "
					+ " OR packagingtask_part1.item_name LIKE '%"+search+"%' OR packagingtask_part1.sku_type LIKE '%"+search+"%'"
					+ " OR packagingtask_part1.task LIKE '%"+search+"%') AND Job_status ='Approved'";
			ResultSet rs = stmt.executeQuery(sqlQuery);
			while(rs.next())
			{
				JSONObject rowWiseData = new JSONObject();
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					rowWiseData.put(taskName,taskValue);
					if(rowWiseData.getInt("task_id1")!=0)
					{
						int duration = totalDuration(rowWiseData.getInt("task_id1"));
						rowWiseData.put("totalDuration", duration +" min");
					}
				}

				responseSearch.put(rowWiseData);
			} 
			conn.close();
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}


		return Response.ok(responseSearch.toString()).build();	
	}

	/**
	 * Getting task info.of a particular job whose status is appproved.
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public JSONArray getTaskInfo() throws SQLException, JSONException
	{
		JSONArray taskInfoArray = new JSONArray();

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql = "select task_id from workflowdetail where status ='Approved' ";
			Statement stmt =  conn.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			//			System.out.println("rs is closed bro:"+res.isClosed());
			while(res.next())
			{
				JSONObject taskInfo = new JSONObject();
				String taskId  =res.getString("task_id");
				try {
					Class.forName(driver);
					conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
					String sqlQuery = "select * from packagingtask_part1 where task_id1 = '"+taskId+"'";
					Statement stmt1 =  conn.createStatement();
					ResultSet rs = stmt1.executeQuery(sqlQuery);
					while(rs.next())
					{
						ResultSetMetaData rsmd =rs.getMetaData();
						int count = rsmd.getColumnCount();
						for(int i=1;i<=count;i++)
						{
							String taskValue = rs.getString(i);
							String taskName = rsmd.getColumnName(i);
							taskInfo.put(taskName, taskValue);

						}
						taskInfo.put("duration", totalDuration(Integer.parseInt(taskId)) +"min");
					}
					conn.close();
				}catch(Exception e) {
					log.error("Exception :" +e.getMessage());
					//e.printStackTrace();
				}
				taskInfoArray.put(taskInfo);
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}

		return taskInfoArray;
	}

	
	

	public JSONArray getTaskHistoryInfo(int taskId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray readAllRowData = new JSONArray();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql  ="select * from workflowdetail where task_id ="+taskId;
			Statement	stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
			{ 
				JSONObject readRowData = new JSONObject();
				ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					if(rs.getString(i)!=null){
						readRowData.put(rsmd.getColumnName(i), rs.getString(i));
					}else
					{
						readRowData.put(rsmd.getColumnName(i), "0");
					}
				}
				readAllRowData.put(readRowData);
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}

		return readAllRowData;

	}
	/**
	 * Calculating total duration of job by merging 
	 * time intervals worked for a particular job
	 * finally displaying total duration of a particular job
	 */
	
	public int totalDuration(int taskId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray taskInfo =getTaskHistoryInfo(taskId);
		int totalDuration = 0;
		for(int i=0;i<taskInfo.length();i++)
		{
			JSONObject task = taskInfo.getJSONObject(i);
			if(!task.isNull("duration"))
			{
				int addDuration = Integer.parseInt(task.getString("duration"));
				totalDuration = totalDuration+addDuration;
			}
		}
		return totalDuration ;

	}
	public JSONObject getTaskBasedRecord(int taskId) throws Exception
	{
		JSONObject rowWiseData = new JSONObject();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();

			String sql = "select * from packagingtask_part1 where task_id1 ="+taskId ;

			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();

				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					rowWiseData.put(taskName, taskValue);	
				}
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}

		return rowWiseData;
	}
}
