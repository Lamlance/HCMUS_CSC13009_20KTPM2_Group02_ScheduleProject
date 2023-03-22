package com.honaglam.scheduleproject.Reminder;

import java.io.Serializable;

public class ReminderData implements Serializable {
  public String Name;
  public long RemindTime;
  public int id = -1;
  public ReminderData(String name, long time,int id){
    this.Name = name;
    this.RemindTime = time;
    this.id = id;
  }

}
