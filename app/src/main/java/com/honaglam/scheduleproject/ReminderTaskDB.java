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
  private static final int DB_VERSION = 2;
  private static final String DB_NAME = "ScheduleProject.db";
  private static final String SQL_DROP_REMINDER_TABLE = "DROP TABLE IF EXISTS " + ReminderTable.TABLE_NAME;
  private static final String SQL_DROP_TASK_TABLE = "DROP TABLE IF EXISTS " + TaskTable.TABLE_NAME;
  private Context context;

  private static class ReminderTable implements BaseColumns {
    public static final String TABLE_NAME = "REMINDER";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_FATHER = "father";
  }

  private static class TaskTable implements BaseColumns {
    public static final String TABLE_NAME = "POMODORO_TASK";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_LOOPS = "loops";
  }


  public ReminderTaskDB(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String createReminderTable =
            "CREATE TABLE " + ReminderTable.TABLE_NAME + " ( "
                    + ReminderTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + ReminderTable.COLUMN_NAME_TITLE + " TEXT ,"
                    + ReminderTable.COLUMN_NAME_TIME + " INTEGER ,"
                    + ReminderTable.COLUMN_NAME_FATHER + " INTEGER ,"
                    + " FOREIGN KEY (" + ReminderTable.COLUMN_NAME_FATHER + ") REFERENCES "
                    + ReminderTable.TABLE_NAME + "( " + ReminderTable.COLUMN_NAME_ID + "));";
    String createTaskTable =
            "CREATE TABLE " + TaskTable.TABLE_NAME + " ( "
                    + TaskTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + TaskTable.COLUMN_NAME_TITLE + " TEXT ,"
                    + TaskTable.COLUMN_NAME_LOOPS + " INTEGER );";
    db.execSQL(createReminderTable);
    db.execSQL(createTaskTable);
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
  public long addTask(String name, int loops) {
    try (SQLiteDatabase db = getWritableDatabase()) {
      ContentValues cv = new ContentValues();
      cv.put(TaskTable.COLUMN_NAME_TITLE, name);
      cv.put(TaskTable.COLUMN_NAME_LOOPS, loops);
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
      do {
        String name = cursor.getString(titleIndex);
        int loops = cursor.getInt(loopIndex);
        int id = cursor.getInt(idIndex);
        list.add(new TaskData(name, loops, id));
      } while (cursor.moveToNext());
    } catch (Exception ignore) {
    }
    return list;
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
  //===
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DROP_REMINDER_TABLE);
    db.execSQL(SQL_DROP_TASK_TABLE);
    onCreate(db);
  }
}
