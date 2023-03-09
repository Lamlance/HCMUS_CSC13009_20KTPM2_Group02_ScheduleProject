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
//import android.os.Looper;

public class TimerService extends Service {
  private final IBinder binder = new LocalBinder();
  CountDownTimer timer;
  private static final long START_TIME_IN_MILLIS = 1500*1000;
  long millisRemain = 0;

  private static final String CHANNEL_ID = "TimerNotificationChanel";
  private static final int NOTIFICATION_ID=6969;
  public TimerTickCallBack tickCallBack = null;
  Intent timerIntent;

  NotificationChannel notificationChannel;

  public TimerService() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Context context = getApplicationContext();
    timerIntent = new Intent(context,TimerService.class); // Build the intent for the service
    context.startForegroundService(timerIntent);

    notificationChannel = new NotificationChannel(CHANNEL_ID,"Timer", NotificationManager.IMPORTANCE_DEFAULT);
    notificationChannel.setDescription("Notification for pomodoro timer service");
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(notificationChannel);


  }

  public Notification makeServiceNotification(String detail){
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, timerIntent, PendingIntent.FLAG_IMMUTABLE);
    Notification notification = new Notification.Builder(this,CHANNEL_ID)
            .setContentTitle("Pomodoro timer")
            .setContentText(detail)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build();
    return notification;
  }

  public void updateServiceNotification(Notification notification){
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.notify(NOTIFICATION_ID,notification);
  }

  class Timer implements Runnable {
    @Override
    public void run() {
      timer = new CountDownTimer(START_TIME_IN_MILLIS,1000) {
        @SuppressLint("DefaultLocale")
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
    Handler handler = new Handler();
    handler.post(new Timer());
  }

  public void pauseTimer(){
    timer.cancel();
  }

  public void resetTimer() {
    millisRemain = START_TIME_IN_MILLIS;
    timer.cancel();
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