package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class Question {

    @SerializedName("0")
    public String chance0;

    @SerializedName("1")
    public String chance1;

    @SerializedName("2")
    public String chance2;

    @SerializedName("3")
    public String chance3;

    @SerializedName("id")
    public String ID;

    @SerializedName("text")
    public String question;

}
/*
 "type":"DNI",
                "number":"36755088",
                "name":"GUIDO",
                "lastname":"RIVALDO",
                "expires":"2020-01-30",
                "tramit":"2343243",
                "gender":"M",
                "birth": "1999-01-30",
                "address":"gxcxcvxcvx"
 */