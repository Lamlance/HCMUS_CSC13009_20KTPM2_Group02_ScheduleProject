package com.honaglam.scheduleproject.Reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderTaskDB extends SQLiteOpenHelper {
  private static final String DB_NAME = "ScheduleProject.db";
  private static final Locale locale = Locale.getDefault();
  private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ReminderTable.TABLE_NAME;
  private Context context;
  public static class ReminderTable implements BaseColumns {
    public static final String TABLE_NAME = "REMINDER";
    public static final String COLUMN_NAME_ID= "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_TIME = "time";
  }



  public ReminderTaskDB(@Nullable Context context) {
    super(context,DB_NAME, null, 1);
    this.context = context;

  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String createTableStatement =
            "CREATE TABLE " + ReminderTable.TABLE_NAME + " ( "
                    + ReminderTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + ReminderTable.COLUMN_NAME_TITLE + " TEXT ,"
                    + ReminderTable.COLUMN_NAME_TIME + " INTEGER )";
    db.execSQL(createTableStatement);
  }

  public boolean addReminder (ReminderData reminderData){
    try(SQLiteDatabase db = this.getWritableDatabase()){
      ContentValues cv = new ContentValues();

      cv.put(ReminderTable.COLUMN_NAME_TITLE,reminderData.Name);
      cv.put(ReminderTable.COLUMN_NAME_TIME, reminderData.RemindTime);

      db.insert(ReminderTable.TABLE_NAME, null,cv);
    }catch(Exception ignore){return false;}
    return true;
  }

  public List<ReminderData> getReminderAt(int date, int month, int year){
    try(SQLiteDatabase db = this.getReadableDatabase()){
      Calendar calendar = Calendar.getInstance();
      calendar.set(year,month,date,0,0,0);
      long startDay = calendar.getTimeInMillis();
      calendar.set(year,month,date,23,59,59);
      long endDay = calendar.getTimeInMillis();

      List<ReminderData> list = new ArrayList<ReminderData>();
      /*String query =
              "SELECT * FROM " + ReminderTable.TABLE_NAME
                      + " WHERE " + ReminderTable.COLUMN_NAME_TIME + " BETWEEN " + startDay + " AND " + endDay;*/

      try(Cursor query1 = db.query(ReminderTable.TABLE_NAME,
              null,
              ReminderTable.COLUMN_NAME_TIME + " BETWEEN ? AND ?",
              new String[]{String.valueOf(startDay), String.valueOf(endDay)},
              null,
              null,
              ReminderTable.COLUMN_NAME_TIME)){

        int idIndex = query1.getColumnIndex(ReminderTable.COLUMN_NAME_ID);
        int nameIndex = query1.getColumnIndex(ReminderTable.COLUMN_NAME_TITLE);
        int timeIndex = query1.getColumnIndex(ReminderTable.COLUMN_NAME_TIME);

        if(!query1.moveToFirst()){return list;}

        do{
          int id = query1.getInt(idIndex);
          String name = query1.getString(nameIndex);
          long time = query1.getLong(timeIndex);

          list.add(new ReminderData(name,time,id));
        }while (query1.moveToNext());
      };
      return list;
    }catch (Exception ignore){}
    return null;
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
  }
}
