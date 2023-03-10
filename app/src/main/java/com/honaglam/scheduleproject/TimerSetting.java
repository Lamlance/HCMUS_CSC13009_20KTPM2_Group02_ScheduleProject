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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimerSetting.
     */
    // TODO: Rename and change types and number of parameters
    public static TimerSetting newInstance(String param1, String param2) {
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
        pomodoroTimePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                pomodoroTimePicker.setValue(i1);
            }
        });

        // Set time for short break time and watch changed valued
        shortBreakPicker = getView().findViewById(R.id.shortBreak);
        shortBreakPicker.setMinValue(minVal);
        shortBreakPicker.setMaxValue(maxVal);
        shortBreakPicker.setValue(5);
        shortBreakPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                shortBreakPicker.setValue(i1);

            }
        });

        // Set time for long break time and watch changed valued
        longBreakPicker = getView().findViewById(R.id.longBreak);
        longBreakPicker.setMinValue(minVal);
        longBreakPicker.setMaxValue(maxVal);
        longBreakPicker.setValue(10);
        longBreakPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                longBreakPicker.setValue(i1);
            }
        });

        // Pass the setting to the timer Fragment upon confirm pressed
        confirmButton = getView().findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.confirmButton){
                    int workTime = pomodoroTimePicker.getValue();
                    int shortBreak = shortBreakPicker.getValue();
                    int longBreak = longBreakPicker.getValue();
                    //TODO: Parse all data through
                    Bundle bundle = new Bundle();
                    bundle.putInt("workTime", workTime);
//                    bundle.putInt("shortBreakTime", shortBreak);
//                    bundle.putInt("longBreakTime", shortBreak);
                    TimerFragment timerFragment = new TimerFragment();
                    timerFragment.setArguments(bundle);

                    //TODO: DATA TRANSFERED TO TIMER FRAGMENT BUT COULD NOT GET DATA
                    // TODO: GET THE DATA IN THE FILE TimerFragment.java and parse it to START_TIME_IN_MILLIS
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, timerFragment).commit();

                    // Redirect to main Pomodoro Page
                    ((MainActivity)getActivity()).switchFragment_Pomodoro();
                }
            }
        });
        // On click event for cancel button
        // TODO: TRANSFER THIS TO TIMER FRAGMENT
        cancelButton = getView().findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add a variable to save the current setting?
                // Redirect to main Pomodoro Page
                ((MainActivity)getActivity()).switchFragment_Pomodoro();
            }
        });

    }

}