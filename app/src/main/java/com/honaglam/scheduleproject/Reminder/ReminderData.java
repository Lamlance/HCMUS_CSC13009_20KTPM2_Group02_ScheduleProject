package com.honaglam.scheduleproject.Reminder;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class ReminderData implements Serializable {
  public String Name;
  public long RemindTime;
  public int id = -1;
  boolean isWeekly = false;
  int myDate;
  public ReminderData(String name, long time,int id){
    this.Name = name;
    this.RemindTime = time;
    this.id = id;

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    this.myDate = calendar.get(Calendar.DATE);
  }
  public int getMyDate(){
    return myDate;
  }
}
