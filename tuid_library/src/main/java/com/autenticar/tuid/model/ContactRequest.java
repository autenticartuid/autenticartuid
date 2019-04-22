package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ContactRequest {
    @SerializedName("dni")
    public String dni;

    @SerializedName("email")
    public String email;

    @SerializedName("asunto")
    public String asunto;

    @SerializedName("mensaje")
    public String mensaje;
}
