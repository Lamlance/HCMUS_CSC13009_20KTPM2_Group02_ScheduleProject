package com.honaglam.scheduleproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;

import java.util.Locale;

import kotlin.NotImplementedError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    // TODO: Rename and change types of parameters


    public CalendarFragment() {
        // Required empty public constructor
    }

    RecyclerView recyclerCalendar;
    CalendarRecyclerViewAdapter calendarRecyclerViewAdapter;
    TextView txtSelectDate;
    Animation ani_month_l2r;
    Animation ani_month_r2l;

    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
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
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerCalendar = getView().findViewById(R.id.recyclerCalendar);

        ani_month_r2l = AnimationUtils.loadAnimation(getContext(), R.anim.calendar_month_change_r2l);
        ani_month_l2r = AnimationUtils.loadAnimation(getContext(), R.anim.calendar_month_change_l2r);

        calendarRecyclerViewAdapter = new CalendarRecyclerViewAdapter(getContext());
        recyclerCalendar.setAdapter(calendarRecyclerViewAdapter);
        calendarRecyclerViewAdapter.selectDateCallBack = new DateSelectCallBack();

        txtSelectDate = getView().findViewById(R.id.txtSelectDate);
        ((Button) getView().findViewById(R.id.btnIncreaseMonth)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarRecyclerViewAdapter.increaseMonth();
                txtSelectDate.setText(calendarRecyclerViewAdapter.getSelectDateString());
                recyclerCalendar.startAnimation(ani_month_l2r);
            }
        });
        ((Button) getView().findViewById(R.id.btnDecreaseMonth)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarRecyclerViewAdapter.decreaseMonth();
                txtSelectDate.setText(calendarRecyclerViewAdapter.getSelectDateString());
                recyclerCalendar.startAnimation(ani_month_r2l);
            }
        });
        (txtSelectDate).setText(calendarRecyclerViewAdapter.getSelectDateString());
    }

    class DateSelectCallBack implements CalendarRecyclerViewAdapter.SelectDateCallBackInterface {
        @Override
        public void clickDate(int date, int month, int year) throws NotImplementedError {
            String dateStr = calendarRecyclerViewAdapter.getSelectDateString();
            txtSelectDate.setText(dateStr);
            Toast.makeText(getContext(), calendarRecyclerViewAdapter.getSelectDateString(), Toast.LENGTH_SHORT).show();
        }
    }
}