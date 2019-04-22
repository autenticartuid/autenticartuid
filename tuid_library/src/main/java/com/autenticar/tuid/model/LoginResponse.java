package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse extends ResponseBase {

    @SerializedName("name")
    public String Name;

    @SerializedName("IdUser")
    public Integer userID;
}
