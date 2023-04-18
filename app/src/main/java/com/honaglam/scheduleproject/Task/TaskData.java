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
    public int date;
    public int month;
    public int year;

    public static final ReminderData DEFAULT_TASK_DATA_HOLDER = new ReminderData("NONE",-1,-1);

    public @NonNull ReminderData reminderData = DEFAULT_TASK_DATA_HOLDER;


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

    public TaskData(String taskName, int numberPomodoros, int numberPomodorosCompleted ,int id,boolean isCompleted, int date, int month, int year) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = numberPomodorosCompleted;
        this.numberPomodoros = numberPomodoros;
        this.isCompleted = isCompleted;
        this.id = id;
        this.date = date;
        this.month = month;
        this.year = year;
    }

}
