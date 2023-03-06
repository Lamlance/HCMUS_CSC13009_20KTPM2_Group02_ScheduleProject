package com.honaglam.scheduleproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;

import org.w3c.dom.Text;

import java.util.Calendar;

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

  // TODO: Rename and change types and number of parameters
  public static CalendarFragment newInstance(String param1, String param2) {
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

    CalendarRecyclerViewAdapter calendarRecyclerViewAdapter = new CalendarRecyclerViewAdapter(getContext());
    recyclerCalendar.setAdapter(calendarRecyclerViewAdapter);

    TextView txtSelectDate = getView().findViewById(R.id.txtSelectDate);
    ((Button)getView().findViewById(R.id.btnIncreaseMonth)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        calendarRecyclerViewAdapter.increaseMonth();
        txtSelectDate.setText(calendarRecyclerViewAdapter.getSelectDateString());
      }
    });
    ((Button)getView().findViewById(R.id.btnDecreaseMonth)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        calendarRecyclerViewAdapter.decreaseMonth();
        txtSelectDate.setText(calendarRecyclerViewAdapter.getSelectDateString());
      }
    });

    (txtSelectDate).setText(calendarRecyclerViewAdapter.getSelectDateString());
  }
}