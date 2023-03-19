package com.honaglam.scheduleproject.Reminder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;

public class ReminderAddDialog extends Dialog {
  public interface ReminderDataCallBack{
    void onSubmit(ReminderData data) throws NotImplementedError;
  }

  ReminderDataCallBack dataCallBack = null;
  public ReminderAddDialog(@NonNull Context context) {
    super(context);
    //this.dataCallBack = dataCallBack;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_reminder_add);
  }
}
