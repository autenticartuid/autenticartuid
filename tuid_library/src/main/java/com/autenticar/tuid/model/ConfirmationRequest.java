package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;


public class ConfirmationRequest {

    @SerializedName("idUser")
    public int userID;

    @SerializedName("codeEmail")
    public String MailCode;

    @SerializedName("codeTel")
    public String SMSCode;

    @Override
    public String toString() {
        return "ConfirmationRequest{" +
                "userID=" + userID +
                ", MailCode='" + MailCode + '\'' +
                ", SMSCode='" + SMSCode + '\'' +
                '}';
    }
}

