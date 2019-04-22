package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

/**
 * Creado por mbatto on 2/11/2016.
 */

public class AuthResponse {

    @SerializedName("access_token")
    public String Token;

    @SerializedName("token_type")
    public String TokenType;

    @SerializedName("expires_in")
    public int ExpiresIn;

    @SerializedName("refresh_token")
    public String RefreshToken;

    @SerializedName("as:client_id")
    public String ClienID;

    @SerializedName("sessionTimeLife")
    public String SessionTimeLife;

}
