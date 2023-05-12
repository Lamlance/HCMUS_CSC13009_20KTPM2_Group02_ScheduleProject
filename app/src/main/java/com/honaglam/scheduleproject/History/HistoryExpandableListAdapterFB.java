package com.honaglam.scheduleproject.History;

import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.HistoryFragment;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Date;
import java.util.Locale;

public class HistoryExpandableListAdapterFB extends BaseExpandableListAdapter {
  LayoutInflater inflater;
  HistoryFragment.TaskDataGetter dataGetter;
  public HistoryExpandableListAdapterFB(LayoutInflater inflater, HistoryFragment.TaskDataGetter getter){
    this.inflater = inflater;
    dataGetter = getter;
  }

  @Override
  public int getGroupCount() {
    return dataGetter.getReminders().size();
  }

  @Override
  public int getChildrenCount(int i) {
    ReminderTaskFireBase.Reminder reminder = dataGetter.getReminders().get(i);
    if(reminder == null){
      return 0;
    }
    return dataGetter.getTaskFromReminder(reminder).size();
  }

  @Override
  @Nullable
  public ReminderTaskFireBase.Reminder getGroup(int i) {
    return dataGetter.getReminders().get(i);
  }

  @Override
  @Nullable
  public ReminderTaskFireBase.Task getChild(int group, int child) {
    ReminderTaskFireBase.Reminder reminder = dataGetter.getReminders().get(group);
    if(reminder == null){
      return null;
    }
    return dataGetter.getTaskFromReminder(reminder).get(child);
  }

  @Override
  public long getGroupId(int i) {
    ReminderTaskFireBase.Reminder reminder = dataGetter.getReminders().get(i);
    if(reminder == null){
      return 0;
    }
    return reminder.id.hashCode();
  }

  @Override
  public long getChildId(int group, int child) {
    ReminderTaskFireBase.Reminder reminder = dataGetter.getReminders().get(group);
    if(reminder == null){
      return 0;
    }
    ReminderTaskFireBase.Task task = dataGetter.getTaskFromReminder(reminder).get(child);
    return task == null ? 0 : task.id.hashCode();
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
    if(view == null){
      view = inflater.inflate(R.layout.task_history_group,viewGroup,false);
    }
    TextView txtHistoryGroupName = view.findViewById(R.id.txtHistoryGroupName);
    ReminderTaskFireBase.Reminder reminder = getGroup(i);
    if(reminder != null){
      long time = reminder.reminderTime;
      String dateFormat = DateFormat.getDateTimeInstance().format(new Date(time));
      txtHistoryGroupName.setText(dateFormat);
    }
    return view;
  }

  @Override
  public View getChildView(int group, int child, boolean b, View view, ViewGroup viewGroup) {
    if(view == null){
      view = inflater.inflate(R.layout.task_history,viewGroup,false);
    }
    TextView txtTaskNameHistory = view.findViewById(R.id.txtTaskNameHistory);
    TextView txtCountPomodoroHistory = view.findViewById(R.id.txtCountPomodoroHistory);

    ReminderTaskFireBase.Task task = getChild(group,child);
    if(task != null){
      txtTaskNameHistory.setText(task.title);
      txtCountPomodoroHistory.setText(String.format(Locale.getDefault(),
              "%d/%d",task.loopsDone,task.loops));
    }

    return view;
  }

  @Override
  public boolean isChildSelectable(int i, int i1) {
    return false;
  }
}
