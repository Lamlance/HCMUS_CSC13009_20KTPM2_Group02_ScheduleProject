package com.honaglam.scheduleproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.honaglam.scheduleproject.Task.AddTaskDialog;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Task.TaskRecyclerViewAdapter;
import com.honaglam.scheduleproject.Task.TaskViewHolder;

import java.util.List;

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
  private FloatingActionButton btnTimerStart;
  private FloatingActionButton btnGiveUp;
  private FloatingActionButton btnSkip;
  private FloatingActionButton btnSetting;


//  private Button btnTimer;
//  private Button btnGiveUp;
//  private Button btnSkip;

  private Button btnAddTask;

  public TextView txtPomodoro;
  public TextView txtShortBreak;
  public TextView txtLongBreak;
  private FloatingActionButton timerSetting;
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
    recyclerTask = timerLayout.findViewById(R.id.recyclerTask);
    recyclerTask.setLayoutManager(new LinearLayoutManager(context));
    taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(context, new TaskRecyclerViewAdapter.GetListCallback() {
      @Override
      public List<TaskData> getList() {
        return activity.tasks;
      }
    });
    recyclerTask.setAdapter(taskRecyclerViewAdapter);

    return timerLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    txtTimer = view.findViewById(R.id.txtTimer);
    txtTimer.setTextSize(50);

    btnTimerStart = (FloatingActionButton) view.findViewById(R.id.btnTimerStart);
    btnTimerStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.startTimer();
      }
    });

    btnGiveUp = (FloatingActionButton) view.findViewById(R.id.btnTimerGiveUp);
    btnGiveUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.resetTimer();
      }
    });

    btnAddTask = getView().findViewById(R.id.btnAddTask);
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

    timerSetting = (FloatingActionButton) getView().findViewById(R.id.btnTimerSetting);
    timerSetting.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.switchFragment_TimerSetting();
      }
    });

    btnSkip = (FloatingActionButton) getView().findViewById((R.id.btnSkip));
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

    long cur = ((MainActivity) getActivity()).getCurrentRemainMillis();
    UpdateTimeUI(cur);
  }

  public void UpdateTimeUI(long millisRemain) {
    int seconds = ((int) millisRemain / 1000) % 60;
    int minutes = (int) millisRemain / (60 * 1000);
    txtTimer.setText(String.format("%d:%02d", minutes, seconds));
  }

  public void updateBackground(int color) {
    getView().setBackgroundColor(color);
  }

  @SuppressLint("ResourceAsColor")
  private void UpdateTimerBackground(int work_state) {
    txtPomodoro = getView().findViewById(R.id.txtPomodoro);
    txtShortBreak = getView().findViewById(R.id.txtShortBreak);
    txtLongBreak = getView().findViewById(R.id.txtLongBreak);

    Drawable rounded_background = getResources().getDrawable(R.drawable.rounded_background);
    txtPomodoro.setBackground(rounded_background);
    txtShortBreak.setBackground(rounded_background);
    txtLongBreak.setBackground(rounded_background);

    btnTimerStart = getView().findViewById(R.id.btnTimerStart);
    btnGiveUp = getView().findViewById(R.id.btnTimerGiveUp);
    btnSkip = getView().findViewById(R.id.btnSkip);
    btnSetting = getView().findViewById(R.id.btnTimerSetting);
    btnAddTask = getView().findViewById(R.id.btnAddTask);
    if (activity.darkModeIsOn == true) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
      requireView().setBackgroundColor(ContextCompat.getColor(context, R.color.black));
      int backgroundButtonColor = getResources().getColor(R.color.image_btn_timer_fragment_background);
      ColorStateList colorStateListBackground = ColorStateList.valueOf(backgroundButtonColor);
      int iconColor = getResources().getColor(R.color.image_btn_timer_fragment_icon);
      ColorStateList colorStateListIcon = ColorStateList.valueOf(iconColor);

      btnTimerStart.setBackgroundTintList(colorStateListBackground);
      btnTimerStart.setImageTintList(colorStateListIcon);
      btnGiveUp.setBackgroundTintList(colorStateListBackground);
      btnGiveUp.setImageTintList(colorStateListIcon);
      btnSkip.setBackgroundTintList(colorStateListBackground);
      btnSkip.setImageTintList(colorStateListIcon);
      btnSetting.setBackgroundTintList(colorStateListBackground);
      btnSetting.setImageTintList(colorStateListIcon);

      btnAddTask.setBackgroundColor(ContextCompat.getColor(context, R.color.add_btn_color_dark_mode));
      btnAddTask.setTextColor(ContextCompat.getColor(context, R.color.add_btn_text_color_dark_mode));

      if (work_state == TimerService.WORK_STATE) {
        txtPomodoro.setBackgroundColor(0x80FFFFFF);
        txtShortBreak.setBackgroundColor(Color.TRANSPARENT);
        txtLongBreak.setBackgroundColor(Color.TRANSPARENT);
      }
      if (work_state == TimerService.SHORT_BREAK_STATE) {
        txtPomodoro.setBackgroundColor(Color.TRANSPARENT);
        txtShortBreak.setBackgroundColor(0x80FFFFFF);
        txtLongBreak.setBackgroundColor(Color.TRANSPARENT);
      }
      if (work_state == TimerService.LONG_BREAK_STATE) {
        txtPomodoro.setBackgroundColor(Color.TRANSPARENT);
        txtShortBreak.setBackgroundColor(Color.TRANSPARENT);
        txtLongBreak.setBackgroundColor(0x80FFFFFF);
      }
    }
    else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
      if (work_state == TimerService.WORK_STATE) {
        requireView().setBackgroundColor(ContextCompat.getColor(context, R.color.work_color));

        int stateColor = getResources().getColor(R.color.text_work_state_background_color);
        txtPomodoro.setBackgroundColor(stateColor);
        txtShortBreak.setBackgroundColor(0x80FFFFFF);
        txtLongBreak.setBackgroundColor(0x80FFFFFF);

        int backgroundButtonColor = getResources().getColor(R.color.background_button_work_color);
        ColorStateList colorStateListBackground = ColorStateList.valueOf(backgroundButtonColor);
        int iconColor = getResources().getColor(R.color.button_work_color);
        ColorStateList colorStateListIcon = ColorStateList.valueOf(iconColor);

        btnTimerStart.setBackgroundTintList(colorStateListBackground);
        btnTimerStart.setImageTintList(colorStateListIcon);
        btnGiveUp.setBackgroundTintList(colorStateListBackground);
        btnGiveUp.setImageTintList(colorStateListIcon);
        btnSkip.setBackgroundTintList(colorStateListBackground);
        btnSkip.setImageTintList(colorStateListIcon);
        btnSetting.setBackgroundTintList(colorStateListBackground);
        btnSetting.setImageTintList(colorStateListIcon);

        btnAddTask.setBackgroundColor(ContextCompat.getColor(context, R.color.background_button_work_color));
        btnAddTask.setTextColor(ContextCompat.getColor(context, R.color.white));

      }
      if (work_state == TimerService.SHORT_BREAK_STATE) {
        requireView().setBackgroundColor(ContextCompat.getColor(context, R.color.short_break_color));

        int stateColor = getResources().getColor(R.color.text_short_break_state_background_color);
        txtPomodoro.setBackgroundColor(stateColor);
        txtPomodoro.setBackgroundColor(0x80FFFFFF);
        txtShortBreak.setBackgroundColor(stateColor);
        txtLongBreak.setBackgroundColor(0x80FFFFFF);

        int backgroundButtonColor = getResources().getColor(R.color.background_button_short_break_color);
        ColorStateList colorStateListBackground = ColorStateList.valueOf(backgroundButtonColor);
        int iconColor = getResources().getColor(R.color.button_short_break_color);
        ColorStateList colorStateListIcon = ColorStateList.valueOf(iconColor);

        btnTimerStart.setBackgroundTintList(colorStateListBackground);
        btnTimerStart.setImageTintList(colorStateListIcon);
        btnGiveUp.setBackgroundTintList(colorStateListBackground);
        btnGiveUp.setImageTintList(colorStateListIcon);
        btnSkip.setBackgroundTintList(colorStateListBackground);
        btnSkip.setImageTintList(colorStateListIcon);
        btnSetting.setBackgroundTintList(colorStateListBackground);
        btnSetting.setImageTintList(colorStateListIcon);

        btnAddTask.setBackgroundColor(ContextCompat.getColor(context, R.color.background_button_short_break_color));
        btnAddTask.setTextColor(ContextCompat.getColor(context, R.color.white));
      }
      if (work_state == TimerService.LONG_BREAK_STATE) {
        requireView().setBackgroundColor(ContextCompat.getColor(context, R.color.long_break_color));

        int stateColor = getResources().getColor(R.color.text_long_break_state_background_color);
        txtPomodoro.setBackgroundColor(stateColor);
        txtPomodoro.setBackgroundColor(0x80FFFFFF);
        txtShortBreak.setBackgroundColor(0x80FFFFFF);
        txtLongBreak.setBackgroundColor(stateColor);

        int backgroundButtonColor = getResources().getColor(R.color.background_button_long_break_color);
        ColorStateList colorStateListBackground = ColorStateList.valueOf(backgroundButtonColor);
        int iconColor = getResources().getColor(R.color.button_long_break_color);
        ColorStateList colorStateListIcon = ColorStateList.valueOf(iconColor);

        btnTimerStart.setBackgroundTintList(colorStateListBackground);
        btnTimerStart.setImageTintList(colorStateListIcon);
        btnGiveUp.setBackgroundTintList(colorStateListBackground);
        btnGiveUp.setImageTintList(colorStateListIcon);
        btnSkip.setBackgroundTintList(colorStateListBackground);
        btnSkip.setImageTintList(colorStateListIcon);
        btnSetting.setBackgroundTintList(colorStateListBackground);
        btnSetting.setImageTintList(colorStateListIcon);

        btnAddTask.setBackgroundColor(ContextCompat.getColor(context, R.color.background_button_long_break_color));
        btnAddTask.setTextColor(ContextCompat.getColor(context, R.color.white));
      }
    }
  }

  class AddTaskDialogListener implements AddTaskDialog.AddTaskDialogListener {
    @Override
    public void onDataPassed(TaskData taskData) {
      int newPos = activity.addTask(taskData.taskName, taskData.numberPomodoros);
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
    public void onStateChange(int newState) {
      UpdateTimerBackground(newState);
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
          activity.tasks.get(selectedTaskIndex).numberCompletedPomodoros += 1;
          recyclerTask.getAdapter().notifyItemChanged(selectedTaskIndex);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}

