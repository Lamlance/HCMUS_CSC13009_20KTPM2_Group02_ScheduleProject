package com.honaglam.scheduleproject;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReminderTaskFireBase {
  public static class TimerStats implements Serializable {
    public static final String TABLE_NAME = "TimerStats";
    public static final String CREATE_DATE_NAME = "createDate";
    public long createDate = 0;
    public long workDur = 0;
    public long shortDur = 0;
    public long longDur = 0;

    TimerStats() {
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


  public static class Initialize{

  }
  static boolean GET_REMINDER_COMPLETED = false;
  static boolean GET_SINGLE_REMIND_TASK_COMPLETED = false;
  static boolean GET_WEEKLY_REMIND_TASK_COMPLETED = false;


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
      Log.i("FIREBASE","NORMAL TASK");
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
      reminderTaskFireBase = new ReminderTaskFireBase(userUID, () -> {});
    }
    return reminderTaskFireBase;
  }

  public static ReminderTaskFireBase GetInstance(String userUID,OnCompleted onCompleted){
    if(reminderTaskFireBase == null){
      reminderTaskFireBase = new ReminderTaskFireBase(userUID,onCompleted);
    }
    return reminderTaskFireBase;
  }

  protected static void RemoveInstance(){
    SINGLE_REMINDER_BY_MONTH.clear();
    WEEKLY_REMINDER_BY_WEEKDAY.clear();

    TASKS_BY_MONTH.clear();
    NORMAL_TASK.clear();
    WEEKLY_TASKS.clear();

    reminderTaskFireBase = null;
  }


  public interface  ReminderResultCallBack{
    void onResult(@Nullable List<Reminder> reminders);
  }

  protected interface OnCompleted{
    void onCompleted();
  }

  class InitializeThread implements Runnable{
    OnCompleted onCompleted;
    InitializeThread(OnCompleted completed){
      this.onCompleted = completed;
    }
    @Override
    public void run() {
      Log.i("FIREBASE","Starting Initializing thread");

      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      todayTime = calendar.getTimeInMillis();

      todayStats = new TimerStats();
      todayStats.createDate = calendar.getTimeInMillis();
      createTodayStats(todayStats);

      getRemindersInAYear(calendar.get(Calendar.YEAR), () -> {
        Log.i("FIREBASE","Finish getting reminders in a year");
        getTasksInAYear(calendar.get(Calendar.YEAR), () -> {
          Log.i("FIREBASE","Finish getting Task in a year");
          getNormalAndWeeklyTask(() -> {
            Log.i("FIREBASE","Finish getting normal and weekly task");
            onCompleted.onCompleted();
          });
        });
      });
    }
  }

  private ReminderTaskFireBase(String deviceUUID,OnCompleted onCompleted) {
    userUID = deviceUUID;
    Log.i("FIREBASE","Create Initialize thread");
    new Thread(new InitializeThread(onCompleted)).start();
  }

  //Initialize
  public void getRemindersInAYear(int year,@NonNull OnCompleted onCompletedInitial) {
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
            .addListenerForSingleValueEvent(new RemindersQueryListener(() -> {
              databaseReference.child(userUID)
                      .child(Reminder.TABLE_NAME)
                      .child(Reminder.WEEKLY_REMINDER_NAME)
                      .orderByChild(Reminder.REMINDER_TIME_NAME)
                      .addListenerForSingleValueEvent(new RemindersQueryListener(onCompletedInitial));
            }));


  }
  private void getTasksInAYear(int year,OnCompleted onCompleted){
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
            .addListenerForSingleValueEvent(new ReminderTasksQueryListener(onCompleted::onCompleted));

  }
  private void getNormalAndWeeklyTask(OnCompleted onCompleted){
    databaseReference.child(this.userUID)
            .child(Task.TABLE_NAME)
            .orderByChild(Task.TASK_REMINDER_NAME + "/" + Reminder.REMINDER_TIME_NAME)
            .equalTo(-1)
            .addListenerForSingleValueEvent(new NormalTaskQueryListener(onCompleted));
  }
  static class ReminderTasksQueryListener implements com.google.firebase.database.ValueEventListener{
    @NonNull protected OnCompleted onCompleted;
    ReminderTasksQueryListener(@NonNull OnCompleted completed){
      onCompleted = completed;
    }
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
      onCompleted.onCompleted();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }
  static class NormalTaskQueryListener implements com.google.firebase.database.ValueEventListener{
    @NonNull OnCompleted onCompleted;

    NormalTaskQueryListener(@NonNull OnCompleted completed) {
      onCompleted = completed;
    }

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

      onCompleted.onCompleted();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }
  static class RemindersQueryListener implements com.google.firebase.database.ValueEventListener {
    @NonNull protected OnCompleted onCompleted;
    RemindersQueryListener(@NonNull OnCompleted completed){
      onCompleted = completed;
    }
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
      if (!snapshot.exists()) {
        onCompleted.onCompleted();
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
      onCompleted.onCompleted();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }

  //===============


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




  public interface OnTaskModifyCompleted{
    void onCompleted(@Nullable Task task);
  }
  public interface OnTaskSetReminderCompleted{
    void onComplete(@Nullable Reminder reminder,@NonNull List<Task> tasks);
  }

  public interface OnActionFailed{
    void onFail(DatabaseError error);
  }

  public void updateTask(Task taskData,@NonNull OnTaskModifyCompleted onCompleted) {
    databaseReference.child(userUID)
            .child(Task.TABLE_NAME)
            .child(taskData.id)
            .setValue(taskData)
            .addOnCompleteListener(task1 -> {
              onCompleted.onCompleted(taskData);
            });
  }

  public void addTask(String title, int loops, int loopsDone,@NonNull OnTaskModifyCompleted onCompleted) {
    Task taskData = new Task();
    taskData.title = title;
    taskData.loops = loops;
    taskData.loopsDone = loopsDone;
    DatabaseReference taskRef = databaseReference.child(userUID)
            .child(Task.TABLE_NAME);
    String taskId = taskRef.push().getKey();
    if (taskId != null) {
      taskData.id = taskId;
      taskRef.child(taskId).setValue(taskData).addOnCompleteListener(task1 -> {
        onCompleted.onCompleted(taskData);
      });
      return;
    }
    onCompleted.onCompleted(null);
  }

  public void makeTaskSingleReminder(
          String title, long time, List<Task> tasks,
          OnTaskSetReminderCompleted completed) {

    Reminder reminder = addReminder(title, time);
    if (reminder == null) {
      return;
    }

    Map<String,Object> taskMap = new HashMap<>();

    for (Task t : tasks) {
      t.reminder = reminder;
      taskMap.put(t.id,t);
    }

    databaseReference.child(userUID)
            .child(Task.TABLE_NAME)
            .updateChildren(taskMap, (error, ref) -> {
              if(error == null){
                completed.onComplete(reminder,tasks);
              }else{
                Log.e("FIREBASE", error.getDetails());
              }
            });
  }

  public void makeTaskWeeklyReminder(
          String title, List<Integer> weekDates, List<Task> tasks,
          OnTaskSetReminderCompleted completed) {

    Reminder reminder = addWeeklyReminder(title, weekDates);
    if (reminder == null) {
      return;
    }

    Map<String,Object> taskMap = new HashMap<>();

    for (Task t : tasks) {
      t.reminder = reminder;
      taskMap.put(t.id,t);
    }
    databaseReference.child(userUID)
            .child(Task.TABLE_NAME)
            .updateChildren(taskMap, (error, ref) -> {
              if(error == null){
                completed.onComplete(reminder,tasks);
              }
              else{
                Log.e("FIREBASE", error.getDetails());
              }
            });
  }

  public void removeTask(Task taskData,@NonNull OnTaskModifyCompleted onCompleted){
    databaseReference.child(userUID)
            .child(Task.TABLE_NAME)
            .child(taskData.id)
            .removeValue()
            .addOnCompleteListener(task -> {
              onCompleted.onCompleted(taskData);
            });
  }



  public interface GetStatsCompletedCallBack{
    void onCompleted(@NonNull List<TimerStats> stats,long startTime,long endTime);
  }
  public void createTodayStats(TimerStats stats){
    databaseReference.child(userUID).child(TimerStats.TABLE_NAME)
            .orderByKey()
            .equalTo(String.valueOf(stats.createDate))
            .addListenerForSingleValueEvent(new TodayStatsValueEventListener());
  }
  public void addTimeTodayTask(int state,long time){
    if(state == TimerService.WORK_STATE){
      todayStats.workDur += time;
    }
    if(state == TimerService.SHORT_BREAK_STATE){
      todayStats.shortDur += time;
    }
    if(state == TimerService.LONG_BREAK_STATE){
      todayStats.longDur += time;
    }

    databaseReference.child(userUID)
            .child(TimerStats.TABLE_NAME)
            .child(String.valueOf(todayStats.createDate))
            .setValue(todayStats);
  }
  public void getTimeStats30DaysBefore(GetStatsCompletedCallBack completedCallBack){
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    long endTime = calendar.getTimeInMillis();

    calendar.add(Calendar.DATE,-30);
    long startTime = calendar.getTimeInMillis();

    databaseReference.child(userUID)
            .child(TimerStats.TABLE_NAME)
            .orderByChild(TimerStats.CREATE_DATE_NAME)
            .startAt(startTime)
            .endAt(endTime)
            .addListenerForSingleValueEvent(new GetManyStatsListener(completedCallBack, startTime, endTime));
  }

  static class GetManyStatsListener implements ValueEventListener{
    GetStatsCompletedCallBack completedCallBack;
    long startTime;
    long endTime;
    GetManyStatsListener(GetStatsCompletedCallBack statsCompletedCallBack,long start,long end){
      completedCallBack = statsCompletedCallBack;
      startTime = start;
      endTime = end;
    }
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
      List<TimerStats> statsList = new LinkedList<>();

      if(!snapshot.exists()){
        completedCallBack.onCompleted(statsList,startTime,endTime);
        return;
      }
      Iterable<DataSnapshot> statsData = snapshot.getChildren();
      for (DataSnapshot data: statsData) {
        TimerStats timerStats = data.getValue(TimerStats.class);
        if(timerStats != null){
          Log.i("FIREBASE","Create date " + timerStats.createDate);
          statsList.add(timerStats);
        }
      }
      completedCallBack.onCompleted(statsList,startTime,endTime);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }

  class TodayStatsValueEventListener implements ValueEventListener {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

      if (!snapshot.exists()) {
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
