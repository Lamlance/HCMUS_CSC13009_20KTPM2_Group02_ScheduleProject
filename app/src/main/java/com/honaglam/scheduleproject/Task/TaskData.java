package com.honaglam.scheduleproject.Task;

public class TaskData {

    public String taskName;
    public int id;
    public int numberCompletedPomodoros;
    public int numberPomodoros;
    public boolean isCompleted;

    public TaskData(String taskName) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = 0;
        this.numberPomodoros = 1;
        this.isCompleted = false;
    }

    public TaskData(String taskName, int numberPomodoros) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = 0;
        this.numberPomodoros = numberPomodoros;
        this.isCompleted = false;
    }
    public TaskData(String taskName, int numberPomodoros,int id) {
        this.taskName = taskName;
        this.numberCompletedPomodoros = 0;
        this.numberPomodoros = numberPomodoros;
        this.isCompleted = false;
        this.id = id;
    }
}
