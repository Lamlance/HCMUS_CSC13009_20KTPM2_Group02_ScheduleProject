package com.honaglam.scheduleproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Task.TaskData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReminderTaskDB extends SQLiteOpenHelper {
  private static final int DB_VERSION = 27;
  public static final boolean IS_DEV = true;
  private static final String DB_NAME = "ScheduleProject.db";
  private static final String SQL_DROP_REMINDER_TABLE = "DROP TABLE IF EXISTS " + ReminderTable.TABLE_NAME;
  private static final String SQL_DROP_TASK_TABLE = "DROP TABLE IF EXISTS " + TaskTable.TABLE_NAME;
  private static final String SQL_DROP_STATS_TABLE = "DROP TABLE IF EXISTS " + StatsTable.TABLE_NAME;
  private static final String SQL_DROP_TASK_REMINDER_TABLE = "DROP TABLE IF EXISTS " + TaskReminderTable.TABLE_NAME;

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
    public static final String COLUMN_NAME_LOOPS_DONE = "loops_done";
  }

  private static class StatsTable implements BaseColumns {
    private static final String TABLE_NAME = "TimerStats";
    public static final String COLUMN_NAME_WORK_DURATION = "work_duration";
    public static final String COLUMN_NAME_SHORT_DURATION = "short_duration";
    public static final String COLUMN_NAME_LONG_DURATION = "long_duration";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_MONTH = "month";
    public static final String COLUMN_NAME_YEAR = "year";
  }

  private static class TaskReminderTable implements BaseColumns{
    public static final String TABLE_NAME = "TaskReminder";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_REMINDER_ID = "reminder_id";
    public static final String COLUMN_NAME_TASK_ID = "task_id";
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
                    + TaskTable.COLUMN_NAME_LOOPS + " INTEGER ,"
                    + TaskTable.COLUMN_NAME_LOOPS_DONE + " INTEGER DEFAULT 0);";
    String createStatsTable =
            "CREATE TABLE " + StatsTable.TABLE_NAME + " ( "
                    + StatsTable.COLUMN_NAME_WORK_DURATION + " INTEGER DEFAULT 0 ,"
                    + StatsTable.COLUMN_NAME_SHORT_DURATION + " INTEGER DEFAULT 0 ,"
                    + StatsTable.COLUMN_NAME_LONG_DURATION + " INTEGER DEFAULT 0 ,"
                    + StatsTable.COLUMN_NAME_YEAR + " INTEGER NOT NULL,"
                    + StatsTable.COLUMN_NAME_MONTH + " INTEGER NOT NULL,"
                    + StatsTable.COLUMN_NAME_DATE + " INTEGER NOT NULL,"
                    + "PRIMARY KEY("
                    + StatsTable.COLUMN_NAME_DATE + ","
                    + StatsTable.COLUMN_NAME_MONTH + ","
                    + StatsTable.COLUMN_NAME_YEAR + "));";
    String createTaskReminder =
            "CREATE TABLE " + TaskReminderTable.TABLE_NAME + " ( "
                    + TaskReminderTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TaskReminderTable.COLUMN_NAME_REMINDER_ID + " INTEGER ,"
                    + TaskReminderTable.COLUMN_NAME_TASK_ID + " INTEGER ,"
                    + "FOREIGN KEY ( " + TaskReminderTable.COLUMN_NAME_REMINDER_ID + " ) "
                    + "REFERENCES " + ReminderTable.TABLE_NAME + "( " + ReminderTable.COLUMN_NAME_ID + " ),"
                    + "FOREIGN KEY ( " + TaskReminderTable.COLUMN_NAME_TASK_ID + " ) "
                    + "REFERENCES " + TaskTable.TABLE_NAME + "( " + TaskTable.COLUMN_NAME_ID + " ));";
    db.execSQL(createReminderTable);
    db.execSQL(createTaskTable);
    db.execSQL(createStatsTable);
    db.execSQL(createTaskReminder);
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

  public long addReminder(String name, long time, int weekDate) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      ContentValues cv = new ContentValues();

      cv.put(ReminderTable.COLUMN_NAME_TITLE, name);
      cv.put(ReminderTable.COLUMN_NAME_TIME, time);
      cv.put(ReminderTable.COLUMN_NAME_WEEKDAY, weekDate);

      return db.insert(ReminderTable.TABLE_NAME, null, cv);
    } catch (Exception ignore) {
      return -1;
    }
  }


  public long addChildReminder(String name, long time, int fatherId) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(ReminderTable.COLUMN_NAME_TITLE, name);
      cv.put(ReminderTable.COLUMN_NAME_TIME, time);
      cv.put(ReminderTable.COLUMN_NAME_FATHER, fatherId);

      return db.insert(ReminderTable.TABLE_NAME, null, cv);
    } catch (Exception ignore) {
    }
    ;
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
                            + ReminderTable.COLUMN_NAME_TITLE + " LIKE ? )",
                    new String[]{String.valueOf(startDate), String.valueOf(endDate), "%" + nameSearch + "%"},
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
      int weekDateIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_WEEKDAY);

      do {
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);
          int weekDate = cursor.isNull(weekDateIndex) ? -1 : cursor.getInt(weekDateIndex);

        list.add(new ReminderData(name, time, id, weekDate));
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
      int weekDateIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_WEEKDAY);

      do {
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);
          int weekDate = cursor.isNull(weekDateIndex) ? -1 : cursor.getInt(weekDateIndex);

        list.add(new ReminderData(name, time, id,weekDate));
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
      int weekDateIndex = cursor.getColumnIndex(ReminderTable.COLUMN_NAME_WEEKDAY);

      do {
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);
        int weekDate = cursor.isNull(weekDateIndex) ? -1 : cursor.getInt(weekDateIndex);

        list.add(new ReminderData(name, time, id,weekDate));
      } while (cursor.moveToNext());
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
    return list;
  }
  //===

  //Task
  public int bindTaskToReminder(int reminderId,List<Integer> taskIdList){
    try(SQLiteDatabase db =getWritableDatabase()){
      int insertedCount = 0;
      for(Integer taskId : taskIdList){
        ContentValues cv = new ContentValues();
        cv.put(TaskReminderTable.COLUMN_NAME_REMINDER_ID,reminderId);
        cv.put(TaskReminderTable.COLUMN_NAME_TASK_ID,taskId);

        long result = db.insert(TaskReminderTable.TABLE_NAME,null,cv);
        insertedCount += (result >= 0) ? 1 : 0;
      }
      return insertedCount;
    }catch (Exception e){
      e.printStackTrace();
    }
    return -1;
  }

  private List<TaskData> getAllReminderTaskInDate(int year,int month,int date){
    List<TaskData> taskDataList = new ArrayList<TaskData>();


    List<ReminderData> reminderData = getReminderAt(date,month,year);
    Map<Integer,List<ReminderData>> reminderDataIdMap = reminderData.stream()
            .collect(Collectors.groupingBy(t->t.id));

    try(SQLiteDatabase db = getReadableDatabase()){
      List<Integer> reminderIds = reminderData.stream().map(d->d.id).collect(Collectors.toList());

      String reminderIdArg = "";
      for (int reminderId:reminderIds) {
        reminderIdArg += reminderId + ",";
      }
      if(reminderIdArg.length() >= 2){
        reminderIdArg = reminderIdArg.substring(0,reminderIdArg.length() - 1) ;
      }else{
        reminderIdArg = "";
      }

      try(Cursor cursor = db.rawQuery(
              "SELECT "
                      + TaskTable.TABLE_NAME + ".* , " + TaskReminderTable.COLUMN_NAME_REMINDER_ID
                      + " FROM " + TaskTable.TABLE_NAME
                      + " JOIN " + TaskReminderTable.TABLE_NAME
                      + " ON " + TaskTable.TABLE_NAME + "." + TaskTable.COLUMN_NAME_ID + " = "
                      + TaskReminderTable.TABLE_NAME + "." + TaskReminderTable.COLUMN_NAME_TASK_ID
                      + " WHERE " + TaskReminderTable.TABLE_NAME + "." + TaskReminderTable.COLUMN_NAME_REMINDER_ID
                      + " IN (" + reminderIdArg + ")",null
      )){
        if(cursor.moveToFirst()){
          int taskTitleIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_TITLE);
          int taskIdIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_ID);
          int taskLoopsIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_LOOPS);
          int taskLoopDoneIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_LOOPS_DONE);
          int reminderIdIndex = cursor.getColumnIndex(TaskReminderTable.COLUMN_NAME_REMINDER_ID);

          do{
            String title = cursor.getString(taskTitleIndex);
            int loops = cursor.getInt(taskLoopsIndex);
            int loopsDone = cursor.getInt(taskLoopDoneIndex);
            int taskId = cursor.getInt(taskIdIndex);
            int reminderId = cursor.getInt(reminderIdIndex);

            ReminderData reminder = reminderDataIdMap.get(reminderId).get(0);
            TaskData task = new TaskData(title,loops,loopsDone,taskId,false);
            task.reminderData = reminder;
            taskDataList.add(task);
          }while (cursor.moveToNext());
        }
      }catch (Exception e){
        e.printStackTrace();
      }

    } catch (Exception e){
      e.printStackTrace();
    }
    return taskDataList;
  }

  private List<TaskData> getNotRemindAllTask() {
    ArrayList<TaskData> list = new ArrayList<TaskData>();
    try (
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT " + TaskTable.TABLE_NAME + ".* "
                            + "FROM " + TaskTable.TABLE_NAME
                            + " LEFT JOIN " + TaskReminderTable.TABLE_NAME
                            + " ON " + TaskTable.TABLE_NAME + "." + TaskTable.COLUMN_NAME_ID
                            + " = " + TaskReminderTable.TABLE_NAME + "." + TaskReminderTable.COLUMN_NAME_TASK_ID
                            + " WHERE " + TaskReminderTable.TABLE_NAME + "." + TaskReminderTable.COLUMN_NAME_TASK_ID + " IS NULL"
                    ,null
            );
    ) {
      if (!cursor.moveToFirst()) {
        return list;
      }
      int titleIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_TITLE);
      int idIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_ID);
      int loopIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_LOOPS);
      int loopCompletedIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_LOOPS_DONE);
      int completeIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_IS_DONE);
      do {
        String name = cursor.getString(titleIndex);
        int loops = cursor.getInt(loopIndex);
        int loopsCompleted = cursor.getInt(loopCompletedIndex);
        int id = cursor.getInt(idIndex);
        boolean isComplete = cursor.getInt(completeIndex) > 0;
        list.add(new TaskData(name, loops, loopsCompleted, id, isComplete));
      } while (cursor.moveToNext());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  public Map<ReminderData,List<TaskData>> getReminderTaskMapByReminder(int year,int month,int date){
    List<TaskData> notRemindTask = getNotRemindAllTask();
    List<TaskData> remindTask = getAllReminderTaskInDate(year,month,date);

    LinkedList<TaskData> fullTaskList = Stream.concat(notRemindTask.stream(), remindTask.stream())
            .collect(Collectors.toCollection(LinkedList::new));

    return fullTaskList.stream().collect(Collectors.groupingBy(t->t.reminderData));
  }

  public long addTask(String name, int loops, int loopsCompleted, boolean isDone) {
    try (SQLiteDatabase db = getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_TITLE, name);
      cv.put(TaskTable.COLUMN_NAME_LOOPS, loops);
      cv.put(TaskTable.COLUMN_NAME_IS_DONE, isDone ? 1 : 0);
      cv.put(TaskTable.COLUMN_NAME_LOOPS_DONE, loopsCompleted);

      return db.insert(TaskTable.TABLE_NAME, null, cv);
    } catch (Exception ignore) {
    }
    return -1;
  }


  public boolean editTask(TaskData newData) {
    try (SQLiteDatabase db = getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_LOOPS, newData.numberPomodoros);
      cv.put(TaskTable.COLUMN_NAME_TITLE, newData.taskName);
      cv.put(TaskTable.COLUMN_NAME_IS_DONE, newData.isCompleted ? 1 : 0);
      cv.put(TaskTable.COLUMN_NAME_LOOPS_DONE, newData.numberCompletedPomodoros);
      long updated = db.update(
              TaskTable.TABLE_NAME,
              cv,
              TaskTable.COLUMN_NAME_ID + " =?",
              new String[]{String.valueOf(newData.id)}
      );

      return updated > 0;
    } catch (Exception ignore) {
    }
    return false;
  }

  public boolean deleteTask(int id) {
    try (
            SQLiteDatabase db = getWritableDatabase();
    ) {
      long delNum = db.delete(
              TaskTable.TABLE_NAME,
              ReminderTable.COLUMN_NAME_ID + " = ?",
              new String[]{String.valueOf(id)}
      );
      return (delNum == 1);
    } catch (Exception ignore) {
    }
    return false;
  }

  public boolean makeTaskHistory(int id) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_HISTORY, 1);
      long update = db.update(
              TaskTable.TABLE_NAME,
              cv,
              TaskTable.COLUMN_NAME_ID + "=?",
              new String[]{String.valueOf(id)}
      );
      return update > 0;
    } catch (Exception ignore) {
    }
    return false;
  }

  // TODO: implement makeTaskToToDo
  public boolean makeTaskToToDo(int id) {
    try (SQLiteDatabase db = this.getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_HISTORY, 0);
      long update = db.update(
              TaskTable.TABLE_NAME,
              cv,
              TaskTable.COLUMN_NAME_ID + "=?",
              new String[]{String.valueOf(id)}
      );
      return update > 0;
    } catch (Exception ignore) {
    }
    return false;
  }

  public List<TaskData> getHistoryTask() {
    ArrayList<TaskData> list = new ArrayList<TaskData>();
    try (
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(
                    TaskTable.TABLE_NAME,
                    null,
                    TaskTable.COLUMN_NAME_HISTORY + " > ? ",
                    new String[]{"0"},
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
      int loopCompletedIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_LOOPS_DONE);
      int completeIndex = cursor.getColumnIndex(TaskTable.COLUMN_NAME_IS_DONE);
      do {
        String name = cursor.getString(titleIndex);
        int loops = cursor.getInt(loopIndex);
        int loopsCompleted = cursor.getInt(loopCompletedIndex);
        int id = cursor.getInt(idIndex);
        boolean isComplete = cursor.getInt(completeIndex) > 0;
        list.add(new TaskData(name, loops, loopsCompleted, id, isComplete));
      } while (cursor.moveToNext());
      Log.i("HISTORY", "Find " + cursor.getCount());
    } catch (Exception ignore) {
    }
    return list;
  }
  //===

  //Stats
  private synchronized boolean isTodayCorrect() {
    Calendar calendar = Calendar.getInstance();
    int todayDate = calendar.get(Calendar.DATE);
    int todayMonth = calendar.get(Calendar.MONTH);
    int todayYear = calendar.get(Calendar.YEAR);

    if (todayDate != date || todayMonth != month || todayYear != year) {
      date = todayDate;
      month = todayMonth;
      year = todayYear;
      return false;
    }
    return true;

  }

  private synchronized boolean createTodayStats() {
    isTodayCorrect();

    try (SQLiteDatabase db = getWritableDatabase()) {
      ContentValues workCv = new ContentValues();
      workCv.put(StatsTable.COLUMN_NAME_DATE, date);
      workCv.put(StatsTable.COLUMN_NAME_MONTH, month);
      workCv.put(StatsTable.COLUMN_NAME_YEAR, year);
      workCv.put(StatsTable.COLUMN_NAME_WORK_DURATION, 0);
      workCv.put(StatsTable.COLUMN_NAME_SHORT_DURATION, 0);
      workCv.put(StatsTable.COLUMN_NAME_LONG_DURATION, 0);


      long res1 = db.insert(StatsTable.TABLE_NAME, null, workCv);
      return res1 >= 0;

    } catch (Exception ignore) {
    }
    return false;
  }

  private synchronized boolean replaceTodayStats(long time, int state) {
    if (state != TimerService.WORK_STATE && state != TimerService.SHORT_BREAK_STATE && state != TimerService.LONG_BREAK_STATE) {
      return false;
    }

    if (!isTodayCorrect()) {
      getTodayStats();
    }

    try (SQLiteDatabase db = getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      if (state == TimerService.WORK_STATE) {
        cv.put(StatsTable.COLUMN_NAME_WORK_DURATION, time);
      } else if (state == TimerService.SHORT_BREAK_STATE) {
        cv.put(StatsTable.COLUMN_NAME_SHORT_DURATION, time);
      } else {
        cv.put(StatsTable.COLUMN_NAME_LONG_DURATION, time);
      }

      int result = db.update(
              StatsTable.TABLE_NAME,
              cv,
              StatsTable.COLUMN_NAME_DATE + " = ? AND "
                      + StatsTable.COLUMN_NAME_MONTH + " = ? AND "
                      + StatsTable.COLUMN_NAME_YEAR + " = ?",
              new String[]{String.valueOf(date), String.valueOf(month), String.valueOf(year)}
      );
      return result > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public synchronized boolean getTodayStats() {
    isTodayCorrect();
    try (
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
    ) {
      if (!query.moveToFirst()) {
        createTodayStats();
        return false;
      }
      int workIndex = query.getColumnIndex(StatsTable.COLUMN_NAME_WORK_DURATION);
      curWork = workIndex >= 0 ? query.getLong(workIndex) : curWork;

      int shortIndex = query.getColumnIndex(StatsTable.COLUMN_NAME_SHORT_DURATION);
      curShort = shortIndex >= 0 ? query.getLong(shortIndex) : curShort;

      int longIndex = query.getColumnIndex(StatsTable.COLUMN_NAME_LONG_DURATION);
      curLong = longIndex >= 0 ? query.getLong(longIndex) : curLong;

      return query.getCount() == 3;
    } catch (Exception ignore) {
    }
    createTodayStats();
    return false;

  }

  public synchronized boolean addTimeTodayStats(long workTime, long shortTime, long longTime) {
    if (workTime != 0) {
      if (replaceTodayStats(curWork + workTime, TimerService.WORK_STATE)) {
        curWork += workTime;
      } else {
        return false;
      }
    }

    if (shortTime != 0) {
      if (replaceTodayStats(curShort + shortTime, TimerService.SHORT_BREAK_STATE)) {
        curShort += shortTime;
      } else {
        return false;
      }
    }

    if (longTime != 0) {
      if (replaceTodayStats(curLong + longTime, TimerService.LONG_BREAK_STATE)) {
        curLong += longTime;
      } else {
        return false;
      }
    }

    return true;
  }

  public static class TimerStatsData {
    public int date;
    public int month;

    public int year;
    public long workDur;
    public long shortDur;
    public long longDur;


    TimerStatsData(int date, int month, int year, long workDur, long shortDur, long longDur) {
      this.date = date;
      this.month = month;
      this.year = year;
      this.workDur = workDur;
      this.shortDur = shortDur;
      this.longDur = longDur;
    }

    public int getDate(){
      return this.date;
    }
    public int getMonth(){
      return this.month;
    }

    @NonNull
    @Override
    public String toString() {
      return "TimerStatsData{" +
              "date=" + date +
              ", month=" + month +
              ", year=" + year +
              ", workDur=" + workDur +
              ", shortDur=" + shortDur +
              ", longDur=" + longDur +
              '}';
    }
  }

  public List<TimerStatsData> get30StatsBeforeToday() {
    ArrayList<TimerStatsData> list = new ArrayList<>();

    if (!isTodayCorrect()) {
      getTodayStats();
    }

    try (SQLiteDatabase db = getReadableDatabase()) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(year, month, date, 0, 0, 0);
      calendar.add(Calendar.DATE, -29);
      int pastDate = calendar.get(Calendar.DATE);
      int pastMonth = calendar.get(Calendar.MONTH);
      int pastYear = calendar.get(Calendar.YEAR);

      String selection = "? BETWEEN '"
              + pastYear + "-" + pastMonth + "-" + pastDate + "' AND '"
              + year + "-" + month + "-" + date + "'";
      String query = "SELECT * , date(substr('0000' || year,-4,4) || '-' || substr('00' || month,-2,2) || '-' || substr('00' || date,-2,2)) AS col_date FROM " + StatsTable.TABLE_NAME
              + " WHERE col_date BETWEEN "
              + " date(substr('0000' || ?,-4,4) || '-' || substr('00' || ?,-2,2) || '-' || substr('00' || ?,-2,2)) AND date(substr('0000' || ?,-4,4) || '-' || substr('00' || ?,-2,2) || '-' || substr('00' || ?,-2,2))"
              + " ORDER BY col_date DESC ";

      try (
              Cursor cursor = db.rawQuery(query, new String[]{
                      String.valueOf(pastYear), String.valueOf(pastMonth), String.valueOf(pastDate),
                      String.valueOf(year), String.valueOf(month), String.valueOf(date)
              });
      ) {
        if (!cursor.moveToFirst()) {
          return list;
        }
        int dateIndex = cursor.getColumnIndex(StatsTable.COLUMN_NAME_DATE);
        int monthIndex = cursor.getColumnIndex(StatsTable.COLUMN_NAME_MONTH);
        int yearIndex = cursor.getColumnIndex(StatsTable.COLUMN_NAME_YEAR);
        int workIndex = cursor.getColumnIndex(StatsTable.COLUMN_NAME_WORK_DURATION);
        int shortIndex = cursor.getColumnIndex(StatsTable.COLUMN_NAME_SHORT_DURATION);
        int longIndex = cursor.getColumnIndex(StatsTable.COLUMN_NAME_LONG_DURATION);

        do {
          list.add(new TimerStatsData(
                  cursor.getInt(dateIndex),
                  cursor.getInt(monthIndex),
                  cursor.getInt(yearIndex),
                  cursor.getLong(workIndex),
                  cursor.getLong(shortIndex),
                  cursor.getLong(longIndex)
          ));
        } while (cursor.moveToNext());

      } catch (Exception e) {
        e.printStackTrace();
      }
    } catch (Exception ignore) {
    }

    return list;
  }
  //===



  public void createSampleData() {
    Calendar calendar = Calendar.getInstance();

    try (SQLiteDatabase db = getWritableDatabase()) {
      for (int i = 1; i < 40; i++) {
        calendar.add(Calendar.DATE, -1);

        int date = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        Log.i("SAMPLE_DATA", String.valueOf(date) + "/" + month + "/" + year);

        ContentValues workCv = new ContentValues();
        workCv.put(StatsTable.COLUMN_NAME_DATE, date);
        workCv.put(StatsTable.COLUMN_NAME_MONTH, month);
        workCv.put(StatsTable.COLUMN_NAME_YEAR, year);
        workCv.put(StatsTable.COLUMN_NAME_WORK_DURATION, (long) ((Math.random() * 60) + 10) * 1000 * 60);
        workCv.put(StatsTable.COLUMN_NAME_SHORT_DURATION, (long) ((Math.random() * 60) + 10) * 1000 * 60);
        workCv.put(StatsTable.COLUMN_NAME_LONG_DURATION, (long) ((Math.random() * 60) + 10) * 1000 * 60);

        try {
          db.insert(StatsTable.TABLE_NAME, null, workCv);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception ignore) {
    }


  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    try {
      db.execSQL(SQL_DROP_REMINDER_TABLE);
      db.execSQL(SQL_DROP_TASK_TABLE);
      db.execSQL(SQL_DROP_STATS_TABLE);
      db.execSQL(SQL_DROP_TASK_REMINDER_TABLE);
    } catch (Exception e) {
      e.printStackTrace();
    }

    onCreate(db);
  }
}
