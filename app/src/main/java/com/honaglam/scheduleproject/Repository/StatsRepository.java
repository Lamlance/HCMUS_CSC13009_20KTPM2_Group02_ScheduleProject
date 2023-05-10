package com.honaglam.scheduleproject.Repository;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

public class StatsRepository {
  String uuid;
  public StatsRepository(String id){
    uuid = id;
  }
  public void addTimeTodayTask(int state,long time){
    ReminderTaskFireBase.GetInstance(uuid).addTimeTodayTask(state,time);
  }
}
