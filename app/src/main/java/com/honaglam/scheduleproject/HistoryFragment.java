package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.honaglam.scheduleproject.History.HistoryExpandableListAdapter;
import com.honaglam.scheduleproject.Reminder.ReminderFilterDialog;
import com.honaglam.scheduleproject.Task.TaskData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HistoryFragment extends Fragment {
  HistoryExpandableListAdapter historyExpandableListAdapter;
  private Context context;
  private ExpandableListView expandableListHistory;
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

  ReminderFilterDialog historyFilter;
  List<TaskData> taskDataList;
  List<Long> dateGroup = new ArrayList<>();
  Map<Long,List<TaskData>> taskDataGroupByDate = new HashMap<>();
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    activity = (MainActivity) getActivity();
    context = requireContext();
    historyFilter = new ReminderFilterDialog(context, new TaskDataDateFilterCallBack());
    LinearLayout historyLayout = (LinearLayout) inflater.inflate(R.layout.fragment_history, container, false);

    //recyclerHistory.setAdapter(historyExpandableListAdapter);

    return historyLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    context = getContext();
    activity = (MainActivity) getActivity();

    historyExpandableListAdapter = new HistoryExpandableListAdapter(
            getLayoutInflater(),
            () -> taskDataGroupByDate,
            () -> dateGroup);
    historyExpandableListAdapter.setClickMakeTaskCallBack(new ChildItemUnarchiveCallBack());


    expandableListHistory = view.findViewById(R.id.recyclerHistory);
    expandableListHistory.setAdapter(historyExpandableListAdapter);

    view.findViewById(R.id.btnFilterHistory).setOnClickListener(clickedView -> {
      historyFilter.show();
    });
    // TODO: List task history
  }

  long fromDate;
  long toDate;

  class TaskDataDateFilterCallBack implements ReminderFilterDialog.OnSelectFromToDate{
    @Override
    public void onSelect(long fromDate, long toDate) {
      HistoryFragment.this.fromDate = fromDate;
      HistoryFragment.this.toDate = toDate;

      Calendar calendar = Calendar.getInstance();
      taskDataList = activity.taskDb.getHistoryAtRange(fromDate,toDate);

      taskDataGroupByDate.clear();
      taskDataGroupByDate = taskDataList.stream().collect(Collectors.groupingBy(t -> {
        calendar.set(t.year,t.month-1,t.date,0,0,0);
        return calendar.getTimeInMillis();
      }));

      dateGroup.clear();
      dateGroup.addAll(taskDataGroupByDate.keySet());

      try {
        historyExpandableListAdapter.notifyDataSetChanged();
      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }

  class ChildItemUnarchiveCallBack implements HistoryExpandableListAdapter.ChildItemClickCallBack{
    @Override
    public void onClick(int groupPos, int childPos) {
      try {
        Long date = dateGroup.get(groupPos);
        activity.moveTaskToToDoTask(taskDataGroupByDate.get(date).get(childPos).id);
        taskDataGroupByDate.get(date).remove(childPos);
        if(taskDataGroupByDate.get(date).size() == 0){
          taskDataGroupByDate.remove(date);
          dateGroup.remove(groupPos);
        }
        historyExpandableListAdapter.notifyDataSetChanged();
      }catch (Exception e){
        e.printStackTrace();
      }

    }
  }

  @Override
  public void onResume() {
    if(fromDate > 0 && toDate > 0 ){
      new TaskDataDateFilterCallBack().onSelect(fromDate,toDate);
    }

    super.onResume();
  }
}
