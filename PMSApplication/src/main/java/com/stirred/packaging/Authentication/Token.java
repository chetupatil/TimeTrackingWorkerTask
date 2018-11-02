package com.stirred.packaging.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
	@JsonProperty
	private String token;
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
