package com.honaglam.scheduleproject.Reminder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;

public class ReminderAddDialog extends Dialog {
  public interface ReminderDataCallBack{
    void onSubmit(String name,int hour24h,int minute) throws NotImplementedError;
  }


  ReminderDataCallBack dataCallBack = null;
  public ReminderAddDialog(@NonNull Context context,ReminderDataCallBack dataCallBack) {
    super(context);
    this.dataCallBack = dataCallBack;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_reminder_add);

    TimePicker timePicker = findViewById(R.id.timeReminderDialog);
    timePicker.setIs24HourView(true);
    EditText editText = findViewById(R.id.txtEditNameReminder);
    ((Button)findViewById(R.id.btnAddReminderDialog)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String name = editText.getText().toString();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        try{
          dataCallBack.onSubmit(name,hour,minute);
        }catch (Exception ignore){}
        ReminderAddDialog.this.dismiss();
      }
    });

    ((Button)findViewById(R.id.btnCancelReminderDialog)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });

  }
}
