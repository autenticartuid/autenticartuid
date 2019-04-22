package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("idDevice")
    public String DeviceID;

    @SerializedName("selfie")
    public String selfie;

    @SerializedName("idDocument")
    public Document DataDocument;
}

