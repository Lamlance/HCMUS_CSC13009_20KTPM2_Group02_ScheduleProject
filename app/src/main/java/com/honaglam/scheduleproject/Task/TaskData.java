package com.honaglam.scheduleproject.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.Reminder.ReminderData;

public class TaskData {

    public String taskName;
    public int id;
    public int numberCompletedPomodoros;
    public int numberPomodoros;
    public boolean isCompleted;

    public static final ReminderData DEFAULT_TASK_DATA_HOLDER = new ReminderData("NONE",-1,-1);

    public @NonNull ReminderData reminderData = DEFAULT_TASK_DATA_HOLDER;

    public TaskData(String taskName, int numberPomodoros,int id) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = 0;
        this.numberPomodoros = numberPomodoros;
        this.isCompleted = false;
        this.id = id;
    }
    public TaskData(String taskName, int numberPomodoros,int id,boolean isCompleted) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = 0;
        this.numberPomodoros = numberPomodoros;
        this.isCompleted = isCompleted;
        this.id = id;
    }

    public TaskData(String taskName, int numberPomodoros, int numberPomodorosCompleted ,int id,boolean isCompleted) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = numberPomodorosCompleted;
        this.numberPomodoros = numberPomodoros;
        this.isCompleted = isCompleted;
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof  TaskData)){
            return false;
        }
        return ((TaskData) obj).id == this.id;
    }
}
