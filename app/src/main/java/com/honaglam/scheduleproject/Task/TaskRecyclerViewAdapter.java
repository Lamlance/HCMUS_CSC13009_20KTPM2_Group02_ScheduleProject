package com.honaglam.scheduleproject.Task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.Model.TaskData;
import com.honaglam.scheduleproject.R;

import java.util.ArrayList;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.TaskViewHolder> {

    private ArrayList<TaskData> data;
    Context context;
    LayoutInflater inflater;


    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTaskName;
        TextView txtCountPomodoro;
        CheckBox checkBoxCompleteTask;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTaskName = itemView.findViewById(R.id.txtTaskName);
            txtCountPomodoro = itemView.findViewById(R.id.txtCountPomodoro);
            checkBoxCompleteTask = itemView.findViewById(R.id.checkBoxCompleteTask);
        }
    }

    public TaskRecyclerViewAdapter(Context context, ArrayList<TaskData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskViewHolder viewHolder = new TaskViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false)
        );

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.txtTaskName.setText(data.get(position).taskName);
        holder.txtCountPomodoro.setText(data.get(position).numberCompletedPomodoros + "/ " + data.get(position).numberPomodoros);
        holder.checkBoxCompleteTask.setChecked(data.get(position).isCompleted);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
