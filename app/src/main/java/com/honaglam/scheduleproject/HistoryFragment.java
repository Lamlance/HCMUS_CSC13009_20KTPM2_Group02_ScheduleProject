package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.History.HistoryRecyclerViewAdapter;
import com.honaglam.scheduleproject.History.HistoryViewHolder;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Task.TaskViewHolder;

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
    activity = (MainActivity) getActivity();
    context = requireContext();
    LinearLayout historyLayout = (LinearLayout) inflater.inflate(R.layout.fragment_history, container, false);
    recyclerHistory = historyLayout.findViewById(R.id.recyclerHistory);
    recyclerHistory.setLayoutManager(new LinearLayoutManager(context));

    historyRecyclerViewAdapter = new HistoryRecyclerViewAdapter(context, new HistoryRecyclerViewAdapter.GetListCallback() {
              @Override
              public List<TaskData> getList() {
                return activity.tasks;
              }
            },  new MoveToToDoTaskCallback());
    recyclerHistory.setAdapter(historyRecyclerViewAdapter);

    return historyLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    context = getContext();
    activity = (MainActivity) getActivity();


  }

  class MoveToToDoTaskCallback implements HistoryViewHolder.OnClickPositionCallBack {
    @Override
    public void clickAtPosition(int position) throws NotImplementedError {
      try {
//        TODO: MOVE TASK TO TO DO TASK, NOT HISTORY
        activity.moveTaskToHistory(activity.tasks.get(position).id);
        activity.tasks.remove(position);
        historyRecyclerViewAdapter.notifyItemRemoved(position);
      } catch (Exception ignore) {
      }
    }
  }

}
