package com.honaglam.scheduleproject;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapter;

import java.util.Calendar;
import java.util.Locale;

import kotlin.NotImplementedError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {
  public CalendarFragment() {
    // Required empty public constructor
  }

  RecyclerView recyclerCalendar;
  CalendarRecyclerViewAdapter calendarRecyclerViewAdapter;
  TextView txtSelectDate;
  Animation ani_month_l2r;
  Animation ani_month_r2l;

  MainActivity mainActivity;
  Context context;

  ReminderRecyclerAdapter reminderRecyclerAdapter;
  int selectedDate = -1;
  int selectedMonth = -1;
  int selectedYear = -1;
  int selectedHour = -1;
  int selectedMinute = -1;
  public static CalendarFragment newInstance() {
    CalendarFragment fragment = new CalendarFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {

    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_calendar, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    context = getContext();
    mainActivity = (MainActivity) getActivity();

    recyclerCalendar = view.findViewById(R.id.recyclerCalendar);

    ani_month_r2l = AnimationUtils.loadAnimation(context,R.anim.calendar_month_change_r2l);
    ani_month_l2r = AnimationUtils.loadAnimation(context,R.anim.calendar_month_change_l2r);

    calendarRecyclerViewAdapter = new CalendarRecyclerViewAdapter(context);
    recyclerCalendar.setAdapter(calendarRecyclerViewAdapter);
    calendarRecyclerViewAdapter.setSelectDateCallBack(new DateSelectCallBack());

    txtSelectDate = view.findViewById(R.id.txtSelectDate);
    ((Button)view.findViewById(R.id.btnIncreaseMonth)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        calendarRecyclerViewAdapter.increaseMonth();
        txtSelectDate.setText(calendarRecyclerViewAdapter.getSelectDateString());
        recyclerCalendar.startAnimation(ani_month_r2l);
      }
    });
    ((Button)view.findViewById(R.id.btnDecreaseMonth)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        calendarRecyclerViewAdapter.decreaseMonth();
        txtSelectDate.setText(calendarRecyclerViewAdapter.getSelectDateString());
        recyclerCalendar.startAnimation(ani_month_l2r);

      }
    });
    (txtSelectDate).setText(calendarRecyclerViewAdapter.getSelectDateString());

    RecyclerView reminderRecycler = view.findViewById(R.id.recyclerReminders);
    reminderRecyclerAdapter= new ReminderRecyclerAdapter(context,mainActivity.reminderDataList);
    reminderRecycler.setAdapter(reminderRecyclerAdapter);

    TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        selectedHour = hour;
        selectedMinute = minute;
        AddReminder();
      }
    },0,0,true);
    ((Button)view.findViewById(R.id.btnSetReminder)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        timePickerDialog.show();
      }
    });
  }

  private void AddReminder(){
    Calendar calendar = Calendar.getInstance();
    calendar.set(selectedYear,selectedMonth,selectedDate,selectedHour,selectedMinute,0);
    long remindTime = calendar.getTimeInMillis();
    Log.d("DATE",String.format("%d",remindTime));
    mainActivity.addReminder(remindTime);
  }

  class DateSelectCallBack implements CalendarRecyclerViewAdapter.SelectDateCallBackInterface{
    @Override
    public void clickDate(int date, int month, int year) throws NotImplementedError {
      String dateStr = calendarRecyclerViewAdapter.getSelectDateString();
      selectedDate = date;
      selectedMonth = month;
      selectedYear = year;
      txtSelectDate.setText(dateStr);
      Toast.makeText(context,dateStr, Toast.LENGTH_SHORT).show();
    }
  }
}