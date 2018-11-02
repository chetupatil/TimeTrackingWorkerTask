package com.stirred.packaging.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * This class is used to set status code and message and return to the user on validation errors.
 * 
 */
public class StatusMessage {
	
	private Integer status;
	private String message;
	
	public StatusMessage() {
	}

	@JsonProperty(value = "status_code")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@JsonProperty(value = "message")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}