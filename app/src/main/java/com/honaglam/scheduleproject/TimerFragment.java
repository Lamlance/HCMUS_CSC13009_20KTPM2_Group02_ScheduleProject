package com.honaglam.scheduleproject;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.Task.AddTaskDialog;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Task.TaskRecyclerViewAdapter;
import com.honaglam.scheduleproject.Task.TaskViewHolder;
import com.honaglam.scheduleproject.TimerViews.TimerFloatingButton;
import com.honaglam.scheduleproject.TimerViews.TimerViewGroupLinear;
import com.honaglam.scheduleproject.UserSetting.UserTimerSettings;

import java.util.List;
import java.util.Locale;

import kotlin.NotImplementedError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimerFragment extends Fragment {


  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  // TODO: Rename and change types of parameters

  private TextView txtTimer;
  private TimerFloatingButton btnTimerStart;
  private TimerFloatingButton btnGiveUp;
  private TimerFloatingButton btnSkip;
  private TimerViewGroupLinear layoutTimerFragment;

//  private Button btnTimer;
//  private Button btnGiveUp;
//  private Button btnSkip;

  private Button btnAddTask;

  public TextView txtPomodoro;
  public TextView txtShortBreak;
  public TextView txtLongBreak;
  private TimerFloatingButton timerSetting;
  private RecyclerView recyclerTask;
  private Context context;
  private MainActivity activity;

  TaskRecyclerViewAdapter taskRecyclerViewAdapter;
  public static TimerFragment newInstance() {
    TimerFragment fragment = new TimerFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public TimerFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    activity = (MainActivity) getActivity();
    context = requireContext();

    LinearLayout timerLayout = (LinearLayout) inflater.inflate(R.layout.fragment_timer, container, false);


    return timerLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    recyclerTask = view.findViewById(R.id.recyclerTask);
    recyclerTask.setLayoutManager(new LinearLayoutManager(context));
    taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(context, new TaskRecyclerViewAdapter.GetListCallback() {
      @Override
      public List<TaskData> getList() {
        return activity.tasks;
      }
    }, new DeleteTaskCallback(), new CheckTaskCallback(), new EditTaskCallback(), new MoveToHistoryCallback());
    recyclerTask.setAdapter(taskRecyclerViewAdapter);

    layoutTimerFragment = view.findViewById(R.id.layoutTimerFragment);

    txtPomodoro = view.findViewById(R.id.txtPomodoro);
    txtLongBreak = view.findViewById(R.id.txtLongBreak);
    txtShortBreak = view.findViewById(R.id.txtShortBreak);

    txtTimer = view.findViewById(R.id.txtTimer);
    txtTimer.setTextSize(50);

    btnTimerStart = (TimerFloatingButton) view.findViewById(R.id.btnTimerStart);
    btnTimerStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.startTimer();
      }
    });

    btnGiveUp = (TimerFloatingButton) view.findViewById(R.id.btnTimerGiveUp);
    btnGiveUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.pauseTimer();
      }
    });

    btnAddTask = view.findViewById(R.id.btnAddTask);
    btnAddTask.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new AddTaskDialog(context, new AddTaskDialogListener()).show();
      }
    });

    activity.setTimerOnTickCallBack(new TimerTickCallBack());
    activity.setTimerStateChangeCallBack(new TimerStateChangeCallBack());
    activity.setTimerOnFinishCallback(new TimerOnFinishCallback());

    //((MainActivity) getActivity()).setTimerOnTickCallBack(remainMillis -> UpdateTimeUI(remainMillis)); UI Update

    timerSetting = (TimerFloatingButton) view.findViewById(R.id.btnTimerSetting);
    timerSetting.setOnClickListener(new TimerSettingFragmentClick());

    btnSkip = (TimerFloatingButton) view.findViewById((R.id.btnSkip));
    btnSkip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          activity.skip();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    setThemeId(activity.loadTimerSettingPref().prefTheme);
  }

  public void UpdateTimeUI(long millisRemain) {
    int seconds = ((int) millisRemain / 1000) % 60;
    int minutes = (int) millisRemain / (60 * 1000);
    txtTimer.setText(String.format("%d:%02d", minutes, seconds));
  }

  public void updateBackground(int color) {
    getView().setBackgroundColor(color);
  }

  private void UpdateTimerBackground(int work_state) {
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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


    }


  }

  private void setThemeId(int themeId){
    btnTimerStart.setPomodoroTheme(themeId);
    btnSkip.setPomodoroTheme(themeId);
    btnGiveUp.setPomodoroTheme(themeId);
    timerSetting.setPomodoroTheme(themeId);
    layoutTimerFragment.setPomodoroTheme(themeId);

  }

  class AddTaskDialogListener implements AddTaskDialog.AddTaskDialogListener {
    @Override
    public void onDataPassed(TaskData taskData) {
      int newPos = activity.addTask(
              taskData.taskName,
              taskData.numberPomodoros,
              taskData.numberCompletedPomodoros,
              taskData.isCompleted);
      taskRecyclerViewAdapter.notifyItemInserted(newPos);
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
      UpdateTimerBackground(newState);

      activity.addStatsTime(
              oldState == TimerService.WORK_STATE ? prevTimeState : 0,
              oldState == TimerService.SHORT_BREAK_STATE ? prevTimeState : 0,
              oldState == TimerService.LONG_BREAK_STATE ? prevTimeState : 0
      );

      String stats = String.format(
              Locale.getDefault(),
              "%d / %d / %d: Work: %d , Short: %d, Long: %d , PrevState: %d, Added time: %d",
              activity.taskDb.date, activity.taskDb.month, activity.taskDb.year,
              activity.taskDb.curWork, activity.taskDb.curShort, activity.taskDb.curLong, oldState, prevTimeState);

      Log.i("Toady_Stats", stats);
    }
  }

  class TimerOnFinishCallback implements TimerService.TimerOnFinishCallback {
    @Override
    public void onFinish(boolean isAutoSwitchTask) throws NotImplementedError {
      if (isAutoSwitchTask) return;
      try {
        TaskRecyclerViewAdapter adapter = (TaskRecyclerViewAdapter) recyclerTask.getAdapter();
        if (adapter != null) {
          int selectedTaskIndex = adapter.getSelectedPosition();
          if (selectedTaskIndex >= 0) {
            activity.tasks.get(selectedTaskIndex).numberCompletedPomodoros += 1;
            activity.editTask(activity.tasks.get(selectedTaskIndex));
            recyclerTask.getAdapter().notifyItemChanged(selectedTaskIndex);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  class DeleteTaskCallback implements TaskViewHolder.OnClickPositionCallBack {
    @Override
    public void clickAtPosition(int position) throws NotImplementedError {
      try {
        activity.deleteTask(activity.tasks.get(position).id);
        activity.tasks.remove(position);
        taskRecyclerViewAdapter.notifyItemRemoved(position);
      } catch (Exception ignore) {
      }
    }
  }

  class EditTaskCallback implements TaskViewHolder.OnClickPositionCallBack {
    @Override
    public void clickAtPosition(int position) throws NotImplementedError {
      AddTaskDialog.AddTaskDialogListener listener = new AddTaskDialog.AddTaskDialogListener() {
        @Override
        public void onDataPassed(TaskData taskData) {
          try {
            activity.editTask(taskData);
            activity.tasks.set(position, taskData);
            taskRecyclerViewAdapter.notifyItemChanged(position);
          } catch (Exception ignore) {
          }
        }
      };
      AddTaskDialog addTaskDialog = new AddTaskDialog(context, listener, activity.tasks.get(position));
      addTaskDialog.show();
    }
  }

  class CheckTaskCallback implements TaskViewHolder.OnClickPositionCallBack {
    @Override
    public void clickAtPosition(int position) throws NotImplementedError {
      boolean isCompleted = activity.tasks.get(position).isCompleted;
      activity.tasks.get(position).isCompleted = !isCompleted;
      try {
        activity.editTask(activity.tasks.get(position));
        taskRecyclerViewAdapter.notifyItemChanged(position);
      } catch (Exception ignore) {
      }
    }
  }

  class MoveToHistoryCallback implements TaskViewHolder.OnClickPositionCallBack {
    @Override
    public void clickAtPosition(int position) throws NotImplementedError {
      try {
        activity.moveTaskToHistory(activity.tasks.get(position).id);
        activity.tasks.remove(position);
        taskRecyclerViewAdapter.notifyItemRemoved(position);
      } catch (Exception ignore) {
      }
    }
  }

  class TimerSettingFragmentClick implements View.OnClickListener{
    @Override
    public void onClick(View view) {
      getParentFragmentManager().setFragmentResultListener(
              TimerSetting.TIMER_SETTING_REQUEST_KEY,
              TimerFragment.this,
              new TimerSettingResultListener());
      ((MainActivity) getActivity()).switchFragment_TimerSetting();
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
}

