package com.coddicted.expensesview.dto;

//@XmlRootElement
public class UserDTO {

	//@XmlElement
	private String userName;
	//@XmlElement
	private String password;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
