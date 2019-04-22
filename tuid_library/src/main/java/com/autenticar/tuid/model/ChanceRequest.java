package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ChanceRequest {

    @SerializedName("idDevice")
    public String DeviceID;

    @SerializedName("IdUser")
    public int userID;

    @SerializedName("answers")
    public ArrayList<Answer> Answers;
}

/*
"id_user": "1",
                        "answers": [
                        {"id": "0", "idChoice":"2"}
                        ]
                        }
 */

