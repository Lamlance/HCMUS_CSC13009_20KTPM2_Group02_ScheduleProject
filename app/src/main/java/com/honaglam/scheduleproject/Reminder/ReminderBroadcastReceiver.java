package com.honaglam.scheduleproject.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
    mediaPlayer.start();
    CountDownTimer timer = new CountDownTimer(5*1000,1*1000) {
      @Override
      public void onTick(long l) {}
      @Override
      public void onFinish() {
        mediaPlayer.stop();
      }
    };
    timer.start();

  }
}
