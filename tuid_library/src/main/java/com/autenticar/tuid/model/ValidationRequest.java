package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ValidationRequest {

    @SerializedName("idUser")
    public int userID;

    @SerializedName("email")
    public String EMail;

    @SerializedName("telefono")
    public String CellPhone;

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "userID=" + userID +
                ", EMail='" + EMail + '\'' +
                ", CellPhone='" + CellPhone + '\'' +
                '}';
    }
}

