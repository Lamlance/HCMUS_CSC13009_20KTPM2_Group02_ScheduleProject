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

  private static final String CHANNEL_ID = "TimerNotificationChanel";
  private static final int NOTIFICATION_ID=6969;
  public TimerTickCallBack tickCallBack = null;
  Intent timerIntent;
  NotificationChannel notificationChannel;
  Handler timerHandler;
  Runnable timerRunnable;

  int runningState = NONE_STATE;
  int timerCount = 0;
  int cycleCount = 0;
  //private static int currentState = 1; //1. WORK - SHORT - LONG
  //private static int countCycle = 0;
  private static final int COMPLETE_CYCLE = 7;

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
      //currentState = WORK_STATE;
      //runningState = currentState;
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
          if(tickCallBack != null){
            try {
              /*
              countCycle++;
              if (countCycle < completedCycle) {
                if (countCycle % 2 == 0) {
                  countCycle = 0;
                  currentState = WORK_STATE;
                  runningState = currentState;
                  millisRemain = workMillis;
                }
                else {
                  currentState = SHORT_BREAK_STATE;
                  runningState = currentState;
                  millisRemain = shortBreakMillis;
                }
                Log.d("State", String.valueOf(runningState));
                Log.d("millis", String.valueOf(millisRemain));
                Log.d("Count", String.valueOf(countCycle));
              }
              else if (countCycle == completedCycle) {
                currentState = LONG_BREAK_STATE;
                runningState = currentState;
                millisRemain = longBreakMillis;
                Log.d("State", String.valueOf(runningState));
                Log.d("millis", String.valueOf(millisRemain));
                Log.d("Count", String.valueOf(countCycle));
              }
              else {
                countCycle = 0;
                currentState = WORK_STATE;
                runningState = currentState;
                millisRemain = workMillis;
                Log.d("State", String.valueOf(runningState));
                Log.d("millis", String.valueOf(millisRemain));
                Log.d("Count", String.valueOf(countCycle));
              }
              */
              //tickCallBack.call(millisRemain);
              switchState();
              tickCallBack.call(millisRemain);
            }catch (Exception e){
              e.printStackTrace();
            }
            runningState = NONE_STATE;
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

  public void switchState(){
    timerCount += 1;
    cycleCount += (timerCount  % 2 == 0) ? 1 : 0;

    if(timerCount % 2 == 0){
      runningState = WORK_STATE;
      millisRemain = workMillis;
    } else if (timerCount % 2 == 1 && timerCount != 7) {
      runningState = SHORT_BREAK_STATE;
      millisRemain = shortBreakMillis;
    }else {
      runningState = LONG_BREAK_STATE;
      timerCount = -1;
      millisRemain = longBreakMillis;
    }

    Log.d("Running state", String.valueOf(runningState));
    Log.d("Timer count", String.valueOf(timerCount));
    Log.d("Cycle count", String.valueOf(cycleCount));
  }

  public void startTimer() {
    if(runningState == NONE_STATE){
      //runningState = currentState;

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

  public void skipTimer() {

    if(runningState != NONE_STATE && timer != null){
      timer.cancel();
    }

    switchState();
    runningState = NONE_STATE;
    try {
      tickCallBack.call(millisRemain);
    } catch (Exception ignore) {}

//    if (runningState != NONE_STATE && timer != null && tickCallBack != null) {
//      if (runningState == WORK_STATE && countCycle < completedCycle) {
//        countCycle++;
//        runningState = SHORT_BREAK_STATE;
//        millisRemain = shortBreakMillis;
//        tickCallBack.call(millisRemain);
//        Log.d("State", String.valueOf(runningState));
//        Log.d("millis", String.valueOf(millisRemain));
//      }
//      else if (runningState == SHORT_BREAK_STATE && countCycle < completedCycle) {
//        countCycle++;
//        runningState = WORK_STATE;
//        millisRemain = workMillis;
//        tickCallBack.call(millisRemain);
//
//        Log.d("State", String.valueOf(runningState));
//        Log.d("millis", String.valueOf(millisRemain));
//      }
//      else if (runningState == SHORT_BREAK_STATE && countCycle == completedCycle) {
//        countCycle++;
//        runningState = LONG_BREAK_STATE;
//        millisRemain = longBreakMillis;
//        tickCallBack.call(millisRemain);
//        Log.d("State", String.valueOf(runningState));
//        Log.d("millis", String.valueOf(millisRemain));
//      }
//    }
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