package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.Model.TaskData;
import com.honaglam.scheduleproject.Task.TaskRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private Button btnTimer;
    private Button btnGiveUp;

    private  Button btnAddTask;
    private Button timerSetting;
    private RecyclerView recyclerTask;
    private Context context = null;

    // TODO: Hardcode data need to be test the function, move this to MainActivity in future
    TaskData[] taskArray = {new TaskData("Học tiếng anh"), new TaskData("Học tiếng việt"), new TaskData("Học tiếng việt"), new TaskData("Học tiếng việt"), new TaskData("Học tiếng việt")};
    ArrayList<TaskData> tasks = new ArrayList<>(Arrays.asList(taskArray));

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
        LinearLayout timerLayout = (LinearLayout) inflater.inflate(R.layout.fragment_timer, container, false);
        recyclerTask = (RecyclerView) timerLayout.findViewById(R.id.recyclerTask);
        recyclerTask.setLayoutManager(new LinearLayoutManager(context));
        TaskRecyclerViewAdapter adapter = new TaskRecyclerViewAdapter(context, tasks);
        recyclerTask.setAdapter(adapter);
        return timerLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTimer = getView().findViewById(R.id.txtTimer);
        txtTimer.setTextSize(50);

        btnTimer = getView().findViewById(R.id.btnTimerStart);
        btnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).startTimer();
            }
        });

        btnGiveUp = getView().findViewById(R.id.btnTimerGiveUp);
        btnGiveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).resetTimer();
            }
        });


        btnAddTask = getView().findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Add function onClick
                showAddTaskDialog();
            }
        });

        ((MainActivity) getActivity()).setTimerOnTickCallBack(new TimerService.TimerTickCallBack() {
            @Override
            public void call(long remainMillis) throws Exception {
                UpdateTimeUI(remainMillis);
            }
        });

        timerSetting = getView().findViewById(R.id.btnTimerSetting);
        timerSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).switchFragment_TimerSetting();
            }
        });

        UpdateTimeUI(((MainActivity) getActivity()).getCurrentRemainMillis());
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

    class AddTaskDialogListener implements AddTaskDialog.AddTaskDialogListener{
        @Override
        public void onDataPassed(TaskData taskData) {
            tasks.add(taskData);
        }
    }

}

