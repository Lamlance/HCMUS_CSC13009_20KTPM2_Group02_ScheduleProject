package com.honaglam.scheduleproject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.honaglam.scheduleproject.Model.TaskData;
import com.honaglam.scheduleproject.Task.TaskRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;

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

  /*
  private Button btnTimer;
  private Button btnGiveUp;
  private Button btnSkip;
  private  Button btnAddTask;
  */
  
  private FloatingActionButton timerSetting;

  private RecyclerView recyclerTask;

  private Context context;
  private MainActivity activity;

  // TODO: Rename and change types and number of parameters
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
    TaskRecyclerViewAdapter adapter = new TaskRecyclerViewAdapter(context, () -> activity.tasks);
    recyclerTask.setAdapter(adapter);
    return timerLayout;
  }
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    txtTimer = getView().findViewById(R.id.txtTimer);
    txtTimer.setTextSize(50);

    btnTimerStart = (FloatingActionButton) getView().findViewById(R.id.btnTimerStart);
    btnTimerStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).startTimer();

      }
    });

    btnGiveUp = (FloatingActionButton) getView().findViewById(R.id.btnTimerGiveUp);
    btnGiveUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).resetTimer();
      }
    });
    
    /* Old btn
    btnTimer = getView().findViewById(R.id.btnTimerStart);
    btnTimer.setOnClickListener(view1 -> ((MainActivity) getActivity()).startTimer());

    btnGiveUp = getView().findViewById(R.id.btnTimerGiveUp);
    btnGiveUp.setOnClickListener(view12 -> ((MainActivity) getActivity()).resetTimer());
    */

    btnAddTask = getView().findViewById(R.id.btnAddTask);
    btnAddTask.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).showAddTaskDialog();
      }
    });
    
    

    ((MainActivity) getActivity()).setTimerOnTickCallBack(new TimerService.TimerTickCallBack() {
      @Override
      public void call(long remainMillis) {
        workState = ((MainActivity) getActivity()).getCurrentWorkState();
        Log.d("UpdateTimeUI: ", String.valueOf(workState));
        // TODO: CHANGE HERE? HELP ME
        UpdateTimerBackground(workState);
        UpdateTimeUI(remainMillis);
      }

    });

    //((MainActivity) getActivity()).setTimerOnTickCallBack(remainMillis -> UpdateTimeUI(remainMillis)); UI Update


    timerSetting = (FloatingActionButton) getView().findViewById(R.id.btnTimerSetting);
    timerSetting.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).switchFragment_TimerSetting();
      }
    });

    btnSkip = (FloatingActionButton) getView().findViewById((R.id.btnSkip));
    btnSkip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          ((MainActivity) getActivity()).skip();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }}}
   
   /*    
    btnSkip = getView().findViewById((R.id.btnSkip));
    btnSkip.setOnClickListener(view13 -> {
      try {
        ((MainActivity) getActivity()).skip();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    */ 
    long cur = ((MainActivity) getActivity()).getCurrentRemainMillis();
    UpdateTimeUI(cur);


  }


  private void showAddTaskDialog() {
    AddTaskDialog dialog = new AddTaskDialog(getContext(), new AddTaskDialogListener());
    dialog.show();
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
    int colorId;
    if (work_state == 1){
      colorId = R.color.work_color;
      updateBackground(colorId);
    }
    if (work_state == 2){
      colorId = R.color.short_break_color;
      updateBackground(colorId);
    }
    if (work_state == 3){
      colorId = R.color.long_break_color;
      updateBackground(colorId);
    }
  }

  class AddTaskDialogListener implements AddTaskDialog.AddTaskDialogListener{
    @Override
    public void onDataPassed(TaskData taskData) {
      tasks.add(taskData);
    }
  }
}

