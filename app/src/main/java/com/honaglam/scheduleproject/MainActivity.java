package com.honaglam.scheduleproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

    private static final String UUID_KEY = "SchedulerKey";
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
    private TimerSetting timerSettingFragment;
    private StatisticFragment statisticFragment;


    // Task
    ArrayList<TaskData> tasks = new ArrayList<>();
    // User setting
    //  private UserSettings userSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskDb = new ReminderTaskDB(this);
        taskDb.getTodayStats();

        if (taskDb.IS_DEV) {
            taskDb.createSampleData();
        }

        List<ReminderTaskDB.TimerStatsData> list = taskDb.get30StatsBeforeToday();
        for (ReminderTaskDB.TimerStatsData data : list) {
            String str = String.format(Locale.getDefault(),
                    "%d / %d / %d, Work: %d ,Short: %d, Long: %d",
                    data.date, data.month, data.year, data.workDur, data.shortDur, data.longDur);
            Log.i("TASK_DATA", str);
        }

        tasks.addAll(taskDb.getAllTask());

        timerIntent = new Intent(this, TimerService.class);
        bindService(timerIntent, new TimerConnectionService(), Context.BIND_AUTO_CREATE);

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

        fragmentManager = getSupportFragmentManager();
        calendarFragment = CalendarFragment.newInstance();
        timerFragment = TimerFragment.newInstance();
        timerSettingFragment = TimerSetting.newInstance();
        statisticFragment = StatisticFragment.newInstance();


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

    public boolean setTimerStateChangeCallBack(TimerService.TimerStateChangeCallBack stateChangeCallBack) {
        if (timerService != null) {
            timerService.setStateChangeCallBack(stateChangeCallBack);
            return true;
        }
        return false;
    }

    public boolean setTimerTime(long workTime, long shortBreakTime, long longBreakTime, Uri alarmSound, boolean autoStartBreak, boolean autoStartPomodoro, long longBreakInterVal) {
        if (timerService != null) {
            timerService.setStateTime(workTime, shortBreakTime, longBreakTime, alarmSound, autoStartBreak, autoStartPomodoro, longBreakInterVal);
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
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
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
                    Toast.makeText(MainActivity.this, "Select Timer", Toast.LENGTH_SHORT).show();
                    return switchFragment_Pomodoro();
                case R.id.nav_schedule:
                    Toast.makeText(MainActivity.this, "Select Schedule", Toast.LENGTH_SHORT).show();
                    return switchFragment_Schedule();
                case R.id.nav_statistic:
                    Toast.makeText(MainActivity.this, "Select Report", Toast.LENGTH_SHORT).show();
                    return switchFragment_Statistic();
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
