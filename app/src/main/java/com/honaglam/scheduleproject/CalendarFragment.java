package com.honaglam.scheduleproject;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;
import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapterFB;
import com.honaglam.scheduleproject.Reminder.ReminderAddDialog;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Reminder.ReminderFilterDialog;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapter;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapterFB;
import com.honaglam.scheduleproject.Repository.ReminderRepository;


import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import kotlin.NotImplementedError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

  RecyclerView recyclerCalendar;
  CalendarRecyclerViewAdapterFB calendarRecyclerViewAdapter;
  TextView txtSelectDate;
  Animation ani_month_l2r;
  Animation ani_month_r2l;

  TextView txtBigDate;
  TextView txtBigWeekDate;

  MainActivity mainActivity;
  Context context;

  ReminderRecyclerAdapterFB reminderRecyclerAdapter;
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

  ConstraintLayout layoutCalendarAll;


  List<ReminderTaskFireBase.Reminder> reminderInMonth;
  Map<Integer, List<ReminderTaskFireBase.Reminder>> reminderMapByDate;

  public class ReminderInDateGetter{
    @NonNull public List<ReminderTaskFireBase.Reminder> getReminder(int date){
      List<ReminderTaskFireBase.Reminder> reminders = reminderMapByDate.get(date);
      if(reminders != null){
        return reminders;
      }
      return new LinkedList<>();
    }

    @NonNull public List<ReminderTaskFireBase.Reminder> getReminder(){
      List<ReminderTaskFireBase.Reminder> reminders = reminderMapByDate.get(selectedDate);
      if(reminders != null){
        return reminders;
      }
      return  new LinkedList<>();
    }
  }

  static private ReminderRepository reminderRepository;

  public static CalendarFragment newInstance(String userId) {
    CalendarFragment fragment = new CalendarFragment();
    Bundle args = new Bundle();
    if(reminderRepository == null){
      reminderRepository = new ReminderRepository(userId);
    }

    fragment.setArguments(args);
    return fragment;
  }

  public CalendarFragment(){
  }
  private void MapReminderMothToDateMap() {
    Calendar calendar = Calendar.getInstance();
    reminderMapByDate = reminderInMonth.stream().collect(
            Collectors.groupingBy(r -> {
              calendar.setTimeInMillis(r.reminderTime);
              return calendar.get(Calendar.DATE);
            })
    );
  }



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    Calendar calendar = Calendar.getInstance();
    selectedMonth = calendar.get(Calendar.MONTH);
    selectedDate = calendar.get(Calendar.DATE);
    reminderInMonth = ReminderTaskFireBase.GetRemindersInMonth(selectedMonth);

    MapReminderMothToDateMap();

    reminderRepository.SetAddReminderCallBack(new ReminderAddedCallBack());
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


    layoutCalendarAll = view.findViewById(R.id.layoutCalendarAll);

    //switchFragmentView(FULL_CALENDAR_VIEW);

    txtBigDate = view.findViewById(R.id.txtBigDateDisplay);
    txtBigWeekDate = view.findViewById(R.id.txtBigWeekDateDisplay);
    txtSelectDate = view.findViewById(R.id.txtSelectDate);

    recyclerCalendar = view.findViewById(R.id.recyclerCalendar);

    ani_month_r2l = AnimationUtils.loadAnimation(context, R.anim.calendar_month_change_r2l);
    ani_month_l2r = AnimationUtils.loadAnimation(context, R.anim.calendar_month_change_l2r);

    calendarRecyclerViewAdapter = new CalendarRecyclerViewAdapterFB(context,new ReminderInDateGetter());
    calendarRecyclerViewAdapter.setSelectDateCallBack((d,m,y,w)->{
      int oldCount = reminderRecyclerAdapter == null ? -1 : reminderRecyclerAdapter.getItemCount();
      selectedDate = d;
      selectedMonth = m;
      selectedYear = y;
      selectedWeekDay = w;
      int newCount = reminderRecyclerAdapter == null ? -1 : reminderRecyclerAdapter.getItemCount() ;
      if(reminderRecyclerAdapter != null && newCount != oldCount){
        reminderRecyclerAdapter.notifyDataSetChanged();
      }
    });
    calendarRecyclerViewAdapter.setOnMonthChangeCallBack((d,m,y,w)->{
      selectedDate = d;
      selectedMonth = m;
      selectedYear = y;
      selectedWeekDay = w;

      reminderInMonth = ReminderTaskFireBase.GetRemindersInMonth(selectedMonth);
      MapReminderMothToDateMap();

      if(reminderRecyclerAdapter != null){
        int oldCount = reminderRecyclerAdapter.getItemCount();
        int newCount = reminderRecyclerAdapter.getItemCount();
        if(oldCount != newCount){
          reminderRecyclerAdapter.notifyDataSetChanged();
        }
      }

    });
    recyclerCalendar.setAdapter(calendarRecyclerViewAdapter);

    view.findViewById(R.id.btnIncreaseMonth).setOnClickListener(view1 -> {
      calendarRecyclerViewAdapter.increaseMonth();
    });
    view.findViewById(R.id.btnDecreaseMonth).setOnClickListener(view12 -> {
      calendarRecyclerViewAdapter.decreaseMonth();
      //recyclerCalendar.startAnimation(ani_month_l2r);

    });

    RecyclerView reminderRecycler = view.findViewById(R.id.recyclerReminders);
    reminderRecyclerAdapter = new ReminderRecyclerAdapterFB(context,new ReminderInDateGetter());
    reminderRecycler.setLayoutManager(new LinearLayoutManager(context));
    reminderRecycler.setAdapter(reminderRecyclerAdapter);


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

    ((AppCompatToggleButton) view.findViewById(R.id.btnToggleCalendar)).setOnCheckedChangeListener(
            (compoundButton, isOn) -> layoutCalendarAll.setVisibility(isOn ? View.GONE : View.VISIBLE)
    );
  }



  private void AddReminder(String name) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(selectedYear, selectedMonth, selectedDate, selectedHour, selectedMinute, 0);
    long remindTime = calendar.getTimeInMillis();

    reminderRepository.addSingleReminder(name,remindTime);
  }

  private void AddReminderWeekly(String name,HashSet<Integer> weekly){
    Calendar calendar = Calendar.getInstance();
    calendar.set(selectedYear, selectedMonth, selectedDate, selectedHour, selectedMinute, 0);
    long remindTime = calendar.getTimeInMillis();

    //TODO Firebase Add Reminder Weekly
  }



  class ReminderAddedCallBack implements ReminderRepository.ReminderAction{
    @Override
    public void onAction(@Nullable ReminderTaskFireBase.Reminder reminder) {
      if(reminder == null){
        return;
      }

      if(reminder.weekDates == null){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminder.reminderTime);
        int date = calendar.get(Calendar.DATE);
        if(!reminderMapByDate.containsKey(date)){
          reminderMapByDate.put(date,new LinkedList<>());
        }
        reminderMapByDate.get(date).add(reminder);

        int pos = calendarRecyclerViewAdapter.dateToPos(date);
        calendarRecyclerViewAdapter.notifyItemChanged(pos);
        reminderRecyclerAdapter.notifyItemInserted(reminderMapByDate.get(date).size() - 1);
        return;
      }

      reminderInMonth = ReminderTaskFireBase.GetRemindersInMonth(selectedMonth);
      MapReminderMothToDateMap();
      calendarRecyclerViewAdapter.notifyDataSetChanged();

    }
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

    //int oldSize = mainActivity.reminderDataList.size();
    //int newSize = mainActivity.searchReminder(searchString,searchStartDate,searchEndDate);

    //reminderRecyclerAdapter.notifyItemRangeChanged(0,Math.max(oldSize,newSize));
  }



}