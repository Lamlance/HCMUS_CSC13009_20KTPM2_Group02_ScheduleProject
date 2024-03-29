package com.honaglam.scheduleproject;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentResultListener;

import com.honaglam.scheduleproject.MyAlramManager.MyAlarmManager;
import com.honaglam.scheduleproject.Reminder.ReminderAddDialog;
import com.honaglam.scheduleproject.Repository.StatsRepository;
import com.honaglam.scheduleproject.Repository.TaskRepository;
import com.honaglam.scheduleproject.Task.AddTaskDialog;
import com.honaglam.scheduleproject.Task.TaskExpandableListAdapterFB;
import com.honaglam.scheduleproject.TimerViews.TimerFloatingButton;
import com.honaglam.scheduleproject.TimerViews.TimerViewGroupConstraint;
import com.honaglam.scheduleproject.UserSetting.UserTimerSettings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimerFragment extends Fragment {
  private TextView txtTimer;
  private TimerFloatingButton btnTimerStart;
  private TimerFloatingButton btnGiveUp;
  private TimerFloatingButton btnSkip;
  private TimerViewGroupConstraint layoutTimerFragment;
  private TimerFloatingButton btnTimerSetReminder;


  boolean spotifyIsRunning = false;
  private Button btnAddTask;
  public TextView txtPomodoro;
  public TextView txtShortBreak;
  public TextView txtLongBreak;
  private TimerFloatingButton timerSetting;
  private ExpandableListView recyclerTask;
  private Context context;
  private MainActivity activity;


  int currentPomodoroState = TimerService.WORK_STATE;
  static TaskRepository taskRepository;
  static StatsRepository statsRepository;

  @Nullable ReminderTaskFireBase.Task selectedTask;
  HashSet<ReminderTaskFireBase.Task> checkedTask = new HashSet<>();

  public static TimerFragment newInstance(String userId) {
    TimerFragment fragment = new TimerFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    if(taskRepository == null){
      taskRepository = new TaskRepository(userId);
    }
    if(statsRepository == null){
      statsRepository = new StatsRepository(userId);
    }
    return fragment;
  }


  final Map<ReminderTaskFireBase.Reminder, List<ReminderTaskFireBase.Task>> taskMapByReminder = new HashMap<>();
  final HashSet<ReminderTaskFireBase.Reminder> reminderList = new HashSet<>();
  TaskExpandableListAdapterFB expandableListAdapter;


  public TimerFragment() {
    // Required empty public constructor
  }

  public class ReminderAndTaskListGetter{
    public List<ReminderTaskFireBase.Reminder> getRemindList(){
      return new LinkedList<>(reminderList);
    }
    @NonNull
    public List<ReminderTaskFireBase.Task> getTaskInReminder(ReminderTaskFireBase.Reminder reminder){
      List<ReminderTaskFireBase.Task> tasks = taskMapByReminder.get(reminder);
      if(tasks != null){
        return tasks;
      }
      return new LinkedList<>();
    }
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    taskRepository.SetOnTaskAdded(new TaskAddedCallBack());
    taskRepository.SetOnTasksSetReminder(new TasksSetReminderCallBack());
    taskRepository.SetOnTaskDeleted(new TaskRemovedReminderCallBack());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    activity = (MainActivity) getActivity();
    context = requireContext();
    //reminderList = new LinkedList<>(activity.taskMapByReminder.keySet());

    return inflater.inflate(R.layout.fragment_timer, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    recyclerTask = view.findViewById(R.id.recyclerTask);

    expandableListAdapter = new TaskExpandableListAdapterFB(
            getLayoutInflater(),
            new ReminderAndTaskListGetter()
    );
    expandableListAdapter.SetOnChildClick(new TaskListItemClick());
    expandableListAdapter.SetOnChildEditClick(new TaskListItemEditClick());
    expandableListAdapter.SetOnChildChecked(new TaskListChildChecked());
    expandableListAdapter.SetOnChildDeleteClick(new TaskListChildDeleteClick());
    recyclerTask.setAdapter(expandableListAdapter);
    layoutTimerFragment = view.findViewById(R.id.layoutTimerFragment);

    txtPomodoro = view.findViewById(R.id.txtPomodoro);
    txtLongBreak = view.findViewById(R.id.txtLongBreak);
    txtShortBreak = view.findViewById(R.id.txtShortBreak);

    txtTimer = view.findViewById(R.id.txtTimer);
    txtTimer.setTextSize(50);

    btnTimerStart = (TimerFloatingButton) view.findViewById(R.id.btnTimerStart);
    btnTimerStart.setOnClickListener(clickedView -> activity.startTimer());

    btnGiveUp = (TimerFloatingButton) view.findViewById(R.id.btnTimerGiveUp);
    btnGiveUp.setOnClickListener(clickedView -> activity.pauseTimer());

    btnAddTask = view.findViewById(R.id.btnAddTask);
    btnAddTask.setOnClickListener(clickedView -> new AddTaskDialog(context, new AddTaskDialogListener()).show());

    activity.setTimerOnTickCallBack(new TimerTickCallBack());
    activity.setTimerStateChangeCallBack(new TimerStateChangeCallBack());
    activity.setTimerOnFinishCallback(new TimerOnFinishCallback());

    //((MainActivity) getActivity()).setTimerOnTickCallBack(remainMillis -> UpdateTimeUI(remainMillis)); UI Update

    timerSetting = (TimerFloatingButton) view.findViewById(R.id.btnTimerSetting);
    timerSetting.setOnClickListener(new TimerSettingFragmentClick());

    btnSkip = (TimerFloatingButton) view.findViewById((R.id.btnSkip));
    btnSkip.setOnClickListener(clickedView -> {
      try {
        activity.skip();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    btnTimerSetReminder = view.findViewById(R.id.btnTimerSetReminder);
    btnTimerSetReminder.setOnClickListener(view1 -> {
      if(checkedTask.size() <= 0){
        Toast.makeText(context, "Please check task(s) need to remind", Toast.LENGTH_SHORT).show();
      }
      new ReminderAddDialog(context, new SetTimerReminderCallBack()).show();
    });

    UpdateTimerBackground(currentPomodoroState);
    setThemeId(activity.loadTimerSettingPref().prefTheme);
    Log.i("DRAW_POMODORO_STATE", "FRAGMENT VIEW CREATED");

    initData();
  }

  @Override
  public void onResume() {
    super.onResume();

    //activity.updateTodayTask();
    expandableListAdapter.notifyDataSetChanged();
  }



  public void initData(){
    Calendar calendar = Calendar.getInstance();
    List<ReminderTaskFireBase.Task> taskList = ReminderTaskFireBase.GetTasksInDate(
            calendar.get(Calendar.DATE),calendar.get(Calendar.MONTH)
    );

    Map<ReminderTaskFireBase.Reminder, List<ReminderTaskFireBase.Task>> map = taskList.stream()
            .collect(Collectors.groupingBy(t -> t.reminder));

    taskMapByReminder.clear();
    map.forEach((k,v)->{
      taskMapByReminder.put(k,new LinkedList<>(v));
    });

    reminderList.clear();
    reminderList.addAll(map.keySet());
  }



  public void UpdateTimeUI(long millisRemain) {
    int seconds = ((int) millisRemain / 1000) % 60;
    int minutes = (int) millisRemain / (60 * 1000);
    txtTimer.setText(String.format("%d:%02d", minutes, seconds));
  }

  private void UpdateTimerBackground(int work_state) {
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    Log.i("DRAW_POMODORO_STATE", "SETTING STATE " + work_state);
    if (
            work_state == TimerService.WORK_STATE
                    || work_state == TimerService.LONG_BREAK_STATE
                    || work_state == TimerService.SHORT_BREAK_STATE
    ) {

      txtPomodoro.setBackgroundColor(work_state == TimerService.WORK_STATE ? Color.BLACK : Color.WHITE);
      txtShortBreak.setBackgroundColor(work_state == TimerService.SHORT_BREAK_STATE ? Color.BLACK : Color.WHITE);
      txtLongBreak.setBackgroundColor(work_state == TimerService.LONG_BREAK_STATE ? Color.BLACK : Color.WHITE);

      txtPomodoro.getBackground().setAlpha(work_state == TimerService.WORK_STATE ? 26 : 76);
      txtShortBreak.getBackground().setAlpha(work_state == TimerService.SHORT_BREAK_STATE ? 26 : 76);
      txtLongBreak.getBackground().setAlpha(work_state == TimerService.LONG_BREAK_STATE ? 26 : 76);

      Log.i("SET_STATE", "SMTH");
      btnTimerStart.setPomodoroState(work_state);
      btnSkip.setPomodoroState(work_state);
      btnGiveUp.setPomodoroState(work_state);
      timerSetting.setPomodoroState(work_state);
      layoutTimerFragment.setPomodoroState(work_state);
      btnTimerSetReminder.setPomodoroState(work_state);

    }


  }

  private void setThemeId(int themeId) {
    btnTimerStart.setPomodoroTheme(themeId);
    btnSkip.setPomodoroTheme(themeId);
    btnGiveUp.setPomodoroTheme(themeId);
    timerSetting.setPomodoroTheme(themeId);
    layoutTimerFragment.setPomodoroTheme(themeId);

  }



  class TaskAddedCallBack implements TaskRepository.OnTaskAction{
    @Override
    public void onAction(@Nullable ReminderTaskFireBase.Task task) {
      if(task == null){
        return;
      }

      reminderList.add(task.reminder);
      if(!taskMapByReminder.containsKey(task.reminder)){
        taskMapByReminder.put(task.reminder,new LinkedList<>());
      }
      taskMapByReminder.get(task.reminder).add(task);

      expandableListAdapter.notifyDataSetChanged();
    }
  }
  class TasksSetReminderCallBack implements TaskRepository.OnTasksSetReminder{
    @Override
    public void onAction(List<ReminderTaskFireBase.Task> tasks, @Nullable ReminderTaskFireBase.Reminder reminder) {
      if(reminder == null){
        return;
      }
      if(!taskMapByReminder.containsKey(ReminderTaskFireBase.Task.DEFAULT_REMINDER)){
        return;
      }

      reminderList.add(reminder);
      taskMapByReminder.put(reminder,new LinkedList<>(tasks));
      taskMapByReminder.get(ReminderTaskFireBase.Task.DEFAULT_REMINDER).removeAll(tasks);

      expandableListAdapter.notifyDataSetChanged();

      if(reminder.weekDates == null){
        MyAlarmManager.SetSingleReminderAlarm(context,reminder);
      }else{
        MyAlarmManager.SetWeeklyReminderAlarm(context,reminder);
      }

      checkedTask.clear();
    }
  }
  class TaskRemovedReminderCallBack implements TaskRepository.OnTaskAction{
    @Override
    public void onAction(@Nullable ReminderTaskFireBase.Task task) {
      if(task == null){
        return;
      }

      if(taskMapByReminder.get(task.reminder).remove(task)){
        if(taskMapByReminder.get(task.reminder).size() == 0){
          taskMapByReminder.remove(task.reminder);
          reminderList.remove(task.reminder);
        }
        expandableListAdapter.notifyDataSetChanged();
      };

    }
  }
  class SetTimerReminderCallBack implements ReminderAddDialog.ReminderDataCallBack {
    @Override
    public void onSubmit(String name, int hour24h, int minute) {
    }

    @Override
    public void onSubmit(String name, Calendar setDate) {
      if(checkedTask.size() <= 0){
        return;
      }
      taskRepository.setTasksSingleReminder(name,setDate.getTimeInMillis(),new LinkedList<>(checkedTask));
    }

    @Override
    public void onSubmitWeekly(String name, Calendar setDate, HashSet<Integer> dailyReminder) {
      if(checkedTask.size() <= 0){
        return;
      }
      taskRepository.setTaskWeeklyReminder(
              name,
              new LinkedList<>(dailyReminder),
              setDate.getTimeInMillis(),
              new LinkedList<>(checkedTask));
    }

    @Override
    public void onSubmitWeekly(String name, int hour24h, int minute, HashSet<Integer> dailyReminder) {

    }
  }



  static class AddTaskDialogListener implements AddTaskDialog.AddTaskDialogListener {
    @Override
    public void onDataPassed(ReminderTaskFireBase.Task task) {
      taskRepository.addTask(task.title,task.loops);
    }
  }
  class UpdateTaskDialogListener implements AddTaskDialog.AddTaskDialogListener{
    @Override
    public void onDataPassed(ReminderTaskFireBase.Task task) {
      int index = taskMapByReminder.get(task.reminder).indexOf(task);
      if(index >= 0){
        taskMapByReminder.get(task.reminder).set(index,task);
        expandableListAdapter.notifyDataSetChanged();
        taskRepository.updateTask(task);
      }
    }
  }



  class TaskListItemClick implements TaskExpandableListAdapterFB.ChildClickAction{
    @Override
    public void onChild(int group, int child, ReminderTaskFireBase.Task task) {
      selectedTask = task;
    }
  }

  class TaskListItemEditClick implements TaskExpandableListAdapterFB.ChildClickAction{
    @Override
    public void onChild(int group, int child, ReminderTaskFireBase.Task task) {
      new AddTaskDialog(context,new UpdateTaskDialogListener(),task).show();
    }
  }

  class TaskListChildChecked implements TaskExpandableListAdapterFB.ChildCheckAction{
    @Override
    public void onChildCheck(int group, int child, ReminderTaskFireBase.Task task, boolean isChecked) {
      if (isChecked) {
        checkedTask.add(task);
      } else {
        checkedTask.remove(task);
      }
    }
  }

  static class TaskListChildDeleteClick implements TaskExpandableListAdapterFB.ChildClickAction{
    @Override
    public void onChild(int group, int child, ReminderTaskFireBase.Task task) {
      taskRepository.removeTask(task);
    }
  }



  class TimerTickCallBack implements TimerService.TimerTickCallBack {
    @Override
    public void call(long remainMillis) {
      UpdateTimeUI(remainMillis);
    }
  }

  class TimerStateChangeCallBack implements TimerService.TimerStateChangeCallBack {
    @Override
    public void onStateChange(int newState, long prevTimeState, int oldState) {
      currentPomodoroState = newState;
      UpdateTimerBackground(newState);
      statsRepository.addTimeTodayTask(oldState,prevTimeState);
      if (newState == TimerService.LONG_BREAK_STATE) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Open Spotify for relaxing? \ud83d\ude09");
        builder.setMessage("You have done so good! \ud83e\udd70 \nIt's time relax! \nDo you want to open Spotify?");
        builder.setIcon(R.drawable.spotify_color_svgrepo_com);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            openSpotify();
          }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // do nothing
            dialogInterface.dismiss();
          }
        });
//        TODO: upgrade chua nghi ra tieng anh de viet
//        if (!spotifyIsRunning) {
//          // show dialog when spotify is not running and skip is spotify is running
//          builder.show();
//        }
        builder.show();
      }
    }
  }

  class TimerOnFinishCallback implements TimerService.TimerOnFinishCallback {
    @Override
    public void onFinish(boolean isAutoSwitchTask)  {
      //if (isAutoSwitchTask) return;
      if(selectedTask == null){
        return;
      }
      selectedTask.loopsDone += 1;
      int index = taskMapByReminder.get(selectedTask.reminder).indexOf(selectedTask);
      if(index >= 0){
        taskMapByReminder.get(selectedTask.reminder).set(index,selectedTask);
        expandableListAdapter.notifyDataSetChanged();
        taskRepository.updateTask(selectedTask);
      }
      //Todo add count task
    }
  }



  class TimerSettingFragmentClick implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      getParentFragmentManager().setFragmentResultListener(
              TimerSetting.TIMER_SETTING_REQUEST_KEY,
              TimerFragment.this,
              new TimerSettingResultListener());
      activity.switchFragment_TimerSetting();
    }
  }

  class TimerSettingResultListener implements FragmentResultListener {
    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
      UserTimerSettings settings = (UserTimerSettings) result.getSerializable(TimerSetting.TIMER_SETTING_RESULT_KEY);
      setThemeId(settings.prefTheme);
      activity.saveTimerSettingPref(settings);
    }
  }

  public void openSpotify() {
    try {
      Intent intent = new Intent();
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.MainActivity"));

      if (intent != null && !spotifyIsRunning) {
        spotifyIsRunning = true;
        startActivity(intent);
      }
      spotifyIsRunning = false;
    }
    catch (ActivityNotFoundException e){
      // If Spotify app is not installed, notify the user to install it
      Toast.makeText(getContext(), "Please install Spotify", Toast.LENGTH_LONG).show();
      // Open the Play Store for user to install the app
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music"));
      startActivity(intent);
    }
  }
}

