package com.stirred.packaging.Endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stirred.packaging.Authentication.TokenGenerator;
import com.stirred.packaging.common.Utilities;
import com.stirred.packaging.main.PMSApplication;

@Component
@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserEndPoint{
	final String driver = "com.mysql.jdbc.Driver";
	private static Logger 	log = LogManager.getLogger(UserEndPoint.class.getName());

	@Autowired
	Utilities utilities;

	@Autowired
	TokenGenerator tg;

	GlobalVariables gv=new GlobalVariables();

	final String DBuser=gv.getString("db.user");
	final String DBport=gv.getString("db.port");
	final String DBpass=gv.getString("db.password");
	final String DBhost=gv.getString("db.hostname");
	final String DBname=gv.getString("db.database");

	final String ConnURL="jdbc:mysql://"+DBhost+":"+DBport+"/"+DBname;

	/*
	 * method:insertDataIntoTable method will add the user details in User Table.
	 * Return:It return may be success or failed message after excuting the query.
	 * */
	@POST
	@Path("/addUser")
	/**
	 * Adding user to the Application (New Employee..)
	 * @param addUser
	 * @return
	 */
	public Response insertDataIntoTable(String addUser)
	{
		JSONObject addUserObj;
		try{
			addUserObj = new JSONObject(addUser);
			int empCode = Integer.parseInt(addUserObj.getString("empCode"));
			JSONObject compareObj = comparefield(addUserObj);
			if(compareObj.length()!=0)
			{
				if(compareObj.getString("email").equals(addUserObj.getString("email")) &&compareObj.getInt("empCode") == empCode)
				{
					return Response.ok(" Email Id and Employee code are already exist").build();
				}
				else if(compareObj.getString("email").equals(addUserObj.getString("email")))
				{
					return Response.ok("Email Id already Exist").build();   
				}else if(compareObj.getInt("empCode") == empCode)
				{
					return Response.ok("Employee code already Exist").build();   
				}
			}else
			{
				String sql=InsertQueryInfo(addUserObj);
				Class.forName(driver);
				Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
				PreparedStatement ps= conn.prepareStatement(sql);
				int count = ps.executeUpdate();
				if((count==0))
				{
					log.error("Failed to add data into UserTable");
					return Response.ok("Error").build();

				}

			}
		} catch (Exception e) {
			log.error("exception" +e.getMessage());
		}
		return Response.ok("").build();
	}

	/*
	 * Method : updateUserInTable method will update the user Information in User table
	 * Return : It return may be success or failed message after executing the query.
	 * */
	@POST
	@Path("/updateUser")
	public Response updateUserInTable(String updateUser) throws JSONException, ClassNotFoundException, SQLException
	{
		JSONObject updateUserObj = new JSONObject(updateUser);
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql =updateQueryInTable(updateUserObj);
			PreparedStatement ps= conn.prepareStatement(sql);
			int count = ps.executeUpdate();
			if(count==0){
				log.error("Failed to update data into userTable");
				return Response.ok().build();
			}
			conn.close();
		}catch(Exception ex) {
			//ex.printStackTrace();
			log.error("Exception:" +ex.getMessage());
		}

		return Response.ok("updated").build();
	}

	/*
	 * Method:readAll method returns all user information persent in User Table.
	 * */
	@GET
	@Path("/readAll")
	public Response readAll() throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray array = readAllUserQuery();
		return Response.ok(array.toString()).build();

	}
	/*
	 * Method : readUser method pass the userId as parameter in a Integer Format.
	 * Returns : returns user information based on the selective user id.
	 * */
	@GET
	@Path("/readUniqueField")
	public Response uniqueField() throws SQLException, JSONException, ClassNotFoundException
	{

		JSONArray userTypeOfArray = new JSONArray();
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			String sql = "select * from user";
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				JSONObject obj = new JSONObject();
				int empCode = rs.getInt("empCode");
				String email = rs.getString("email");
				obj.put("empCode", empCode);
				obj.put("email", email);
				userTypeOfArray.put(obj);
			}
			conn.close();
		}catch(Exception ex) {
			//ex.printStackTrace();
			log.error("Exception:"+ex.getMessage());
		}


		return Response.ok(userTypeOfArray.toString()).build();
	}
	@GET
	@Path("/read")
	/**
	 * Reading user based user ID
	 * @param userId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public Response readUser(@HeaderParam("userId") int userId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONObject obj = new JSONObject();
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from user where user_id= "+userId;

			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			while (rs.next())
			{

				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					obj.put(rsmd.getColumnName(i),columnValue);

				}
			}
			conn.close();
		}catch(Exception ex){
			//ex.printStackTrace();
			log.error("Exception :"+ex.getMessage());
		}
		return Response.ok(obj.toString()).build();

	}
	
	@GET
	@Path("/readTypeOfUser")
	/**
	 * Method : readTypeOfUser method is pass the parameter as userType and dataType is String
	 * Return : this method returns users details depends on the selective user type.
	 *
    */
	public Response readTypeOfUser(@HeaderParam("userType") String userType) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray userArray = new JSONArray();
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from user where userType = '"+userType+"' or userType = 'TeamLead'";
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (rs.next())
			{
				JSONObject obj = new JSONObject();
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					obj.put(rsmd.getColumnName(i),columnValue);

				}            		
				userArray.put(obj);
			}
			conn.close();			
		}catch(Exception ex) {
			//ex.printStackTrace();
			log.error("Exception:" +ex.getMessage());

		}
		return Response.ok(userArray.toString()).build();
	}
	/*
	 * Method : loginUser method will get required login details
	 * Return :auth key to pprocess further
	 * */

	@POST
	@Path("/login")
	public Response loginUser(String loginData) throws SQLException, JSONException {
		//System.out.println("loginData"+loginData);
		JSONObject loginObj = new JSONObject(loginData);
		JSONObject UserObj = new JSONObject();
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String loginPassword = utilities.getSecurePassword(loginObj.getString("password"));
			String sql = " select * from user "+
					"where email ="+"'"+loginObj.getString("username")+"'";
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			if(rs.next() && !rs.wasNull())
			{
				for(int i=1;i<=columnCount;i++){
					String columnValue = rs.getString(i);
					String columnName = rsmd.getColumnName(i);
					UserObj.put(columnName, columnValue);}
				if(!UserObj.getString("password").equals(loginPassword)||!(UserObj.getString("status").equals("Active")))
				{
					log.error("Password not exist!");
					return Response.ok("Invalid Password").build();
				}
			}else{
				log.error("User Not exist in a table");

				return Response.ok("User Not Exists").build();
			}
			String token = tg.generateToken("").getToken(); 

			UserObj.put("token", token);
			conn.close();
		}catch(Exception ex) {
			//ex.printStackTrace();
			log.error("Exception:" +ex.getMessage());
		}
		return Response.ok(UserObj.toString()).build();
	}

	@GET
	@Path("/viewProfile")
	/**
	 * viewing the profile of user based on used ID.
	 * Accepting input as userId.
	 * @param userId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public Response viewProfileUser(@HeaderParam("userId") int userId) throws ClassNotFoundException, SQLException, JSONException
	{
		JSONObject viewUser = new JSONObject();
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			String sql = "select * from user where user_id = "+userId;
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			if(rs.next()||!rs.wasNull()){
				for(int i=1;i<=count;i++){
					String userValue = rs.getString(i);
					String Name = rsmd.getColumnName(i);
					viewUser.put(Name, userValue);
				}
			}
			conn.close();
		}catch(Exception ex) {
			//ex.printStackTrace();
			log.error("Exception:"+ex.getMessage());
		}
		return Response.ok(viewUser.toString()).build();
	}

	@GET
	@Path("/password")
	public String getEncryptedPassword() {
		String password = "Stirred@123";
		return utilities.getSecurePassword(password);
	}
	@GET
	@Path("/convertPassword")
	public Response convertPassword(@HeaderParam("Password") String Password){
		String encryptedPassword = utilities.getSecurePassword(Password);
		return Response.ok(encryptedPassword).build();
	}
	@GET
	@Path("/changePassword")
	public Response updateUser(@HeaderParam("Password") String Password,@HeaderParam("userId")int userId) throws Exception
	{

		String userNewPassword = utilities.getSecurePassword(Password);
		String sql = "UPDATE user SET password ="+"'"+userNewPassword+"'"+" where user_id="+userId;
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			PreparedStatement ps= conn.prepareStatement(sql);
			int updateCount = ps.executeUpdate();
			if((updateCount==0)){
				log.error("Failed to update password in existing User");
				return Response.ok("Failed to Update").build();
			}
		}catch(Exception ex) {
			log.error("Exception:"+ex.getMessage());
			//ex.printStackTrace();
		}

		return Response.ok("Updated Successfully").build();
	}
	@GET
	@Path("/readAllUserName")
	public Response readAllUsers() throws ClassNotFoundException, SQLException, JSONException {

		JSONArray array = readAllUserQuery();
		JSONArray user = new JSONArray();
		for(int i=0;i<array.length();i++){
			JSONObject obj = array.getJSONObject(i);
			JSONObject userObj = new JSONObject();
			userObj.put("userName", obj.getString("firstName"));
			userObj.put("userId", obj.getString("user_id"));
			user.put(userObj);

		}
		return Response.ok(user.toString()).build();
	}

	@GET
	@Path("/enable")
	/**
	 * Enabling user,to perform activities/jobs
	 * @param userId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public Response enableUser(@HeaderParam("userId") int userId) throws ClassNotFoundException, SQLException, JSONException {
		String sql = "update user set status ='Active' ,userStatus = 'Open' where user_id="+userId;
		JSONObject enableInfo =getUserEnableInfo(userId);
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			PreparedStatement ps= conn.prepareStatement(sql);
			int updateCount = ps.executeUpdate();
			if(!(updateCount>0)){
				log.error("Failed to Enable the User Account");
				return Response.ok("Failed to Update").build();
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception:"+ex.getMessage());
			//ex.printStackTrace();
		}
		return Response.ok(enableInfo.toString()).build();	
	}

	@GET
	@Path("/disable")	
	/**
	 * Disabling user,Not to perform any work/jobs
	 * @param userId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public Response disableUser(@HeaderParam("userId") int userId) throws ClassNotFoundException, SQLException, JSONException {
		String sql = "update user set status ='Inactive', userStatus = 'Close' where user_id= "+userId;
		JSONObject disableInfo =getUserEnableInfo(userId);
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			PreparedStatement ps= conn.prepareStatement(sql);
			int updateCount = ps.executeUpdate();
			if(!(updateCount>0)){
				log.error("Failed to disable User Account");
				return Response.ok("Failed to Update").build();
			}
		}catch(Exception ex) {
			log.error("Exception :"+ex.getMessage());
			//	ex.printStackTrace();
		}

		return Response.ok(disableInfo.toString()).build();		
	}

	/*
	 * method:getQueryInfo() this method insert all data into user table
	 * 
	 */

	public String InsertQueryInfo(JSONObject insertData) throws Exception
	{
		int empCode = insertData.getInt("empCode");
		String firstName = insertData.getString("firstName");
		String lastName = insertData.getString("lastName");
		String email = insertData.getString("email");
		long mobileNumber = insertData.getLong("mobileNumber");
		String password = encryptedPassword();
		String userType= insertData.getString("userType");
		String status = "Active";
		String userStatus = "Open";
		String sql = "insert into user (empCode,firstName,lastName,email,mobileNumber,password,userType,status,userStatus) "
				+ "values("+empCode+",'"+firstName+"','"+lastName+"','"+email+"',"+mobileNumber+",'"+password+"','"+userType+"','"+status+"','"+userStatus+"')";

		return sql;
	}
	public JSONArray readAllUserQuery() throws ClassNotFoundException, SQLException, JSONException
	{
		JSONArray userTypeOfArray = new JSONArray();
		String sql = "select * from user";
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (rs.next())
			{
				JSONObject obj = new JSONObject();
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					obj.put(rsmd.getColumnName(i),columnValue);

				}
				userTypeOfArray.put(obj);
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception:"+ex.getMessage());
			//ex.printStackTrace();
		}
		return userTypeOfArray;

	}

	public String updateQueryInTable(JSONObject jsonObj) throws JSONException
	{
		StringBuilder dataKeys = new StringBuilder();
		StringBuilder dataValues = new StringBuilder();
		String userId = jsonObj.getString("user_id");
		StringBuilder data = new StringBuilder();
		Iterator<String> itr = jsonObj.keys();
		String sqlQuery = null;
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
		sqlQuery = "UPDATE user SET "+newData +" where user_id = "+userId;

		return sqlQuery;
	}
	public JSONObject getUserEnableInfo(int userId) throws SQLException, JSONException, ClassNotFoundException
	{
		String sqlCall = "select * from user where user_id ="+userId;
		JSONObject enableInfo = new JSONObject();		
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt =  conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCall);
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			if(rs.next()||!rs.wasNull()){
				for(int i=1;i<=count;i++){
					String userValue = rs.getString(i);
					String Name = rsmd.getColumnName(i);
					enableInfo.put(Name, userValue);
				}
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception:"+ex.getMessage());
			//ex.printStackTrace();
		}
		return enableInfo;

	}
	public JSONObject comparefield(JSONObject addJsonObj) throws JSONException, SQLException, ClassNotFoundException
	{
		JSONObject obj = new JSONObject();
		String compareField ="select empCode,email from user where email ='"+addJsonObj.getString("email")+"'"
				+ " or empCode ="+addJsonObj.getInt("empCode");
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(ConnURL,DBuser,DBpass);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(compareField);
			while(rs.next())
			{
				String email = null;
				int empCode =0;
				if(rs.getString("email") != null)
				{
					email = rs.getString("email");
				}
				if(rs.getInt("empCode")!=0)
				{
					empCode = rs.getInt("empCode");
				}
				obj.put("email", email);
				obj.put("empCode", empCode);
			}
			conn.close();
		}catch(Exception ex) {
			log.error("Exception:"+ex.getMessage());
			//ex.printStackTrace();
		}

		return obj;

	}
	public String encryptedPassword() {
		String password = "Stirred@123";
		return utilities.getSecurePassword(password);
	}
}
