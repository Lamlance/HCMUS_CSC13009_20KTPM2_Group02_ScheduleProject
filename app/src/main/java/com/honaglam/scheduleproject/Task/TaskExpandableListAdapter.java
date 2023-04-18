package com.honaglam.scheduleproject.Task;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.Reminder.ReminderData;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskExpandableListAdapter extends BaseExpandableListAdapter {

  public interface OnChildAction {
    void onCheck(int childPos, int groupPos);
  }

  public interface TaskDataMapByReminderGetter {
    Map<ReminderData, LinkedList<TaskData>> getMap();
  }

  public interface ReminderGroupGetter {
    List<ReminderData> getGroup();
  }

  TaskDataMapByReminderGetter mapGetter;
  ReminderGroupGetter groupGetter;
  LayoutInflater inflater;

  @Nullable
  OnChildAction childClickCallBack;
  @Nullable
  OnChildAction editClickCallBack;
  @Nullable
  OnChildAction deleteClickCallBack;
  @Nullable
  OnChildAction archiveClickCallBack;
  @Nullable
  OnChildAction childUpdatedCallBack;

  View prevClickView = null;
  int selectedGroup = -1;
  int selectedChild = -1;

  HashSet<TaskData> checkedTask = new HashSet<TaskData>();
  public void callChildAction(OnChildAction childAction,int groupPos,int childPos){
    if(childAction != null){
      try {
        childAction.onCheck(childPos,groupPos);
      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }
  public void addAndUpdateChildView(){
    if(selectedGroup < 0  || selectedChild < 0){
      return;
    }
    ReminderData reminder = groupGetter.getGroup().get(selectedGroup);
    try {
      mapGetter.getMap().get(reminder).get(selectedChild).numberCompletedPomodoros += 1;
      callChildAction(childUpdatedCallBack,selectedGroup,selectedChild);
      TaskExpandableListAdapter.this.notifyDataSetInvalidated();
    }catch (Exception e){
      e.printStackTrace();
    }


  }
  public TaskExpandableListAdapter(
          LayoutInflater inflater,
          TaskDataMapByReminderGetter mapGetter,
          ReminderGroupGetter groupGetter) {
    super();
    this.inflater = inflater;
    this.mapGetter = mapGetter;
    this.groupGetter = groupGetter;
  }

  @Override
  public int getGroupCount() {
    return groupGetter.getGroup().size();
  }

  @Override
  public int getChildrenCount(int i) {
    ReminderData reminder = groupGetter.getGroup().get(i);
    List<TaskData> list = mapGetter.getMap().get(reminder);
    return (list == null) ? 0 : list.size();
  }

  @Override
  public Object getGroup(int i) {
    return groupGetter.getGroup().get(i);
  }

  @Override
  public Object getChild(int groupPos, int childPos) {
    ReminderData reminder = groupGetter.getGroup().get(groupPos);
    List<TaskData> list = mapGetter.getMap().get(reminder);
    return (list != null) ? list.get(childPos) : null;
  }

  @Override
  public long getGroupId(int i) {
    return i;
  }

  @Override
  public long getChildId(int groupPos, int childPos) {
    return childPos;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
    ReminderData reminder = groupGetter.getGroup().get(i);
    if (view == null) {
      view = inflater.inflate(R.layout.task_item_group, viewGroup, false);
    }
    TextView txtGroupName = view.findViewById(R.id.txtTaskGroupName);
    txtGroupName.setTypeface(null, Typeface.BOLD);
    txtGroupName.setText(reminder.Name);

    return view;
  }

  @Override
  public View getChildView(int groupPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
    ReminderData reminder = groupGetter.getGroup().get(groupPos);
    List<TaskData> list = mapGetter.getMap().get(reminder);
    TaskData data = (list == null) ? null : list.get(childPos);
    if (data == null) {
      return null;
    }

    if (view == null) {
      view = inflater.inflate(R.layout.task_item, viewGroup, false);
    }

    view.setOnClickListener(clickedView -> {
      if(prevClickView != null){
        prevClickView.setBackgroundColor(Color.TRANSPARENT);
      }
      selectedChild = childPos;
      selectedGroup = groupPos;
      clickedView.setBackgroundColor(Color.RED);
      prevClickView = clickedView;
      callChildAction(childClickCallBack,groupPos,childPos);
    });

    TextView txtTaskName = view.findViewById(R.id.txtTaskName);
    TextView txtCountPomodoro = view.findViewById(R.id.txtCountPomodoro);
    CheckBox checkBox = view.findViewById(R.id.checkBoxCompleteTask);
    ImageButton editBtn = view.findViewById(R.id.imgBtnEditTask);
    ImageButton deleteBtn = view.findViewById(R.id.imgBtnDeleteTask);
    ImageButton archiveBtn = view.findViewById(R.id.imgBtnMoveToHistory);

    txtTaskName.setText(data.taskName);
    txtCountPomodoro.setText(String.format(Locale.getDefault(),
            "%d/%d", data.numberCompletedPomodoros, data.numberPomodoros));

    checkBox.setOnCheckedChangeListener((compoundButton, isCheck) -> {
      if(isCheck){
        checkedTask.add(data);
      }else{
        checkedTask.remove(data);
      }
    });
    checkBox.setChecked(false);

    editBtn.setOnClickListener((clickedView)->{
      callChildAction(editClickCallBack,groupPos,childPos);
    });

    deleteBtn.setOnClickListener((clickedView)->{
      callChildAction(deleteClickCallBack,groupPos,childPos);
    });

    archiveBtn.setOnClickListener((clickedView)->{
      callChildAction(archiveClickCallBack,groupPos,childPos);
    });

    return view;
  }

  @Override
  public boolean isChildSelectable(int i, int i1) {
    return true;
  }

  public void setChildClickCallBack(@NonNull OnChildAction action){
    this.childClickCallBack = action;
  }
  public void setDeleteClickCallBack(@NonNull OnChildAction action){
    this.deleteClickCallBack = action;
  }
  public void setEditClickCallBack(@NonNull OnChildAction action){
    this.editClickCallBack = action;
  }
  public void setArchiveClickCallBack(@NonNull OnChildAction action){
    this.archiveClickCallBack = action;
  }
  public void setChildUpdateCallBack(@NonNull OnChildAction action){
    this.childUpdatedCallBack = action;
  }

  public HashSet<TaskData> getCheckedTask(){
    return checkedTask;
  }
}
