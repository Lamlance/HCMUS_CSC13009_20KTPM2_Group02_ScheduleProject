package com.honaglam.scheduleproject.Task;

public class TaskData {

    public String taskName;
    public int id;
    public int numberCompletedPomodoros;
    public int numberPomodoros;
    public boolean isCompleted;

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

}
