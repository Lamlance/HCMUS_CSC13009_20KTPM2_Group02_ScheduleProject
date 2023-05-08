package com.honaglam.scheduleproject.Repository;

import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Calendar;
import java.util.List;

public class ReminderRepository {
  public interface ReminderAction {
    public void onAction(@Nullable ReminderTaskFireBase.Reminder reminder);
  }

  public interface ReminderSearchResult {
    public void onAction(@Nullable List<ReminderTaskFireBase.Reminder> reminders);
  }

  @Nullable
  ReminderAction OnAddReminder;
  @Nullable
  ReminderAction OnDeleteReminder;
  @Nullable
  ReminderSearchResult OnSearchResult;

  String userId;

  public ReminderRepository(String userId) {
    this.userId = userId;
  }


  public void SetAddReminderCallBack(ReminderAction action) {
    OnAddReminder = action;
  }

  public void SetDeleteReminderCallBack(ReminderAction action) {
    OnDeleteReminder = action;
  }

  public void SetSearchResultCallBack(ReminderSearchResult action){
    OnSearchResult = action;
  }


  private void callReminderAction(ReminderAction action, ReminderTaskFireBase.Reminder reminder) {
    if (action == null) {
      return;
    }
    try {
      action.onAction(reminder);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addSingleReminder(String title, long time) {
    ReminderTaskFireBase.Reminder reminder = ReminderTaskFireBase.GetInstance(userId).addReminder(title, time);
    callReminderAction(OnAddReminder, reminder);
  }

  public void addWeeklyReminder(String title, List<Integer> weekDates) {
    ReminderTaskFireBase.Reminder reminder = ReminderTaskFireBase.GetInstance(userId).addWeeklyReminder(title, weekDates);
    callReminderAction(OnAddReminder, reminder);
  }

  public void removeReminder(ReminderTaskFireBase.Reminder reminder) {
    ReminderTaskFireBase.GetInstance(userId).removeReminder(reminder);
    callReminderAction(OnDeleteReminder, reminder);
  }

  public void SearchReminder(String title, long timeStart, long timeEnd) {
    if (OnSearchResult == null) {
      return;
    }

    boolean isEmptyTitle = title.isBlank() || title.isEmpty();
    boolean isEmptyTime = timeStart <= 0 || timeEnd <= 0;
    if(isEmptyTime && isEmptyTitle ){
      return;
    }

    if(isEmptyTitle){
      ReminderTaskFireBase.GetInstance(userId).searchReminder(timeStart, timeEnd, reminders -> {
        OnSearchResult.onAction(reminders);
      });
      return;
    }

    if(isEmptyTime){
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.DAY_OF_YEAR, 1);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      timeStart  = calendar.getTimeInMillis();

      calendar.set(Calendar.MONTH, Calendar.DECEMBER);
      calendar.set(Calendar.DATE, 31);
      calendar.set(Calendar.HOUR, 23);
      calendar.set(Calendar.MINUTE, 59);
      timeEnd = calendar.getTimeInMillis();
    }

    ReminderTaskFireBase.GetInstance(userId).searchReminder(title,timeStart,timeEnd,reminders -> {
      OnSearchResult.onAction(reminders);
    });
  }

}
