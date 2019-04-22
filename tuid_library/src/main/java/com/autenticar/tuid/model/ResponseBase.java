package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ResponseBase {
    @SerializedName("res")
    public String Response;

    @SerializedName("status")
    public int Status;

    @SerializedName("message")
    public String Message;

    @SerializedName("details")
    public String Details;


    public boolean IsOk() {
        return (this.Response.equals("ok") && Status == 200 && (!Message.equals("chance")));
    }

    public boolean IsChance() {
        return (this.Response.equals("ok") && Status == 200 && (Message.equals("chance")));
    }
}


/*
appData = {
        res: 'ok',
        status: 200,
        result: 'image recibida',
    };
 */
