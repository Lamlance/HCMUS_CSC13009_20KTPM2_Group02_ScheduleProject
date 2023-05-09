package com.honaglam.scheduleproject.Task;

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

import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;
import com.honaglam.scheduleproject.TimerFragment;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TaskExpandableListAdapterFB extends BaseExpandableListAdapter {
  public interface ChildClickAction{
    void onChild(int group, int child, ReminderTaskFireBase.Task task);
  }

  public interface ChildCheckAction{
    void onChildCheck(int group, int child, ReminderTaskFireBase.Task task, boolean isChecked);
  }

  LayoutInflater inflater;
  TimerFragment.ReminderAndTaskListGetter getter;

  @Nullable View prevClickView;
  @Nullable ChildClickAction onChildClick;
  @Nullable ChildClickAction onChildDeleteClick;
  @Nullable ChildClickAction onChildArchiveClick;
  @Nullable ChildClickAction onChildEditClick;
  @Nullable ChildCheckAction onChildChecked;

  public TaskExpandableListAdapterFB(LayoutInflater inflater,TimerFragment.ReminderAndTaskListGetter getter){
    super();
    this.inflater = inflater;
    this.getter = getter;
  }


  public void SetOnChildChecked(ChildCheckAction action){
    onChildChecked = action;
  }

  public void SetOnChildClick(ChildClickAction action){
    onChildClick = action;
  }

  public void SetOnChildDeleteClick(ChildClickAction action){
    onChildDeleteClick = action;
  }

  public void SetOnChildArchiveClick(ChildClickAction action){
    onChildArchiveClick = action;
  }

  public void SetOnChildEditClick(ChildClickAction action){
    onChildEditClick = action;
  }

  private void CallChildAction(ChildClickAction action, int group, int child, ReminderTaskFireBase.Task task){
    if(action == null){
      return;
    }
    action.onChild(group,child,task);
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
    TextView txtCountPomodoro = view.findViewById(R.id.txtCountPomodoro);
    CheckBox checkBox = view.findViewById(R.id.checkBoxCompleteTask);
    ImageButton editBtn = view.findViewById(R.id.imgBtnEditTask);
    ImageButton deleteBtn = view.findViewById(R.id.imgBtnDeleteTask);
    ImageButton archiveBtn = view.findViewById(R.id.imgBtnMoveToHistory);

    txtTaskName.setText(task.title);
    txtCountPomodoro.setText(String.format(Locale.getDefault(),"%d/%d",task.loopsDone,task.loops));
    checkBox.setChecked(false);

    if(!task.reminder.equals(ReminderTaskFireBase.Task.DEFAULT_REMINDER)){
      checkBox.setVisibility(View.GONE);
    }else{
      checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
        if(onChildChecked != null){
          onChildChecked.onChildCheck(group,child,task,isChecked);
        }
      });
    }



    view.setOnClickListener(clickedView -> {
      if(prevClickView != null){
        prevClickView.setBackgroundColor(Color.TRANSPARENT);
      }
      prevClickView = clickedView;
      clickedView.setBackgroundColor(Color.parseColor("#80D2D2D2"));
      CallChildAction(onChildClick,group,child,task);
    });
    editBtn.setOnClickListener(clickedView -> {
      CallChildAction(onChildEditClick,group,child,task);
    });
    deleteBtn.setOnClickListener(clickedView -> {
      CallChildAction(onChildDeleteClick,group,child,task);
    });
    archiveBtn.setOnClickListener(clickedView -> {
      CallChildAction(onChildArchiveClick,group,child,task);
    });

    return view;
  }

  @Override
  public boolean isChildSelectable(int i, int i1) {
    return false;
  }
}
