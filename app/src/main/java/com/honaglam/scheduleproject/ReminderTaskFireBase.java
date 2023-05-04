package com.honaglam.scheduleproject;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ReminderTaskFireBase {
  public static class TimerStats implements Serializable {
    public static final String TABLE_NAME = "TimerStats";
    public static final String CREATE_DATE_NAME = "createDate";
    public long createDate;
    public long workDur = 0;
    public long shortDur = 0;
    public long longDur = 0;

    TimerStats() {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      createDate = calendar.getTimeInMillis();
    }

    @NonNull
    @Override
    public String toString() {
      return String.format(Locale.getDefault(), "%d w:%d - s:%d - l:%d", createDate, workDur, shortDur, longDur);
    }
  }

  public static class Reminder implements Serializable {
    public static final String REMINDER_TIME_NAME = "reminderTime";
    public static final String TABLE_NAME = "Reminder";
    public static final String SINGLE_REMINDER_NAME = "Single";
    public static final String WEEKLY_REMINDER_NAME = "Weekly";

    public String title;
    public long reminderTime = -1;
    public String id;

    @Nullable
    List<Integer> weekDates = null;

    public Reminder() {

    }

    public Reminder(String id, String title, long reminderTime) {
      this.id = id;
      this.title = title;
      this.reminderTime = reminderTime;
    }
  }

  public static class Task implements Serializable {
    public static final Reminder DEFAULT_REMINDER = new Reminder("NONE", "NONE", -1);
    public static final String TABLE_NAME = "Task";
    String id;
    public String title;
    public int loops;
    public int loopsDone;

    @NonNull
    Reminder reminder = DEFAULT_REMINDER;
  }

  static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
  private final String userUID;
  TimerStats todayStats;
  long todayTime;


  static private final HashMap<Integer, List<Reminder>> SINGLE_REMINDER_BY_MONTH = new HashMap<>();
  static private final HashMap<Integer, List<Reminder>> WEEKLY_REMINDER_BY_WEEKDAY = new HashMap<>();

  static private void AddWeeklyReminderToMap(Reminder r) {
    if (r.weekDates == null) {
      return;
    }

    for (Integer weekDate : r.weekDates) {
      if (!WEEKLY_REMINDER_BY_WEEKDAY.containsKey(weekDate)) {
        WEEKLY_REMINDER_BY_WEEKDAY.put(weekDate, new LinkedList<Reminder>());
      }
      WEEKLY_REMINDER_BY_WEEKDAY.get(weekDate).add(r);
    }
  }

  static private void AddSingleReminderToMap(Reminder r) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(r.reminderTime);
    int month = calendar.get(Calendar.MONTH);
    if (!SINGLE_REMINDER_BY_MONTH.containsKey(month)) {
      SINGLE_REMINDER_BY_MONTH.put(month, new LinkedList<Reminder>());
    }
    SINGLE_REMINDER_BY_MONTH.get(month).add(r);
  }

  static public @NonNull List<Reminder> GetRemindersInMonth(int month){
    List<Reminder> reminders = SINGLE_REMINDER_BY_MONTH.get(month);
    if(reminders != null){
      return  reminders;
    }
    return new LinkedList<Reminder>();
  }

  public ReminderTaskFireBase(String deviceUUID) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    todayTime = calendar.getTimeInMillis();

    userUID = deviceUUID;
    todayStats = new TimerStats();


    getRemindersInAYear(calendar.get(Calendar.YEAR));
    /*
    databaseReference.child(userUID)
            .child(TimerStats.TABLE_NAME)
            .orderByChild(TimerStats.CREATE_DATE_NAME)
            .equalTo(todayTime)
            .addListenerForSingleValueEvent(new TodayStatsValueEventListener());

     */

    //addReminder("Today reminder",todayTime);
    //addWeeklyReminder("Today weekly reminder",todayTime,Calendar.MONDAY-1);
  }



  @Nullable
  public Reminder addReminder(String title, long time) {
    Reminder reminder = new Reminder();
    reminder.title = title;
    reminder.reminderTime = time;

    DatabaseReference singleReminderRef = databaseReference.child(userUID)
            .child(Reminder.TABLE_NAME)
            .child(Reminder.SINGLE_REMINDER_NAME);

    String reminderId = singleReminderRef.push().getKey();
    if (reminderId != null) {
      reminder.id = reminderId;
      singleReminderRef.child(reminderId).setValue(reminder);
      return reminder;
    }
    return null;
  }

  @Nullable
  private Reminder addWeeklyReminder(String title, List<Integer> weekDates) {
    Reminder reminder = new Reminder();
    reminder.title = title;
    reminder.weekDates = weekDates;

    DatabaseReference weeklyReminderRef = databaseReference.child(userUID)
            .child(Reminder.TABLE_NAME)
            .child(Reminder.WEEKLY_REMINDER_NAME);
    String reminderId = weeklyReminderRef.push().getKey();
    if (reminderId != null) {
      reminder.id = reminderId;
      weeklyReminderRef.child(reminderId).setValue(reminder);
      return reminder;
    }
    return null;
  }

  public void removeReminder(Reminder reminder) {
    if (reminder.weekDates == null) {
      databaseReference.child(userUID)
              .child(Reminder.TABLE_NAME)
              .child(Reminder.SINGLE_REMINDER_NAME)
              .child(reminder.id)
              .removeValue();
    } else {
      databaseReference.child(userUID)
              .child(Reminder.TABLE_NAME)
              .child(Reminder.WEEKLY_REMINDER_NAME)
              .child(reminder.id)
              .removeValue();
    }
  }

  public void getRemindersInAYear(int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);

    calendar.set(Calendar.DAY_OF_YEAR, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    long startOfYear = calendar.getTimeInMillis();

    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DATE, 31);
    calendar.set(Calendar.HOUR, 23);
    calendar.set(Calendar.MINUTE, 59);
    long endOfYear = calendar.getTimeInMillis();

    ReminderTaskFireBase.SINGLE_REMINDER_BY_MONTH.clear();
    ReminderTaskFireBase.WEEKLY_REMINDER_BY_WEEKDAY.clear();

    databaseReference.child(userUID)
            .child(Reminder.TABLE_NAME)
            .child(Reminder.SINGLE_REMINDER_NAME)
            .orderByChild(Reminder.REMINDER_TIME_NAME)
            .startAt(startOfYear)
            .endAt(endOfYear)
            .addListenerForSingleValueEvent(new RemindersQueryListener());

    databaseReference.child(userUID)
            .child(Reminder.TABLE_NAME)
            .child(Reminder.WEEKLY_REMINDER_NAME)
            .orderByChild(Reminder.REMINDER_TIME_NAME)
            .addListenerForSingleValueEvent(new RemindersQueryListener());
  }

  static class RemindersQueryListener implements com.google.firebase.database.ValueEventListener {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
      if (!snapshot.exists()) {
        return;
      }
      Iterable<DataSnapshot> reminders = snapshot.getChildren();
      for (DataSnapshot data : reminders) {
        Reminder reminder = data.getValue(Reminder.class);
        if (reminder == null) {
          continue;
        }

        if (reminder.weekDates == null) {
          ReminderTaskFireBase.AddSingleReminderToMap(reminder);
        } else {
          ReminderTaskFireBase.AddWeeklyReminderToMap(reminder);
        }

      }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }



  public void updateTask(Task task) {
    databaseReference.child(userUID)
            .child(Task.TABLE_NAME)
            .child(task.id)
            .setValue(task);
  }

  @Nullable
  public String addTask(String title, int loops, int loopsDone) {
    Task task = new Task();
    task.title = title;
    task.loops = loops;
    task.loopsDone = loopsDone;
    DatabaseReference taskRef = databaseReference.child(userUID)
            .child(Task.TABLE_NAME);
    String taskId = taskRef.push().getKey();
    if (taskId != null) {
      task.id = taskId;
      taskRef.child(taskId).setValue(task);
      return taskId;
    }
    return null;
  }

  public void makeTaskSingleReminder(String title, long time, List<Task> tasks) {
    Reminder reminder = addReminder(title, time);
    if (reminder == null) {
      return;
    }

    for (Task t : tasks) {
      t.reminder = reminder;
      updateTask(t);
    }
  }

  public void makeTaskWeeklyReminder(String title, List<Integer> weekDates, List<Task> tasks) {
    Reminder reminder = addWeeklyReminder(title, weekDates);
    if (reminder == null) {
      return;
    }

    for (Task t : tasks) {
      t.reminder = reminder;
      updateTask(t);
    }

  }

  class TodayStatsValueEventListener implements ValueEventListener {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

      if (!snapshot.exists()) {
        Log.i("FireBase", "Creating today stats");

        databaseReference.child(userUID).child(TimerStats.TABLE_NAME)
                .child(String.valueOf(todayTime))
                .setValue(todayStats);
        return;
      }

      try {
        TimerStats data = snapshot.child(String.valueOf(todayTime)).getValue(TimerStats.class);
        if (data != null) {
          todayStats = data;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }
}
