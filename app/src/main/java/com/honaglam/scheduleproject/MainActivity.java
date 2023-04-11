package com.honaglam.scheduleproject;

import static com.honaglam.scheduleproject.TimerSetting.TIMER_SETTING_RESULT_KEY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Reminder.ReminderBroadcastReceiver;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.UserSetting.UserTimerSettings;
//import com.honaglam.scheduleproject.UserSetting.UserSettings;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

  public static final String FRAGMENT_TAG_TIMER = "pomodoro_timer";
  public static final String FRAGMENT_TAG_SCHEDULE = "scheduler";
  public static final String FRAGMENT_TAG_STATISTIC = "statstic";
  public static final String FRAGMENT_TAG_HISTORY = "history";

  private static final String UUID_KEY = "SchedulerKey";
  public boolean darkModeIsOn = false;
  private String userDBUuid = null;

  //Timer
  private Intent timerIntent;
  private ServiceConnection timerServiceConnection;
  protected TimerService timerService;

  private SwitchMaterial darkThemeSwitcher;


  //Reminder
  public LinkedList<ReminderData> reminderDataList = new LinkedList<ReminderData>();
  private static final String REMINDER_FILE_NAME = "ScheduleReminder";
  File reminderFile = null;
  ReminderTaskDB taskDb;
  //========

  private DrawerLayout drawerLayout;
  private NavigationView sideNavView;
  private Button toolbarBtn;
  private boolean nightMode;

  private FragmentManager fragmentManager;
  private CalendarFragment calendarFragment;
  private TimerFragment timerFragment;
  private StatisticFragment statisticFragment;
  private HistoryFragment historyFragment;


  SharedPreferences userTimerSetting;
  static final String PREF_KEY_WORK_TIME = "work_time";
  static final String PREF_KEY_SHORT_TIME = "short_time";
  static final String PREF_KEY_LONG_TIME = "long_time";
  static final String PREF_KEY_AUTO_BREAK = "auto_break";
  static final String PREF_KEY_AUTO_POMODORO = "auto_pomodoro";
  static final String PREF_KEY_LONG_INTERVAL = "long_interval";
  static final String PREF_KEY_ALARM = "alarm";

  // Task
  ArrayList<TaskData> tasks = new ArrayList<>();
  // User setting
  //  private UserSettings userSettings;
  static final int IS_CALENDAR_FRAGMENT = 1;
  static final int IS_TIMER_FRAGMENT = 2;
  static final int IS_STATISTIC_FRAGMENT = 3;
  static final int IS_HISTORY_FRAGMENT = 4;

  int currentFragment = IS_CALENDAR_FRAGMENT;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    setContentView(R.layout.activity_main);

    fragmentManager = getSupportFragmentManager();
    userTimerSetting = getSharedPreferences("userTimerSetting", MODE_PRIVATE);

    timerIntent = new Intent(this, TimerService.class);
    bindService(timerIntent, new TimerConnectionService(), Context.BIND_AUTO_CREATE);

    taskDb = new ReminderTaskDB(this);
    taskDb.getTodayStats();


    if (taskDb.IS_DEV) {
      //taskDb.createSampleData();
    }

    tasks.addAll(taskDb.getAllTask());
    setSupportActionBar(findViewById(R.id.toolbar));

    drawerLayout = findViewById(R.id.drawerLayout);
    sideNavView = findViewById(R.id.navSideMenu);
    sideNavView.setNavigationItemSelectedListener(new SideNavItemSelect());

    SwitchCompat switchTheme = (SwitchCompat) sideNavView.getMenu().findItem(R.id.nav_switchTheme)
            .getActionView().findViewById(R.id.nav_switchTheme_switchView);
    switchTheme.setOnCheckedChangeListener(new DarkThemeSwitch());

    toolbarBtn = findViewById(R.id.toolbarBtn);
    toolbarBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
      }
    });

    int stackCount = fragmentManager.getBackStackEntryCount();
    for (int i = 0; i < stackCount; ++i) {
      fragmentManager.popBackStack();
    }

  }

  // Switch to Pomodoro Fragment / Timer Fragment
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

  // Switch to Schedule Fragment
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


  // Switch to Statistic Fragment
  public boolean switchFragment_Statistic() {
    if (statisticFragment.isVisible()) {
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, statisticFragment, FRAGMENT_TAG_STATISTIC)
            .addToBackStack(FRAGMENT_TAG_STATISTIC)
            .commit();
    return true;
  }

  // Switch to History Fragment
  public boolean switchFragment_History() {
    if (historyFragment.isVisible()) {
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, historyFragment, FRAGMENT_TAG_HISTORY)
            .addToBackStack(FRAGMENT_TAG_HISTORY)
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


  public boolean switchFragment_TimerSetting() {
    TimerSetting.newInstance(loadTimerSettingPref()).show(fragmentManager, "SettingFragment");
    return true;
  }


  public boolean setTimerOnTickCallBack(TimerService.TimerTickCallBack tickCallBack) {
    if (timerService != null) {
      timerService.setTickCallBack(tickCallBack);
      return true;
    }
    return false;
  }

  public boolean setTimerStateChangeCallBack(TimerService.TimerStateChangeCallBack stateChangeCallBack) {
    if (timerService != null) {
      timerService.setStateChangeCallBack(stateChangeCallBack);
      return true;
    }
    return false;
  }


  public boolean pauseTimer() {
    if (timerService != null) {
      timerService.pauseTimer();
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

  //TODO Timer On Finished
  public boolean setTimerOnFinishCallback(TimerService.TimerOnFinishCallback onFinishCallback) {
    if (timerFragment != null) {
      timerService.setOnFinishCallback(onFinishCallback);
      return true;
    }
    return false;
  }

  public boolean addStatsTime(long workTime, long shortTime, long longTime) {
    return taskDb.addTimeTodayStats(workTime, shortTime, longTime);
  }

  public long getCurrentRemainMillis() {
    if (timerService != null) {
      return timerService.millisRemain;
    }
    return -1;
  }

  public UserTimerSettings saveTimerSettingPref(
          UserTimerSettings settings) {
    SharedPreferences.Editor edit = userTimerSetting.edit();
    edit.putLong(PREF_KEY_WORK_TIME, settings.workMillis);
    edit.putLong(PREF_KEY_SHORT_TIME, settings.shortBreakMillis);
    edit.putLong(PREF_KEY_LONG_TIME, settings.longBreakMillis);
    if(settings.alarmUri != null){
      edit.putString(PREF_KEY_ALARM, settings.alarmUri.toString());
    }
    edit.putBoolean(PREF_KEY_AUTO_BREAK, settings.autoStartBreakSetting);
    edit.putBoolean(PREF_KEY_AUTO_POMODORO, settings.autoStartPomodoroSetting);
    edit.putLong(PREF_KEY_LONG_INTERVAL, settings.longBreakInterValSetting);
    edit.apply();

    timerService.setStateTime(settings);
    return settings;
  }

  public UserTimerSettings loadTimerSettingPref() {
    long workTime = userTimerSetting.getLong(PREF_KEY_WORK_TIME, TimerService.DEFAULT_WORK_TIME);
    long shortTime = userTimerSetting.getLong(PREF_KEY_SHORT_TIME, TimerService.DEFAULT_SHORT_BREAK_TIME);
    long longTime = userTimerSetting.getLong(PREF_KEY_LONG_TIME, TimerService.DEFAULT_LONG_BREAK_TIME);
    boolean autoBreak = userTimerSetting.getBoolean(PREF_KEY_AUTO_BREAK, false);
    boolean autoWork = userTimerSetting.getBoolean(PREF_KEY_AUTO_POMODORO, false);
    long longInterval = userTimerSetting.getLong(PREF_KEY_LONG_INTERVAL, 4);
    String uri = userTimerSetting.getString(PREF_KEY_ALARM, null);
    Uri alarm = null;
    if (uri != null) {
      alarm = Uri.parse(uri);
    }

    return new UserTimerSettings(
            workTime, shortTime, longTime,
            alarm, autoBreak, autoWork, longInterval
    );
  }

  public int addTask(String name, int loops, int loopsCompleted, boolean isDone) {
    try {
      int id = Math.toIntExact(taskDb.addTask(name, loops, loopsCompleted, isDone));
      tasks.add(new TaskData(name, loops, id));
      return tasks.size() - 1;
    } catch (Exception ignore) {
    }
    return -1;
  }

  public int editTask(TaskData data) {
    return taskDb.editTask(data) ? tasks.size() - 1 : -1;
  }

  public boolean makeTaskHistory(int id) {
    return taskDb.makeTaskHistory(id);
  }

  public boolean deleteTask(int id) {
    return taskDb.deleteTask(id);
  }

  public boolean moveTaskToHistory(int id) {
    return taskDb.makeTaskHistory(id);
  }

  public boolean moveTaskToToDoTask(int id) {
    return taskDb.makeTaskToToDo(id);
  }
  //===

  @Override
  protected void onDestroy() {
    super.onDestroy();

    Log.i("ON_DESTROY", "ACTIVITY DESTROYING");
    if (timerServiceConnection != null) {
      Log.i("ON_DESTROY", "ACTIVITY UNBINDING SERVICE");
      unbindService(timerServiceConnection);
    }
  }


  public static final String NOTIFICATION_CHANEL_ID = "ReminderNotificationChanel";
  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
          .setSmallIcon(R.drawable.baseline_notifications_active_24)
          .setContentTitle("Reminder notification")
          .setContentText("Reminding")
          .setPriority(NotificationCompat.PRIORITY_DEFAULT);

  public int addReminder(String name, long time) {
    long result = taskDb.addReminder(name, time);

    try {
      int id = Math.toIntExact(result);

      ReminderData reminderData = new ReminderData(name, time, id);
      reminderDataList.add(reminderData);
      //Toast.makeText(this, "Success = " + result, Toast.LENGTH_SHORT).show();

      notificationBuilder.setContentText(name);
      Notification notification = notificationBuilder.build();


      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
      intent.putExtra(ReminderBroadcastReceiver.NAME_TAG, name);
      intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_KEY, notification);
      intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_ID_KEY, 1);
      intent.putExtra(ReminderBroadcastReceiver.REMINDER_ID_KEY, id);

      PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent,
              PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
      alarmManager.setExact(AlarmManager.RTC, time, pendingIntent);

    } catch (Exception ignore) {
    }

    return reminderDataList.size();
  }

  public int addReminderWeekly(String name, long time, HashSet<Integer> weekDates) {
    ArrayList<ReminderData> reminders = new ArrayList<ReminderData>();

    try {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(time);
      long result = taskDb.addReminder(name, time, calendar.get(Calendar.DAY_OF_WEEK));
      reminders.add(new ReminderData(
              name,
              calendar.getTimeInMillis(),
              Math.toIntExact(result)
      ));

      int curWeekDate = calendar.get(Calendar.DAY_OF_WEEK);

      for (Integer wDate : weekDates) {
        int weekDateDif = Math.min(
                Math.abs(curWeekDate - wDate),
                Math.abs(7 - (curWeekDate - wDate))
        );


        calendar.add(Calendar.DAY_OF_WEEK, weekDateDif);
        Log.d("WEEKDAY_DATE", String.valueOf(calendar.get(Calendar.DATE)));

        long idResult = taskDb.addReminder(name, calendar.getTimeInMillis());
        reminders.add(new ReminderData(
                name,
                calendar.getTimeInMillis(),
                Math.toIntExact(idResult)
        ));

        calendar.add(Calendar.DAY_OF_WEEK, -weekDateDif);
      }

      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      notificationBuilder.setContentText(name);
      Notification notification = notificationBuilder.build();

      for (ReminderData reminderData : reminders) {
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra(ReminderBroadcastReceiver.NAME_TAG, name);
        intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_KEY, notification);
        intent.putExtra(ReminderBroadcastReceiver.NOTIFICATION_ID_KEY, 1);
        intent.putExtra(ReminderBroadcastReceiver.REMINDER_ID_KEY, reminderData.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderData.id, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                reminderData.RemindTime,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );

      }
    } catch (Exception ignore) {
    }

    reminderDataList.add(reminders.get(0));
    return reminderDataList.size();
  }

  public void removeReminder(int pos) {
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
    } catch (Exception ignore) {
    }
  }

  /*
  public int getReminderAt(int date, int month, int year) {
    List<ReminderData> data = taskDb.getReminderAt(date, month, year);
    Log.d("DataLength", String.valueOf(data.size()));

    int oldSize = reminderDataList.size();
    reminderDataList.clear();
    reminderDataList.addAll(data);
    int newSize = reminderDataList.size();

    return Math.max(oldSize, newSize);
  }
  */

  public int searchReminder(String name, long startDate, long endDate) {
    List<ReminderData> newList = taskDb.findReminders(name, startDate, endDate);
    reminderDataList.clear();
    reminderDataList.addAll(newList);

    Toast.makeText(this, "Search size " + reminderDataList.size(), Toast.LENGTH_SHORT).show();

    return reminderDataList.size();
  }

  public List<ReminderData> getSearchReminder(String name, long startDate, long endDate) {
    return taskDb.findReminders(name, startDate, endDate);
  }

  class DarkThemeSwitch implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
      Toast.makeText(MainActivity.this, "Dark is on " + isOn, Toast.LENGTH_SHORT).show();
      if (isOn == true) {
        darkModeIsOn = true;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
      } else {
        darkModeIsOn = false;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
      }
    }
  }

  class SideNavItemSelect implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();

      switch (id) {
        case R.id.nav_timer:
          currentFragment = IS_TIMER_FRAGMENT;
          Toast.makeText(MainActivity.this, "Select Timer", Toast.LENGTH_SHORT).show();
          return switchFragment_Pomodoro();
        case R.id.nav_schedule:
          currentFragment = IS_CALENDAR_FRAGMENT;
          Toast.makeText(MainActivity.this, "Select Schedule", Toast.LENGTH_SHORT).show();
          return switchFragment_Schedule();
        case R.id.nav_statistic:
          currentFragment = IS_STATISTIC_FRAGMENT;
          Toast.makeText(MainActivity.this, "Select Report", Toast.LENGTH_SHORT).show();
          return switchFragment_Statistic();
        case R.id.nav_history:
          currentFragment = IS_HISTORY_FRAGMENT;
          Toast.makeText(MainActivity.this, "Select History", Toast.LENGTH_SHORT).show();
          return switchFragment_History();
      }
      return false;
    }
  }

  class TimerConnectionService implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      timerService = ((TimerService.LocalBinder) iBinder).getService();
      timerService.setStateTime(loadTimerSettingPref());

      calendarFragment = CalendarFragment.newInstance();
      timerFragment = TimerFragment.newInstance();
      statisticFragment = StatisticFragment.newInstance();
      historyFragment = HistoryFragment.newInstance();

      fragmentManager
              .beginTransaction()
              .replace(R.id.fragmentContainerView, timerFragment, FRAGMENT_TAG_TIMER)
              .addToBackStack(FRAGMENT_TAG_TIMER)
              .commit();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
  }
}
