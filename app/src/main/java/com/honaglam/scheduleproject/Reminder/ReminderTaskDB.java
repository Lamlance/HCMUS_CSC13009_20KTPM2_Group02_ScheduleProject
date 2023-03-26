package com.honaglam.scheduleproject.Reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderTaskDB extends SQLiteOpenHelper {
  private static final int DB_VERSION = 1;
  private static final String DB_NAME = "ScheduleProject.db";
  private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ReminderTable.TABLE_NAME;
  private Context context;

  public static class ReminderTable implements BaseColumns {
    public static final String TABLE_NAME = "REMINDER";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_TIME = "time";
  }


  public ReminderTaskDB(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.context = context;

  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String createTableStatement =
            "CREATE TABLE " + ReminderTable.TABLE_NAME + " ( "
                    + ReminderTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + ReminderTable.COLUMN_NAME_TITLE + " TEXT ,"
                    + ReminderTable.COLUMN_NAME_TIME + " INTEGER)";
    db.execSQL(createTableStatement);
  }

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
      /*String query =
              "SELECT * FROM " + ReminderTable.TABLE_NAME
                      + " WHERE " + ReminderTable.COLUMN_NAME_TIME + " BETWEEN " + startDay + " AND " + endDay;*/

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


  public @NonNull List<ReminderData> findReminders(String nameSearch, long startDate, long endDate)  {
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
                    new String[]{"%"+searchName+"%"},
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

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
  }
}
