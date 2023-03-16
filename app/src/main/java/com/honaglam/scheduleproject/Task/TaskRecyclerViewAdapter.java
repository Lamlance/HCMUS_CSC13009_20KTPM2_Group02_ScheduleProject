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

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.TaskViewHolder> {

    private TaskData[] data;
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


    public TaskRecyclerViewAdapter(Context context, TaskData[] data) {
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
        holder.txtTaskName.setText(data[position].taskName);
        holder.txtCountPomodoro.setText(data[position].numberCompletedPomodoros + "/ " + data[position].numberPomodoros);
        holder.checkBoxCompleteTask.setChecked(data[position].isCompleted);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return data.length;
    }
}
