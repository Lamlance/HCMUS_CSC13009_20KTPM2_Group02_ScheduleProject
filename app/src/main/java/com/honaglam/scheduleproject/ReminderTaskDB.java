package com.honaglam.scheduleproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Task.TaskData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderTaskDB extends SQLiteOpenHelper {
  private static final int DB_VERSION = 14;
  private static final String DB_NAME = "ScheduleProject.db";
  private static final String SQL_DROP_REMINDER_TABLE = "DROP TABLE IF EXISTS " + ReminderTable.TABLE_NAME;
  private static final String SQL_DROP_TASK_TABLE = "DROP TABLE IF EXISTS " + TaskTable.TABLE_NAME;
  private static final String SQL_DROP_STATS_TABLE = "DROP TABLE IF EXISTS " + StatsTable.TABLE_NAME;

  private Context context;
  int date = -1;
  int month = -1;
  int year = -1;

  long curWork = 0;
  long curShort = 0;
  long curLong = 0;

  private static class ReminderTable implements BaseColumns {
    public static final String TABLE_NAME = "REMINDER";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_FATHER = "father";
    public static final String COLUMN_NAME_WEEKDAY = "weekday";
  }

  private static class TaskTable implements BaseColumns {
    public static final String TABLE_NAME = "POMODORO_TASK";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_LOOPS = "loops";
    public static final String COLUMN_NAME_HISTORY = "history";
    public static final String COLUMN_NAME_IS_DONE = "done";
  }

  private static class StatsTable implements BaseColumns{
    private static final String TABLE_NAME = "TimerStats";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_STATE = "state";
    public static final String COLUMN_NAME_DURATION = "duration";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_MONTH = "month";
    public static final String COLUMN_NAME_YEAR = "year";
  }
  boolean todayStatsCreated = false;

  public ReminderTaskDB(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.context = context;

    Calendar calendar = Calendar.getInstance();
    date = calendar.get(Calendar.DATE);
    month = calendar.get(Calendar.MONTH);
    year = calendar.get(Calendar.YEAR);

  }



  @Override
  public void onCreate(SQLiteDatabase db) {
    String createReminderTable =
            "CREATE TABLE " + ReminderTable.TABLE_NAME + " ( "
                    + ReminderTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + ReminderTable.COLUMN_NAME_TITLE + " TEXT ,"
                    + ReminderTable.COLUMN_NAME_TIME + " INTEGER ,"
                    + ReminderTable.COLUMN_NAME_FATHER + " INTEGER ,"
                    + ReminderTable.COLUMN_NAME_WEEKDAY + " INTEGER ,"
                    + " FOREIGN KEY (" + ReminderTable.COLUMN_NAME_FATHER + ") REFERENCES "
                    + ReminderTable.TABLE_NAME + "( " + ReminderTable.COLUMN_NAME_ID + "));";
    String createTaskTable =
            "CREATE TABLE " + TaskTable.TABLE_NAME + " ( "
                    + TaskTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + TaskTable.COLUMN_NAME_TITLE + " TEXT ,"
                    + TaskTable.COLUMN_NAME_HISTORY + " INTEGER ,"
                    + TaskTable.COLUMN_NAME_IS_DONE + " INTEGER ,"
                    + TaskTable.COLUMN_NAME_LOOPS + " INTEGER );";
    String createStatsTable =
            "CREATE TABLE " +StatsTable.TABLE_NAME + " ( "
                    + StatsTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + StatsTable.COLUMN_NAME_DURATION + " INTEGER DEFAULT 0 ,"
                    + StatsTable.COLUMN_NAME_YEAR + " INTEGER ,"
                    + StatsTable.COLUMN_NAME_MONTH + " INTEGER ,"
                    + StatsTable.COLUMN_NAME_DATE + " INTEGER ,"
                    + StatsTable.COLUMN_NAME_STATE + " INTEGER );";
    db.execSQL(createReminderTable);
    db.execSQL(createTaskTable);
    db.execSQL(createStatsTable);
  }

  //Reminder
  public long addReminder(String name, long time) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      ContentValues cv = new ContentValues();

      cv.put(ReminderTable.COLUMN_NAME_TITLE, name);
      cv.put(ReminderTable.COLUMN_NAME_TIME, time);

      return db.insert(ReminderTable.TABLE_NAME, null, cv);
    } catch (Exception ignore) {
      return -1;
    }
  }

  public long addReminder(String name, long time,int weekDate) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      ContentValues cv = new ContentValues();

      cv.put(ReminderTable.COLUMN_NAME_TITLE, name);
      cv.put(ReminderTable.COLUMN_NAME_TIME, time);
      cv.put(ReminderTable.COLUMN_NAME_WEEKDAY,weekDate);

      return db.insert(ReminderTable.TABLE_NAME, null, cv);
    } catch (Exception ignore) {
      return -1;
    }
  }

  public long addChildReminder(String name,long time, int fatherId){
    try(SQLiteDatabase db = this.getWritableDatabase()){
      ContentValues cv = new ContentValues();
      cv.put(ReminderTable.COLUMN_NAME_TITLE,name);
      cv.put(ReminderTable.COLUMN_NAME_TIME,time);
      cv.put(ReminderTable.COLUMN_NAME_FATHER, fatherId);

      return db.insert(ReminderTable.TABLE_NAME,null,cv);
    }catch (Exception ignore){};
    return -1;
  }

  public List<ReminderData> getReminderAt(int date, int month, int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month, date, 0, 0, 0);
    long startDay = calendar.getTimeInMillis();
    calendar.set(year, month, date, 23, 59, 59);
    long endDay = calendar.getTimeInMillis();

    try (
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor query1 = db.query(ReminderTable.TABLE_NAME,
                    null,
                    ReminderTable.COLUMN_NAME_TIME + " BETWEEN ? AND ?",
                    new String[]{String.valueOf(startDay), String.valueOf(endDay)},
                    null,
                    null,
                    ReminderTable.COLUMN_NAME_TIME)
    ) {


      List<ReminderData> list = new ArrayList<ReminderData>();

      if (!query1.moveToFirst()) {
        return list;
      }

      int idIndex = query1.getColumnIndex(ReminderTable.COLUMN_NAME_ID);
      int nameIndex = query1.getColumnIndex(ReminderTable.COLUMN_NAME_TITLE);
      int timeIndex = query1.getColumnIndex(ReminderTable.COLUMN_NAME_TIME);

      do {
        int id = query1.getInt(idIndex);
        String name = query1.getString(nameIndex);
        long time = query1.getLong(timeIndex);

        list.add(new ReminderData(name, time, id));
      } while (query1.moveToNext());

      return list;
    } catch (Exception ignore) {
    }
    return null;
  }

  public long removeReminder(long id) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      return db.delete(
              ReminderTable.TABLE_NAME,
              ReminderTable.COLUMN_NAME_ID + " = ?",
              new String[]{String.valueOf(id)}
      );
    } catch (Exception ignore) {
      return -1;
    }
  }

  public @NonNull List<ReminderData> findReminders(String nameSearch, long startDate, long endDate) {
    if (startDate < 0 || endDate < 0) {
      return this.findReminders(nameSearch);
    }

    if (nameSearch == null || nameSearch.isEmpty()) {
      return this.findReminders(startDate, endDate);
    }
    ArrayList<ReminderData> list = new ArrayList<ReminderData>();

    try (
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(
                    ReminderTable.TABLE_NAME,
                    null,
                    "( " + ReminderTable.COLUMN_NAME_TIME + " BETWEEN ? AND ? ) AND ("
                            + ReminderTable.COLUMN_NAME_TITLE + " LIKE % ? % )",
                    new String[]{String.valueOf(startDate), String.valueOf(endDate), nameSearch},
                    null,
                    null,
                    ReminderTable.COLUMN_NAME_TIME)
    ) {


      if (!cursor.moveToFirst()) {
        return list;
      }

      int idIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_ID);
      int nameIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_TITLE);
      int timeIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_TIME);

      do {
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);

        list.add(new ReminderData(name, time, id));
      } while (cursor.moveToNext());
    } catch (Exception ignore) {
    }

    return list;
  }

  public @NonNull List<ReminderData> findReminders(long startDate, long endDate) {
    ArrayList<ReminderData> list = new ArrayList<ReminderData>();

    try (
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(
                    ReminderTable.TABLE_NAME,
                    null,
                    ReminderTable.COLUMN_NAME_TIME + " BETWEEN ? AND ?",
                    new String[]{String.valueOf(startDate), String.valueOf(endDate)},
                    null,
                    null,
                    ReminderTable.COLUMN_NAME_TIME)
    ) {
      if (!cursor.moveToFirst()) {
        return list;
      }

      int idIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_ID);
      int nameIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_TITLE);
      int timeIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_TIME);

      do {
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);

        list.add(new ReminderData(name, time, id));
      } while (cursor.moveToNext());
    } catch (Exception ignore) {
    }

    return list;
  }

  public @NonNull List<ReminderData> findReminders(String searchName) {
    ArrayList<ReminderData> list = new ArrayList<ReminderData>();

    try (
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(
                    ReminderTable.TABLE_NAME,
                    null,
                    ReminderTable.COLUMN_NAME_TITLE + " LIKE ?",
                    new String[]{"%" + searchName + "%"},
                    null,
                    null,
                    ReminderTable.COLUMN_NAME_TIME)
    ) {
      if (!cursor.moveToFirst()) {
        return list;
      }

      int idIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_ID);
      int nameIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_TITLE);
      int timeIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_TIME);

      do {
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);

        list.add(new ReminderData(name, time, id));
      } while (cursor.moveToNext());
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
    return list;
  }
  //===

  //Task
  public long addTask(String name, int loops,boolean isDone) {
    try (SQLiteDatabase db = getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_TITLE, name);
      cv.put(TaskTable.COLUMN_NAME_LOOPS, loops);
      cv.put(TaskTable.COLUMN_NAME_IS_DONE, isDone ? 1 : 0);
      return db.insert(TaskTable.TABLE_NAME, null, cv);
    } catch (Exception ignore) {
    }
    return -1;
  }

  public List<TaskData> getAllTask() {
    ArrayList<TaskData> list = new ArrayList<TaskData>();
    try (
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(
                    TaskTable.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
    ) {
      if (!cursor.moveToFirst()) {
        return list;
      }
      int titleIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_TITLE);
      int idIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_ID);
      int loopIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_LOOPS);
      int completeIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_IS_DONE);
      do {
        String name = cursor.getString(titleIndex);
        int loops = cursor.getInt(loopIndex);
        int id = cursor.getInt(idIndex);
        boolean isComplete = cursor.getInt(completeIndex) > 0;
        list.add(new TaskData(name, loops, id,isComplete));
      } while (cursor.moveToNext());
    } catch (Exception ignore) {
    }
    return list;
  }

  public boolean editTask(TaskData newData){
    try(SQLiteDatabase db = getWritableDatabase()){
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_LOOPS,newData.numberPomodoros);
      cv.put(TaskTable.COLUMN_NAME_TITLE,newData.taskName);
      cv.put(TaskTable.COLUMN_NAME_IS_DONE,newData.isCompleted  ? 1 : 0);
      long updated = db.update(
              TaskTable.TABLE_NAME,
              cv,
              TaskTable.COLUMN_NAME_ID + " =?",
              new String[]{String.valueOf(newData.id)}
      );

      return updated > 0;
    }catch (Exception ignore){}
    return false;
  }

  public boolean deleteTask(int id){
    try(
            SQLiteDatabase db = getWritableDatabase();
    ){
      long delNum = db.delete(
              TaskTable.TABLE_NAME,
              ReminderTable.COLUMN_NAME_ID + " = ?",
              new String[]{String.valueOf(id)}
      );
      return (delNum == 1);
    }catch (Exception ignore){}
    return false;
  }

  public boolean makeTaskHistory(int id){
    try(SQLiteDatabase db = this.getWritableDatabase()){
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_HISTORY,1);
      long update = db.update(
              TaskTable.TABLE_NAME,
              cv,
              TaskTable.COLUMN_NAME_ID + "=?",
              new String[]{String.valueOf(id)}
      );
      return update > 0;
    }catch (Exception ignore){
    }
    return false;
  }
  //===

  //Stats
  private synchronized boolean isTodayCorrect(){
    Calendar calendar = Calendar.getInstance();
    int todayDate = calendar.get(Calendar.DATE);
    int todayMonth = calendar.get(Calendar.MONTH);
    int todayYear = calendar.get(Calendar.YEAR);

    if(todayDate != date || todayMonth != month || todayYear != year){
      date = todayDate;
      month = todayMonth;
      year = todayYear;
      return false;
    }
    return true;

  }
  private synchronized boolean createTodayStats(){
    isTodayCorrect();

    try(SQLiteDatabase db = getWritableDatabase()){
      ContentValues workCv = new ContentValues();
      workCv.put(StatsTable.COLUMN_NAME_DATE,date);
      workCv.put(StatsTable.COLUMN_NAME_MONTH,month);
      workCv.put(StatsTable.COLUMN_NAME_YEAR,year);
      workCv.put(StatsTable.COLUMN_NAME_DURATION,0);
      workCv.put(StatsTable.COLUMN_NAME_STATE,TimerService.WORK_STATE);

      ContentValues shortCv = new ContentValues();
      shortCv.put(StatsTable.COLUMN_NAME_DATE,date);
      shortCv.put(StatsTable.COLUMN_NAME_MONTH,month);
      shortCv.put(StatsTable.COLUMN_NAME_YEAR,year);
      shortCv.put(StatsTable.COLUMN_NAME_DURATION,0);
      shortCv.put(StatsTable.COLUMN_NAME_STATE,TimerService.SHORT_BREAK_STATE);

      ContentValues longCv = new ContentValues();
      longCv.put(StatsTable.COLUMN_NAME_DATE,date);
      longCv.put(StatsTable.COLUMN_NAME_MONTH,month);
      longCv.put(StatsTable.COLUMN_NAME_YEAR,year);
      longCv.put(StatsTable.COLUMN_NAME_DURATION,0);
      longCv.put(StatsTable.COLUMN_NAME_STATE,TimerService.LONG_BREAK_STATE);

      return (
              db.insert(StatsTable.TABLE_NAME,null,workCv)
              * db.insert(StatsTable.TABLE_NAME,null,shortCv)
              * db.insert(StatsTable.TABLE_NAME,null,longCv)
      ) >= 0;

    }catch (Exception ignore){}
    return false;
  }
  private synchronized boolean replaceTodayStats(long time,int state){
    if(state != TimerService.WORK_STATE && state != TimerService.SHORT_BREAK_STATE && state != TimerService.LONG_BREAK_STATE){
      return false;
    }

    if(!isTodayCorrect()){
      getTodayStats();
    }

    try(SQLiteDatabase db = getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(StatsTable.COLUMN_NAME_DURATION,time);

      int result = db.update(
              StatsTable.TABLE_NAME,
              cv,
              StatsTable.COLUMN_NAME_DATE + " = ? AND "
                      + StatsTable.COLUMN_NAME_MONTH + " = ? AND "
                      + StatsTable.COLUMN_NAME_YEAR + " = ? AND "
                      + StatsTable.COLUMN_NAME_STATE + " = ?",
              new String[]{String.valueOf(date), String.valueOf(month), String.valueOf(year), String.valueOf(state)}
      );
      return result > 0;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }
  public synchronized boolean getTodayStats(){
    isTodayCorrect();
    try(
            SQLiteDatabase db = getReadableDatabase();
            Cursor query = db.query(
                    StatsTable.TABLE_NAME,
                    null,
                    StatsTable.COLUMN_NAME_DATE + " = ? AND "
                            + StatsTable.COLUMN_NAME_MONTH + " = ? AND "
                            + StatsTable.COLUMN_NAME_YEAR + " = ?",
                    new String[]{String.valueOf(date), String.valueOf(month), String.valueOf(year)},
                    null,
                    null,
                    null
            );
    ){
      if(!query.moveToFirst()){
        createTodayStats();
        return false;
      }

      int indexDuration = query.getColumnIndex(StatsTable.COLUMN_NAME_DURATION);
      int indexState = query.getColumnIndex(StatsTable.COLUMN_NAME_STATE);
      do{
        switch (query.getInt(indexState)){
          case TimerService.WORK_STATE:{
            curWork = query.getLong(indexDuration);
            break;
          }
          case TimerService.SHORT_BREAK_STATE:{
            curShort = query.getLong(indexDuration);
            break;
          }
          case TimerService.LONG_BREAK_STATE:{
            curLong = query.getLong(indexDuration);
            break;
          }
        }
      }while (query.moveToNext());
      return true;
    }catch (Exception ignore){}
    createTodayStats();
    return false;

  }

  public synchronized boolean addTimeTodayStats(long workTime,long shortTime,long longTime){
    if(workTime != 0){
      if(replaceTodayStats(curWork + workTime, TimerService.WORK_STATE)){
        curWork += workTime;
      }else{
        return false;
      }
    }

    if(shortTime != 0){
      if(replaceTodayStats(curShort + shortTime,TimerService.SHORT_BREAK_STATE)){
        curShort += shortTime;
      }else {
        return false;
      }
    }

    if(longTime != 0){
      if(replaceTodayStats(curLong + longTime,TimerService.LONG_BREAK_STATE)){
        curLong += longTime;
      }else {
        return false;
      }
    }

    return true;
  }
  //===

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    try {
      db.execSQL(SQL_DROP_REMINDER_TABLE);
      db.execSQL(SQL_DROP_TASK_TABLE);
      db.execSQL(SQL_DROP_STATS_TABLE);
    }catch (Exception e){
      e.printStackTrace();
    }

    onCreate(db);
  }
}
