package com.honaglam.scheduleproject.Reminder;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.honaglam.scheduleproject.R;

import java.util.AbstractMap;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kotlin.NotImplementedError;

public class ReminderAddDialog extends Dialog {
  public interface ReminderDataCallBack {
    void onSubmit(String name, int hour24h, int minute);

    void onSubmitWeekly(String name, int hour24h, int minute, HashSet<Integer> dailyReminder);

    default void onSubmit(String name, Calendar setDate) {
      ReminderDataCallBack.this
              .onSubmit(name, setDate.get(Calendar.HOUR_OF_DAY), setDate.get(Calendar.MINUTE));
    }
    default void onSubmitWeekly(String name, Calendar setDate,HashSet<Integer> dailyReminder) {
      ReminderDataCallBack.this
              .onSubmitWeekly(name, setDate.get(Calendar.HOUR_OF_DAY), setDate.get(Calendar.MINUTE),dailyReminder);
    }
  }

  private static final HashMap<String, Integer> DATE_TO_CALENDAR_INT = new HashMap<String, Integer>() {{
    put("SU", Calendar.SUNDAY);
    put("MO", Calendar.MONDAY);
    put("TU", Calendar.TUESDAY);
    put("WE", Calendar.WEDNESDAY);
    put("TH", Calendar.THURSDAY);
    put("FR", Calendar.FRIDAY);
    put("SA", Calendar.SATURDAY);
  }};


  Animation shakeAnim;
  Calendar currCalendar;
  ReminderDataCallBack dataCallBack = null;
  LinearLayout linearLayoutDailyBtn = null;
  EditText editTextName;
  TimePicker timePicker;
  DatePicker datePicker;
  HashSet<Integer> dailyReminder = new HashSet<Integer>();
  SwitchCompat switchDaily;

  public ReminderAddDialog(
          @NonNull Context context,
          ReminderDataCallBack dataCallBack,
          Calendar current) {
    super(context);
    this.dataCallBack = dataCallBack;
    this.shakeAnim = AnimationUtils.loadAnimation(context, R.anim.shake);
    this.shakeAnim.setInterpolator(new CycleInterpolator(7));
    this.currCalendar = current;
  }

  boolean displayCalendar = false;

  public ReminderAddDialog(
          @NonNull Context context,
          ReminderDataCallBack dataCallBack) {
    super(context);
    this.dataCallBack = dataCallBack;
    this.shakeAnim = AnimationUtils.loadAnimation(context, R.anim.shake);
    this.shakeAnim.setInterpolator(new CycleInterpolator(7));
    this.currCalendar = Calendar.getInstance();
    displayCalendar = true;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_reminder_add);

    timePicker = findViewById(R.id.timeReminderDialog);
    timePicker.setIs24HourView(true);
    editTextName = findViewById(R.id.txtEditNameReminder);
    datePicker = findViewById(R.id.dateReminderDialog);
    datePicker.setVisibility(displayCalendar ? View.VISIBLE : View.GONE);

    findViewById(R.id.btnAddReminderDialog).setOnClickListener(new AddReminderButton());

    findViewById(R.id.btnCancelReminderDialog).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });
    linearLayoutDailyBtn = findViewById(R.id.layoutAddReminderDaily);
    for (int i = 0; i < linearLayoutDailyBtn.getChildCount(); i++) {
      View v = linearLayoutDailyBtn.getChildAt(i);
      if (v instanceof AppCompatToggleButton) {
        ((AppCompatToggleButton) v).setOnCheckedChangeListener(new DateToggleButtonClick());
        String txt = ((AppCompatToggleButton) v).getText().toString();
        ((AppCompatToggleButton) v).setChecked(DATE_TO_CALENDAR_INT.get(txt) == currCalendar.get(Calendar.DAY_OF_WEEK));
      }
    }
    switchDaily = findViewById(R.id.switchAddReminderDaily);
    switchDaily.setOnCheckedChangeListener(new SwitchDailyClick());


    setEnableDaily(false);
  }


  void setEnableDaily(boolean isEnable) {
    linearLayoutDailyBtn.setVisibility(isEnable ? View.VISIBLE : View.GONE);

    dailyReminder.add(currCalendar.get(Calendar.DAY_OF_WEEK));
  }

  class AddReminderButton implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      String name = editTextName.getText().toString();
      int hour = timePicker.getHour();
      int minute = timePicker.getMinute();

      if (name.isEmpty()) {
        editTextName.startAnimation(shakeAnim);
        editTextName.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getContext(), R.color.red_700)
        ));
        Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
        return;
      }

      try {
        currCalendar.set(Calendar.HOUR_OF_DAY,hour);
        currCalendar.set(Calendar.MINUTE,minute);

        if(displayCalendar){
          currCalendar.set(Calendar.YEAR,datePicker.getYear());
          currCalendar.set(Calendar.MONTH,datePicker.getMonth());
          currCalendar.set(Calendar.DATE,datePicker.getDayOfMonth());
        }

        if (switchDaily.isChecked()) {
          dataCallBack.onSubmitWeekly(name, currCalendar, dailyReminder);
        } else {
          dataCallBack.onSubmit(name, currCalendar);
        }
      } catch (Exception ignore) {
      }
      ReminderAddDialog.this.dismiss();
    }
  }

  class SwitchDailyClick implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
      setEnableDaily(b);
    }
  }

  class DateToggleButtonClick implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
      try {
        if (DATE_TO_CALENDAR_INT.get(compoundButton.getText()) == currCalendar.get(Calendar.DAY_OF_WEEK)) {
          compoundButton.setChecked(true);
          return;
        }

        if (b) {
          dailyReminder.add(DATE_TO_CALENDAR_INT.get(compoundButton.getText()));
        } else {
          dailyReminder.remove(DATE_TO_CALENDAR_INT.get(compoundButton.getText()));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
}
