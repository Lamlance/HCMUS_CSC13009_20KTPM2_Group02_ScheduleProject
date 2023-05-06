package com.honaglam.scheduleproject.Task;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;
import com.honaglam.scheduleproject.TimerFragment;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TaskExpandableListAdapterFB extends BaseExpandableListAdapter {

  LayoutInflater inflater;
  TimerFragment.ReminderAndTaskListGetter getter;
  public TaskExpandableListAdapterFB(LayoutInflater inflater,TimerFragment.ReminderAndTaskListGetter getter){
    super();
    this.inflater = inflater;
    this.getter = getter;
  }

  @Override
  public int getGroupCount() {
    return getter.getRemindList().size();
  }

  @Override
  public int getChildrenCount(int i) {
    try {
      ReminderTaskFireBase.Reminder reminder = getter.getRemindList().get(i);
      List<ReminderTaskFireBase.Task> tasks = getter.getTaskInReminder(reminder);
      return tasks.size();
    }catch (Exception e){
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public ReminderTaskFireBase.Reminder getGroup(int i) {
    return getter.getRemindList().get(i);
  }

  @Override
  public ReminderTaskFireBase.Task getChild(int group, int child) {
    try {
      ReminderTaskFireBase.Reminder reminder = getter.getRemindList().get(group);
      return getter.getTaskInReminder(reminder).get(child);
    }catch (Exception e){
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public long getGroupId(int i) {
    ReminderTaskFireBase.Reminder reminder = getGroup(i);
    if(reminder != null){
      return reminder.id.hashCode();
    }
    return 0;
  }

  @Override
  public long getChildId(int group, int child) {
    ReminderTaskFireBase.Task task = getChild(group,child);
    if(task != null){
      return task.id.hashCode();
    }
    return 0;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
    if(view == null){
      view = inflater.inflate(R.layout.task_item_group, viewGroup, false);
    }
    ReminderTaskFireBase.Reminder reminder = getGroup(i);
    if(reminder == null){
      return view;
    }
    TextView txtGroupName = view.findViewById(R.id.txtTaskGroupName);
    txtGroupName.setTypeface(null, Typeface.BOLD);
    txtGroupName.setText(reminder.title);
    return view;
  }

  @Override
  public View getChildView(int group, int child, boolean b, View view, ViewGroup viewGroup) {
    if (view == null) {
      view = inflater.inflate(R.layout.task_item, viewGroup, false);
    }
    ReminderTaskFireBase.Task task = getChild(group,child);
    if(task == null){
      return view;
    }
    TextView txtTaskName = view.findViewById(R.id.txtTaskName);
    txtTaskName.setText(task.title);
    return view;
  }

  @Override
  public boolean isChildSelectable(int i, int i1) {
    return false;
  }
}