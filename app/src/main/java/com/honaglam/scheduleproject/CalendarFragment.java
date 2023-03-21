package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;
import com.honaglam.scheduleproject.Reminder.ReminderAddDialog;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapter;


import java.util.Calendar;
import java.util.List;
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

  TextView txtBigDate;
  TextView txtBigWeekDate;

  MainActivity mainActivity;
  Context context;

  ReminderRecyclerAdapter reminderRecyclerAdapter;
  int selectedDate = -1;
  int selectedMonth = -1;
  int selectedYear = -1;
  int selectedHour = -1;
  int selectedMinute = -1;
  int selectedWeekDay = -1;


  public static CalendarFragment newInstance() {
    CalendarFragment fragment = new CalendarFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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

    txtBigDate = view.findViewById(R.id.txtBigDateDisplay);
    txtBigWeekDate = view.findViewById(R.id.txtBigWeekDateDisplay);
    txtSelectDate = view.findViewById(R.id.txtSelectDate);

    recyclerCalendar = view.findViewById(R.id.recyclerCalendar);

    ani_month_r2l = AnimationUtils.loadAnimation(context, R.anim.calendar_month_change_r2l);
    ani_month_l2r = AnimationUtils.loadAnimation(context, R.anim.calendar_month_change_l2r);

    calendarRecyclerViewAdapter = new CalendarRecyclerViewAdapter(context);
    recyclerCalendar.setAdapter(calendarRecyclerViewAdapter);
    calendarRecyclerViewAdapter.setSelectDateCallBack(new DateSelectCallBack());

    view.findViewById(R.id.btnIncreaseMonth).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        calendarRecyclerViewAdapter.increaseMonth();
        recyclerCalendar.startAnimation(ani_month_r2l);
      }
    });
    view.findViewById(R.id.btnDecreaseMonth).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        calendarRecyclerViewAdapter.decreaseMonth();
        recyclerCalendar.startAnimation(ani_month_l2r);
      }
    });

    RecyclerView reminderRecycler = view.findViewById(R.id.recyclerReminders);
    reminderRecyclerAdapter = new ReminderRecyclerAdapter(context, new ReminderRecyclerAdapter.ReminderListGetter() {
      @Override
      public List<ReminderData> get() throws NotImplementedError {
        return mainActivity.reminderDataList;
      }
    });
    reminderRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
    reminderRecycler.setAdapter(reminderRecyclerAdapter);
    ItemTouchHelper helper = new ItemTouchHelper(recyclerReminderSwipeHelper);
    helper.attachToRecyclerView(reminderRecycler);

    ((Button) view.findViewById(R.id.btnSetReminder)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ReminderAddDialog reminderAddDialog = new ReminderAddDialog(context, new ReminderAddDialog.ReminderDataCallBack() {
          @Override
          public void onSubmit(String name, int hour24h, int minute) throws NotImplementedError {
            selectedHour = hour24h;
            selectedMinute = minute;
            AddReminder(name);
          }
        });
        reminderAddDialog.show();
      }
    });

    updateDateUI();
  }

  private void AddReminder(String name) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(selectedYear, selectedMonth, selectedDate, selectedHour, selectedMinute, 0);
    long remindTime = calendar.getTimeInMillis();
    Log.d("DATE", String.format("%d", remindTime));
    int size = mainActivity.addReminder(name, remindTime);
    reminderRecyclerAdapter.notifyItemInserted(size - 1);
  }

  private void updateDateUI() {
    String dateStr = String.format(
            Locale.getDefault(), "%d/%d/%d", selectedDate, selectedMonth + 1, selectedYear);
    txtSelectDate.setText(dateStr);
    txtBigDate.setText(String.format(Locale.getDefault(), "%d", selectedDate));
    txtBigWeekDate.setText(CalendarRecyclerViewAdapter.WEEKDAY_NAMES_MEDIUM[selectedWeekDay - 1]);

  }

  class DateSelectCallBack implements CalendarRecyclerViewAdapter.SelectDateCallBackInterface {
    @Override
    public void clickDate(int date, int month, int year, int weekDay) throws NotImplementedError {
      if ((date != selectedDate || month != selectedMonth || year != selectedYear)
              && (date * month * year * weekDay) > 0) {
        int size = mainActivity.getReminderAt(date, month, year);
        if(reminderRecyclerAdapter != null){
          reminderRecyclerAdapter.notifyItemRangeChanged(0,size);
        }
      }

      selectedDate = date;
      selectedMonth = month;
      selectedYear = year;
      selectedWeekDay = weekDay;

      updateDateUI();

    }
  }

  ItemTouchHelper.SimpleCallback recyclerReminderSwipeHelper = new ItemTouchHelper.SimpleCallback(
          0,
          ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
      return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
      int pos = reminderRecyclerAdapter.selectedItemPos;
      mainActivity.reminderDataList.remove(pos);
      reminderRecyclerAdapter.notifyItemRemoved(pos);
      Toast.makeText(context, String.format(Locale.getDefault(), "Delete %d ", pos), Toast.LENGTH_SHORT).show();
    }
  };
}