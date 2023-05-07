package com.honaglam.scheduleproject.MyAlramManager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.Reminder.ReminderBroadcastReceiver;
import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class MyAlarmManager {
  static @Nullable AlarmManager alarmManager;
  static final String[] WEEK_DAYS_NAMES = new String[]{
    "SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"
  };

  public static final String NOTIFICATION_CHANEL_ID = "ReminderNotificationChanel";
  static public void Initialize(Context context){
    if(alarmManager == null){
      alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
  }

  static private NotificationCompat.Builder getNotificationBuilder(Context context){
    return new NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle("Reminder")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
  }


  static public void SetSingleReminderAlarm(Context context, ReminderTaskFireBase.Reminder reminder){
    Initialize(context);
    if(alarmManager == null){
      return;
    }

    NotificationCompat.Builder notificationBuilder = getNotificationBuilder(context);
    notificationBuilder.setContentTitle("Reminder for " + reminder.title);

    Notification notification = notificationBuilder.build();
    int reminderId = reminder.id.hashCode();

    Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
    intent.putExtra(ReminderBroadcastReceiver.NAME_TAG, reminder.title);
    intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_KEY, notification);
    intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_ID_KEY, 1);
    intent.putExtra(ReminderBroadcastReceiver.REMINDER_ID_KEY, reminderId);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
    alarmManager.setExact(AlarmManager.RTC, reminder.reminderTime, pendingIntent);
  }

  static public void CancelSingleReminderAlarm(Context context, ReminderTaskFireBase.Reminder reminder){
    Initialize(context);
    if(alarmManager == null){
      return;
    }
    int reminderId = reminder.id.hashCode();
    Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pendingIntent);
  }



  static public void SetWeeklyReminderAlarm(Context context, ReminderTaskFireBase.Reminder reminder){
    if(reminder.weekDates == null){
      return;
    }
    Initialize(context);
    if(alarmManager == null){
      return;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(reminder.reminderTime);

    int curWeekDate = calendar.get(Calendar.DAY_OF_WEEK);
    NotificationCompat.Builder notificationBuilder = getNotificationBuilder(context);
    notificationBuilder.setContentTitle("Weekly reminder for " + reminder.title);

    for (Integer wDate : reminder.weekDates) {
      int weekDateDif = Math.min(
              Math.abs(curWeekDate - wDate),
              Math.abs(7 - (curWeekDate - wDate))
      );

      calendar.add(Calendar.DAY_OF_WEEK, weekDateDif);
      int reminderId = (reminder.id + WEEK_DAYS_NAMES[wDate - 1]).hashCode();

      Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
      intent.putExtra(ReminderBroadcastReceiver.NAME_TAG, reminder.title);
      intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_KEY, notificationBuilder.build());
      intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_ID_KEY, 1);
      intent.putExtra(ReminderBroadcastReceiver.REMINDER_ID_KEY, reminderId);


      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent,
              PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

      alarmManager.setInexactRepeating(
              AlarmManager.RTC,
              reminder.reminderTime,
              AlarmManager.INTERVAL_DAY * 7,
              pendingIntent
      );
    }


  }

  static public void CancelWeeklyReminderAlarm(Context context, ReminderTaskFireBase.Reminder reminder){
    if(reminder.weekDates == null){
      return;
    }
    Initialize(context);
    if(alarmManager == null){
      return;
    }

    for (Integer wDate : reminder.weekDates) {
      int reminderId = (reminder.id + WEEK_DAYS_NAMES[wDate - 1]).hashCode();
      Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent,
              PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
      alarmManager.cancel(pendingIntent);
    }
  }
}
