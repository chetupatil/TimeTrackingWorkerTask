package com.stirred.packaging.Endpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;


@Component
@Path("/workFlow")
/**
 * This class defines all workflow details in it.
 * From starting to End of a Job,
 * Job activity status.
 * Job timing play pause mechanisim..
 * @author Chetana
 *
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkFlowEndPoint {

	final String driver = "com.mysql.jdbc.Driver";
	private static Logger log = LogManager.getLogger(WorkFlowEndPoint.class.getName());
	Connection conn =null;
	GlobalVariables gv=new GlobalVariables();

	final String DBuser=gv.getString("db.user");
	final String DBport=gv.getString("db.port");
	final String DBpass=gv.getString("db.password");
	final String DBhost=gv.getString("db.hostname");
	final String DBname=gv.getString("db.database");

	final String ConnURL="jdbc:mysql://"+DBhost+":"+DBport+"/"+DBname;

	@POST
	@Path("/addWorkFlow")
	/**
	 * Assigning job to account manager .
	 * by using a particular job details.
	 * @param workFlowData
	 * @return
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Response addWorkFlowDetails(String workFlowData) throws JSONException, ClassNotFoundException, SQLException
	{
		JSONObject workFlowObj = new JSONObject(workFlowData);
		String sql = createInsertQueryofWorkFlowCreation(workFlowObj);
		JSONObject selectedRecord =new JSONObject();
		PreparedStatement ps=null;
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				ps= conn.prepareStatement(sql);
				int count = ps.executeUpdate();
				int taskId = workFlowObj.getInt("task_id");
				selectedRecord = getSelectedRow(taskId);
					if((count==0))
					{
						log.error("Failed to add data into Work Flow Table");
						return Response.ok("Failed").build();
					}
					try {
						Class.forName(driver);
						conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
						String sql1  = "UPDATE packagingtask_part1 SET selectManager = '"+workFlowObj.getString("assign_to")+"' where task_id1 ="+taskId;
						ps= conn.prepareStatement(sql1);
						int count1 = ps.executeUpdate();
							if((count1==0))
							{
								log.error("Failed to Update data into Work Flow Table");
								return Response.ok("Failed").build();
							}
					}catch(Exception ex) {
						log.error("Exception:"+ex.getMessage());
						//ex.printStackTrace();
					}
	
			}catch(Exception ex) {
				log.error("Exception : "+ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}


		return Response.ok(selectedRecord.toString()).build();

	}
	@POST
	@Path("/insertWorkFlow")
	public Response insertWorkFlowDetail(String workFlowData) throws Exception
	{
		JSONObject workFlowDataObj = new JSONObject(workFlowData);
		String sqlInsert = insertWorkFlowDetail(workFlowDataObj);
		JSONObject responseOfWorkFlow = new JSONObject(); 
		System.out.println("insertWorkFlow");
		Class.forName(driver);
			try {
	
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				PreparedStatement ps= conn.prepareStatement(sqlInsert);
				int count1 = ps.executeUpdate();
					if(count1==0)
					{
						log.error("Failed to Insert data into workFlow Table");
						return Response.ok("Failed to Insert data ").build();
					}
				JSONObject workFlowDetail = getWorkFlow(workFlowDataObj.getInt("assignToId"));
				String endTime = workFlowDataObj.getString("taskTime");
				int wfId = workFlowDataObj.getInt("wfId");
					try {
						conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
						String sqlUpdate = "UPDATE workflowdetail SET endTime ='"+endTime+"',wf_status = 'Close' where wf_id="+wfId;
						ps= conn.prepareStatement(sqlUpdate);
						int count = ps.executeUpdate();
							if(count==0)
							{
								log.error("Failed to Insert data into Work Flow Table");
								return Response.ok("Failed to Insert data").build();
							}	    	  
							if(!workFlowDetail.isNull("status")){
								int countTask = taskUpdate(workFlowDetail);
							}
						responseOfWorkFlow = getSelectedTimer(workFlowDataObj.getInt("wfId"));
						JSONObject responseOfTaskDetail = getTaskBasedRecord(workFlowDataObj.getInt("task_id"));
						responseOfWorkFlow.put("taskDetail", responseOfTaskDetail);
					}catch(Exception ex) {
						log.error("Exception:"+ex.getMessage());
						//ex.printStackTrace();
					}
			}catch(Exception ex) {
				log.error("Exception: "+ex.getMessage());
			//	ex.printStackTrace();
			}finally {
				conn.close();
			}


		return Response.ok(responseOfWorkFlow.toString()).build();
	}


	@POST
	@Path("/startTimer")
	/**
	 * Noting time when job get started.
	 * @param workFlow
	 * @return
	 * @throws Exception
	 */
	public Response startTimerDetail(String workFlow) throws Exception
	{
		JSONObject record= new JSONObject();	

			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				JSONObject workFlowObj = new JSONObject(workFlow);
				String sql =startTimerQueryInsert(workFlowObj);
				PreparedStatement ps= conn.prepareStatement(sql);
				int count = ps.executeUpdate();
						if((count==0))
						{
							log.error("Failed to insert start Timer details");
							
							return Response.ok("Failed to insert start Timer").build();
						}
				JSONObject timerRecordDetail = getWorkFlowDetailAfrerStartTimer(workFlowObj.getInt("wfId"));
					try {
						conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
						String sql1 = "UPDATE workflowdetail SET play_status ="+workFlowObj.getBoolean("play_status")+""
								+ ",timerId ="+timerRecordDetail.getInt("timerId")+",start_timer_wf='"+workFlowObj.getString("start_timer")
								+ "' where wf_id ="+workFlowObj.getInt("wfId");
						PreparedStatement ps1= conn.prepareStatement(sql1);
						int count1 = ps1.executeUpdate();
							if((count1==0)){
								log.error("Failed to update play Status");
								return Response.ok("Failed to update play Status").build();
							}
						record = getSelectedRowTimer(workFlowObj.getString("wfId"));
						boolean playStatus =getPlayStatus(record.getInt("wfId"));
						record.put("play_status", playStatus);
					}catch(Exception ex) {
						log.error("Exception:"+ex.getMessage());
						//ex.printStackTrace();
					}
			}catch(Exception ex) {
				log.error("Exception :"+ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}


		return Response.ok(record.toString()).build();
	}

	@GET
	@Path("/readStartTimer")
	/**
	 * Reading total time intervals from work flow table
	 * by accepting the workflow id as input
	 * @param wfId
	 * @return
	 */
	public Response getStartTimer(@HeaderParam("wfId") int wfId) {
		JSONObject record = null;
			try {
				record = getSelectedTimer(wfId);
			} catch (Exception e) {
				log.error("Exception :"+e.getMessage());
		      // e.printStackTrace();
			} 
		return 	Response.ok(record.toString()).build();
	}
	@POST
	@Path("/updateApprovedStatus")
	/**
	 * Changing job status from in progress to completed /approved.
	 * 
	 * @param approvedStatus
	 * @return
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Response updateApprovedStatus(String approvedStatus) throws JSONException, ClassNotFoundException, SQLException
	{
       System.out.println(approvedStatus);
		JSONObject approvedStatusObj = new JSONObject(approvedStatus);
		PreparedStatement ps= null;
		JSONObject updateResponse= new JSONObject();
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				String sql = "UPDATE workflowdetail SET wf_status ='"+approvedStatusObj.getString("wf_status")+"',"
						+ " endTime = '"+approvedStatusObj.getString("endTime")+"' where wf_id ="+approvedStatusObj.getInt("wf_id"); 
				ps= conn.prepareStatement(sql);
				int count = ps.executeUpdate();
						if(count == 0){
							log.error("Failed to update Approved Status in workFlow Table");
							
							Response.ok("Failed to Update").build();
						}
				updateResponse = getSelectedTimer(approvedStatusObj.getInt("wf_id"));
				updateResponse.put("submit", true);
					try {
						Class.forName(driver);
						conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
						String sqlUpdate = "UPDATE packagingtask_part1 SET date_delivered ='"+updateResponse.getString("endTime")+"' where task_id1="+
								updateResponse.getString("task_id");
						ps= conn.prepareStatement(sqlUpdate);
						int countUpdate = ps.executeUpdate();
							if(countUpdate == 0){
								log.error("Failed to update data of delivered in Packaging Task Part 1 table");
								Response.ok("Failed to update data of delivered").build();
							}
					}catch(Exception ex) {
						log.error("Exception :"+ex.getMessage());
						//ex.printStackTrace();
					}
	
			}catch(Exception ex) {
				log.error("Exception :" +ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}

		return Response.ok(updateResponse.toString()).build();

	}

	@GET
	@Path("/assignManagerInfo")
	/**
	 * Assigning task to manager from account manager 
	 * by accepting taskid as input.
	 * @param taskId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public Response assignManagerInfo(@HeaderParam("taskId") String taskId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONObject rowWiseData = new JSONObject();
				try {
					Class.forName(driver);
					conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
					Statement stmt =  conn.createStatement();
					String sql ="select * from workflowdetail  where  task_id ='"+taskId+"'";
					ResultSet rs = stmt.executeQuery(sql);
		
						while(rs.next())
						{
							ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			
							int count = rsmd.getColumnCount();
								for(int i=1;i<=count;i++)
								{
									String taskName = rsmd.getColumnName(i);
									String taskValue = rs.getString(i);
									
									rowWiseData.put(taskName, taskValue);			
								}
						}
				}
				catch(Exception ex) {
					log.error("Exception :"+ex.getMessage());
					//ex.printStackTrace();
				}
		return Response.ok(rowWiseData.toString()).build();

	}

	@POST
	@Path("/pauseTimer")
	/**
	 * Stopping the timer..
	 * @param timerRecordDetail
	 * @return
	 * @throws Exception
	 */
	public Response pauseTimerDetail(String timerRecordDetail) throws Exception
	{
		JSONObject TRDetail = new JSONObject(timerRecordDetail);
		PreparedStatement ps=null;
		int count = updateQueryInTable(TRDetail);
			if((count==0))
			{
				log.error("Failed to update Pause Timer in Table");
				return Response.ok("Failed to update Pause Timer").build();
			}
		String startTimer = getStartTimerFromTable(TRDetail.getInt("timerId"));
		int duration = durationCalculation(startTimer,TRDetail.getString("stop_timer"));
		String sql1 = "UPDATE time_record SET duration = "+duration+" "+"where timer_id = "+TRDetail.getInt("timerId");
		Class.forName(driver);
			try {
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				ps= conn.prepareStatement(sql1);
				int count1 = ps.executeUpdate();
					if((count1==0)){
						log.error("Failed to update workFlow Specified field data into TimerRecord Table");
						return Response.ok("Failed to update").build();
					}
				JSONObject detail  = JSONUpdateOfWorkFlow(TRDetail.getInt("wfId"));
	
					try {
						conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
						String sql2 ="UPDATE workflowdetail SET duration = "+detail.getInt("totalDuration") +
								" , play_status = "+TRDetail.getBoolean("play_status")+" , timerId = "+null+" where wf_id ="+TRDetail.getInt("wfId");
						ps= conn.prepareStatement(sql2);
						int count2 = ps.executeUpdate();
							if((count2==0)){ 
								log.error("Failed to update total duration in a work Flow Table");
								return Response.ok("Failed to update total duration").build();
							}
						int updateEndTimer = updateEndTimerInWorkFlow(TRDetail);
							if((updateEndTimer==0))
							{ 
								log.error("Failed to update End Timer in work Flow Table");
								return Response.ok("Failed to update End Timer").build();
							}
						dynamicUpdateStatus(TRDetail.getInt("wfId"));
					}catch(Exception ex) {
						log.error("Exceptio :"+ex.getMessage());
						//ex.printStackTrace();
					}
			}catch(Exception ex) {
				log.error("Exception :" +ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}
			
		boolean pauseTimerRecord = getPlayStatus(TRDetail.getInt("wfId"));
		return Response.ok(pauseTimerRecord).build();	
	}


	@GET
	@Path("/getTimeRecordRelatedToJob")
	public Response getTimeRecordRelatedToJob(@HeaderParam("wfId") int wfId) throws ParseException, ClassNotFoundException, SQLException, JSONException
	{
		JSONArray jsonArray = new JSONArray();
			try {
				String sql = "select * from time_record where wf_id ="+wfId;
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
					while(rs.next())
					{
						ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
						int count = rsmd.getColumnCount();
						JSONObject obj = new JSONObject();
							for(int i=1;i<=count;i++){
								obj.put(rsmd.getColumnName(i), rs.getString(i));
							}
						jsonArray.put(obj);	
					}
			}catch(Exception ex) {
				log.error("Exception :"+ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}
	
		return Response.ok(jsonArray.toString()).build();
	}

	@GET
	@Path("/readAllTimeRecord")
	public Response getAllTimeRecord() throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray jsonArray = new JSONArray();
			try {
				String sql = "select * from time_record";
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
					while(rs.next())
					{
						ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
						int count = rsmd.getColumnCount();
						JSONObject obj = new JSONObject();
							for(int i=1;i<=count;i++)
							{
								obj.put(rsmd.getColumnName(i), rs.getString(i));
							}
						jsonArray.put(obj);	
					}
			}catch(Exception ex) {
				log.error("Exception :" +ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}

		return Response.ok(jsonArray.toString()).build();

	}


	@GET
	@Path("/readWorkFlowOfUser")
	public Response readWorkFlowOfUser(@HeaderParam("userId") int userId) throws ClassNotFoundException, SQLException, JSONException
	{
		ResultSet rs1 =null;
		JSONArray taskAllRecord = new JSONArray();
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				String sql ="select * from workflowdetail where assigned_to_id = "+userId;
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				JSONArray workFlowRecord = getRowRecord(rs);
					for(int i=0;i<workFlowRecord.length();i++)
					{
						JSONObject taskRecord = new JSONObject();
						JSONObject flowRecord = workFlowRecord.getJSONObject(i);
							if(!flowRecord.isNull("task_id")) {
								String sqlQuery ="select * from packagingtask_part1"
										+ " where task_id1 ="+flowRecord.getInt("task_id");
								Statement stmt1 =  conn.createStatement();
								rs1 = stmt1.executeQuery(sqlQuery);
			
								JSONArray jobRecord = getRowRecord(rs1);
									for(int j=0;j<jobRecord.length();j++)
									{
										taskRecord =jobRecord.getJSONObject(j);
										flowRecord.put("taskDetails", taskRecord);
										flowRecord.put("wfId",flowRecord.getString("wf_id"));
											if(flowRecord.getString("assigned_to_id") != null){
												taskRecord.put("assignToId", flowRecord.getString("assigned_to_id"));
											}
										flowRecord.put("playStatus", ( flowRecord.getString("play_status") != null && flowRecord.getString("play_status").equals("1") ) ? true : false );
										flowRecord.put("submit", true);
									}
							}
						taskAllRecord.put(flowRecord);
					}
			}catch(Exception ex) {
				log.error("Exception :"+ex.getMessage());
				//ex.printStackTrace();
			}finally {
				conn.close();
			}
		return Response.ok(taskAllRecord.toString()).build();
	}

	@GET
	@Path("/readAllWorkFlow")
	public Response readAllWorkFlow() throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray readAllRowData = new JSONArray();
			try {
				String sql = "select * from workflowdetail,packagingtask_part1 "
						+"where workflowdetail.task_id=packagingtask_part1.task_id1";
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
					while(rs.next())
					{ 
						JSONObject readRowData = new JSONObject();
						ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
						int count = rsmd.getColumnCount();
							for(int i=1;i<=count;i++)
							{
								readRowData.put(rsmd.getColumnName(i), rs.getString(i));
							}
						readAllRowData.put(readRowData);
					}
			}catch(Exception ex) {
				log.error("Exception :"+ex.getMessage());
			//	ex.printStackTrace();
			}finally {
				conn.close();
			}

		return Response.ok(readAllRowData.toString()).build();

	}
	public int durationCalculation(String startTimer , String pauseTimer) throws ParseException
	{
		System.out.println("starttimer"+startTimer);
		System.out.println("pauseTimer"+pauseTimer);
		String[] startTimer1 = startTimer.split(",");
		String[] pauseTimer1 = pauseTimer.split(",");
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date date1 = format.parse(startTimer1[1]);
		Date date2 = format.parse(pauseTimer1[1]);
		long difference = date2.getTime() - date1.getTime(); 
		int durInSevs = (int) (difference/1000);
		int durInMins = 0;
		float mins=durInSevs%60;
		System.out.println("mins are:"+mins);
			if(mins==0.0) {
				durInMins=durInSevs/60;
			}else {
				durInMins=(durInSevs/60)+1;
			}
	//	System.out.println("durInMins:"+durInMins);
		//log.info("Duration");
		return durInMins ;

	}

	public String startTimerQueryInsert(JSONObject workFlow) throws JSONException
	{
		String sqlQuery = null;
		if(!(workFlow.isNull("user")) && !( workFlow.isNull("status"))){
			sqlQuery = "INSERT INTO  time_record(wf_id,task_id,assignBy,user,status,start_timer)"
					+ " values("+workFlow.getInt("wfId")+",'"+workFlow.getString("task_id")+"','"+workFlow.getString("assignBy")
					+"','"+workFlow.getString("user")+"','"+workFlow.getString("status")+"','"+workFlow.getString("start_timer")+"')";
		}else
		{
			sqlQuery = "INSERT INTO  time_record(wf_id,task_id,assignBy,start_timer)"
					+ " values("+workFlow.getInt("wfId")+",'"+workFlow.getString("task_id")+"','"+workFlow.getString("assignBy")
					+"','"+workFlow.getString("start_timer")+"')";
		}
		return sqlQuery;
	}


	public JSONObject getSelectedRow(int taskId) throws SQLException, ClassNotFoundException, JSONException
	{
		JSONObject rowWiseData = new JSONObject();

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from workflowdetail where task_id ="+taskId;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				rowWiseData.put("wfId", rs.getInt("wf_id"));
				rowWiseData.put("taskId", rs.getString("task_id"));
				rowWiseData.put("assignBy", rs.getString("assign_by"));
				rowWiseData.put("assignTo", rs.getString("assign_to"));
				rowWiseData.put("status", rs.getString("status"));
				rowWiseData.put("date",rs.getString("status"));
				rowWiseData.put("assignDisable",rs.getString("assignDisable"));

			}

		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
		//	ex.printStackTrace();
		}finally {
			conn.close();
		}

		return rowWiseData;
	}

	public JSONObject getSelectedRowTimer(String wfId) throws SQLException, ClassNotFoundException, JSONException
	{
		JSONObject rowWiseData = new JSONObject();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from time_record where wf_id ='"+wfId+"'";
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				rowWiseData.put("timerId", rs.getInt("timer_id"));
				rowWiseData.put("wfId", rs.getInt("wf_id"));
				rowWiseData.put("taskId", rs.getString("task_id"));
				rowWiseData.put("assignTo", rs.getString("user"));
				rowWiseData.put("status", rs.getString("status"));		
			}

		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return rowWiseData;
	}

	public int updateQueryInTable(JSONObject TRDetail) throws ClassNotFoundException, SQLException, JSONException
	{
		PreparedStatement ps=null;
		int count =0;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql = "UPDATE time_record SET stop_timer = '"+TRDetail.getString("stop_timer")+"' "+"where timer_id = "+TRDetail.getInt("timerId");
			ps= conn.prepareStatement(sql);
			count = ps.executeUpdate();
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return count;	
	}

	public JSONObject JSONUpdateOfWorkFlow(int wf_id) throws Exception
	{
		JSONObject totalObj = new JSONObject();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from time_record where wf_id = "+wf_id;
			ResultSet rs = stmt.executeQuery(sql);

			int totalDuration=0;
			int timerId=0;
			while(rs.next())
			{ 
				int duration = rs.getInt("duration");
				totalDuration = totalDuration+duration;
				timerId = rs.getInt("timer_id");

			}
			totalObj.put("totalDuration",totalDuration);
			totalObj.put("timerId", timerId);

		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return totalObj;

	}
	public JSONObject getWorkFlowDetailAfrerStartTimer(int wf_id) throws SQLException
	{
		JSONObject totalObj = new JSONObject();
		int timerId=0;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from time_record where wf_id = "+wf_id;
			ResultSet rs = stmt.executeQuery(sql);


			while(rs.next())
			{ 
				timerId = rs.getInt("timer_id");			
			}
			totalObj.put("timerId", timerId);

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return totalObj;

	}

	public boolean getPlayStatus(int wfId) throws ClassNotFoundException, SQLException, JSONException
	{
		boolean PlayStatus = false;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select play_status from workflowdetail where wf_id = "+wfId;
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				PlayStatus = rs.getString("play_status") != null;
			}

		}catch(Exception ex) 
		{
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}
		finally {
			conn.close();
		}
		return PlayStatus;

	}
	public String getStartTimerFromTable(int timerId) throws ClassNotFoundException, SQLException
	{
		String startTimer = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sqlSelect = "select * from time_record  where timer_id = "+timerId;
			ResultSet rs = stmt.executeQuery(sqlSelect);
			while(rs.next())
			{
				startTimer = rs.getString("start_timer");	 
			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}
		return startTimer;
	}
	
	public String insertWorkFlowDetail(JSONObject workFlowJson) throws JSONException
	{
		String SqlQuery = "INSERT INTO  workflowdetail(task_id,assign_by,assign_to,assigned_by_id,assigned_to_id,status,startTime,play_status,wf_status)"
				+ " values("+workFlowJson.getString("task_id")+",'"+workFlowJson.getString("assign_by")
				+"','"+workFlowJson.getString("assign_to")+"','"+workFlowJson.getString("assignById")+"','"+workFlowJson.getString("assignToId")
				+ "','"+workFlowJson.getString("status")+"','"+workFlowJson.getString("taskTime")+"',"+workFlowJson.getBoolean("play_status")+",'Open')";

		return SqlQuery;
	}

	public JSONArray getRecord(ResultSet rs) throws SQLException, JSONException
	{

		JSONArray allRowWiseData = new JSONArray();
		while(rs.next())
		{
			JSONObject rowWiseData = new JSONObject();
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			int count = rsmd.getColumnCount();
			for(int i=1;i<=count;i++)
			{
				String taskName = rsmd.getColumnName(i);
				String taskValue = rs.getString(i);
				if(taskValue!=null)
				{
					rowWiseData.put(taskName, taskValue.toString());
				}else
				{
					continue; 
				}
			}
			allRowWiseData.put(rowWiseData);
		}
		return allRowWiseData;
	}
	public JSONArray getRowRecord(ResultSet rs) throws SQLException, JSONException
	{
		JSONArray rowAllData = new JSONArray();
		while(rs.next())
		{
			JSONObject rowWiseData = new JSONObject();
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			int count = rsmd.getColumnCount();
			for(int i=1;i<=count;i++)
			{
				String taskName = rsmd.getColumnName(i);
				String taskValue = rs.getString(i);
				if(taskValue!=null)
				{
					rowWiseData.put(taskName, taskValue.toString());
				}
				else
				{
					rowWiseData.put(taskName, "0");
				}
			}
			rowAllData.put(rowWiseData);
		}
		return rowAllData;
	}

	public String createInsertQueryofWorkFlowCreation(JSONObject addInfoJson) throws JSONException {
		String SqlQuery = "INSERT INTO workflowdetail (task_id,assign_by,assign_to,"
				+ "assigned_by_id,assigned_to_id, status,startTime,play_status,wf_status,assignDisable) VALUES('"+addInfoJson.getString("task_id")+"',"
				+ "'"+addInfoJson.getString("assign_by")+"','"+addInfoJson.getString("assign_to")+"',"
				+ "'"+addInfoJson.getString("assigned_by_id")+"',"
				+ "'"+addInfoJson.getString("assigned_to_id")+"','"
				+addInfoJson.getString("status")+ "','"+addInfoJson.getString("startTime")+"',"
				+addInfoJson.getBoolean("play_status")+",'"+addInfoJson.getString("wf_status")+"','"
				+""+addInfoJson.getString("assignDisablity")+"')";

		return SqlQuery;
	}

	public JSONArray getWorkFlowRecord(String taskId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray workFlowArray = new JSONArray();

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from workflowdetail where task_id ='"+taskId+"'";
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();
				JSONObject rowWiseData = new JSONObject();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					if(taskValue!=null){
						rowWiseData.put(taskName, taskValue.toString());}else
						{
							continue;
						}
				}
				workFlowArray.put(rowWiseData);
			}
		}catch(Exception ex) {
			log.error("exception :"+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return workFlowArray;
	}

	public String updateWorkFlowDetail(JSONObject updateData) throws JSONException
	{
		String sql = "UPDATE workflowdetail SET task_id = '"+updateData.getString("task_id")+"', assign_by ='"+
				updateData.getString("assign_by")+"', assign_to = '"+updateData.getString("assign_to")+"',status = '"+
				updateData.getString("status")+"', date = '"+updateData.getString("date")+"', "
				+ "play_status ="+updateData.getBoolean("play_status")+" where wf_id ="+updateData.getInt("wfId");
		return sql;
	}

	public int taskUpdate(JSONObject workFlowObj) throws JSONException, ClassNotFoundException, SQLException
	{
		String status = workFlowObj.getString("status");
		PreparedStatement ps=null;
		int taskId = Integer.parseInt(workFlowObj.getString("task_id"));
		int count = 0;
		int duration = totalDurationWf(taskId);
		String dur = duration+" min";
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			switch(status)
			{
			case "Pre Flight-Check" : {
				String query  = "UPDATE packagingtask_part1 SET pfc = '"+workFlowObj.getString("assign_to")+"' ,"
						+ "job_status = '"+status+"',duration='"+dur+"' where task_id1 ="+taskId;
				ps= conn.prepareStatement(query);
				count = ps.executeUpdate();
				break;
			}
			case "WIP" : {
				String query  = "UPDATE packagingtask_part1 SET pa = '"+workFlowObj.getString("assign_to")+"' ,"
						+ "job_status = '"+status+"',duration='"+dur+"' where task_id1 ="+taskId;
				ps= conn.prepareStatement(query);
				count = ps.executeUpdate();
				break;
			}
			case "QC" :{ 
				String query  = "UPDATE packagingtask_part1 SET qc = '"+workFlowObj.getString("assign_to")+"' ,"
						+ "job_status = '"+status+"',duration='"+dur+"'  where task_id1 ="+taskId;
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				ps= conn.prepareStatement(query);
				count = ps.executeUpdate();
				break;
			}
			case "Approved" : {
				String query ="UPDATE packagingtask_part1 SET task_status = 'Close' , job_status = '"+status+"',duration='"+dur+"'"
						+ " where task_id1 ="+taskId;
				ps= conn.prepareStatement(query);
				count = ps.executeUpdate();
				break;
			}
			
			default:{
				String query ="UPDATE packagingtask_part1 SET  job_status = '"+status+"',duration='"+dur+"'"
						+ " where task_id1 ="+taskId;
				ps= conn.prepareStatement(query);
				count = ps.executeUpdate();
				break;
			}

			}
		}catch(Exception ex) {
			//ex.printStackTrace();
		}finally {
			conn.close();
		}
		return count;
	}


	public JSONObject getWorkFlow(int assignedToId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONObject rowWiseData = new JSONObject();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from workflowdetail where assigned_to_id ="+assignedToId;
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					if(taskValue!=null){
						rowWiseData.put(taskName, taskValue);
					}else{
						rowWiseData.put(taskName,"0"); 
					}
				}
			}

		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		} finally {
			conn.close();
		}

		return rowWiseData;
	}

	public JSONObject getSelectedTimer(int wfId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONObject rowWiseData = new JSONObject();
		try {

			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql = "select * from workflowdetail where wf_id ="+wfId;
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					String taskValue = rs.getString(i);
					if(taskValue!=null){
						rowWiseData.put(taskName, taskValue);
					}else{
						rowWiseData.put(taskName,"0"); 
					}
				}
			}
		}catch(Exception ex) 
		{
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		} 
		finally{
			conn.close();
		}
		return rowWiseData;


	}
	public JSONObject getTaskBasedRecord(int taskId) throws Exception
	{
		JSONObject rowWiseData = new JSONObject();
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql = "select * from packagingtask_part1"
					+ "  where task_id1 ="+taskId ;
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);


			while(rs.next())
			{
				ResultSetMetaData rsmd = rs.getMetaData();

				int count = rsmd.getColumnCount();
				for(int i=1;i<=count;i++)
				{
					String taskName = rsmd.getColumnName(i);
					if(rs.getString(i)!=null){
						String taskValue = rs.getString(i);
						rowWiseData.put(taskName, taskValue.toString());	
					}else
					{
						rowWiseData.put(taskName, "0");
					}
				}  
			}

		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}
		return rowWiseData;
	}
	public int updateTimerInWorkFlow(JSONObject jsonObj) throws JSONException, ClassNotFoundException, SQLException
	{
		PreparedStatement ps=null;
		int count =0;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql = "select * from workflowdetail where wf_id = "+jsonObj.getInt("wfId");
			ps=conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			String startTimerWf = null;

			if(rs.next())
			{
				startTimerWf = rs.getString("start_timer_wf");
			}
			if(startTimerWf==null)
			{
				String sqlUpdate = "UPDATE workflowdetail SET start_timer_wf = '"+jsonObj.getString("start_timer")+"' "
						+ "where wf_id = "+jsonObj.getInt("wfId");
				ps= conn.prepareStatement(sqlUpdate);
				count = ps.executeUpdate();
			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}


		return count;
	}

	public int updateEndTimerInWorkFlow(JSONObject jsonObj) throws JSONException, ClassNotFoundException, SQLException
	{
		int count = 0;
		PreparedStatement ps=null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sqlUpdate = "UPDATE workflowdetail SET stop_timer_wf = '"+jsonObj.getString("stop_timer")+"' "
					+ "where wf_id = "+jsonObj.getInt("wfId");
			ps= conn.prepareStatement(sqlUpdate);
			count = ps.executeUpdate();

		}
		catch(Exception ex)
		{
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}

		return count;   
	}
	public int dynamicUpdateStatus(int wf_id) throws SQLException{
		 PreparedStatement ps=null;
		 int count = 0;
		 try {
				Class.forName(driver);
				conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				String sql = "select status , duration,task_id from workflowdetail"
						+ "  where wf_id ="+wf_id ;
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					int durTotal = totalDurationWf(rs.getInt("task_id"));
					System.out.println("total duration:" +durTotal);
					String dur = durTotal+" min";
					Class.forName(driver);
					conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
					String query ="UPDATE packagingtask_part1 SET job_status = '"+rs.getString("status")+"',duration='"+dur+"'"
							+ " where task_id1 ="+rs.getString("task_id");
					ps= conn.prepareStatement(query);
					count = ps.executeUpdate();
						if(count==0)
						{
							log.error("Failed to update Job status in Packaging Task Part1 Table");
							return 0;
						}
				}
		 
		 }catch(Exception e){
			 log.error("Exception :" +e.getMessage());
			// e.printStackTrace();
		 }finally {
			conn.close();	
			}

		return count;
		 
	 }



	public int totalDurationWf(int taskId) throws ClassNotFoundException, SQLException
	{
		int duration =0;
		try {
			String sql = "select duration from workflowdetail where task_id='"+taskId+"'";
			Class.forName(driver);
			conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				if(rs.getString("duration")!=null)
				{
					int dur = Integer.parseInt(rs.getString("duration"));
					duration += dur;
				}
				else
				{
					continue;
				}
			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}finally {
			conn.close();
		}
		return duration;
	}
	
 
}
