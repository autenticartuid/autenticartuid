package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
	@SerializedName("grant_type")
	public String grant_type;

	@SerializedName("client_id")
	public String client_id;

	@SerializedName("username")
	public String user_name;

	@SerializedName("password")
	public String password;

	public AuthRequest(String user_name, String password) {
		this.user_name = user_name;
		this.password = password;
		grant_type = "password";
		client_id = "tuid";
	}

}
