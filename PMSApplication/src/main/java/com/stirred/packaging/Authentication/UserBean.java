package com.stirred.packaging.Authentication;
/**
 * This class is used to return to user for successful login.
 * All the values that needed to return for successful login are set to this class and finally this class will return to user.
 * 
 */
public class UserBean {
	private String user_id;
	private String firstName;
	private String mobile;
	private String email;
	private String password;
	private String lastName;
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "UserBean [user_id=" + user_id + ", firstname=" + firstName + ", mobile=" + mobile + ", email=" + email + ", password=" + password + ", lastname=" + lastName
				+ ",  token=" + token + "]";
	}

	public String getUser_Id() {
		return user_id;
	}

	public void setuser_Id(String user_id) {
		this.user_id = user_id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
