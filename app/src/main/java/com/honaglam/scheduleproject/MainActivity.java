package com.honaglam.scheduleproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.honaglam.scheduleproject.Model.TaskData;
import com.honaglam.scheduleproject.Reminder.ReminderBroadcastReceiver;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Reminder.ReminderTaskDB;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.LinkedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

  public static final String FRAGMENT_TAG_TIMER = "pomodoro_timer";
  public static final String FRAGMENT_TAG_SCHEDULE = "scheduler";

  private static final String UUID_KEY = "SchedulerKey";
  private String userDBUuid = null;

  //Timer
  private Intent timerIntent;
  private ServiceConnection timerServiceConnection;
  protected TimerService timerService;


  //Reminder
  public LinkedList<ReminderData> reminderDataList = new LinkedList<ReminderData>();
  private static final String REMINDER_FILE_NAME = "ScheduleReminder";
  File reminderFile = null;
  ReminderTaskDB taskDb;
  //========

  private DrawerLayout drawerLayout;
  private NavigationView sideNavView;
  private Button toolbarBtn;

  private FragmentManager fragmentManager;
  private CalendarFragment calendarFragment;
  private TimerFragment timerFragment;
  private TimerSetting timerSettingFragment;



  // Task
  ArrayList<TaskData> tasks = new ArrayList<>();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //reminderFile = new File(getFilesDir(), REMINDER_FILE_NAME);
    //LoadLocalReminder();

    Toast.makeText(this, String.format("Length %d", reminderDataList.size()), Toast.LENGTH_SHORT).show();
    taskDb = new ReminderTaskDB(this);

    /*
    SharedPreferences sPrefs= PreferenceManager.getDefaultSharedPreferences(this);
    userDBUuid = sPrefs.getString(UUID_KEY,null);
    if(userDBUuid == null){
     userDBUuid =  UUID.randomUUID().toString();
     SharedPreferences.Editor editor = sPrefs.edit();
     editor.putString(UUID_KEY, userDBUuid);
     editor.apply();
    }*/

    timerIntent = new Intent(this, TimerService.class);
    bindService(timerIntent, new TimerConnectionService(), Context.BIND_AUTO_CREATE);

    setSupportActionBar(findViewById(R.id.toolbar));

    drawerLayout = findViewById(R.id.drawerLayout);
    sideNavView = findViewById(R.id.navSideMenu);
    sideNavView.setNavigationItemSelectedListener(new SideNavItemSelect());

    toolbarBtn = findViewById(R.id.toolbarBtn);
    toolbarBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
      }
    });

    fragmentManager = getSupportFragmentManager();
    calendarFragment = CalendarFragment.newInstance();
    timerFragment = TimerFragment.newInstance();
    timerSettingFragment = TimerSetting.newInstance();

    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, calendarFragment, FRAGMENT_TAG_SCHEDULE)
            .addToBackStack(FRAGMENT_TAG_SCHEDULE)
            .commit();
  }


  public boolean switchFragment_Pomodoro() {
    if (timerFragment.isVisible()) {
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, timerFragment, FRAGMENT_TAG_TIMER)
            .addToBackStack(FRAGMENT_TAG_TIMER)
            .commit();
    return true;
  }


  public boolean switchFragment_Schedule() {
    if (calendarFragment.isVisible()) {
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, calendarFragment, FRAGMENT_TAG_SCHEDULE)
            .addToBackStack(FRAGMENT_TAG_SCHEDULE)
            .commit();
    return true;
  }

  public boolean switchFragment_TimerSetting() {
    if (timerSettingFragment.isVisible()) {
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, timerSettingFragment, "SettingFragment")
            .addToBackStack("SettingFragment")
            .commit();
    return true;
  }

  //Timer Service

  public boolean startTimer() {
    if (timerService != null) {
      timerService.startTimer();
      return true;
    }
    return false;
  }


  public boolean skip() {
    if (timerService != null) {
      timerService.skipTimer();
      return true;
    }
    return false;
  }

  public boolean resetTimer() {
    if (timerService != null) {
      timerService.resetTimer();
      return true;
    }
    return false;
  }

  public boolean setTimerOnTickCallBack(TimerService.TimerTickCallBack tickCallBack) {
    if (timerService != null) {
      timerService.setTickCallBack(tickCallBack);
      return true;
    }
    return false;
  }
  public boolean setTimerStateChangeCallBack(TimerService.TimerStateChangeCallBack stateChangeCallBack){
    if (timerService != null) {
      timerService.setStateChangeCallBack(stateChangeCallBack);
      return true;
    }
    return false;
  }
  public boolean setTimerTime(long workTime, long shortBreakTime, long longBreakTime, Uri alarmSound) {
    if (timerService != null) {
      timerService.setStateTime(workTime, shortBreakTime, longBreakTime, alarmSound);
    }
    return false;
  }

  public long getCurrentRemainMillis() {
    if (timerService != null) {
      return timerService.millisRemain;
    }
    return -1;
  }

  //===

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }


  public static final String NOTIFICATION_CHANEL_ID = "ReminderNotificationChanel";
  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
          .setSmallIcon(R.drawable.baseline_notifications_active_24)
          .setContentTitle("Reminder notification")
          .setContentText("Reminding")
          .setPriority(NotificationCompat.PRIORITY_DEFAULT);

  public int addReminder(String name, long time) {
    long result = taskDb.addReminder(name,time);

    try {
      int id = Math.toIntExact(result);

      ReminderData reminderData = new ReminderData(name,time,id);
      reminderDataList.add(reminderData);
      Toast.makeText(this, "Success = " + result, Toast.LENGTH_SHORT).show();

      notificationBuilder.setContentText(name);
      Notification notification = notificationBuilder.build();


      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
      intent.putExtra(ReminderBroadcastReceiver.NAME_TAG, name);
      intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_KEY, notification);
      intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_ID_KEY, 1);

      PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent,
              PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
      alarmManager.setExact(AlarmManager.RTC, time, pendingIntent);

    } catch (Exception ignore) {}


    return reminderDataList.size();
  }
  public void removeReminder(int pos){
    try {
      ReminderData data = reminderDataList.get(pos);
      int id = data.id;
      long remove = taskDb.removeReminder(id);
      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent,
              PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
      alarmManager.cancel(pendingIntent);

      reminderDataList.remove(pos);
    }catch (Exception ignore){}
  }
  public int getReminderAt(int date, int month, int year) {
    List<ReminderData> data = taskDb.getReminderAt(date, month, year);
    Log.d("DataLength", String.valueOf(data.size()));

    int oldSize = reminderDataList.size();
    reminderDataList.clear();
    reminderDataList.addAll(data);
    int newSize = reminderDataList.size();

    return Math.max(oldSize, newSize);
  }
  public int searchReminder(String name,long startDate,long endDate){
    List<ReminderData> newList = taskDb.findReminders(name,startDate,endDate);
    reminderDataList.clear();
    reminderDataList.addAll(newList);

    Toast.makeText(this, "Search size " + reminderDataList.size(), Toast.LENGTH_SHORT).show();

    return  reminderDataList.size();
  }

  class SideNavItemSelect implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();

      switch (id) {
        case R.id.nav_timer:
          Toast.makeText(MainActivity.this, "Select Timer", Toast.LENGTH_SHORT).show();
          return switchFragment_Pomodoro();
        case R.id.nav_schedule:
          Toast.makeText(MainActivity.this, "Select schedule", Toast.LENGTH_SHORT).show();
          return switchFragment_Schedule();
      }
      return false;
    }
  }
  class TimerConnectionService implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      timerService = ((TimerService.LocalBinder) iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
  }

}
