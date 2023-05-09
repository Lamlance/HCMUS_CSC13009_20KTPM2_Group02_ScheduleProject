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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public boolean equals(@Nullable Object obj) {
      if(obj instanceof TimerStats){
        return this.createDate == ((TimerStats)obj).createDate;
      }
      return false;
    }

    @Override
    public int hashCode() {
      try{
        return Math.toIntExact(createDate);
      }catch (Exception e){
        e.printStackTrace();
        return super.hashCode();
      }
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
    public List<Integer> weekDates = null;

    public Reminder() {

    }

    public Reminder(String id, String title, long reminderTime) {
      this.id = id;
      this.title = title;
      this.reminderTime = reminderTime;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if(obj instanceof Reminder){
        return this.id.equals(((Reminder)obj).id);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.id.hashCode();
    }
  }

  public static class Task implements Serializable {
    public static final Reminder DEFAULT_REMINDER = new Reminder("NONE", "NONE", -1);
    public static final String TABLE_NAME = "Task";
    public static final String TASK_REMINDER_NAME = "reminder";

    public String id;
    public String title;
    public int loops;
    public int loopsDone;
    public boolean isArchive = false;

    public Reminder reminder =  DEFAULT_REMINDER;

    @Override
    public boolean equals(@Nullable Object obj) {
      if(obj instanceof Task){
        return this.id.equals(((Task)obj).id);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.id.hashCode();
    }
  }

  static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
  TimerStats todayStats;
  long todayTime;


  static private final HashMap<Integer, List<Reminder>> SINGLE_REMINDER_BY_MONTH = new HashMap<>();
  static private final HashMap<Integer, List<Reminder>> WEEKLY_REMINDER_BY_WEEKDAY = new HashMap<>();
  static private final HashMap<Integer,List<Task>> TASKS_BY_MONTH = new HashMap<>();
  static private final List<Task> NORMAL_TASK = new LinkedList<>();
  static private final Map<Integer,List<Task>> WEEKLY_TASKS = new HashMap<>();



  static private void AddWeeklyReminderToMap(@NonNull Reminder r) {
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
  static private void AddSingleReminderToMap(@NonNull Reminder r) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(r.reminderTime);
    int month = calendar.get(Calendar.MONTH);
    if (!SINGLE_REMINDER_BY_MONTH.containsKey(month)) {
      SINGLE_REMINDER_BY_MONTH.put(month, new LinkedList<Reminder>());
    }
    SINGLE_REMINDER_BY_MONTH.get(month).add(r);
  }

  static private void RemoveWeeklyReminder(@NonNull Reminder r){
    if(r.weekDates == null){
      return;
    }

    for (Integer wd:r.weekDates) {
      WEEKLY_REMINDER_BY_WEEKDAY.get(wd).remove(r);
    }
  }
  static private void RemoveSingleReminder(@NonNull Reminder r){
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(r.reminderTime);
    int month = calendar.get(Calendar.MONTH);

    SINGLE_REMINDER_BY_MONTH.get(month).remove(r);
  }


  static private void AddTaskToMap(@NonNull Task t){
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(t.reminder.reminderTime);
    int month = calendar.get(Calendar.MONTH);
    if(!TASKS_BY_MONTH.containsKey(month)){
      TASKS_BY_MONTH.put(month,new LinkedList<>());
    }
    TASKS_BY_MONTH.get(month).add(t);
  }
  static private void AddNormalOrWeeklyTask(@NonNull Task t){
    if(t.reminder.weekDates == null){
      NORMAL_TASK.add(t);
      return;
    }
    AddWeeklyTask(t);
  }
  static private void AddWeeklyTask(@NonNull Task t){
    if(t.reminder.weekDates == null){
      return;
    }

    for (Integer wd: t.reminder.weekDates) {
      if(!WEEKLY_TASKS.containsKey(wd)){
        WEEKLY_TASKS.put(wd,new LinkedList<>());
      }
      WEEKLY_TASKS.get(wd).add(t);
      //Log.i("FIREBASE","ADDING WEEKLY TASK " + t.title + " " + wd);
    }
  }
  static private void RemoveTask(@NonNull Task task){
    if(task.reminder == Task.DEFAULT_REMINDER){
      NORMAL_TASK.remove(task);
    }else{
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(task.reminder.reminderTime);
      TASKS_BY_MONTH.get(calendar.get(Calendar.MONTH)).remove(task);
    }
  }

  static public @NonNull List<Reminder> GetRemindersInMonth(int month){
    List<Reminder> reminders = SINGLE_REMINDER_BY_MONTH.get(month);
    if(reminders != null){
      return  reminders;
    }
    return new LinkedList<Reminder>();
  }
  static public @NonNull List<Task> GetTasksInDate(int date,int month){

    List<Task> tasks = new LinkedList<>(NORMAL_TASK);

    List<Task> taskInMonth = TASKS_BY_MONTH.get(month);
    if(taskInMonth != null){
      Calendar calendar = Calendar.getInstance();
      List<Task> taskInDate = taskInMonth.stream().filter((t)->{
        calendar.setTimeInMillis(t.reminder.reminderTime);
        return calendar.get(Calendar.DATE) == date;
      }).collect(Collectors.toList());
      tasks.addAll(taskInDate);
    }

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MONTH,month);
    calendar.set(Calendar.DATE,date);
    List<Task> taskInWeekDate = WEEKLY_TASKS.get(calendar.get(Calendar.DAY_OF_WEEK));
    if(taskInWeekDate != null){
      //Log.i("FIREBASE","Today " + taskInWeekDate.size() + " weekly tasks");
      tasks.addAll(taskInWeekDate);
    }

    return tasks;
  }
  static public @NonNull HashMap<Integer, List<Reminder>> GetWeeklyReminderByWeekDay(){
    return new HashMap<>(WEEKLY_REMINDER_BY_WEEKDAY);
  }

  private final String userUID;

  static private ReminderTaskFireBase reminderTaskFireBase = null;
  public static ReminderTaskFireBase GetInstance(String userUID){
    if(reminderTaskFireBase == null){
      reminderTaskFireBase = new ReminderTaskFireBase(userUID);
    }
    return reminderTaskFireBase;
  }


  public interface  ReminderResultCallBack{
    void onResult(@Nullable List<Reminder> reminders);
  }

  private ReminderTaskFireBase(String deviceUUID) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    todayTime = calendar.getTimeInMillis();

    userUID = deviceUUID;
    todayStats = new TimerStats();


    getRemindersInAYear(calendar.get(Calendar.YEAR));
    getTasksInAYear(calendar.get(Calendar.YEAR));
    getNormalAndWeeklyTask();


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
      AddSingleReminderToMap(reminder);
      return reminder;
    }
    return null;
  }

  @Nullable
  public Reminder addWeeklyReminder(String title, List<Integer> weekDates) {
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
      AddWeeklyReminderToMap(reminder);
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
      RemoveSingleReminder(reminder);
    } else {
      databaseReference.child(userUID)
              .child(Reminder.TABLE_NAME)
              .child(Reminder.WEEKLY_REMINDER_NAME)
              .child(reminder.id)
              .removeValue();
      RemoveWeeklyReminder(reminder);
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

  public void searchReminder(long startTime,long endTime,@NonNull ReminderResultCallBack callBack){
    databaseReference.child(userUID)
            .child(Reminder.TABLE_NAME)
            .child(Reminder.SINGLE_REMINDER_NAME)
            .orderByChild(Reminder.REMINDER_TIME_NAME)
            .startAt(startTime)
            .endAt(endTime)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                  callBack.onResult(null);
                  return;
                }
                LinkedList<Reminder> reminderLinkedList = new LinkedList<>();
                Iterable<DataSnapshot> reminders = snapshot.getChildren();
                for (DataSnapshot data : reminders) {
                  Reminder reminder = data.getValue(Reminder.class);
                  if(reminder != null){
                    reminderLinkedList.add(reminder);
                  }
                }
                callBack.onResult(reminderLinkedList);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
  }
  public void searchReminder(String title,long startTime,long endTime,@NonNull ReminderResultCallBack callBack){
    String titleLowerCased = title.toLowerCase();
    searchReminder(startTime,endTime,(reminders -> {
      if(reminders == null){
        callBack.onResult(null);
        return ;
      }
      List<Reminder> filteredReminders = reminders.stream()
              .filter(r -> r.title.toLowerCase().contains(titleLowerCased))
              .collect(Collectors.toList());
      callBack.onResult(filteredReminders);
    }));
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
  public Task addTask(String title, int loops, int loopsDone) {
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
      return task;
    }
    return null;
  }

  public @Nullable Reminder makeTaskSingleReminder(String title, long time, List<Task> tasks) {
    Reminder reminder = addReminder(title, time);
    if (reminder == null) {
      return null;
    }

    for (Task t : tasks) {
      t.reminder = reminder;
      updateTask(t);
    }
    return reminder;
  }

  public @Nullable Reminder makeTaskWeeklyReminder(String title, List<Integer> weekDates, List<Task> tasks) {
    Reminder reminder = addWeeklyReminder(title, weekDates);
    if (reminder == null) {
      return null;
    }

    for (Task t : tasks) {
      t.reminder = reminder;
      updateTask(t);
    }
    return reminder;
  }

  private void getTasksInAYear(int year){
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


    databaseReference.child(this.userUID)
            .child(Task.TABLE_NAME)
            .orderByChild(Task.TASK_REMINDER_NAME + "/" + Reminder.REMINDER_TIME_NAME)
            .startAt(startOfYear)
            .endAt(endOfYear)
            .addListenerForSingleValueEvent(new ReminderTasksQueryListener());

  }

  private void getNormalAndWeeklyTask(){
    databaseReference.child(this.userUID)
            .child(Task.TABLE_NAME)
            .orderByChild(Task.TASK_REMINDER_NAME + "/" + Reminder.REMINDER_TIME_NAME)
            .equalTo(-1)
            .addListenerForSingleValueEvent(new NormalTaskQueryListener());
  }

  public void removeTask(Task task){
    databaseReference.child(userUID)
            .child(Task.TABLE_NAME)
            .child(task.id)
            .removeValue();
    ReminderTaskFireBase.RemoveTask(task);
  }



  static class ReminderTasksQueryListener implements com.google.firebase.database.ValueEventListener{
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
      if(!snapshot.exists()){
        return;
      }
      Iterable<DataSnapshot> dataList = snapshot.getChildren();
      for (DataSnapshot data: dataList) {
        Task task = data.getValue(Task.class);
        if(task != null){
          AddTaskToMap(task);
        }
      }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }

  static class NormalTaskQueryListener implements com.google.firebase.database.ValueEventListener{

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
      if(!snapshot.exists()){
        return;
      }
      Iterable<DataSnapshot> dataList = snapshot.getChildren();
      for (DataSnapshot data: dataList) {
        Task task = data.getValue(Task.class);
        if(task != null){
          AddNormalOrWeeklyTask(task);
        }
      }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

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
