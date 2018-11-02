package com.stirred.packaging.Endpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.stereotype.Component;

/***
 *  This Reports shows the history of work
 *  it consist of following methods.
 *  1.No of jobs/client
 *  2.job type/client
 *  3.category/client
 *  4.brand/client
 *  5.jobs done by packing designer 
 *  6.jobs done by Quality controller.
 *  
 * @author Siraj
 *
 */

@Component
@Path("/Reports")
public class ReportsEndPoint {

	final String Driver_Manager ="com.mysql.jdbc.Driver";
	private static final String PROP_FILENAME = "PMS.properties";
	static PropertiesConfiguration config;
	private static Logger log = Logger.getLogger(ReportsEndPoint.class.getName());

	GlobalVariables gv=new GlobalVariables();
	MasterJsonFileEndPoint mj=new MasterJsonFileEndPoint();

	final String DBuser=gv.getString("db.user");
	final String DBport=gv.getString("db.port");
	final String DBpass=gv.getString("db.password");
	final String DBhost=gv.getString("db.hostname");
	final String DBname=gv.getString("db.database");

	final String ConnURL="jdbc:mysql://"+DBhost+":"+DBport+"/"+DBname;

	@GET
	@Path("/completedJobs")

	/***
	 * Based on client name and year counting number of 
	 * jobs has been completed.
	 * @param year
	 * @param client_name
	 * @return
	 * @throws Exception
	 */

	public Response DeliveredJob(@HeaderParam("year") String year,@HeaderParam("client_name") String client_name) throws Exception{

		System.out.println(year);
		System.out.println(client_name);
		String yearStart=year+"-01-01";
		String yearEnd=year+"-12-31";
		JSONArray jarray= new JSONArray();

		try {

			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);

			//			String Query = "SELECT workflowdetail.task_id ,packagingtask_part1.brief_date FROM packagingtask_part1,workflowdetail "
			//					+ "    WHERE packagingtask_part1.task_id1=workflowdetail.task_id and"
			//					+ "    workflowdetail.status = 'Approved' and "
			//					+ "    brief_date between ? and ? ORDER BY brief_date ASC";
			String Query="SELECT task_id1,brief_date FROM packagingtask_part1 WHERE Job_status= 'Approved' AND client_name = ? AND brief_date BETWEEN ? AND ? ORDER BY brief_date ASC";

			PreparedStatement stmt=con.prepareStatement(Query);  

			stmt.setString(1, client_name);
			stmt.setString(2,yearStart);  
			stmt.setString(3,yearEnd);

			ResultSet rs=stmt.executeQuery();

			String Month="";
			String Count="";

			while(rs.next()) {

				Count=Count+rs.getString(2).substring(0,rs.getString(2).length() - 3)+" ";
				//				System.out.println(rs.getString(1)+"--------------->"+rs.getString(2));
			}
			if(Count!=null) {
				final String[] ListOfMonths = {year+"-01", year+"-02", year+"-03",year+"-04",year+"-05",year+"-06",year+"-07",year+"-08",year+"-09",year+"-10",year+"-11",year+"-12"};
				List<String> listOfLocations = Arrays.asList(Count.split(" "));
				List<String> listOfMonths = Arrays.asList(ListOfMonths);
				Set<String> uniqLocation = new HashSet<String>(listOfMonths);

				for(int i=0;i<uniqLocation.size();i++) {

					JSONObject json=new JSONObject();

					String date[]=ListOfMonths[i].split("-");
					String month=date[1];
					//	System.out.println(month);
					switch(month) {
					case "01":
						Month = "Jan";
						break;
					case "02":
						Month = "Feb";
						break;
					case "03":
						Month = "Mar";
						break;
					case "04":
						Month = "Apr";
						break;
					case "05":
						Month = "May";
						break;
					case "06":
						Month = "Jun";
						break;
					case "07":
						Month = "Jul";
						break;
					case "08":
						Month = "Aug";
						break;
					case "09":
						Month = "Sep";
						break;
					case "10":
						Month = "Oct";
						break;
					case "11":
						Month = "Nov";
						break;
					case "12":
						Month = "Dec";
						break;
					}
					json.put("Year", year);
					json.put("Month", Month);
					json.put("DeliveredJobs", Collections.frequency(listOfLocations, ListOfMonths[i]));
					jarray.put(json);
				}
			}
			//	System.out.println("No of Jobs :"+jarray);
		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}

		return Response.ok(jarray.toString()).build();

	}

	@POST
	@Path("/JobTypes")	
	/**
	 * Populating data based on jobtype,clientName,year and month as input.
	 * Fetching data for particular client has completed certain jobs on certain year
	 * based on Job-type .
	 * 
	 * @param jobTypeData
	 * @return
	 * @throws Exception
	 */
	public Response JobTypes(String jobTypeData) throws Exception{
		JSONObject jobj=new JSONObject(jobTypeData);

		String Year=jobj.getString("year");
		String Month=jobj.getString("month").toString().toLowerCase();

		System.out.println(Month);

		String client_name=jobj.getString("client_name");
		JSONArray JobType = (JSONArray)jobj.get("jobtype");

		String data=null;

		String yearStart=null;
		String yearEnd=null;

		JSONArray jarray = new JSONArray();

		switch(Month) {

		case "jan":
			yearStart=Year+"-01-01";
			yearEnd=Year+"-01-31";
			break;

		case "feb":
			yearStart=Year+"-02-01";
			yearEnd=Year+"-02-29";
			break;

		case "mar":
			yearStart=Year+"-03-01";
			yearEnd=Year+"-03-31";
			break;

		case "apr":
			yearStart=Year+"-04-01";
			yearEnd=Year+"-04-30";
			break;

		case "may":
			yearStart=Year+"-05-01";
			yearEnd=Year+"-05-31";
			break;

		case "jun":
			yearStart=Year+"-06-01";
			yearEnd=Year+"-06-30";
			break;

		case "jul":
			yearStart=Year+"-07-01";
			yearEnd=Year+"-07-31";
			break;

		case "aug":
			yearStart=Year+"-08-01";
			yearEnd=Year+"-08-31";
			break;

		case "sep":
			yearStart=Year+"-09-01";
			yearEnd=Year+"-09-30";
			break;

		case "oct":
			yearStart=Year+"-10-01";
			yearEnd=Year+"-10-31";
			break;

		case "nov":
			yearStart=Year+"-11-01";
			yearEnd=Year+"-11-30";
			break;

		case "dec":
			yearStart=Year+"-12-01";
			yearEnd=Year+"-12-31";
			break;

		case "all":
			yearStart=Year+"-01-01";
			yearEnd=Year+"-12-31";
			break;
		}
		//System.out.println(JobType.length());
		try {

			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			List<String> AllJobs=new ArrayList<String>();
			List<String> Amend=new ArrayList<String>();
			List<String> BUild=new ArrayList<String>();
			List<String> ReBuild=new ArrayList<String>();
			List<String> AmendRecreation=new ArrayList<String>();
			List<String> AmendArt=new ArrayList<String>();
			List<String> ImageWork=new ArrayList<String>();
			List<String> Dispatch=new ArrayList<String>();
			List<String> Npd=new ArrayList<String>();
			List<String> Repro=new ArrayList<String>();
			List<String> Others=new ArrayList<String>();
			for(int i=0;i<JobType.length();i++) {
				//System.out.println(JobType.get(i));
				switch(JobType.get(i).toString()){
				case "AMEND":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type NOT LIKE '%amend artwork%' AND job_type LIKE '%amend%' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						Amend.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&AMEND");
					}
					//					AllTaskI.addAll(AmendList);
					//					Iterator it = AmendList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					AllJobs.addAll(Amend);
					break;
				}
				case "BUILD":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type NOT LIKE '%re-build%' AND job_type LIKE '%build%' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						BUild.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&BUILD");
					}
					AllJobs.addAll(BUild);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}	case "RE-BUILD":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type LIKE '%re-build%' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						ReBuild.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&RE-BUILD");
					}
					AllJobs.addAll(ReBuild);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}case "AMEND_ARTWORK":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type LIKE 'amend artwork' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						AmendArt.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&AMEND_ARTWORK");
					}
					AllJobs.addAll(AmendArt);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}
				case "REPRO":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type LIKE 'repro' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						Repro.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&REPRO");
					}
					AllJobs.addAll(Repro);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}case "DISPATCH":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type LIKE '%DISPATCH%' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						Dispatch.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&DISPATCH");
					}
					AllJobs.addAll(Dispatch);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}case "NPD":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type LIKE 'npd' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						Npd.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&NPD");
					}
					AllJobs.addAll(Npd);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}case "IMAGE_WORK":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type = 'Image work' AND brief_date BETWEEN ? AND ?  AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						System.out.println();
						ImageWork.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&IMAGE_WORK");
					}
					AllJobs.addAll(ImageWork);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}case "ARTWORK_RECREATION":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type LIKE 'Artwork Re-Creation' AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						AmendRecreation.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&ARTWORK_RECREATION");
					}
					AllJobs.addAll(AmendRecreation);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}case "OTHERS":{
					String Query ="select job_type,brief_date from packagingtask_part1 WHERE client_name = ? AND job_type IN ('PV','DV','SOP','Package Request') AND brief_date BETWEEN ? AND ? AND Job_status = 'Approved'";
					PreparedStatement ps=con.prepareStatement(Query);
					ps.setString(1, client_name);
					ps.setString(2, yearStart);
					ps.setString(3, yearEnd);
					ResultSet rs=ps.executeQuery();
					while(rs.next()) {
						Others.add(rs.getString(2).substring(0,rs.getString(2).length() - 3)+"&OTHERS");
					}
					AllJobs.addAll(Others);
					//					Iterator it = BUildList.iterator();
					//					while(it.hasNext()) {
					//					System.out.println(it.next());
					//					}
					break;
				}

				}
			}
			if((Month.equalsIgnoreCase("ALL"))){
				int size=JobType.length();
				String jobdata[] = new String[size*12];
				for(int i=0;i<size;i++) {
					//	System.out.println("am in "+i);
					jobdata[i]=Year+"-01&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(1*size)]=Year+"-02&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(2*size)]=Year+"-03&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(3*size)]=Year+"-04&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(4*size)]=Year+"-05&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(5*size)]=Year+"-06&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(6*size)]=Year+"-07&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(7*size)]=Year+"-08&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(8*size)]=Year+"-09&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(9*size)]=Year+"-10&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(10*size)]=Year+"-11&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(11*size)]=Year+"-12&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
				}
				final String[] ListOfMonths=jobdata;
				List<String> ListOfJobtypes = Arrays.asList(ListOfMonths);
				Set<String> uniqType = new HashSet<String>(ListOfJobtypes);
				for(int i=0;i<uniqType.size();i++) {
					JSONObject json=new JSONObject();

					String unique[]=ListOfMonths[i].split("-");
					String date[]=unique[1].split("&");
					String month=date[0];
					String typejob=date[1];
					switch(month) {

					case "01":
						month = "Jan";
						break;
					case "02":
						month = "Feb";
						break;
					case "03":
						month = "Mar";
						break;
					case "04":
						month = "Apr";
						break;
					case "05":
						month = "May";
						break;
					case "06":
						month = "Jun";
						break;
					case "07":
						month = "Jul";
						break;
					case "08":
						month = "Aug";
						break;
					case "09":
						month = "Sep";
						break;
					case "10":
						month = "Oct";
						break;
					case "11":
						month = "Nov";
						break;
					case "12":
						month = "Dec";
						break;
					}
					json.put("Client", client_name);
					json.put("Year", Year);
					json.put("Month", month);
					json.put("JobType", typejob.replaceAll("_", " "));
					json.put("DeliveredJobs", Collections.frequency(AllJobs, ListOfMonths[i]));
					jarray.put(json);
				}
			}else if(!Month.equalsIgnoreCase("ALL")) {
				int size=JobType.length();
				String jobdata[] = new String[size];
				String month="";
				switch(Month) {
				case "jan":
					month="01";
					break;
				case "feb":
					month="02";
					break;
				case "mar":
					month="03";
					break;
				case "apr":
					month="04";
					break;
				case "may":
					month="05";
					break;
				case "jun":
					month="06";
					break;
				case "jul":
					month="07";
					break;
				case "aug":
					month="08";
					break;
				case "sep":
					month="09";
					break;
				case "oct":
					month="10";
					break;
				case "nov":
					month="11";
					break;
				case "dec":
					month="12";
					break;

				}
				for(int i=0;i<size;i++) {
					//	System.out.println("am in "+i);
					jobdata[i]=Year+"-"+month+"&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					System.out.println(jobdata[i]);
				}
				final String[] ListOfMonths=jobdata;
				List<String> ListOfJobtypes = Arrays.asList(ListOfMonths);
				Set<String> uniqType = new HashSet<String>(ListOfJobtypes);
				for(int i=0;i<uniqType.size();i++) {
					JSONObject json=new JSONObject();

					String unique[]=ListOfMonths[i].split("-");
					String date[]=unique[1].split("&");
					String typejob=date[1];

					json.put("Client", client_name);
					json.put("Year", Year);
					json.put("Month", Month);
					json.put("JobType", typejob.replaceAll("_", " "));
					json.put("DeliveredJobs", Collections.frequency(AllJobs, ListOfMonths[i]));
					jarray.put(json);
				}
			}
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(jarray.toString()).build();

	}


	@POST
	@Path("/JobCategory")	
	/**
	 * Populating data based on category accepting input year,month,client name
	 * And category.
	 * @param jobCategoies
	 * @return
	 * @throws Exception
	 */
	public Response JobCategory(String jobCategoies) throws Exception{

		JSONObject jobj=new JSONObject(jobCategoies);
		System.out.println(jobCategoies);
		String Year=jobj.getString("year");
		String Month=jobj.getString("month").toUpperCase();
		String client_name=jobj.getString("client_name");
		JSONArray JobType = (JSONArray)jobj.get("category");

		String data=null;

		String yearStart=null;
		String yearEnd=null;

		JSONArray jarray = new JSONArray();

		switch(JobType.length()) {
		case 1:
			data="category = '"+JobType.get(0)+"' ";
			break;
		case 2: 
			data="category = '"+JobType.get(0)+"' OR category = '"+JobType.get(1)+"' ";
			break;
		case 3:
			data="category = '"+JobType.get(0)+"' OR category = '"+JobType.get(1)+"' OR category = '"+JobType.get(2)+"' ";
			break;
		case 4:
			data="category = '"+JobType.get(0)+"' OR category = '"+JobType.get(1)+"' OR category = '"+JobType.get(2)+"' OR category = '"+JobType.get(3)+"' ";
			break;
		case 5:
			data="category = '"+JobType.get(0)+"' OR category = '"+JobType.get(1)+"' OR category = '"+JobType.get(2)+"' OR category = '"+JobType.get(3)+"' OR category = '"+JobType.get(4)+"' ";
			break;
		}

		switch(Month) {

		case "JAN":
			yearStart=Year+"-01-01";
			yearEnd=Year+"-01-31";
			break;

		case "FEB":
			yearStart=Year+"-02-01";
			yearEnd=Year+"-02-29";
			break;

		case "MAR":
			yearStart=Year+"-03-01";
			yearEnd=Year+"-03-31";
			break;

		case "APR":
			yearStart=Year+"-04-01";
			yearEnd=Year+"-04-30";
			break;

		case "MAY":
			yearStart=Year+"-05-01";
			yearEnd=Year+"-05-31";
			break;

		case "JUN":
			yearStart=Year+"-06-01";
			yearEnd=Year+"-06-30";
			break;

		case "JUL":
			yearStart=Year+"-07-01";
			yearEnd=Year+"-07-31";
			break;

		case "AUG":
			yearStart=Year+"-08-01";
			yearEnd=Year+"-08-31";
			break;

		case "SEP":
			yearStart=Year+"-09-01";
			yearEnd=Year+"-09-30";
			break;

		case "OCT":
			yearStart=Year+"-10-01";
			yearEnd=Year+"-10-31";
			break;

		case "NOV":
			yearStart=Year+"-11-01";
			yearEnd=Year+"-11-30";
			break;

		case "DEC":
			yearStart=Year+"-12-01";
			yearEnd=Year+"-12-31";
			break;

		case "ALL":
			yearStart=Year+"-01-01";
			yearEnd=Year+"-12-31";
			break;
		}

		//		String Query= "select COUNT(task_id1),category,brief_date FROM packagingtask_part1 WHERE Job_status= 'Approved' AND client_name = ? "
		//				+ "AND brief_date BETWEEN ? AND ? "
		//				+ "AND ("+data+") GROUP BY category, EXTRACT(MONTH FROM brief_date)ORDER BY brief_date";
		String Query = "select brief_date,category FROM packagingtask_part1 WHERE Job_status= 'Approved' AND client_name = ? "
				+ "AND brief_date BETWEEN ? AND ? "
				+ "AND ("+data+") ";
		System.out.println(Query);

		try {
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);

			PreparedStatement ps=con.prepareStatement(Query);
			ps.setString(1, client_name);
			ps.setString(2, yearStart);
			ps.setString(3, yearEnd);
			ResultSet rs=ps.executeQuery();

			String count="";

			if(Month.equalsIgnoreCase("ALL")){
				while(rs.next()) {
					//					System.out.println(rs.getString(1)+"&"+rs.getString(2));
					//					System.out.println(count); 
					count=count+rs.getString(1).substring(0,rs.getString(1).length() - 3)+"&"+rs.getString(2).replace(" ", "_").toUpperCase()+" ";
				}
				System.out.println(count); 
				int size=JobType.length();
				String jobdata[] = new String[size*12];
				for(int i=0;i<size;i++) {
					//	System.out.println("am in "+i);
					jobdata[i]=Year+"-01&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(1*size)]=Year+"-02&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(2*size)]=Year+"-03&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(3*size)]=Year+"-04&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(4*size)]=Year+"-05&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(5*size)]=Year+"-06&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(6*size)]=Year+"-07&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(7*size)]=Year+"-08&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(8*size)]=Year+"-09&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(9*size)]=Year+"-10&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(10*size)]=Year+"-11&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(11*size)]=Year+"-12&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
				}
				final String[] ListOfMonths=jobdata;
				List<String> listOfJobs = Arrays.asList(count.split(" "));
				List<String> ListOfJobs = Arrays.asList(ListOfMonths);
				Set<String> uniqLocation = new HashSet<String>(ListOfJobs);
				for(int i=0;i<uniqLocation.size();i++) {
					JSONObject json=new JSONObject();

					String unique[]=ListOfMonths[i].split("-");
					String date[]=unique[1].split("&");
					String month=date[0];
					String typejob=date[1];
					switch(month) {

					case "01":
						month = "Jan";
						break;
					case "02":
						month = "Feb";
						break;
					case "03":
						month = "Mar";
						break;
					case "04":
						month = "Apr";
						break;
					case "05":
						month = "May";
						break;
					case "06":
						month = "Jun";
						break;
					case "07":
						month = "Jul";
						break;
					case "08":
						month = "Aug";
						break;
					case "09":
						month = "Sep";
						break;
					case "10":
						month = "Oct";
						break;
					case "11":
						month = "Nov";
						break;
					case "12":
						month = "Dec";
						break;
					}
					json.put("Client_name", client_name);
					json.put("Year", Year);
					json.put("Month", month.toUpperCase());
					json.put("category", typejob.replaceAll("_", " "));
					json.put("DeliveredJobs", Collections.frequency(listOfJobs, ListOfMonths[i]));
					jarray.put(json);
				}
			}else if(!Month.equalsIgnoreCase("ALL")) {
				//System.out.println("am else part");
				while(rs.next()) {
					count=count+rs.getString(2).replace(" ", "_")+" ";
					//					System.out.println(rs.getString(1)+"----"+rs.getString(2));
				}
				String[] jobdata = new String[JobType.length()];
				for(int i=0;i<JobType.length();i++) {
					jobdata[i]=JobType.get(i).toString().toUpperCase().replace(" ", "_");
				}
				final String[] ListOfJobs = jobdata;
				List<String> listOfLocations = Arrays.asList(count.split(" "));
				List<String> listOfJobs = Arrays.asList(ListOfJobs);
				Set<String> uniqLocation = new HashSet<String>(listOfJobs);
				for (String location : ListOfJobs) {

					JSONObject json=new JSONObject();

					json.put("Year", Year);
					json.put("Month", Month);
					json.put("Client_name", client_name);
					json.put("category", location.replace("_", " "));
					json.put("DeliveredJobs", Collections.frequency(listOfLocations, location));
					jarray.put(json);
				}
			}
			//System.out.println(jarray);
		}catch(Exception ex){
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(jarray.toString()).build();
	}

	@POST
	@Path("/JobBrand")	
	/**
	 * Populating data based in brands accepting year,month,client-name and brand names as inputs
	 * @param jobBrands
	 * @return
	 * @throws Exception
	 */
	public Response JobBrand(String jobBrands) throws Exception{


		JSONObject jobj=new JSONObject(jobBrands);

		String Year=jobj.getString("year");
		String Month=jobj.getString("month").toUpperCase();
		String client_name=jobj.getString("client_name");
		JSONArray JobType = (JSONArray)jobj.get("Brands");

		String data=null;

		String yearStart=null;
		String yearEnd=null;

		JSONArray jarray = new JSONArray();

		switch(JobType.length()) {
		case 1:
			data="brand = '"+JobType.get(0)+"' ";
			break;
		case 2: 
			data="brand = '"+JobType.get(0)+"' OR brand = '"+JobType.get(1)+"' ";
			break;
		case 3:
			data="brand = '"+JobType.get(0)+"' OR brand = '"+JobType.get(1)+"' OR brand = '"+JobType.get(2)+"' ";
			break;
		case 4:
			data="brand = '"+JobType.get(0)+"' OR brand = '"+JobType.get(1)+"' OR brand = '"+JobType.get(2)+"' OR brand = '"+JobType.get(3)+"' ";
			break;
		case 5:
			data="brand = '"+JobType.get(0)+"' OR brand = '"+JobType.get(1)+"' OR brand = '"+JobType.get(2)+"' OR brand = '"+JobType.get(3)+"' OR brand = '"+JobType.get(4)+"' ";
			break;
		}

		switch(Month) {

		case "JAN":
			yearStart=Year+"-01-01";
			yearEnd=Year+"-01-31";
			break;

		case "FEB":
			yearStart=Year+"-02-01";
			yearEnd=Year+"-02-29";
			break;

		case "MAR":
			yearStart=Year+"-03-01";
			yearEnd=Year+"-03-31";
			break;

		case "APR":
			yearStart=Year+"-04-01";
			yearEnd=Year+"-04-30";
			break;

		case "MAY":
			yearStart=Year+"-05-01";
			yearEnd=Year+"-05-31";
			break;

		case "JUN":
			yearStart=Year+"-06-01";
			yearEnd=Year+"-06-30";
			break;

		case "JUL":
			yearStart=Year+"-07-01";
			yearEnd=Year+"-07-31";
			break;

		case "AUG":
			yearStart=Year+"-08-01";
			yearEnd=Year+"-08-31";
			break;

		case "SEP":
			yearStart=Year+"-09-01";
			yearEnd=Year+"-09-30";
			break;

		case "OCT":
			yearStart=Year+"-10-01";
			yearEnd=Year+"-10-31";
			break;

		case "NOV":
			yearStart=Year+"-11-01";
			yearEnd=Year+"-11-30";
			break;

		case "DEC":
			yearStart=Year+"-12-01";
			yearEnd=Year+"-12-31";
			break;

		case "ALL":
			yearStart=Year+"-01-01";
			yearEnd=Year+"-12-31";
			break;
		}

		//		String Query= "select COUNT(task_id1),category,brief_date FROM packagingtask_part1 WHERE Job_status= 'Approved' AND client_name = ? "
		//				+ "AND brief_date BETWEEN ? AND ? "
		//				+ "AND ("+data+") GROUP BY category, EXTRACT(MONTH FROM brief_date)ORDER BY brief_date";
		String Query = "select brief_date,brand FROM packagingtask_part1 WHERE Job_status= 'Approved' AND client_name = ? "
				+ "AND brief_date BETWEEN ? AND ? "
				+ "AND ("+data+") ";
		//System.out.println(Query);

		try {
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);

			PreparedStatement ps=con.prepareStatement(Query);
			ps.setString(1, client_name);
			ps.setString(2, yearStart);
			ps.setString(3, yearEnd);
			ResultSet rs=ps.executeQuery();

			String count="";

			if(Month.equalsIgnoreCase("ALL")){
				while(rs.next()) {
					//					System.out.println(rs.getString(1)+"&"+rs.getString(2));
					//					System.out.println(count); 
					count=count+rs.getString(1).substring(0,rs.getString(1).length() - 3)+"&"+rs.getString(2).replace(" ", "_").toUpperCase()+" ";
				}
				//System.out.println(count); 
				int size=JobType.length();
				String jobdata[] = new String[size*12];
				for(int i=0;i<size;i++) {
					//	System.out.println("am in "+i);
					jobdata[i]=Year+"-01&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(1*size)]=Year+"-02&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(2*size)]=Year+"-03&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(3*size)]=Year+"-04&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(4*size)]=Year+"-05&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(5*size)]=Year+"-06&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(6*size)]=Year+"-07&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(7*size)]=Year+"-08&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(8*size)]=Year+"-09&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(9*size)]=Year+"-10&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(10*size)]=Year+"-11&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
					jobdata[i+(11*size)]=Year+"-12&"+JobType.get(i).toString().toUpperCase().replace(" ", "_");
				}
				final String[] ListOfMonths=jobdata;
				List<String> listOfJobs = Arrays.asList(count.split(" "));
				List<String> ListOfJobs = Arrays.asList(ListOfMonths);
				Set<String> uniqLocation = new HashSet<String>(ListOfJobs);
				for(int i=0;i<uniqLocation.size();i++) {
					JSONObject json=new JSONObject();

					String unique[]=ListOfMonths[i].split("-");
					String date[]=unique[1].split("&");
					String month=date[0];
					String typejob=date[1];
					switch(month) {

					case "01":
						month = "JAN";
						break;
					case "02":
						month = "FEB";
						break;
					case "03":
						month = "MAR";
						break;
					case "04":
						month = "APR";
						break;
					case "05":
						month = "MAY";
						break;
					case "06":
						month = "JUN";
						break;
					case "07":
						month = "JUL";
						break;
					case "08":
						month = "AUG";
						break;
					case "09":
						month = "SEP";
						break;
					case "10":
						month = "OCT";
						break;
					case "11":
						month = "NOV";
						break;
					case "12":
						month = "DEC";
						break;
					}
					json.put("Client_name", client_name);
					json.put("Year", Year);
					json.put("Month", month.toUpperCase());
					json.put("Brand", typejob.replaceAll("_", " "));
					json.put("DeliveredJobs", Collections.frequency(listOfJobs, ListOfMonths[i]));
					jarray.put(json);
				}
			}else if(!Month.equalsIgnoreCase("ALL")) {
				System.out.println("am else part");
				while(rs.next()) {
					count=count+rs.getString(2).toUpperCase().replace(" ", "_")+" ";
					//										System.out.println(rs.getString(1)+"----"+rs.getString(2));
				}
				String[] jobdata = new String[JobType.length()];
				for(int i=0;i<JobType.length();i++) {
					jobdata[i]=JobType.get(i).toString().toUpperCase().replace(" ", "_");
				}
				final String[] ListOfJobs = jobdata;
				List<String> listOfLocations = Arrays.asList(count.split(" "));
				List<String> listOfJobs = Arrays.asList(ListOfJobs);
				Set<String> uniqLocation = new HashSet<String>(listOfJobs);
				for (String location : ListOfJobs) {

					JSONObject json=new JSONObject();

					json.put("Year", Year);
					json.put("Month", Month.toUpperCase());
					json.put("Client_name", client_name);
					json.put("Brand", location.replace("_", " "));
					json.put("DeliveredJobs", Collections.frequency(listOfLocations, location));
					jarray.put(json);
				}
			}
			System.out.println(jarray);
		}catch(Exception ex){
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(jarray.toString()).build();



	}

	@POST
	@Path("/PdJobs")	
	/**
	 * Populating data of product designer jobs has been done by him,
	 * By accepting client-name,year and designer name.
	 *  
	 * @param pd_data
	 * @return
	 * @throws Exception
	 */
	public Response Pdjobs(String pd_data) throws Exception{
		System.out.println(pd_data);
		JSONObject jobj=new JSONObject(pd_data);

		String Year=jobj.getString("year");
		String client_name=jobj.getString("client_name");
		String Pd = jobj.getString("PdPerson");

		String yearStart=Year+"-01-01";
		String yearEnd=Year+"-12-31";

		JSONArray jarray= new JSONArray();

		final String[] ListOfMonths = {Year+"-01", Year+"-02", Year+"-03",Year+"-04",Year+"-05",Year+"-06",
				Year+"-07",Year+"-08",Year+"-09",Year+"-10",Year+"-11",Year+"-12"};
		try {

			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			List<Integer> AlltaskIds = new ArrayList<Integer>();

			String Query="SELECT task_id1 FROM packagingtask_part1 "
					+ "WHERE Job_status = 'Approved' AND client_name = ?"
					+ " AND pa = ? AND brief_date BETWEEN ? AND ?";
			PreparedStatement ps = con.prepareStatement(Query);
			ps.setString(1,client_name);
			ps.setString(2,Pd);
			ps.setString(3, yearStart);
			ps.setString(4, yearEnd);
			ResultSet rs=ps.executeQuery();

			while(rs.next()) {

				AlltaskIds.add(rs.getInt(1));
				System.out.println(rs.getInt(1));
			}
			Set<Integer> uniquetask = new HashSet<Integer>(AlltaskIds);
			int count =0;

			for(int i=0;i<ListOfMonths.length;i++) {

				JSONObject json= new JSONObject();
				JSONArray Suc= new JSONArray();
				JSONArray Intrnlerr= new JSONArray();
				JSONArray Extrnlerr= new JSONArray();
				String dates[]= ListOfMonths[i].split("-");
				String month =dates[1];

				switch(month) {

				case "01":
					month = "Jan";
					break;
				case "02":
					month = "Feb";
					break;
				case "03":
					month = "Mar";
					break;
				case "04":
					month = "Apr";
					break;
				case "05":
					month = "May";
					break;
				case "06":
					month = "Jun";
					break;
				case "07":
					month = "Jul";
					break;
				case "08":
					month = "Aug";
					break;
				case "09":
					month = "Sep";
					break;
				case "10":
					month = "Oct";
					break;
				case "11":
					month = "Nov";
					break;
				case "12":
					month = "Dec";
					break;
				}
				for(int task:uniquetask) {

					String Month="";
					count = Collections.frequency(AlltaskIds, task);

					String Brief_Query ="SELECT brief_date FROM packagingtask_part1 WHERE task_id1 =?";

					ps=con.prepareStatement(Brief_Query);  
					ps.setInt(1,task); 

					ResultSet date_rs= ps.executeQuery();

					while(date_rs.next()) {

						Month = Month+date_rs.getString(1).substring(0,date_rs.getString(1).length() - 3);
					}

					//					System.out.println(Month);

					if(Month.equals(ListOfMonths[i])) {

						String date[]=Month.split("-");

						switch(date[1]) {
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "08":
						{
							Month = "AUG";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;
						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "01":{

							Month = "JAN";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								//
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "02":{

							Month = "FEB";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "03":{

							Month = "MAR";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "04":{

							Month = "APR";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "05":{

							Month = "MAY";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "06":{

							Month = "JUN";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "07":{

							Month = "JUL";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "09":{

							Month = "SEP";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "10":{

							Month = "OCT";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "11":{

							Month = "NOV";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						case "12":{

							Month = "DEC";
							Query="SELECT task_id FROM workflowdetail WHERE (assigned_by_id = assigned_to_id) AND status ='QC-Corr-WIP' and task_id = ?";
							PreparedStatement test= con.prepareStatement(Query);
							test.setInt(1, task);
							ResultSet rs1=test.executeQuery();
							while(rs1.next()) {
								count=rs1.getInt(1);
							}

							if(task==count) {
								JSONObject error= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								error.put("Tr_Number",details[0]);
								error.put("JobType",details[1].replaceAll("_", " "));
								error.put("Brand",details[2].replaceAll("_", " "));
								error.put("Category",details[3].replaceAll("_", " "));
								error.put("ProductDesigner",details[4]);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
								Intrnlerr.put(error);
							}else {
								JSONObject Success= new JSONObject();
								JSONObject ExternalError= new JSONObject();

								String[] details=Details(task).split(" ");
								String[] ExError=ExternalErrorDetails(task).split(" ");

								Success.put("Tr_Number",details[0]);
								Success.put("JobType",details[1].replaceAll("_", " "));
								Success.put("Brand",details[2].replaceAll("_", " "));
								Success.put("Category",details[3].replaceAll("_", " "));
								Success.put("ProductDesigner",details[4]);
								Suc.put(Success);
								if(ExError.length>1) {
									ExternalError.put("Tr_Number",ExError[0]);
									ExternalError.put("JobType",ExError[1].replaceAll("_", " "));
									ExternalError.put("Brand",ExError[2].replaceAll("_", " "));
									ExternalError.put("Category",ExError[3].replaceAll("_", " "));
									ExternalError.put("ProductDesigner",ExError[4]);
									Extrnlerr.put(ExternalError);
								}
							}

							break;

						}
						/**
						 * Here Qc-corr-wip means this job has an internal error.
						 * Finding this error WIP and identifying this person has an 
						 * internal error.
						 * else part Finding that this jobs have no error.
						 */
						}

					}else
						continue;


				}
				json.put("year", Year);
				json.put("Month",month);
				//				json.put("DeliveredJobs", Suc.length()+Intrnlerr.length());
				json.put("success",Suc);
				json.put("InternalError", Intrnlerr);
				json.put("ExternalError",Extrnlerr);
				jarray.put(json);
			}

			//			System.out.println(jarray);

		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}

		return Response.ok(jarray.toString()).build();
	}

	@POST
	@Path("/QcJobs")
	/**
	 * Populating data QC person jobs completed data..
	 * Accepting input client name,year and Qc person name.
	 *  
	 * @param qc_data
	 * @return
	 * @throws Exception
	 */
	public Response Qcjobs(String qc_data) throws Exception{	

		JSONObject jobj=new JSONObject(qc_data);
		String Year=jobj.getString("year");
		String client_name=jobj.getString("client_name");
		String Qc = jobj.getString("QcPerson");
		String yearStart=Year+"-01-01";
		String yearEnd=Year+"-12-31";
		JSONArray jarray= new JSONArray();
		final String[] ListOfMonths = {Year+"-01", Year+"-02", Year+"-03",Year+"-04",Year+"-05",Year+"-06",
				Year+"-07",Year+"-08",Year+"-09",Year+"-10",Year+"-11",Year+"-12"};

		List<Integer> task_id = new ArrayList<Integer>();

		try {
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String Query="SELECT task_id1 FROM packagingtask_part1 "
					+ "WHERE Job_status = 'Approved' AND client_name = ?"
					+ " AND qc = ? AND brief_date BETWEEN ? AND ?";
			PreparedStatement ps = con.prepareStatement(Query);
			ps.setString(1,client_name);
			ps.setString(2,Qc);
			ps.setString(3, yearStart);
			ps.setString(4, yearEnd);

			ResultSet rs=ps.executeQuery();
			while(rs.next()) {
				task_id.add(rs.getInt(1));
			}
			Set<Integer> uniquetask = new HashSet<Integer>(task_id);

			for(int i=0;i<ListOfMonths.length;i++) {


				JSONObject json= new JSONObject();
				JSONArray Suc= new JSONArray();
				JSONArray Error= new JSONArray();

				String dates[]= ListOfMonths[i].split("-");
				String month =dates[1];

				switch(month) {

				case "01":
					month = "Jan";
					break;
				case "02":
					month = "Feb";
					break;
				case "03":
					month = "Mar";
					break;
				case "04":
					month = "Apr";
					break;
				case "05":
					month = "May";
					break;
				case "06":
					month = "Jun";
					break;
				case "07":
					month = "Jul";
					break;
				case "08":
					month = "Aug";
					break;
				case "09":
					month = "Sep";
					break;
				case "10":
					month = "Oct";
					break;
				case "11":
					month = "Nov";
					break;
				case "12":
					month = "Dec";
					break;
				}
				for(int task:uniquetask) {

					String Month="";

					String Brief_Query ="SELECT brief_date FROM packagingtask_part1 WHERE task_id1 =?";

					PreparedStatement ps1=con.prepareStatement(Brief_Query);  
					ps1.setInt(1,task); 

					ResultSet date_rs= ps1.executeQuery();

					while(date_rs.next()) {

						Month = Month+date_rs.getString(1).substring(0,date_rs.getString(1).length() - 3);
					}
					if(Month.equals(ListOfMonths[i])) {

						String date[]=Month.split("-");
						switch(date[1]){

						case "01":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}


						}
						break;

						case "02":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}


						}
						break;
						case "03":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}


						}
						break;
						case "04":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "05":
						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "06":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "07":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "08":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "09":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "10":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "11":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;
						case "12":

						{
							JSONObject Success= new JSONObject();
							JSONObject ExternalError= new JSONObject();

							String[] details=Details(task).split(" ");
							String[] Errors=ExternalErrorDetails(task).split(" ");

							//							Success.put("TaskId", task);
							Success.put("Tr_Number",details[0]);
							Success.put("JobType",details[1].replaceAll("_", " "));
							Success.put("Brand",details[2].replaceAll("_", " "));
							Success.put("Category",details[3].replaceAll("_", " "));
							Success.put("ProductDesigner",details[4]);
							Suc.put(Success);

							if(Errors.length>1) {
								ExternalError.put("Tr_Number",Errors[0]);
								ExternalError.put("JobType",Errors[1].replaceAll("_", " "));
								ExternalError.put("Brand",Errors[2].replaceAll("_", " "));
								ExternalError.put("Category",Errors[3].replaceAll("_", " "));
								ExternalError.put("ProductDesigner",Errors[4]);
								Error.put(ExternalError);
							}
						}
						break;

						}
					}else
						continue;
				}
				json.put("year", Year);
				json.put("Month",month);
				json.put("Client", client_name);
				json.put("QcChecked",Suc);
				json.put("ExternalErrors", Error);
				jarray.put(json);
			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}

		return Response.ok(jarray.toString()).build();
	}


	/**
	 * Getting External data using taskid as Input,.
	 * @param task
	 * @return
	 * @throws SQLException
	 */
	private String ExternalErrorDetails(int task) throws SQLException{

		String ErrorDetails="";
		try {
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String Query="SELECT tr_number,job_type,brand,category,pa FROM packagingtask_part1 WHERE task_id1 = (SELECT task_id1 FROM external_error WHERE task_id1 = ?)";
			PreparedStatement stmt=con.prepareStatement(Query);  
			stmt.setInt(1,task); 
			ResultSet rs=stmt.executeQuery();
			while(rs.next()) {
				//System.out.println(rs.getString(1));
				ErrorDetails=rs.getString(1).replaceAll(" ", "_")+" "+rs.getString(2).replaceAll(" ", "_")+" "+rs.getString(3).replaceAll(" ", "_")+" "+rs.getString(4).replaceAll(" ", "_")+" "+rs.getString(5)+" ";
			}
		}
		catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}
		//System.out.println(ErrorDetails);
		return ErrorDetails;
	}

	public String Details(int task) throws SQLException {
		String details="";
		try {
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String Query="SELECT tr_number,job_type,brand,category,pa FROM packagingtask_part1 WHERE task_id1 = ?";
			PreparedStatement stmt=con.prepareStatement(Query);  
			stmt.setInt(1,task); 
			ResultSet rs=stmt.executeQuery();
			while(rs.next()) {
				details=rs.getString(1).replaceAll(" ", "_")+" "+rs.getString(2).replaceAll(" ", "_")+" "+rs.getString(3).replaceAll(" ", "_")+" "+rs.getString(4).replaceAll(" ", "_")+" "+rs.getString(5)+" ";
			}
		}catch(Exception ex) 
		{
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}
		System.out.println(details+"--->"+task);
		return details;

	}

	/**
	 * Reading PMS config file from :
	 * src/main/resource/config folder 
	 */
	private ReportsEndPoint () {

		try {

			String configDir = System.getProperty ("configDir", "config");
			String filePath = configDir + "/" + PROP_FILENAME;
			System.out.println ("PMS Configuration file: " + filePath);

			config = new PropertiesConfiguration(filePath);
			FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
			strategy.setRefreshDelay(30);
			config.setReloadingStrategy(strategy);

		} catch (Exception e) {
			log.error("Exception :" +e.getMessage());
			System.err.println(e);
		}			
	}
	public String getString (String key) {

		return config.getString(key);
	}

	/***
	 * fetching data from Json file of job type.
	 * @return
	 * @throws Exception
	 */
	public Object[] getData() throws Exception{
		String jobs="";
		jobs = mj.getJsonFile().getEntity().toString();
		JSONObject jobjects=new JSONObject(jobs);
		JSONArray data = jobjects.getJSONArray("job_type");
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < data.length(); i++){
			list.add(data.getJSONObject(i).getString("key"));
		}
		Object[] objects = list.toArray();
		return objects;
	}
	@GET
	@Path("/brandList")
	/**
	 * Fetching Unique brands from the DB.
	 * @return
	 */
	public Response Brands() {

		String Query="SELECT DISTINCT brand from packagingtask_part1";
		JSONArray Brand= new JSONArray();

		try {
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);

			PreparedStatement ps=con.prepareStatement(Query);
			ResultSet rs=ps.executeQuery();
			while(rs.next()) {
				int total_brands = rs.getMetaData().getColumnCount();
				for (int i = 0; i < total_brands; i++) {
					JSONObject brand = new JSONObject();
					brand.put("Brand",rs.getString(1));
					Brand.put(brand);
				}
			}

		}catch(Exception ex){
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(Brand.toString()).build();
	}
	@GET
	@Path("/categoryList")
	/**
	 * Fetching unique category from DB.
	 * @return
	 */
	public Response Categories() {

		String Query="SELECT DISTINCT category from packagingtask_part1";
		JSONArray Category= new JSONArray();

		try {
			//			System.out.println("hello");
			Class.forName(Driver_Manager);  
			Connection con = DriverManager.getConnection(ConnURL,DBuser,DBpass);

			PreparedStatement ps=con.prepareStatement(Query);
			ResultSet rs=ps.executeQuery();
			while(rs.next()) {
				int total_category = rs.getMetaData().getColumnCount();
				for (int i = 0; i < total_category; i++) {
					JSONObject category = new JSONObject();
					category.put("category",rs.getString(1));
					Category.put(category);
				}
			}

		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//ex.printStackTrace();
		}
		log.debug(Category.toString());
		//		System.out.println(Category.toString());
		return Response.ok(Category.toString()).build();
	}

	@GET
	@Path("/JobTypeList")
	/**
	 * Fetching Job type from database 
	 * @return
	 * @throws Exception
	 */
	public Response jobTypeList() throws Exception {
		String JobType="";
		JSONArray JobtypeList=new JSONArray();

		try {
			JobType=mj.getJsonFile().getEntity().toString();
			JSONObject jsonObj= new JSONObject(JobType);
			JSONArray ja_data = jsonObj.getJSONArray("JobType");
			for(int i=0;i<ja_data.length();i++) {
				JSONObject list = new JSONObject();
				list.put("JobType",ja_data.getJSONObject(i).getString("key"));
				JobtypeList.put(list);
			}
		}catch(Exception ex) {
			log.error("Exception :" +ex.getMessage());
			//ex.printStackTrace();
		}
		//		System.out.println("JobtypeList"+JobtypeList.toString());
		return Response.ok(JobtypeList.toString()).build();
	}
	public Object[] CategoryList() throws JSONException {

		String categorylist = Categories().getEntity().toString();
		System.out.println(categorylist);
		JSONArray arr = new JSONArray(categorylist);
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < arr.length(); i++){
			list.add(arr.getJSONObject(i).getString("category"));
		}
		Object[] objects = list.toArray();
		//		for(int i=0;i<objects.length;i++) {
		//			System.out.println(objects[i]);
		//		}
		return objects;
	}
}
