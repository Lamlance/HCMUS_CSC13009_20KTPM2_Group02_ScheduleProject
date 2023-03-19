package com.honaglam.scheduleproject.Reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;

import com.honaglam.scheduleproject.MainActivity;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
  public static final String NAME_TAG = "ReminderName";
  public static final String NOTIFICATION_KEY = "ReminderNotification";
  public static String NOTIFICATION_ID_KEY = "ReminderNotificationId";
  @Override
  public void onReceive(Context context, Intent intent) {
    MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
    mediaPlayer.start();

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context. NOTIFICATION_SERVICE );
    Notification notification = intent.getParcelableExtra( NOTIFICATION_KEY) ;
    String name = intent.getStringExtra(NAME_TAG);
    int notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY,0);

    NotificationChannel notificationChannel = new NotificationChannel(
            MainActivity.NOTIFICATION_CHANEL_ID, "REMINDER_NOTIFICATION_CHANEL" , NotificationManager. IMPORTANCE_HIGH ) ;
    notificationManager.createNotificationChannel(notificationChannel) ;

    CountDownTimer timer = new CountDownTimer(5*1000,1*1000) {
      @Override
      public void onTick(long l) {}
      @Override
      public void onFinish() {
        mediaPlayer.stop();
      }
    };
    timer.start();
    notificationManager.notify(notificationId , notification) ;
  }
}
