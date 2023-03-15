package com.honaglam.scheduleproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.honaglam.scheduleproject.Model.TaskData;

public class TaskListAdapter extends BaseAdapter {

    private TaskData[] data;

    Context context;
    LayoutInflater inflater;


    public TaskListAdapter(Context context, LayoutInflater inflater, TaskData[] data) {
        this.data = data;
        this.inflater = inflater;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.task_item, parent, false);
        } else {
            TextView txtTaskName = (TextView) view.findViewById(R.id.txtTaskName);
            TextView txtCountPomodoro = (TextView) view.findViewById(R.id.txtCountPomodoro);
            CheckBox checkBoxCompleteTask = (CheckBox) view.findViewById(R.id.checkBoxCompleteTask);

            txtTaskName.setText(data[i].taskName);
            txtCountPomodoro.setText(data[i].numberCompletedPomodoros  + "/" + data[i].numberPomodoros);
            if (data[i].isCompleted) {
                checkBoxCompleteTask.setChecked(true);
            } else checkBoxCompleteTask.setChecked(false);
        }
        return view;
    }
}
