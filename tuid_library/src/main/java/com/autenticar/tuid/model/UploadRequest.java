package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class UploadRequest {


    @SerializedName("name")
    public String Name;

    @SerializedName("file")
    public String FileEncode64;
}

