package com.honaglam.scheduleproject.Reminder;

import java.io.Serializable;

public class ReminderData implements Serializable {
  public String Name;
  public long RemindTime;
  public ReminderData(String name, long time){
    this.Name = name;
    this.RemindTime = time;
  }
}
