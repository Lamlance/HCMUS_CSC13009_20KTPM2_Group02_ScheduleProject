package com.honaglam.scheduleproject;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.honaglam.scheduleproject.UserSetting.UserTimerSettings;

import kotlin.NotImplementedError;
//import android.os.Looper;

public class TimerService extends Service {
  private final IBinder binder = new LocalBinder();
  CountDownTimer timer;
  private MediaPlayer alarmMediaPlayer;
  private MediaPlayer tickingMediaPlayer;

  public static final long DEFAULT_WORK_TIME = 5000; //5second
  public static final long DEFAULT_SHORT_BREAK_TIME = 8000; //6second
  public static final long DEFAULT_LONG_BREAK_TIME = 10000; //7second

  //public static final int NONE_STATE = 0;
  public static final int WORK_STATE = 1;
  public static final int SHORT_BREAK_STATE = 2;
  public static final int LONG_BREAK_STATE = 3;


  long millisRemain = 0;
  long workMillis = 0;
  long shortBreakMillis = 0;
  long longBreakMillis = 0;

  boolean isRunning = false;
  boolean autoStartBreakSetting = false;
  boolean autoStartPomodoroSetting = false;
  long longBreakInterValSetting = 4;
  Uri alarmUri;

  private static final String CHANNEL_ID = "TimerNotificationChanel";
  private static final int NOTIFICATION_ID = 6969;
  private TimerTickCallBack tickCallBack = null;
  private TimerStateChangeCallBack stateChangeCallBack = null;
  Intent timerIntent;
  NotificationChannel notificationChannel;
  Handler timerHandler;
  Runnable timerRunnable;
  int runningState = WORK_STATE;
  int timerCount = 0;
  int cycleCount = 0;
  NotificationCompat.Builder notificationBuilder;
  TimerOnFinishCallback onFinishCallback;


  public TimerService() {

  }

  private static final long NOTIFICATION_FLAG_IS_RUNNING = -2;
  private static final long NOTIFICATION_FLAG_WORK_FINISHED = -3;
  private static final long NOTIFICATION_FLAG_BREAK_FINISHED = -4;

  private static final String TIMER_REQ_CODE_KEY = "timer-req-key";
  private static final int START_TIMER_REQ_CODE = 0;
  private static final int PAUSE_TIMER_REQ_CODE = 1;
  private static final int SKIP_TIMER_REQ_CODE = 2;
  BroadcastReceiver receiver;

  @Override
  public void onCreate() {
    super.onCreate();
    Context context = getApplicationContext();

    workMillis = DEFAULT_WORK_TIME;
    shortBreakMillis = DEFAULT_SHORT_BREAK_TIME;
    longBreakMillis = DEFAULT_LONG_BREAK_TIME;
    millisRemain = DEFAULT_WORK_TIME;

    timerIntent = new Intent(context, TimerService.class); // Build the intent for the service
    context.startForegroundService(timerIntent);

    notificationChannel = new NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_DEFAULT);
    notificationChannel.setDescription("Notification for pomodoro timer service");

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(notificationChannel);

    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, timerIntent, PendingIntent.FLAG_IMMUTABLE);
    notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro timer")
            .setContentText("Welcome to pomodoro")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true);


    receiver = new ServiceControlBroadCastReceiver();
    IntentFilter serviceControlFilter = new IntentFilter();
    serviceControlFilter.addAction("com.hoanglam.scheduleproject.controltimer");
    registerReceiver(receiver,serviceControlFilter);

    // timerFragment = new TimerFragment();
  }

  @Override
  public void onDestroy() {
    timer.cancel();
    unregisterReceiver(receiver);
    super.onDestroy();
  }

  public Notification makeServiceNotification(long time) {
    notificationBuilder.setContentText(makeTimeString(time));
    if(time == NOTIFICATION_FLAG_IS_RUNNING
            || time == NOTIFICATION_FLAG_WORK_FINISHED
            || time == NOTIFICATION_FLAG_BREAK_FINISHED

    ){
      notificationBuilder.clearActions();

      if(time == NOTIFICATION_FLAG_WORK_FINISHED || time == NOTIFICATION_FLAG_BREAK_FINISHED){
        String action = (time == NOTIFICATION_FLAG_WORK_FINISHED) ? "Start break" : "Start focusing";

        Intent startStateIntent = new Intent("com.hoanglam.scheduleproject.controltimer");
        startStateIntent.putExtra(TIMER_REQ_CODE_KEY,START_TIMER_REQ_CODE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                startStateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.addAction(R.mipmap.ic_launcher_round,action,pendingIntent);

      }else {
        String startStop = isRunning ? "Pause timer" : "Start timer";
        String skipTimer = "Skip timer";

        Intent startStopIntent = new Intent("com.hoanglam.scheduleproject.controltimer");
        startStopIntent.putExtra(TIMER_REQ_CODE_KEY,isRunning ? PAUSE_TIMER_REQ_CODE : START_TIMER_REQ_CODE);
        PendingIntent startStopPending= PendingIntent.getBroadcast(
                this,
                1,
                startStopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent skipStateIntent = new Intent("com.hoanglam.scheduleproject.controltimer");
        skipStateIntent.putExtra(TIMER_REQ_CODE_KEY,SKIP_TIMER_REQ_CODE);
        PendingIntent skipStatePending = PendingIntent.getBroadcast(
                this,
                2,
                skipStateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder.addAction(R.mipmap.ic_launcher_round,startStop,startStopPending);
        notificationBuilder.addAction(R.mipmap.ic_launcher_round,skipTimer,skipStatePending);

      }


    }



    return notificationBuilder.build();
  }

  private String makeTimeString(long time){
    Log.i("TIMER_SERVICE", "Make time string: " + time);

    if(time == NOTIFICATION_FLAG_WORK_FINISHED){
      return "Work time had finished. Start your break~~";
    }
    if(time == NOTIFICATION_FLAG_BREAK_FINISHED){
      return "Break time finished. Let's start working! ";
    }
    if(time == NOTIFICATION_FLAG_IS_RUNNING){
      int minute = ((int) Math.floor((double)millisRemain / 60000.0)) * (millisRemain > 0 ? 1 : 0);
      int seconds = ((int)Math.floor((double)millisRemain / 1000.0) - (minute*60)) * (millisRemain > 0 ? 1 : 0);
      return (("00" + minute).substring(String.valueOf(minute).length())+":"+("00" + seconds).substring(String.valueOf(seconds).length()));
    }



    int minute = ((int) Math.floor((double)time / 60000.0)) * (time > 0 ? 1 : 0);
    int seconds = ((int)Math.floor((double)time / 1000.0) - (minute*60)) * (time > 0 ? 1 : 0);
    String timerString = (("00" + minute).substring(String.valueOf(minute).length())+":"+("00" + seconds).substring(String.valueOf(seconds).length()));
    Log.i("TIMER_SERVICE",timerString);
    return timerString;
  }

  public void updateServiceNotification(Notification notification) {
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.notify(NOTIFICATION_ID, notification);
  }

  class Timer implements Runnable {
    class PomodoroTimerCountDown extends CountDownTimer {
      public PomodoroTimerCountDown(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        isRunning = true;
        updateServiceNotification(makeServiceNotification(NOTIFICATION_FLAG_IS_RUNNING));
      }

      @Override
      public void onTick(long l) {
        millisRemain = l;
        updateServiceNotification(makeServiceNotification(l));

        try {
          if (tickingMediaPlayer != null && !tickingMediaPlayer.isPlaying() && millisRemain < 4000) {
            //Start tickling sound
            tickingMediaPlayer.start();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }

        callTickCallBack(millisRemain);
      }

      @Override
      public void onFinish() {
        millisRemain = 0;
        updateServiceNotification(makeServiceNotification(
                runningState == WORK_STATE ? NOTIFICATION_FLAG_WORK_FINISHED : NOTIFICATION_FLAG_BREAK_FINISHED
        ));

        if (tickingMediaPlayer != null) {
          // Stop the tickling sound and release media player
          tickingMediaPlayer.stop();
          tickingMediaPlayer.release();
          tickingMediaPlayer = null;
        }

        if (alarmUri == null) {
          alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        alarmMediaPlayer = MediaPlayer.create(getApplicationContext(), alarmUri);

        if (alarmMediaPlayer != null) {
          PlayLoopSound playLoopSound = new PlayLoopSound(5000, 1000, alarmMediaPlayer);
          playLoopSound.start();
        }

        switchState();
        callTickCallBack(millisRemain);
        
        isRunning = false;

        if(runningState != WORK_STATE && autoStartBreakSetting){
          startTimer();
        } else if (runningState == WORK_STATE && autoStartPomodoroSetting) {
          startTimer();
        }

      }
    }

    @Override
    public void run() {
      try {
        tickingMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.count_down_sound);
      } catch (Exception e) {
        e.printStackTrace();
      }
      timer = new PomodoroTimerCountDown(millisRemain, 1000);
      timer.start();
    }
  }

  static class PlayLoopSound extends CountDownTimer {
    MediaPlayer player;

    public PlayLoopSound(long millisInFuture, long countDownInterval, MediaPlayer mediaPlayer) {
      super(millisInFuture, countDownInterval);
      player = mediaPlayer;
      try {
        player.setLooping(true);
        player.start();
      } catch (Exception ignore) {
        player = null;
      }
    }

    @Override
    public void onTick(long l) {

    }

    @Override
    public void onFinish() {
      if (player == null) {
        return;
      }
      try {
        player.stop();
        player.release();
      } catch (Exception ignore) {
      }
    }
  }

  public void setTickCallBack(TimerTickCallBack tickCallBack) {
    this.tickCallBack = tickCallBack;
    callTickCallBack(millisRemain);
  }

  public void callTickCallBack(long millis) {
    if (tickCallBack == null) {
      return;
    }
    try {
      tickCallBack.call(millis);
    } catch (Exception ignore) {
    }
  }

  public void setStateChangeCallBack(TimerStateChangeCallBack stateChangeCallBack) {
    this.stateChangeCallBack = stateChangeCallBack;
    callStateChangeCallBack(calculateCurrentState(),0,0);
  }

  public void setOnFinishCallback(TimerOnFinishCallback onFinishCallback) {
    this.onFinishCallback = onFinishCallback;
  }

  public void callOnFinishCallback(boolean isAutoSwitchTask) {
    if (onFinishCallback == null) {
      return;
    }
    try {
      onFinishCallback.onFinish(isAutoSwitchTask);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void callStateChangeCallBack(int newState, long timePrevSate, int oldState) {
    if (stateChangeCallBack == null) {
      return;
    }
    try {
      stateChangeCallBack.onStateChange(newState,timePrevSate,oldState);
    } catch (Exception ignore) {
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    startForeground(NOTIFICATION_ID, makeServiceNotification(NOTIFICATION_FLAG_IS_RUNNING));
    updateServiceNotification(makeServiceNotification(NOTIFICATION_FLAG_IS_RUNNING));
    return super.onStartCommand(intent, flags, startId);
  }

  public void startTimer() {
    if (!isRunning) {
      timerRunnable = new Timer();
      timerHandler = new Handler();
      timerHandler.post(timerRunnable);
    }
  }
  public void pauseTimer() {
    if (isRunning && timer != null) {
      timer.cancel();
      isRunning = false;
      timer.cancel();
      timer = null;
      timerHandler.removeCallbacks(timerRunnable);
    }
    callTickCallBack(millisRemain);
    updateServiceNotification(makeServiceNotification(NOTIFICATION_FLAG_IS_RUNNING));
  }
  public void resetTimer() {
    millisRemain = workMillis;
    if (isRunning && timer != null) {
      timer.cancel();
      timerHandler.removeCallbacks(timerRunnable);
      isRunning = false;
    }
    runningState = WORK_STATE;
    timerCount = 0;
    cycleCount = 0;
    callTickCallBack(millisRemain);
    callStateChangeCallBack(WORK_STATE,0,WORK_STATE);
    updateServiceNotification(makeServiceNotification(NOTIFICATION_FLAG_IS_RUNNING));
  }

  public void setStateTime(UserTimerSettings settings) {
    pauseTimer();

    workMillis = settings.workMillis;
    shortBreakMillis = settings.shortBreakMillis;
    longBreakMillis = settings.shortBreakMillis;
    millisRemain = settings.workMillis;
    alarmUri = settings.alarmUri;
    autoStartBreakSetting = settings.autoStartBreakSetting;
    autoStartPomodoroSetting = settings.autoStartPomodoroSetting;
    longBreakInterValSetting = settings.longBreakInterValSetting;

    resetTimer();
  }

  public int calculateCurrentState() {
    long LONG_BREAK_INTERVAL = (longBreakInterValSetting * 2) - 1;
    if (timerCount % 2 == 0) {
      return WORK_STATE;
    } else if (timerCount % 2 == 1 && timerCount != LONG_BREAK_INTERVAL) {
      return SHORT_BREAK_STATE;
    }
    return LONG_BREAK_STATE;
  }

  public void switchState() {
    timerCount += 1;
    cycleCount += (timerCount % 2 == 0) ? 1 : 0;
    long LONG_BREAK_INTERVAL = (longBreakInterValSetting * 2) - 1;

    int oldState = runningState;
    long stateTime = 0;
    switch (oldState){
      case WORK_STATE:{
        stateTime = workMillis;
        break;
      }
      case SHORT_BREAK_STATE:{
        stateTime = shortBreakMillis;
        break;
      }
      case LONG_BREAK_STATE:{
        stateTime = longBreakMillis;

        break;
      }
    }
    long timePassed = stateTime - millisRemain;

    if (timerCount % 2 == 0) {
      runningState = calculateCurrentState();
      millisRemain = workMillis;
      callStateChangeCallBack(WORK_STATE,timePassed,oldState);
    } else if (timerCount % 2 == 1 && timerCount != LONG_BREAK_INTERVAL) {
      runningState = calculateCurrentState();
      millisRemain = shortBreakMillis;
      callOnFinishCallback(false);
      callStateChangeCallBack(SHORT_BREAK_STATE,timePassed,oldState);
    } else {
      runningState = calculateCurrentState();
      timerCount = -1;
      millisRemain = longBreakMillis;
      callOnFinishCallback(false);
      callStateChangeCallBack(LONG_BREAK_STATE,timePassed,oldState);
    }
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.i("UNBIND_SERVICE","Service is being unbind");
    tickCallBack = null;
    return super.onUnbind(intent);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public void skipTimer() {

    if (isRunning && timer != null) {
      timer.cancel();
      isRunning = false;
      timerHandler.removeCallbacks(timerRunnable);

    }

    if (tickingMediaPlayer != null) {
      tickingMediaPlayer.stop();
      tickingMediaPlayer.release();
      tickingMediaPlayer = null;
    }


    callTickCallBack(millisRemain);
    switchState();
    callTickCallBack(millisRemain);
    updateServiceNotification(makeServiceNotification(NOTIFICATION_FLAG_IS_RUNNING));
  }

  public class LocalBinder extends Binder {
    TimerService getService() {
      return TimerService.this;
    }
  }

  public class ServiceControlBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      int reqCode = intent.getIntExtra(TIMER_REQ_CODE_KEY,100);
      if(reqCode == START_TIMER_REQ_CODE){
        startTimer();
      } else if (reqCode == PAUSE_TIMER_REQ_CODE) {
        pauseTimer();
      } else if (reqCode == SKIP_TIMER_REQ_CODE) {
        skipTimer();
      }
    }
  }

  public interface TimerTickCallBack {
    void call(long remainMillis) throws Exception;
  }

  public interface TimerStateChangeCallBack {
    void onStateChange(int newState,long timePrevState,int oldState) throws NotImplementedError;
  }

  public interface TimerOnFinishCallback {
    void onFinish(boolean isAutoSwitchTask) throws NotImplementedError;
  }


}