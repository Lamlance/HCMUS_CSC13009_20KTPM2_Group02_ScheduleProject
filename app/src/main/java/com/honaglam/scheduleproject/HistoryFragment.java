package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.Calendar.CalendarRecyclerViewAdapter;
import com.honaglam.scheduleproject.History.HistoryRecyclerViewAdapter;
import com.honaglam.scheduleproject.Reminder.ReminderAddDialog;
import com.honaglam.scheduleproject.Reminder.ReminderData;
import com.honaglam.scheduleproject.Reminder.ReminderRecyclerAdapter;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Task.TaskRecyclerViewAdapter;

import java.util.List;

import kotlin.NotImplementedError;


public class HistoryFragment extends Fragment {
  HistoryRecyclerViewAdapter historyRecyclerViewAdapter;
  private Context context;
  private RecyclerView recyclerHistory;
  private MainActivity activity;

  public static HistoryFragment newInstance() {
    HistoryFragment fragment = new HistoryFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  // Required empty public constructor
  public HistoryFragment() {
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_history, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    context = getContext();
    activity = (MainActivity) getActivity();

  }

}
