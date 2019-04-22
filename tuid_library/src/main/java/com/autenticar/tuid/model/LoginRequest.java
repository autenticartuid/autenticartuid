package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

/*
    @SerializedName("dni")
    public String DNI;

    @SerializedName("sexo")
    public String Sexo;
*/

    @SerializedName("file")
    public String FileEncode64;
}

