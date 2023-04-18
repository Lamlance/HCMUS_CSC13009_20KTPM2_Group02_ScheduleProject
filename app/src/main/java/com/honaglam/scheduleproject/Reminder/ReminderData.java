package com.honaglam.scheduleproject.Reminder;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class ReminderData implements Serializable {
  public String Name;
  public long RemindTime;
  public int id = -1;
  boolean isWeekly = false;
  int myDate;
  public int weekDate = -1;
  public ReminderData(String name, long time,int id){
    this.Name = name;
    this.RemindTime = time;
    this.id = id;

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    this.myDate = calendar.get(Calendar.DATE);
  }

  public ReminderData(String name, long time,int id,int weekDate){
    this.Name = name;
    this.RemindTime = time;
    this.id = id;

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    this.myDate = calendar.get(Calendar.DATE);
    this.weekDate = calendar.get(Calendar.DAY_OF_WEEK);
  }

  public int getMyDate(){
    return myDate;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if(!(obj instanceof ReminderData)){
      return false;
    }
    ReminderData data = (ReminderData) obj;
    return this.id == data.id;
  }
}
