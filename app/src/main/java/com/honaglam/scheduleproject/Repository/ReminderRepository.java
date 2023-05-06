package com.honaglam.scheduleproject.Repository;

import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class ReminderRepository {
  public interface ReminderAction{
    public void onAction( @Nullable ReminderTaskFireBase.Reminder reminder);
  }

  @Nullable ReminderAction OnAddReminder;
  @Nullable ReminderAction OnDeleteReminder;

  String userId;
  public ReminderRepository(String userId){
    this.userId = userId;
  }

  public void SetAddReminderCallBack(ReminderAction action){
    OnAddReminder = action;
  }

  private void callReminderAction(ReminderAction action, ReminderTaskFireBase.Reminder reminder){
    if(action == null){
      return;
    }
    try {
      action.onAction(reminder);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public void addSingleReminder(String title,long time){
    ReminderTaskFireBase.Reminder reminder = ReminderTaskFireBase.GetInstance(userId).addReminder(title,time);
    callReminderAction(OnAddReminder,reminder);
  }
  public void addWeeklyReminder(String title, List<Integer> weekDates){
    ReminderTaskFireBase.Reminder reminder = ReminderTaskFireBase.GetInstance(userId).addWeeklyReminder(title,weekDates);
    callReminderAction(OnAddReminder,reminder);
  }
}
