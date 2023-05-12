package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.honaglam.scheduleproject.History.HistoryExpandableListAdapterFB;
import com.honaglam.scheduleproject.Reminder.ReminderFilterDialog;
import com.honaglam.scheduleproject.Repository.TaskRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HistoryFragment extends Fragment {
  HistoryExpandableListAdapterFB historyExpandableListAdapter;
  private Context context;
  private ExpandableListView expandableListHistory;
  static TaskRepository taskRepository;
  public static HistoryFragment newInstance(String uuid) {
    HistoryFragment fragment = new HistoryFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    if(taskRepository == null){
      taskRepository = new TaskRepository(uuid);
    }
    return fragment;
  }

  // Required empty public constructor
  public HistoryFragment() {
  }

  ReminderFilterDialog historyFilter;
  final LinkedList<ReminderTaskFireBase.Task> taskDataList = new LinkedList<>();
  final Map<ReminderTaskFireBase.Reminder,List<ReminderTaskFireBase.Task>> taskMapByReminder = new HashMap<>();

  public class TaskDataGetter{
    public @NonNull List<ReminderTaskFireBase.Reminder> getReminders(){
      return new LinkedList<>(taskMapByReminder.keySet());
    }
    public @NonNull List<ReminderTaskFireBase.Task> getTaskFromReminder(ReminderTaskFireBase.Reminder reminder){
      List<ReminderTaskFireBase.Task> tasks = taskMapByReminder.get(reminder);
      if(tasks == null){
        return new LinkedList<>();
      }
      return tasks;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    taskRepository.SetOnTaskSearchCompleted(new TaskSearchResultListener());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
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

    historyExpandableListAdapter = new HistoryExpandableListAdapterFB(getLayoutInflater(),new TaskDataGetter());
    //historyExpandableListAdapter.setClickMakeTaskCallBack(new ChildItemUnarchiveCallBack());


    expandableListHistory = view.findViewById(R.id.recyclerHistory);
    expandableListHistory.setAdapter(historyExpandableListAdapter);

    view.findViewById(R.id.btnFilterHistory).setOnClickListener(clickedView -> {
      historyFilter.show();
    });
    // TODO: List task history
    if(fromDate > 0 && toDate > 0 ){
      new TaskDataDateFilterCallBack().onSelect(fromDate,toDate);
    }else{
      Calendar calendar = Calendar.getInstance();
      toDate = calendar.getTimeInMillis();
      calendar.add(Calendar.DATE,-14);
      fromDate = calendar.getTimeInMillis();
      new TaskDataDateFilterCallBack().onSelect(fromDate,toDate);
    }

  }

  long fromDate = -1;
  long toDate = -1;

  private void MapTaskDataByReminder(){
    taskMapByReminder.clear();
    Map<ReminderTaskFireBase.Reminder,List<ReminderTaskFireBase.Task>>
            taskGroupByReminder = taskDataList.stream().collect(Collectors.groupingBy(t -> t.reminder));
    taskMapByReminder.putAll(taskGroupByReminder);

    Log.i("HISTORY","SEARCH RESULT " + taskDataList.size());
    historyExpandableListAdapter.notifyDataSetChanged();
  }

  class TaskDataDateFilterCallBack implements ReminderFilterDialog.OnSelectFromToDate{
    @Override
    public void onSelect(long fromDate, long toDate) {
      HistoryFragment.this.fromDate = fromDate;
      HistoryFragment.this.toDate = toDate;

      taskRepository.searchTaskInRange(fromDate,toDate);
    }
  }

  /*
  class ChildItemUnarchiveCallBack implements HistoryExpandableListAdapter.ChildItemClickCallBack{
    @Override
    public void onClick(int groupPos, int childPos) {
    }
  }
   */

  class TaskSearchResultListener implements TaskRepository.OnTaskSearchCompleted{
    @Override
    public void onCompleted(@NonNull List<ReminderTaskFireBase.Task> tasks) {
      taskDataList.clear();
      taskDataList.addAll(tasks);
      MapTaskDataByReminder();

    }
  }
  @Override
  public void onResume() {


    super.onResume();
  }
}
