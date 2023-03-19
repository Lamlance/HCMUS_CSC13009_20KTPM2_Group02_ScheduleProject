package com.honaglam.scheduleproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
  public static final String FRAGMENT_TAG_TIMER = "pomodoro_timer";
  public static final String FRAGMENT_TAG_SCHEDULE = "scheduler";
  private Intent timerIntent;
  private ServiceConnection timerServiceConnection;
  protected TimerService timerService;


  private DrawerLayout drawerLayout;
  private NavigationView sideNavView;
  private Button toolbarBtn;

  private FragmentManager fragmentManager;
  private CalendarFragment calendarFragment;
  private TimerFragment timerFragment;
  private TimerSetting timerSettingFragment;

  private MediaPlayer mediaPlayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    timerIntent = new Intent(this,TimerService.class);
    bindService(timerIntent,new TimerConnectionService(),Context.BIND_AUTO_CREATE);

    setSupportActionBar((androidx.appcompat.widget.Toolbar)findViewById(R.id.toolbar));

    drawerLayout = findViewById(R.id.drawerLayout);
    sideNavView =  findViewById(R.id.navSideMenu);
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
            .replace(R.id.fragmentContainerView,calendarFragment,FRAGMENT_TAG_SCHEDULE)
            .addToBackStack(FRAGMENT_TAG_SCHEDULE)
            .commit();
  }

  public boolean switchFragment_Pomodoro(){
    if (timerFragment.isVisible()){
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView,timerFragment,FRAGMENT_TAG_TIMER)
            .addToBackStack(FRAGMENT_TAG_TIMER)
            .commit();
    return true;
  }
  public boolean switchFragment_Schedule(){
    if(calendarFragment.isVisible()){
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView,calendarFragment,FRAGMENT_TAG_SCHEDULE)
            .addToBackStack(FRAGMENT_TAG_SCHEDULE)
            .commit();
    return true;
  }
  public boolean switchFragment_TimerSetting(){
    if (timerSettingFragment.isVisible()){
      return false;
    }
    fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView,timerSettingFragment,"SettingFragment")
            .addToBackStack("SettingFragment")
            .commit();
    return true;
  }

  //Timer Service
  public boolean startTimer(){
    if(timerService != null){
      timerService.startTimer();
      return true;
    }
    return false;
  }
  public boolean pauseTimer(){
    if(timerService != null){
      timerService.pauseTimer();
    }
    return false;
  }
  public boolean resetTimer(){
    if(timerService != null){
      timerService.resetTimer();
      return true;
    }
    return false;
  }
  public boolean setTimerOnTickCallBack(TimerService.TimerTickCallBack tickCallBack){
    if(timerService != null){
      timerService.tickCallBack = tickCallBack;
      return true;
    }
    return false;
  }
  public boolean setTimerTime(long workTime, long shortBreakTime, long longBreakTime, Uri alarmSound){
    if(timerService != null){
      timerService.setStateTime(workTime,shortBreakTime,longBreakTime, alarmSound);
    }
    return false;
  }
  public long getCurrentRemainMillis(){
    if (timerService != null){
      return timerService.millisRemain;
    }
    return -1;
  }
  //===
  // Create a countdown sound
  public void playCountDown(){
    if (mediaPlayer == null){
      mediaPlayer = MediaPlayer.create(this, R.raw.count_down_sound);
      mediaPlayer.start();
    }
  }
  class SideNavItemSelect implements NavigationView.OnNavigationItemSelectedListener{
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();

      switch (id){
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
  class TimerConnectionService implements ServiceConnection{
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      timerService = ((TimerService.LocalBinder)iBinder).getService();
    }
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
  }
}
