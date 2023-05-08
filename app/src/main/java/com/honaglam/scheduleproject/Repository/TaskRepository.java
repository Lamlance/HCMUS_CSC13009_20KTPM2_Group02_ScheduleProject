package com.honaglam.scheduleproject.Repository;

import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class TaskRepository {
  public interface OnTaskAction{
    void onAction(@Nullable ReminderTaskFireBase.Task task);
  }

  @Nullable OnTaskAction OnAddTask;
  @Nullable OnTaskAction OnDeleteTask;

  String userId;
  public TaskRepository(String userId) {
    this.userId = userId;
  }

  private void CallTaskAction(OnTaskAction action, ReminderTaskFireBase.Task task){
    if(action == null){
      return;
    }
    action.onAction(task);
  }

  public void SetOnTaskAdded(OnTaskAction action){
    OnAddTask = action;
  }


  public void addTask(String title,int loops){
    ReminderTaskFireBase.Task task = ReminderTaskFireBase.GetInstance(userId).addTask(title,loops,0);
    CallTaskAction(OnAddTask,task);
  }

  public void removeTask(ReminderTaskFireBase.Task task){
    ReminderTaskFireBase.GetInstance(userId).removeTask(task);
  }
}
