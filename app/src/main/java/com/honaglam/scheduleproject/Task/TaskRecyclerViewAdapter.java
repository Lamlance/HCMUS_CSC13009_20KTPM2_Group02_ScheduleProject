package com.honaglam.scheduleproject.Task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.Calendar.CalendarViewHolder;
import com.honaglam.scheduleproject.Model.TaskData;
import com.honaglam.scheduleproject.R;

import java.util.ArrayList;

import kotlin.NotImplementedError;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private ArrayList<TaskData> data;
    Context context;
    LayoutInflater inflater;

    public TaskRecyclerViewAdapter(Context context, ArrayList<TaskData> data) {
        this.context = context;
        this.data = data;
    }
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskViewHolder viewHolder = new TaskViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false),
                deleteTaskCallback,
                checkTaskCallback
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

    TaskViewHolder.OnClickPositionCallBack deleteTaskCallback = new TaskViewHolder.OnClickPositionCallBack() {
        @Override
        public void clickAtPosition(int position) {
            data.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, data.size());
        }
    };

    TaskViewHolder.OnClickPositionCallBack checkTaskCallback = new TaskViewHolder.OnClickPositionCallBack() {
        @Override
        public void clickAtPosition(int position) {
            data.get(position).isCompleted = !data.get(position).isCompleted;
            notifyItemChanged(position);
        }
    };
}
