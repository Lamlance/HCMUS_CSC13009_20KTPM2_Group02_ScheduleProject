package com.honaglam.scheduleproject.UserSetting;

import android.net.Uri;

import java.io.Serializable;

public class UserTimerSettings implements Serializable {
  public long workMillis = 0;
  public long shortBreakMillis = 0;
  public long longBreakMillis = 0;

  public boolean autoStartBreakSetting = false;
  public boolean autoStartPomodoroSetting = false;
  public long longBreakInterValSetting = 4;
  public Uri alarmUri;

  public int prefTheme = 0;
  
  public UserTimerSettings(
          long workTime, long shortBreakTime, long longBreakTime, Uri alarmSound,
          boolean autoStartBreak, boolean autoStartPomodoro, long longBreakInterVal,
          int prefThemeId
  ){
    workMillis = workTime;
    shortBreakMillis = shortBreakTime;
    longBreakMillis = longBreakTime;
    alarmUri = alarmSound;
    autoStartBreakSetting = autoStartBreak;
    autoStartPomodoroSetting = autoStartPomodoro;
    longBreakInterValSetting = longBreakInterVal;
    prefTheme = prefThemeId;
  }

}
