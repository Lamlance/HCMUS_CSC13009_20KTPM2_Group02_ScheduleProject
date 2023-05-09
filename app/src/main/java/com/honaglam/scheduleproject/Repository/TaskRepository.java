package com.honaglam.scheduleproject.Repository;

import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class TaskRepository {
  public interface OnTaskAction{
    void onAction(@Nullable ReminderTaskFireBase.Task task);
  }

  public interface OnTasksSetReminder{
    void onAction(List<ReminderTaskFireBase.Task> tasks, @Nullable ReminderTaskFireBase.Reminder reminder);
  }

  @Nullable OnTaskAction OnAddTask;
  @Nullable OnTaskAction OnDeleteTask;
  @Nullable OnTasksSetReminder onTasksSetReminder;
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

  public void SetOnTasksSetReminder(OnTasksSetReminder action){
    onTasksSetReminder = action;
  }


  public void addTask(String title,int loops){
    ReminderTaskFireBase.Task task = ReminderTaskFireBase.GetInstance(userId).addTask(title,loops,0);
    CallTaskAction(OnAddTask,task);
  }

  public void updateTask(ReminderTaskFireBase.Task task){
    ReminderTaskFireBase.GetInstance(userId).updateTask(task);
  }

  public void removeTask(ReminderTaskFireBase.Task task){
    ReminderTaskFireBase.GetInstance(userId).removeTask(task);
  }


  public void setTasksSingleReminder(String reminderTitle, long reminderTime, List<ReminderTaskFireBase.Task> tasks){
    ReminderTaskFireBase.Reminder reminder = ReminderTaskFireBase
            .GetInstance(userId)
            .makeTaskSingleReminder(reminderTitle,reminderTime,tasks);
    if(onTasksSetReminder != null){
      onTasksSetReminder.onAction(tasks,reminder);
    }
  }

  public void setTaskWeeklyReminder(String reminderTitle, List<Integer> remindWeekDays, List<ReminderTaskFireBase.Task> tasks){
    ReminderTaskFireBase.Reminder reminder = ReminderTaskFireBase
            .GetInstance(userId)
            .makeTaskWeeklyReminder(reminderTitle,remindWeekDays,tasks);

    if(onTasksSetReminder != null){
      onTasksSetReminder.onAction(tasks,reminder);
    }
  }
}
