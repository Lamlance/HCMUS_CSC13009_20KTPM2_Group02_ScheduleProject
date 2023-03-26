package com.honaglam.scheduleproject;

import java.io.Serializable;

public class MainActivitySavedData implements Serializable {
  protected int reminderCount = 0;
  protected String alarmUri = null;
  protected long workMillis = 0;
  protected long shortBreakMillis = 0;
  protected long longBreakMillis = 0;
}
