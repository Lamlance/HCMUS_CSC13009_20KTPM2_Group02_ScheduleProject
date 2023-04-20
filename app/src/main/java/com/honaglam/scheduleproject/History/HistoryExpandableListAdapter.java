package com.honaglam.scheduleproject.History;

import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.Task.TaskData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryExpandableListAdapter extends BaseExpandableListAdapter {

  public interface TaskDataMapByDateGetter{
    Map<Long, List<TaskData>> getMap();
  }
  public interface TaskDataDateListGetter{
    List<Long> getList();
  }
  public interface ChildItemClickCallBack{
    void onClick(int groupPos,int childPos);
  }

  TaskDataMapByDateGetter taskDataMapGetter = null;
  TaskDataDateListGetter taskDataDateGetter = null;
  LayoutInflater inflater;
  ChildItemClickCallBack clickMakeTaskCallBack;

  public HistoryExpandableListAdapter(
          LayoutInflater inflater,
          TaskDataMapByDateGetter mapGetter,
          TaskDataDateListGetter listGetter ){
    super();
    taskDataMapGetter = mapGetter;
    taskDataDateGetter = listGetter;
    this.inflater = inflater;
  }

  void callChildAction(ChildItemClickCallBack action,int groupPos,int childPos){
    if(action == null){
      return;
    }
    try {
      action.onClick(groupPos,childPos);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public int getGroupCount() {
    return taskDataDateGetter.getList().size();
  }

  @Override
  public int getChildrenCount(int groupPos) {
    Long date = taskDataDateGetter.getList().get(groupPos);
    if(date == null){
      return 0;
    }

    List<TaskData> taskDataList = taskDataMapGetter.getMap().get(date);
    if(taskDataList == null){
      return 0;
    }

    return taskDataList.size();
  }

  @Override
  public Object getGroup(int i) {
    return taskDataDateGetter.getList().get(i);
  }

  @Override
  public Object getChild(int groupPos, int childPos) {
    Long date = taskDataDateGetter.getList().get(groupPos);
    if(date == null){
      return 0;
    }
    return taskDataMapGetter.getMap().get(date);
  }

  @Override
  public long getGroupId(int i) {
    return taskDataDateGetter.getList().get(i);
  }

  @Override
  public long getChildId(int groupPos, int childPos) {
    Long date = taskDataDateGetter.getList().get(groupPos);
    if(date == null){
      return 0;
    }

    List<TaskData> taskDataList = taskDataMapGetter.getMap().get(date);
    if(taskDataList == null){
      return 0;
    }

    TaskData taskData = taskDataList.get(childPos);
    if(taskData == null){
      return 0;
    }

    return taskData.id;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getGroupView(int groupPos, boolean b, View view, ViewGroup viewGroup) {
    if(view == null){
      view = inflater.inflate(R.layout.task_history_group,viewGroup,false);
    }
    TextView txtHistoryGroupName = view.findViewById(R.id.txtHistoryGroupName);

    Long time = taskDataDateGetter.getList().get(groupPos);
    if(time != null){
      String dateFormat = DateFormat.getDateTimeInstance().format(new Date(time));
      txtHistoryGroupName.setText(dateFormat);
    }

    return view;
  }

  @Override
  public View getChildView(int groupPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
    if(view == null){
      view = inflater.inflate(R.layout.task_history,viewGroup,false);
    }

    TextView txtTaskNameHistory = view.findViewById(R.id.txtTaskNameHistory);
    TextView txtCountPomodoroHistory = view.findViewById(R.id.txtCountPomodoroHistory);

    Long date = taskDataDateGetter.getList().get(groupPos);
    if (date == null || !taskDataMapGetter.getMap().containsKey(date)){
      return view;
    }
    TaskData data = taskDataMapGetter.getMap().get(date).get(childPos);
    if(data == null){
      return view;
    }

    txtTaskNameHistory.setText(data.taskName);
    txtCountPomodoroHistory.setText(String.format(Locale.getDefault(),
            "%d/%d",data.numberCompletedPomodoros,data.numberPomodoros));

    view.findViewById(R.id.imgBtnMoveToTodoTask).setOnClickListener(clickedView -> {
      callChildAction(clickMakeTaskCallBack,groupPos,childPos);
    });

    return view;
  }

  @Override
  public boolean isChildSelectable(int i, int i1) {
    return true;
  }

  public void setClickMakeTaskCallBack(ChildItemClickCallBack action){
    clickMakeTaskCallBack = action;
  }
}
