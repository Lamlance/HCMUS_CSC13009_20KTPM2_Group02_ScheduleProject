package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageButton;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapterFB;
import com.honaglam.scheduleproject.MyAlramManager.MyAlarmManager;
import com.honaglam.scheduleproject.Reminder.ReminderAddDialog;
import com.honaglam.scheduleproject.Reminder.ReminderFilterDialog;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapterFB;
import com.honaglam.scheduleproject.Repository.ReminderRepository;


import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {
  private static final String[] WEEK_DAY_NAMES = new String[]{
    "SUN","MON","TUE","WED","THU","FRI","SAT"
  };

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
  Map<Integer, List<ReminderTaskFireBase.Reminder>> singleReminderMapByDate;

  HashMap<Integer, List<ReminderTaskFireBase.Reminder>> reminderMapByWeekDay;

  public class ReminderInDateGetter{
    private static boolean returnSearchResult = false;
    private static @NonNull List<ReminderTaskFireBase.Reminder> SEARCH_RESULT = new LinkedList<>();

    @NonNull public List<ReminderTaskFireBase.Reminder> getReminder(int date){
      List<ReminderTaskFireBase.Reminder> remindersInDate = new LinkedList<>();
      if(date <= 0){
        return remindersInDate;
      }
      Calendar calendar = Calendar.getInstance();
      calendar.set(selectedYear,selectedMonth,date);

      List<ReminderTaskFireBase.Reminder> singleReminders = singleReminderMapByDate.get(date);
      //Log.i("CALENDAR_FRAGMENT","single reminder size " + (singleReminders == null ? 0 : singleReminders.size()));

      List<ReminderTaskFireBase.Reminder> weeklyReminder = getWeeklyReminder(calendar.get(Calendar.DAY_OF_WEEK));
      //Log.i("CALENDAR_FRAGMENT","weekly reminder size " + weeklyReminder.size());

      if(singleReminders != null){
        remindersInDate.addAll(singleReminders);
      }
      remindersInDate.addAll(weeklyReminder);


      return remindersInDate;
    }

    @NonNull public List<ReminderTaskFireBase.Reminder> getReminder(){
      if(returnSearchResult){
        return SEARCH_RESULT;
      }
      return getReminder(selectedDate);
    }

    @NonNull public List<ReminderTaskFireBase.Reminder> getWeeklyReminder(int weekDay){
      List<ReminderTaskFireBase.Reminder> reminders = reminderMapByWeekDay.get(weekDay);


      if(reminders != null){
        //Log.i("CALENDAR_FRAGMENT","reminderMapByWeekDay size " + reminders.size());
        return reminders;
      }
      //Log.i("CALENDAR_FRAGMENT","reminderMapByWeekDay size " + 0);

      return new LinkedList<>();
    }
  }

  ReminderInDateGetter reminderInDateGetter;

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
    Map<Integer, List<ReminderTaskFireBase.Reminder>> reminderByDate = reminderInMonth.stream().collect(
            Collectors.groupingBy(r -> {
              calendar.setTimeInMillis(r.reminderTime);
              return calendar.get(Calendar.DATE);
            })
    );

    singleReminderMapByDate = new HashMap<>();
    reminderByDate.forEach((k,v)->{
      singleReminderMapByDate.put(k,new LinkedList<>(v));
    });
  }



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    reminderInDateGetter = new ReminderInDateGetter();

    Calendar calendar = Calendar.getInstance();
    selectedMonth = calendar.get(Calendar.MONTH);
    selectedDate = calendar.get(Calendar.DATE);
    selectedWeekDay = calendar.get(Calendar.DAY_OF_WEEK);
    selectedYear = calendar.get(Calendar.YEAR);

    reminderRepository.SetAddReminderCallBack(new ReminderAddedCallBack());
    reminderRepository.SetDeleteReminderCallBack(new ReminderDeletedCallBack());
    reminderRepository.SetSearchResultCallBack(new SearchReminderCallBack());
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

    reminderInMonth = ReminderTaskFireBase.GetRemindersInMonth(selectedMonth);
    reminderMapByWeekDay = ReminderTaskFireBase.GetWeeklyReminderByWeekDay();
    MapReminderMothToDateMap();

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
      int oldDate = selectedDate;
      selectedDate = d;
      selectedMonth = m;
      selectedYear = y;
      selectedWeekDay = w;

      ReminderInDateGetter.returnSearchResult = false;

      txtBigDate.setText(String.valueOf(d));
      txtBigWeekDate.setText(WEEK_DAY_NAMES[w-1]);
      txtSelectDate.setText(String.format(Locale.getDefault(),"%d/%d/%d",d,m,y));

      if(reminderRecyclerAdapter != null && oldDate != selectedDate){
        reminderRecyclerAdapter.notifyDataSetChanged();
      }
    });
    calendarRecyclerViewAdapter.setOnMonthChangeCallBack((d,m,y,w)->{
      int oldDate = selectedDate;
      selectedDate = d;
      selectedMonth = m;
      selectedYear = y;
      selectedWeekDay = w;

      txtBigDate.setText(String.valueOf(d));
      txtBigWeekDate.setText(WEEK_DAY_NAMES[w-1]);
      txtSelectDate.setText(String.format(Locale.getDefault(),"%d/%d/%d",d,m,y));

      reminderInMonth = ReminderTaskFireBase.GetRemindersInMonth(selectedMonth);
      MapReminderMothToDateMap();

      if(reminderRecyclerAdapter != null){
        reminderRecyclerAdapter.notifyDataSetChanged();
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
    reminderRecyclerAdapter.setDeleteClickAction((pos,reminder)->{
      RemoveReminder(reminder);
    });
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
    reminderRepository.addWeeklyReminder(name, new LinkedList<>(weekly));
  }

  private void RemoveReminder(ReminderTaskFireBase.Reminder reminder){
    reminderRepository.removeReminder(reminder);
  }

  private void searchReminder(){
    String searchString = txtSearchReminder.getText().toString();

    reminderRepository.SearchReminder(searchString,searchStartDate,searchEndDate);
  }


  class ReminderAddedCallBack implements ReminderRepository.ReminderAction{
    @Override
    public void onAction(@Nullable ReminderTaskFireBase.Reminder reminder) {
      if(reminder == null){
        return;
      }
      //TODO ENABLE ALARM MANAGER

      if(reminder.weekDates == null){
        MyAlarmManager.SetSingleReminderAlarm(context,reminder);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminder.reminderTime);
        int date = calendar.get(Calendar.DATE);
        if(!singleReminderMapByDate.containsKey(date)){
          singleReminderMapByDate.put(date,new LinkedList<>());
        }
        singleReminderMapByDate.get(date).add(reminder);

        int pos = calendarRecyclerViewAdapter.dateToPos(date);
        calendarRecyclerViewAdapter.notifyItemChanged(pos);
        reminderRecyclerAdapter.notifyItemInserted(reminderInDateGetter.getReminder(date).size() - 1);
        return;
      }

      for (Integer wd: reminder.weekDates) {
        if(!reminderMapByWeekDay.containsKey(wd)){
          reminderMapByWeekDay.put(wd,new LinkedList<>());
        }
        Log.i("CALENDAR_FRAGMENT","ADD REMINDER TO " + WEEK_DAY_NAMES[wd-1]);
        //reminderMapByWeekDay.get(wd).add(reminder);
      }

      MyAlarmManager.SetWeeklyReminderAlarm(context,reminder);
      calendarRecyclerViewAdapter.notifyDataSetChanged();

      int newSize = reminderInDateGetter.getReminder().size();
      //Log.i("CALENDAR_FRAGMENT","NEW REMINDER SIZE" + newSize);

      if(reminder.weekDates.contains(selectedWeekDay)){
          reminderRecyclerAdapter.notifyItemInserted(reminderInDateGetter.getReminder().size() - 1);
      }
    }
  }

  class ReminderDeletedCallBack implements ReminderRepository.ReminderAction{

    @Override
    public void onAction(@Nullable ReminderTaskFireBase.Reminder reminder) {
      if(reminder == null){
        return;
      }

      if(reminder.weekDates == null){
        removeSingle(reminder);
      }else {
        removeWeekly(reminder);
      }
    }

    private void removeWeekly(ReminderTaskFireBase.Reminder reminder){
      if(reminder.weekDates == null){
        return;
      }

      for (Integer wd: reminder.weekDates) {
        reminderMapByWeekDay.get(wd).remove(reminder);
      }

      MapReminderMothToDateMap();

      reminderRecyclerAdapter.notifyDataSetChanged();
      calendarRecyclerViewAdapter.notifyDataSetChanged();
      MyAlarmManager.CancelSingleReminderAlarm(context,reminder);
    }

    private void removeSingle(ReminderTaskFireBase.Reminder reminder){
      if(reminder == null){
        return;
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(reminder.reminderTime);

      if(calendar.get(Calendar.MONTH) != selectedMonth){
        return;
      }



      boolean removeResult = singleReminderMapByDate
              .get(calendar.get(Calendar.DATE))
              .remove(reminder);

      //Log.i("CALENDAR_FRAGMENT","REMOVE RESULT " + (removeResult ? "TRUE" : "FALSE"));

      if(removeResult){
        int pos = calendarRecyclerViewAdapter.dateToPos(calendar.get(Calendar.DATE));
        calendarRecyclerViewAdapter.notifyItemChanged(pos);
        reminderRecyclerAdapter.notifyDataSetChanged();
        MyAlarmManager.CancelWeeklyReminderAlarm(context,reminder);
      }
    }
  }

  class SearchReminderCallBack implements ReminderRepository.ReminderSearchResult{
    @Override
    public void onAction(@Nullable List<ReminderTaskFireBase.Reminder> reminders) {
      if(reminders == null){
        return;
      }

      ReminderInDateGetter.returnSearchResult = true;
      ReminderInDateGetter.SEARCH_RESULT.clear();
      ReminderInDateGetter.SEARCH_RESULT.addAll(reminders);
      reminderRecyclerAdapter.notifyDataSetChanged();
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
      dailyReminder.add(selectedWeekDay);
      AddReminderWeekly(name,dailyReminder);
    }
  }

  class OnSelectFromToDate implements ReminderFilterDialog.OnSelectFromToDate{
    @Override
    public void onSelect(long fromDate, long toDate) {
      searchStartDate = fromDate;
      searchEndDate = toDate;
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

}