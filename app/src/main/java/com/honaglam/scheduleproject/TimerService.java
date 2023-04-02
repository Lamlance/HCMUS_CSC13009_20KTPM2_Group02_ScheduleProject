package com.honaglam.scheduleproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import kotlin.NotImplementedError;
//import android.os.Looper;

public class TimerService extends Service {
  private final IBinder binder = new LocalBinder();
  CountDownTimer timer;

  private static final long DEFAULT_WORK_TIME = 5000; //5second
  private static final long DEFAULT_SHORT_BREAK_TIME = 8000; //6second
  private static final long DEFAULT_LONG_BREAK_TIME = 10000; //7second

  public static final int NONE_STATE = 0;
  public static final int WORK_STATE = 1;
  public static final int SHORT_BREAK_STATE = 2;
  public static final int LONG_BREAK_STATE = 3;


  long millisRemain = 0;
  long workMillis = 0;
  long shortBreakMillis = 0;
  long longBreakMillis = 0;

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
  int runningState = NONE_STATE;
  int timerCount = 0;
  int cycleCount = 0;
  NotificationCompat.Builder notificationBuilder;
  TimerOnFinishCallback onFinishCallback;
  public TimerService() {

  }

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

    // timerFragment = new TimerFragment();
  }

  public Notification makeServiceNotification(String detail) {
    notificationBuilder.setContentText(detail);
    return notificationBuilder.build();
  }

  public void updateServiceNotification(Notification notification) {
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.notify(NOTIFICATION_ID, notification);
  }

  class Timer implements Runnable {
    private MediaPlayer alarmMediaPlayer;
    private MediaPlayer tickingMediaPlayer;

    class PomodoroTimerCountDown extends CountDownTimer {
      public PomodoroTimerCountDown(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
      }

      @Override
      public void onTick(long l) {
        millisRemain = l;
        updateServiceNotification(makeServiceNotification(String.format("%d", l)));

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
        runningState = NONE_STATE;

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

        // TODO: Set isAutoSwitchTask after adding state of timer setting done

        switchState();
        runningState = NONE_STATE;
        callTickCallBack(millisRemain);

      }
    }

    @Override
    public void run() {
      //runningState = WORK_STATE;
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

  public boolean callTickCallBack(long millis) {
    if (tickCallBack == null) {
      return false;
    }
    try {
      tickCallBack.call(millis);
    } catch (Exception ignore) {
      return false;
    }
    return true;
  }

  public void setStateChangeCallBack(TimerStateChangeCallBack stateChangeCallBack) {
    this.stateChangeCallBack = stateChangeCallBack;
    callStateChangeCallBack(calculateCurrentState());
  }

  public void setOnFinishCallback(TimerOnFinishCallback onFinishCallback) {
    this.onFinishCallback = onFinishCallback;
  }

  public boolean callOnFinishCallback(boolean isAutoSwitchTask) {
    if (onFinishCallback == null) {
      return false;
    }
    try {
      onFinishCallback.onFinish(isAutoSwitchTask);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }


  public boolean callStateChangeCallBack(int newState) {
    if (stateChangeCallBack == null) {
      return false;
    }
    try {
      stateChangeCallBack.onStateChange(newState);
    } catch (Exception ignore) {
      return false;
    }
    return true;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    startForeground(NOTIFICATION_ID, makeServiceNotification("Hello !"));
    updateServiceNotification(makeServiceNotification("Hello User"));
    return super.onStartCommand(intent, flags, startId);
  }


  public void startTimer() {
    if (runningState == NONE_STATE) {
      timerRunnable = new Timer();
      timerHandler = new Handler();
      timerHandler.post(timerRunnable);
    }
  }


  public void pauseTimer() {
    if (runningState != NONE_STATE && timer != null) {
      timer.cancel();
      runningState = NONE_STATE;
    }
    if (tickCallBack == null) {
      return;
    }
    callTickCallBack(millisRemain);
  }

  public void resetTimer() {
    millisRemain = workMillis;
    if (runningState != NONE_STATE && timer != null) {
      timer.cancel();
      timerHandler.removeCallbacks(timerRunnable);
      callTickCallBack(millisRemain);
      runningState = NONE_STATE;
    }
  }

  public void setStateTime(long workTime, long shortBreakTime, long longBreakTime, Uri alarmSound) {
    if (runningState != NONE_STATE && timer != null) {
      timer.cancel();
      runningState = NONE_STATE;
    }
    workMillis = workTime;
    shortBreakMillis = shortBreakTime;
    longBreakMillis = longBreakTime;
    millisRemain = workTime;
    alarmUri = alarmSound;
  }

  @Override
  public void onDestroy() {
    timer.cancel();
    super.onDestroy();
  }

  public void setStateTime(
          long workTime, long shortBreakTime, long longBreakTime,
          Uri alarmSound, boolean autoStartBreak, boolean autoStartPomodoro,
          long longBreakInterVal) {
    if (runningState != NONE_STATE && timer != null) {
      timer.cancel();
      runningState = NONE_STATE;
    }
    workMillis = workTime;
    shortBreakMillis = shortBreakTime;
    longBreakMillis = longBreakTime;
    millisRemain = workTime;
    alarmUri = alarmSound;
    autoStartBreakSetting = autoStartBreak;
    autoStartPomodoroSetting = autoStartPomodoro;
    longBreakInterValSetting = longBreakInterVal;
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
    if (timerCount % 2 == 0) {
      runningState = calculateCurrentState();
      millisRemain = workMillis;
      callStateChangeCallBack(WORK_STATE);
    } else if (timerCount % 2 == 1 && timerCount != LONG_BREAK_INTERVAL) {
      runningState = calculateCurrentState();
      millisRemain = shortBreakMillis;
      callOnFinishCallback(false);
      callStateChangeCallBack(SHORT_BREAK_STATE);
    } else {
      runningState = calculateCurrentState();
      timerCount = -1;
      millisRemain = longBreakMillis;
      callOnFinishCallback(false);
      callStateChangeCallBack(LONG_BREAK_STATE);
    }
  }

  @Override
  public boolean onUnbind(Intent intent) {
    tickCallBack = null;
    return super.onUnbind(intent);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public void skipTimer() {

    if (runningState != NONE_STATE && timer != null) {
      timer.cancel();
    }

    switchState();
    runningState = NONE_STATE;
    callTickCallBack(millisRemain);
  }

  public class LocalBinder extends Binder {
    TimerService getService() {
      return TimerService.this;
    }
  }

  public interface TimerTickCallBack {
    void call(long remainMillis) throws Exception;
  }

  public interface TimerStateChangeCallBack {
    void onStateChange(int newState) throws NotImplementedError;
  }

  public interface TimerOnFinishCallback {
    void onFinish(boolean isAutoSwitchTask) throws NotImplementedError;
  }
}