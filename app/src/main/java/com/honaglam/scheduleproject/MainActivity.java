package com.honaglam.scheduleproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Reminder.ReminderBroadcastReceiver;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.UserSetting.UserTimerSettings;
//import com.honaglam.scheduleproject.UserSetting.UserSettings;

import java.util.HashMap;
import java.util.LinkedList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  public boolean darkModeIsOn = false;
  //Timer
  private Intent timerIntent;
  private ServiceConnection timerServiceConnection;
  protected TimerService timerService;


  //Reminder
  public LinkedList<ReminderData> reminderDataList = new LinkedList<ReminderData>();
  //ReminderTaskDB taskDb;
  //========

  private DrawerLayout drawerLayout;
  private NavigationView sideNavView;
  private Button toolbarBtn;
  private boolean nightMode;

  SharedPreferences userTimerSetting;
  static final String PREF_KEY_WORK_TIME = "work_time";
  static final String PREF_KEY_SHORT_TIME = "short_time";
  static final String PREF_KEY_LONG_TIME = "long_time";
  static final String PREF_KEY_AUTO_BREAK = "auto_break";
  static final String PREF_KEY_AUTO_POMODORO = "auto_pomodoro";
  static final String PREF_KEY_LONG_INTERVAL = "long_interval";
  static final String PREF_KEY_ALARM = "alarm";
  static final String PREF_KEY_THEME = "theme";

  ReminderBroadcastReceiver reminderBroadcastReceiver;

  ReminderTaskFireBase fireBase;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    setContentView(R.layout.activity_main);


    Log.i("MAIN_ACTIVITY","INIT UI");
    fragmentManager = getSupportFragmentManager();
    userTimerSetting = getSharedPreferences("userTimerSetting", MODE_PRIVATE);

    timerServiceConnection = new TimerConnectionService();
    timerIntent = new Intent(this, TimerService.class);
    bindService(timerIntent, timerServiceConnection, Context.BIND_AUTO_CREATE);

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

    /*
    int stackCount = fragmentManager.getBackStackEntryCount();
    for (int i = 0; i < stackCount; ++i) {
      fragmentManager.popBackStack();
    }
     */

    reminderBroadcastReceiver = new ReminderBroadcastReceiver();
    IntentFilter filter = new IntentFilter("com.hoanglam.scheduleproject.reminder");
    registerReceiver(reminderBroadcastReceiver, filter);

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    Log.i("ON_DESTROY", "ACTIVITY DESTROYING");
    if (timerServiceConnection != null) {
      Log.i("ON_DESTROY", "ACTIVITY UNBINDING SERVICE");
      unbindService(timerServiceConnection);
    }
    ReminderTaskFireBase.RemoveInstance();
    Log.i("ON_DESTROY", "Removing db instance");

  }

  private void InitFragment(){
    calendarFragment = CalendarFragment.newInstance("lamhoangdien113@gmail,com");
    timerFragment = TimerFragment.newInstance("lamhoangdien113@gmail,com");
    statisticFragment = StatisticFragment.newInstance();
    historyFragment = HistoryFragment.newInstance();
    auth0Fragment = Auth0Fragment.newInstance();

    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, timerFragment, FRAGMENT_TAG_TIMER)
            .addToBackStack(FRAGMENT_TAG_TIMER)
            .commit();
  }

  private void InitFireBase(){
    Dialog dialog = new MaterialAlertDialogBuilder(this)
            .setMessage("Loading db")
            .setCancelable(false)
            .create();
    fireBase = ReminderTaskFireBase.GetInstance("lamhoangdien113@gmail,com", () -> {
      Log.i("MAIN_ACTIVITY","FINISH INIT DATA");
      InitFragment();
      dialog.dismiss();
    });
    dialog.show();
  }

  //Timer Service
  public void startTimer() {
    if (timerService != null) {
      timerService.startTimer();
    }
  }

  public void skip() {
    if (timerService != null) {
      timerService.skipTimer();
    }
  }

  public void setTimerOnTickCallBack(TimerService.TimerTickCallBack tickCallBack) {
    if (timerService != null) {
      timerService.setTickCallBack(tickCallBack);
    }
  }

  public void setTimerStateChangeCallBack(TimerService.TimerStateChangeCallBack stateChangeCallBack) {
    if (timerService != null) {
      timerService.setStateChangeCallBack(stateChangeCallBack);
    }
  }

  public void pauseTimer() {
    if (timerService != null) {
      timerService.pauseTimer();
    }
  }

  public void setTimerOnFinishCallback(TimerService.TimerOnFinishCallback onFinishCallback) {
    if (timerFragment != null) {
      timerService.setOnFinishCallback(onFinishCallback);
    }
  }

  public UserTimerSettings saveTimerSettingPref(UserTimerSettings settings) {
    SharedPreferences.Editor edit = userTimerSetting.edit();
    edit.putLong(PREF_KEY_WORK_TIME, settings.workMillis);
    edit.putLong(PREF_KEY_SHORT_TIME, settings.shortBreakMillis);
    edit.putLong(PREF_KEY_LONG_TIME, settings.longBreakMillis);
    if (settings.alarmUri != null) {
      edit.putString(PREF_KEY_ALARM, settings.alarmUri.toString());
    }
    edit.putBoolean(PREF_KEY_AUTO_BREAK, settings.autoStartBreakSetting);
    edit.putBoolean(PREF_KEY_AUTO_POMODORO, settings.autoStartPomodoroSetting);
    edit.putLong(PREF_KEY_LONG_INTERVAL, settings.longBreakInterValSetting);
    edit.putInt(PREF_KEY_THEME, settings.prefTheme);
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
    int prefTheme = userTimerSetting.getInt(PREF_KEY_THEME, 0);

    return new UserTimerSettings(
            workTime, shortTime, longTime,
            alarm, autoBreak, autoWork, longInterval,
            prefTheme
    );
  }


  //Reminder



  /*
  public static final String NOTIFICATION_CHANEL_ID = "ReminderNotificationChanel";
  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
          .setSmallIcon(R.drawable.baseline_notifications_active_24)
          .setContentTitle("Reminder notification")
          .setContentText("Reminding")
          .setPriority(NotificationCompat.PRIORITY_DEFAULT);
   */


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


  public static final String FRAGMENT_TAG_TIMER = "pomodoro_timer";
  public static final String FRAGMENT_TAG_SCHEDULE = "scheduler";
  public static final String FRAGMENT_TAG_STATISTIC = "statstic";
  public static final String FRAGMENT_TAG_HISTORY = "history";
  public static final String FRAGMENT_TAG_AUTH = "auth";

  private FragmentManager fragmentManager;
  private CalendarFragment calendarFragment;
  private TimerFragment timerFragment;
  private StatisticFragment statisticFragment;
  private HistoryFragment historyFragment;
  private Auth0Fragment auth0Fragment;

  static final int IS_CALENDAR_FRAGMENT = 1;
  static final int IS_TIMER_FRAGMENT = 2;
  static final int IS_STATISTIC_FRAGMENT = 3;
  static final int IS_HISTORY_FRAGMENT = 4;
  int currentFragment = IS_CALENDAR_FRAGMENT;

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

  public boolean switchFragment_TimerSetting() {
    TimerSetting.newInstance(loadTimerSettingPref()).show(fragmentManager, "SettingFragment");
    return true;
  }

  public boolean switchFragment_Auth() {
    if (auth0Fragment == null || auth0Fragment.isVisible()) {
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, auth0Fragment, FRAGMENT_TAG_AUTH)
            .addToBackStack(FRAGMENT_TAG_AUTH)
            .commit();
    return true;
  }

  class SideNavItemSelect implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();

      if (id == R.id.nav_timer) {
        currentFragment = IS_TIMER_FRAGMENT;
        Toast.makeText(MainActivity.this, "Select Timer", Toast.LENGTH_SHORT).show();
        return switchFragment_Pomodoro();
      } else if (id == R.id.nav_schedule) {
        currentFragment = IS_CALENDAR_FRAGMENT;
        Toast.makeText(MainActivity.this, "Select Schedule", Toast.LENGTH_SHORT).show();
        return switchFragment_Schedule();
      } else if (id == R.id.nav_statistic) {
        currentFragment = IS_STATISTIC_FRAGMENT;
        Toast.makeText(MainActivity.this, "Select Report", Toast.LENGTH_SHORT).show();
        return switchFragment_Statistic();
      } else if (id == R.id.nav_history) {
        currentFragment = IS_HISTORY_FRAGMENT;
        Toast.makeText(MainActivity.this, "Select History", Toast.LENGTH_SHORT).show();
        return switchFragment_History();
      } else if (id == R.id.nav_auth) {
        return switchFragment_Auth();
      }

      return false;
    }
  }

  class TimerConnectionService implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      timerService = ((TimerService.LocalBinder) iBinder).getService();
      timerService.setStateTime(loadTimerSettingPref());
      InitFireBase();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
  }
}
