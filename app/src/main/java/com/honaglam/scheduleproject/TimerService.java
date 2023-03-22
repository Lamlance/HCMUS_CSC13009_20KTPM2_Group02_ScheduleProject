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

import androidx.core.app.NotificationCompat;
//import android.os.Looper;

public class TimerService extends Service {
  private final IBinder binder = new LocalBinder();
  CountDownTimer timer;

  private static final long DEFAULT_WORK_TIME = 5000; //5second
  private static final long DEFAULT_SHORT_BREAK_TIME = 8000; //6second
  private static final long DEFAULT_LONG_BREAK_TIME = 10000; //7second

  private static final int NONE_STATE = 0;
  private static final int WORK_STATE = 1;
  private static final int SHORT_BREAK_STATE = 2;
  private static final int LONG_BREAK_STATE = 3;


  long millisRemain = 0;
  long workMillis = 0;
  long shortBreakMillis = 0;
  long longBreakMillis = 0;

  Uri alarmUri;

  private static final String CHANNEL_ID = "TimerNotificationChanel";
  private static final int NOTIFICATION_ID = 6969;
  public TimerTickCallBack tickCallBack = null;
  Intent timerIntent;
  NotificationChannel notificationChannel;
  Handler timerHandler;
  Runnable timerRunnable;


  int runningState = NONE_STATE;
  int timerCount = 0;
  int cycleCount = 0;

  NotificationCompat.Builder notificationBuilder;

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

    @Override
    public void run() {
      runningState = WORK_STATE;
      try {
        tickingMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.count_down_sound);
      } catch (Exception e) {
        e.printStackTrace();
      }

      timer = new CountDownTimer(millisRemain, 1000) {
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

          if(alarmUri == null){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
          }

          alarmMediaPlayer = MediaPlayer.create(getApplicationContext(), alarmUri);

          if (alarmMediaPlayer != null) {
            PlayLoopSound playLoopSound = new PlayLoopSound(5000,1000,alarmMediaPlayer);
            playLoopSound.start();
          }

          switchState();
          runningState = NONE_STATE;
          callTickCallBack(millisRemain);
        }
      };
      timer.start();

    }
  }

  class PlayLoopSound extends CountDownTimer {
    MediaPlayer player;
    public PlayLoopSound(long millisInFuture, long countDownInterval,MediaPlayer mediaPlayer) {
      super(millisInFuture, countDownInterval);
      player = mediaPlayer;
      try {
        player.setLooping(true);
        player.start();
      }catch (Exception ignore){player = null;}
    }

    @Override
    public void onTick(long l) {

    }

    @Override
    public void onFinish() {
      if(player == null){return;}
      try {
        player.stop();
        player.release();
      }catch (Exception ignore){}
    }
  }


  public boolean callTickCallBack(long millis){
    if(tickCallBack == null){return false;}
    try{
      tickCallBack.call(millis);
    }catch (Exception ignore){return false;}
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

  public void switchState() {
    timerCount += 1;
    cycleCount += (timerCount % 2 == 0) ? 1 : 0;

    if (timerCount % 2 == 0) {
      runningState = WORK_STATE;
      millisRemain = workMillis;
    } else if (timerCount % 2 == 1 && timerCount != 7) {
      runningState = SHORT_BREAK_STATE;
      millisRemain = shortBreakMillis;
    } else {
      runningState = LONG_BREAK_STATE;
      timerCount = -1;
      millisRemain = longBreakMillis;
    }

    Log.d("Running state", String.valueOf(runningState));
    Log.d("Timer count", String.valueOf(timerCount));
    Log.d("Cycle count", String.valueOf(cycleCount));
  }

  @Override
  public void onDestroy() {
    timer.cancel();
    super.onDestroy();
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

}