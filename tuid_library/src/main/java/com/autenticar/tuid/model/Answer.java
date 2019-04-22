package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class Answer {


  @SerializedName("id")
  public String ID;

  @SerializedName("idChoice")
  public String IDChoice;

  public Answer(String id, String idChoice) {
    this.ID = id;
    this.IDChoice = idChoice;
  }
}

/*
"id_user": "1",
                        "answers": [
                        {"id": "0", "idChoice":"2"}
                        ]
                        }
 */

