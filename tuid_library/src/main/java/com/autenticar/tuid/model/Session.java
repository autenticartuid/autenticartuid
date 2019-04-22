package com.autenticar.tuid.model;

public class Session {

  AuthResponse SessionInfo;
  public int userID;
  public String dni;
  public String gender;
  public String description;
  public static Session build(AuthResponse sessionInfo) {
    Session ret = new Session();
    ret.SessionInfo = sessionInfo;
    return ret;
  }

  public static Session build(int userID, String dni, String gender, String description) {
    Session ret = new Session();
    ret.userID = userID;
    ret.dni = dni;
    ret.gender = gender;
    ret.description = description;
    return ret;
  }

}
