//package com.stirred.packaging.securityConfig;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.stirred.packaging.Authentication.UserBean;
//import com.stirred.packaging.DAO.UserEntity;
//import com.stirred.packaging.DAO.UserRepository;
//import com.stirred.packaging.common.Utilities;
//
//
///**
// * <h1>User Service!</h1>
// * This Program contains services for user.
// * 
// */
//@Service
//public class UserService {
//
//	@Autowired
//	static
//	UserRepository repository;
//	
//	public void saveUser(UserEntity entity) {
//		repository.save(entity);
//	}
//	
//	
//	/**
//	 * This method is used to delete the user using id - primary key of user table.
//	 * Note: Currently this method is not using in project.
//	 * @param id: id is the primary key of user table. 
//	 * @return Nothing.
//	 * 
//	 * TODO Remove this method in production if delete operation is not required.
//	 */
//	public void deleteUser(String id) {
//		repository.delete(id);
//	}
//	
//	/**
//	 * This method is used to get the user from table using id.
//	 * @param id: primary key of user table. 
//	 * @return User object.
//	 */
//	public UserEntity getUser(String id) {
//		return repository.findOne(id);
//	}
//	
//	/**
//	 * This method is used to validate whether the user exists or not in table for login in.
//	 * 
//	 * @param userName: String value that can be either email or mobile that is required to login.
//	 * @param password: password of the given userName to validate the user.
//	 * @param userType: userType can be customer or tenant.
//	 * @return UserBean which contains valid user details. Null value if login credentials are wrong.
//	 */
//	public static UserBean validate(String userName, String password) {
//		UserBean bean = null;
//		for (UserEntity us : repository.findAll()) {
//			if (((us.getEmail() != null && us.getEmail().equals(userName)) || (us.getMobileNumber() != null && us.getMobileNumber().equals(userName))) ) {
//				if (password != null && !us.getPassword().equals(Utilities.getSecurePassword(password))) {
//					break;
//				}
//				bean = new UserBean();
//				bean.setuser_Id(us.getUser_id());
//				bean.setFirstName(us.getFirstName());
//				bean.setLastName(us.getLastName());
//				bean.setEmail(us.getEmail());
//				bean.setMobile(us.getMobileNumber());
//				break;
//			}
//		}
//		return bean;
//	}
//
//	/**
//	 * This method is used to check whether the user already exists or not in database.
//	 * 
//	 * @param user: A json object with id and newPassword.
//	 * @return Nothing.
//	 */
//	public Boolean isUserExists(String email, String mobile, String userType) {
//		for (UserEntity us : repository.findAll()) {
//			if (((us.getEmail() != null && us.getEmail().equals(email)) || (us.getMobileNumber() != null && us.getMobileNumber().equals(mobile))) && us.getUserType().equals(userType)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//}
