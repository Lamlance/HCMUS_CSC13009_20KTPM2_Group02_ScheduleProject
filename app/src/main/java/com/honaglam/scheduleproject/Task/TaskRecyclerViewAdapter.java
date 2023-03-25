package com.honaglam.scheduleproject.Task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionBarPolicy;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.AddTaskDialog;
import com.honaglam.scheduleproject.Model.TaskData;
import com.honaglam.scheduleproject.R;

import java.util.ArrayList;
import java.util.List;

import kotlin.NotImplementedError;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    Context context;
    LayoutInflater inflater;
    AddTaskDialog.AddTaskDialogListener listener;

    TaskViewHolder.OnClickPositionCallBack deleteTaskCallback = new TaskViewHolder.OnClickPositionCallBack() {
        @Override
        public void clickAtPosition(int position) throws NotImplementedError {
            dataGet.getList().remove(position);
            notifyItemRemoved(position);
        }
    };

    TaskViewHolder.OnClickPositionCallBack checkTaskCallback = new TaskViewHolder.OnClickPositionCallBack() {
        @Override
        public void clickAtPosition(int position) throws NotImplementedError {
            dataGet.getList().get(position).isCompleted = !dataGet.getList().get(position).isCompleted;
            notifyItemChanged(position);
        }
    };

    TaskViewHolder.OnClickPositionCallBack editTaskCallback = new TaskViewHolder.OnClickPositionCallBack() {
        @Override
        public void clickAtPosition(int position) throws NotImplementedError {
        }
    };


    public interface  GetListCallback {
        public List<TaskData> getList();
    }

    GetListCallback dataGet;
    public TaskRecyclerViewAdapter(Context context, GetListCallback callback) {
        this.context = context;
        dataGet = callback;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskViewHolder viewHolder = new TaskViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false),
                deleteTaskCallback,
                checkTaskCallback,
                editTaskCallback
        );

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.txtTaskName.setText(dataGet.getList().get(position).taskName);
        holder.txtCountPomodoro.setText(dataGet.getList().get(position).numberCompletedPomodoros + "/ " + dataGet.getList().get(position).numberPomodoros);
        holder.checkBoxCompleteTask.setChecked(dataGet.getList().get(position).isCompleted);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return dataGet.getList().size();
    }
}
