package com.honaglam.scheduleproject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
//import android.os.Looper;

public class TimerService extends Service {
  private final IBinder binder = new LocalBinder();
  CountDownTimer timer;
  private static final long DEFAULT_WORK_TIME = 25000; //25second
  private static final long DEFAULT_SHORT_BREAK_TIME = 5000; //5second
  private static final long DEFAULT_LONG_BREAK_TIME = 10000; //10second

  private static final int NONE_STATE = 0;
  private static final int WORK_STATE = 1;
  private static final int SHORT_BREAK_STATE = 2;
  private static final int LONG_BREAK_STATE = 3;

  long millisRemain = 0;
  long workMillis = 0;
  long shortBreakMillis = 0;
  long longBreakMillis = 0;

  private static final String CHANNEL_ID = "TimerNotificationChanel";
  private static final int NOTIFICATION_ID=6969;
  public TimerTickCallBack tickCallBack = null;
  Intent timerIntent;
  NotificationChannel notificationChannel;
  Handler timerHandler;
  Runnable timerRunnable;
  int runningState = NONE_STATE;
  NotificationCompat.Builder  notificationBuilder;
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

    timerIntent = new Intent(context,TimerService.class); // Build the intent for the service
    context.startForegroundService(timerIntent);

    notificationChannel = new NotificationChannel(CHANNEL_ID,"Timer", NotificationManager.IMPORTANCE_DEFAULT);
    notificationChannel.setDescription("Notification for pomodoro timer service");

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(notificationChannel);

    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, timerIntent, PendingIntent.FLAG_IMMUTABLE);
    notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Pomodoro timer")
            .setContentText("Welcome to pomodoro")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true);

  }

  public Notification makeServiceNotification(String detail){
    notificationBuilder.setContentText(detail);
    return notificationBuilder.build();
  }

  public void updateServiceNotification(Notification notification){
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.notify(NOTIFICATION_ID,notification);
  }

  class Timer implements Runnable {
    @Override
    public void run() {
      runningState = WORK_STATE;
      timer = new CountDownTimer(millisRemain,1000) {
        @Override
        public void onTick(long l) {
          millisRemain = l;
          updateServiceNotification(makeServiceNotification(String.format("%d",l)));
          if(tickCallBack != null){
            try {
              tickCallBack.call(millisRemain);
            }catch (Exception e){
              e.printStackTrace();
            }
          }
        }

        @Override
        public void onFinish() {
          millisRemain = 0;
          runningState = NONE_STATE;
          if(tickCallBack != null){
            try {
              tickCallBack.call(0);
            }catch (Exception e){
              e.printStackTrace();
            }
          }
        }
      };
      timer.start();
    }
  }


  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    startForeground(NOTIFICATION_ID,makeServiceNotification("Hello !"));
    updateServiceNotification(makeServiceNotification("Hello User"));

    return super.onStartCommand(intent, flags, startId);
  }

  public void startTimer(){
    if(runningState == NONE_STATE){
      millisRemain = workMillis;
      timerRunnable = new Timer();
      timerHandler = new Handler();
      timerHandler.post(timerRunnable);
    }

  }
  public void pauseTimer(){
    if(runningState != NONE_STATE && timer != null){
      timer.cancel();
      runningState = NONE_STATE;
      if(tickCallBack != null){
        try {
          tickCallBack.call(millisRemain);
        } catch (Exception ignored) {}
      }
    }
  }
  public void resetTimer() {
    millisRemain = workMillis;
    if(runningState != NONE_STATE && timer != null){
      timer.cancel();
      timerHandler.removeCallbacks(timerRunnable);
      if(tickCallBack != null){
        try {
          tickCallBack.call(millisRemain);
        } catch (Exception e) {}
      }
      runningState = NONE_STATE;
    }

  }
  public void setStateTime(long workTime,long shortBreakTime, long longBreakTime){
    if(runningState != NONE_STATE){
      timer.cancel();
      runningState = NONE_STATE;
    }
    workMillis = workTime;
    shortBreakMillis = shortBreakTime;
    longBreakMillis = longBreakTime;
    millisRemain = workTime;

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
    // TODO: Return the communication channel to the service.
    return binder;
  }
  public class LocalBinder extends Binder {
    TimerService getService() {
      return TimerService.this;
    }
  }

  public interface TimerTickCallBack{
    void call(long remainMillis) throws Exception;
  }

}