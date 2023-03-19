package com.honaglam.scheduleproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimerSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimerSetting extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Button confirmButton;
    private Button cancelButton;
    private NumberPicker pomodoroTimePicker;
    private NumberPicker shortBreakPicker;
    private NumberPicker longBreakPicker;


    public TimerSetting() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TimerSetting newInstance() {
        TimerSetting fragment = new TimerSetting();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_timer_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int minVal = 0;
        int maxVal = 120;
        // Set time for pomodoro work time and watch changed valued
        pomodoroTimePicker = getView().findViewById(R.id.workTime);
        pomodoroTimePicker.setMinValue(minVal);
        pomodoroTimePicker.setMaxValue(maxVal);
        pomodoroTimePicker.setValue(25);


        // Set time for short break time and watch changed valued
        shortBreakPicker = getView().findViewById(R.id.shortBreak);
        shortBreakPicker.setMinValue(minVal);
        shortBreakPicker.setMaxValue(maxVal);
        shortBreakPicker.setValue(5);


        // Set time for long break time and watch changed valued
        longBreakPicker = getView().findViewById(R.id.longBreak);
        longBreakPicker.setMinValue(minVal);
        longBreakPicker.setMaxValue(maxVal);
        longBreakPicker.setValue(10);


        // Pass the setting to the timer Fragment upon confirm pressed
        confirmButton = getView().findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long workTime = pomodoroTimePicker.getValue() * (long) 1000;
                long shortBreak = shortBreakPicker.getValue() * (long) 1000;
                long longBreak = longBreakPicker.getValue() * (long) 1000;

                ((MainActivity) getActivity()).setTimerTime(workTime, shortBreak, longBreak);
                ((MainActivity) getActivity()).switchFragment_Pomodoro();
            }
        });
        cancelButton = getView().findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add a variable to save the current setting?
                // Redirect to main Pomodoro Page
                ((MainActivity) getActivity()).switchFragment_Pomodoro();
            }
        });

    }

}