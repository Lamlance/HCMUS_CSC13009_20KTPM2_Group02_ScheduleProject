package com.honaglam.scheduleproject.UserSetting;

import androidx.annotation.Nullable;

public class UserAuthData {
  public String USER_ID = "";
  public String USER_IMAGE_URL = "";

  static private @Nullable UserAuthData MY_INSTANCE = null;

  static public UserAuthData GetInstance(){
    if(MY_INSTANCE == null){
      MY_INSTANCE = new UserAuthData();
    }
    return MY_INSTANCE;
  }

  private UserAuthData(){

  }
}
