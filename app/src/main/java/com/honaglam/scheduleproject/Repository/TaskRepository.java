package com.honaglam.scheduleproject.Repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class TaskRepository {
  public interface OnTaskAction {
    void onAction(@Nullable ReminderTaskFireBase.Task task);
  }

  public interface OnTasksSetReminder {
    void onAction(List<ReminderTaskFireBase.Task> tasks, @Nullable ReminderTaskFireBase.Reminder reminder);
  }

  public interface OnTaskSearchCompleted {
    void onCompleted(@NonNull List<ReminderTaskFireBase.Task> tasks);
  }

  @Nullable
  OnTaskAction OnAddTask;
  @Nullable
  OnTaskAction OnDeleteTask;
  @Nullable
  OnTasksSetReminder onTasksSetReminder;
  @Nullable
  OnTaskSearchCompleted onTaskSearchCompleted;
  String userId;

  public TaskRepository(String userId) {
    this.userId = userId;
  }

  private void CallTaskAction(OnTaskAction action, ReminderTaskFireBase.Task task) {
    if (action == null) {
      return;
    }
    action.onAction(task);
  }

  public void SetOnTaskAdded(OnTaskAction action) {
    OnAddTask = action;
  }

  public void SetOnTasksSetReminder(OnTasksSetReminder action) {
    onTasksSetReminder = action;
  }

  public void SetOnTaskDeleted(OnTaskAction action) {
    OnDeleteTask = action;
  }

  public void SetOnTaskSearchCompleted(OnTaskSearchCompleted completed){
    this.onTaskSearchCompleted = completed;
  }

  public void addTask(String title, int loops) {
    ReminderTaskFireBase.GetInstance(userId,"Task").addTask(title, loops, 0, task -> {
      CallTaskAction(OnAddTask, task);
    });
  }

  public void updateTask(ReminderTaskFireBase.Task task) {
    ReminderTaskFireBase.GetInstance(userId,"Task").updateTask(task, newTask -> {
    });
  }

  public void removeTask(ReminderTaskFireBase.Task task) {
    ReminderTaskFireBase.GetInstance(userId,"Task").removeTask(task, removedTask -> {
      CallTaskAction(OnDeleteTask, removedTask);
    });
  }


  public void setTasksSingleReminder(String reminderTitle, long reminderTime, List<ReminderTaskFireBase.Task> tasks) {
    ReminderTaskFireBase
            .GetInstance(userId,"Task")
            .makeTaskSingleReminder(reminderTitle, reminderTime, tasks, (reminder, taskList) -> {
              if (onTasksSetReminder != null) {
                onTasksSetReminder.onAction(taskList, reminder);
              }
            });
  }

  public void setTaskWeeklyReminder(String reminderTitle, List<Integer> remindWeekDays, List<ReminderTaskFireBase.Task> tasks) {
    ReminderTaskFireBase
            .GetInstance(userId,"Task")
            .makeTaskWeeklyReminder(reminderTitle, remindWeekDays, tasks, (reminder, taskList) -> {
              if (onTasksSetReminder != null) {
                onTasksSetReminder.onAction(taskList, reminder);
              }
            });
  }


  public void searchTaskInRange(long startTime, long endTime) {
    if (onTaskSearchCompleted == null) {
      return;
    }

    ReminderTaskFireBase.GetInstance(userId,"Reminder").SearchSingleReminderTask(startTime, endTime, taskList -> {
      onTaskSearchCompleted.onCompleted(taskList);
    });
  }

}
