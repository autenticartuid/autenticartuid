package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse extends ResponseBase {

    @SerializedName("question")
    public Question Questions;

    @SerializedName("iduser")
    public Integer userID;
}

/*
{
	"res": "ok",
	"status": 200,
	"message": "chance",
	"similarity": 100,
	"question": {
		"0": "Fradkin Ricardo Guido G",
		"1": "Rey Jose Alfredo Y Rey Juan Carlos",
		"2": "El Regreso Srl",
		"3": "Ninguna es correcta",
		"id": "4",
		"text": "Trabaja o trabajo en alguna de estas empresas"
	},
	"id_user": 155
}
 */

