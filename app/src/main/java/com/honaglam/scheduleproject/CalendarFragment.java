package com.honaglam.scheduleproject;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;
import com.honaglam.scheduleproject.Reminder.ReminderAddDialog;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Reminder.ReminderFilterDialog;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapter;


import java.util.Calendar;
import java.util.HashSet;
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

  long searchStartDate = -1;
  long searchEndDate = -1;
  ImageButton filterBtn;
  EditText txtSearchReminder;

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

    calendarRecyclerViewAdapter = new CalendarRecyclerViewAdapter(context, new GetReminderInMonth());
    recyclerCalendar.setAdapter(calendarRecyclerViewAdapter);

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

    view.findViewById(R.id.btnSetReminder).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ReminderAddDialog reminderAddDialog = new ReminderAddDialog(
                context,
                new AddReminderDialogCallBack(),
                calendarRecyclerViewAdapter.calendar);
        reminderAddDialog.show();
        reminderAddDialog.getWindow().setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
      }
    });

    txtSearchReminder = view.findViewById(R.id.txtEditSearchReminder);
    view.findViewById(R.id.btnSearchReminder).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        searchReminder();
      }
    });

    filterBtn = view.findViewById(R.id.btnFilterReminder);
    filterBtn.setOnClickListener(new FilterBtnClick());
    //updateDateUI();
    calendarRecyclerViewAdapter.setSelectDateCallBack(new DateSelectCallBack());

  }

  private void AddReminder(String name) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(selectedYear, selectedMonth, selectedDate, selectedHour, selectedMinute, 0);
    long remindTime = calendar.getTimeInMillis();
    //Log.d("DATE", String.format("%d", remindTime));
    int size = mainActivity.addReminder(name, remindTime);
    calendarRecyclerViewAdapter.addInMonthReminder(
            mainActivity.reminderDataList.get(size-1),selectedDate);
    reminderRecyclerAdapter.notifyItemInserted(size - 1);
  }

  private void AddReminderWeekly(String name,HashSet<Integer> weekly){
    Calendar calendar = Calendar.getInstance();
    calendar.set(selectedYear, selectedMonth, selectedDate, selectedHour, selectedMinute, 0);
    long remindTime = calendar.getTimeInMillis();
    int size = mainActivity.addReminderWeekly(name,remindTime,weekly);
    reminderRecyclerAdapter.notifyItemInserted(size - 1);
  }

  private void updateDateUI() {
    String dateStr = String.format(
            Locale.getDefault(), "%d/%d/%d", selectedDate, selectedMonth + 1, selectedYear);
    txtSelectDate.setText(dateStr);
    txtBigDate.setText(String.format(Locale.getDefault(), "%d", selectedDate));
    txtBigWeekDate.setText(CalendarRecyclerViewAdapter.WEEKDAY_NAMES_MEDIUM[selectedWeekDay - 1]);

  }

  class AddReminderDialogCallBack implements ReminderAddDialog.ReminderDataCallBack{
    @Override
    public void onSubmit(String name, int hour24h, int minute) {
      selectedHour = hour24h;
      selectedMinute = minute;
      AddReminder(name);
    }

    @Override
    public void onSubmitWeekly(String name, int hour24h, int minute, HashSet<Integer> dailyReminder) {
      selectedHour = hour24h;
      selectedMinute = minute;
      AddReminderWeekly(name,dailyReminder);
    }
  }

  class FilterBtnClick implements View.OnClickListener{
    OnSelectFromToDate onSelectFromToDate = new OnSelectFromToDate();
    ReminderFilterDialog reminderFilterDialog = new ReminderFilterDialog(context,onSelectFromToDate);
    @Override
    public void onClick(View view) {
      reminderFilterDialog.show();
    }
  }

  class OnSelectFromToDate implements ReminderFilterDialog.OnSelectFromToDate{
    @Override
    public void onSelect(long fromDate, long toDate) {
      searchStartDate = fromDate;
      searchEndDate = toDate;
    }
  }

  private void searchReminder(){
    String searchString = txtSearchReminder.getText().toString();

    if(searchString.isEmpty() && searchStartDate < 0 && searchEndDate < 0){
      return;
    }

    int oldSize = mainActivity.reminderDataList.size();
    int newSize = mainActivity.searchReminder(searchString,searchStartDate,searchEndDate);

    reminderRecyclerAdapter.notifyItemRangeChanged(0,Math.max(oldSize,newSize));
  }

  class DateSelectCallBack implements CalendarRecyclerViewAdapter.SelectDateCallBackInterface {
    @Override
    public void clickDate(int date, int month, int year, int weekDay,List<ReminderData> reminders) throws NotImplementedError {
      /*
      if ((date != selectedDate || month != selectedMonth || year != selectedYear)
              && (date * month * year * weekDay) > 0) {
        if(reminderRecyclerAdapter != null && (reminders == null || reminders.size() == 0)){
          int size = mainActivity.getReminderAt(date, month, year);
          reminderRecyclerAdapter.notifyItemRangeChanged(0,size);
        }
      }
      */

      if(reminders != null){
        mainActivity.reminderDataList.clear();
        mainActivity.reminderDataList.addAll(reminders);
        reminderRecyclerAdapter.notifyItemRangeChanged(0,mainActivity.reminderDataList.size());
      }else{
        int sizeBefore = mainActivity.reminderDataList.size();
        mainActivity.reminderDataList.clear();
        reminderRecyclerAdapter.notifyItemRangeChanged(0,sizeBefore);
      }

      selectedDate = date;
      selectedMonth = month;
      selectedYear = year;
      selectedWeekDay = weekDay;

      updateDateUI();

    }
  }

  class GetReminderInMonth implements CalendarRecyclerViewAdapter.GetReminderInMonth{
    @Override
    public List<ReminderData> getReminderInMonth(int year,int month) {
      try {
        Calendar calendar = Calendar.getInstance();

        calendar.set(year,month,1,0,0,0);
        long firstOfMonth = calendar.getTimeInMillis();

        calendar.set(year,month,calendar.getActualMaximum(Calendar.DATE),23,59,59);
        long lastOfMonth = calendar.getTimeInMillis();

        return mainActivity.getSearchReminder("",firstOfMonth,lastOfMonth);
      }catch (Exception ignore){}
      return null;
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
      mainActivity.removeReminder(pos);
      reminderRecyclerAdapter.notifyItemRemoved(pos);
      Toast.makeText(context, String.format(Locale.getDefault(), "Delete %d ", pos), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
      super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive);
    }
  };
}