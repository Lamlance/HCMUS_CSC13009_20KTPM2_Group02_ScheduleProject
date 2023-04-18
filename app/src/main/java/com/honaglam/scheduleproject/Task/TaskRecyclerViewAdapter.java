package com.honaglam.scheduleproject.Task;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import java.util.List;

import kotlin.NotImplementedError;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskViewHolder> {

  Context context;
  LayoutInflater inflater;

  private int selectedPosition = -1;

  TaskViewHolder.OnClickPositionCallBack deleteTaskCallback;

  TaskViewHolder.OnClickPositionCallBack checkTaskCallback;

  TaskViewHolder.OnClickPositionCallBack editTaskCallback;
  TaskViewHolder.OnClickPositionCallBack moveToHistoryCallback;


  public interface GetListCallback {
    public List<TaskData> getList();
  }

  GetListCallback dataGet;

  public TaskRecyclerViewAdapter(
          Context context,
          GetListCallback callback,
          TaskViewHolder.OnClickPositionCallBack deleteTaskCallback,
          TaskViewHolder.OnClickPositionCallBack checkTaskCallback,
          TaskViewHolder.OnClickPositionCallBack editTaskCallback,
          TaskViewHolder.OnClickPositionCallBack moveToHistoryCallback
  ) {
    this.context = context;
    dataGet = callback;
    this.deleteTaskCallback = deleteTaskCallback;
    this.checkTaskCallback = checkTaskCallback;
    this.editTaskCallback = editTaskCallback;
    this.moveToHistoryCallback = moveToHistoryCallback;
  }

  @NonNull
  @Override
  public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    TaskViewHolder viewHolder = new TaskViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false),
            deleteTaskCallback,
            checkTaskCallback,
            editTaskCallback,
            moveToHistoryCallback
    );

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
    TaskData taskData = dataGet.getList().get(position);

    holder.txtTaskName.setText(taskData.taskName);
    holder.txtCountPomodoro.setText(
            taskData.numberCompletedPomodoros + "/ " + taskData.numberPomodoros);
    holder.checkBoxCompleteTask.setChecked(taskData.isCompleted);
    if(taskData.isCompleted){
      holder.txtTaskName.setPaintFlags(
              holder.txtTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
      );
    }else{
      if((holder.txtTaskName.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == Paint.STRIKE_THRU_TEXT_FLAG){
        holder.txtTaskName.setPaintFlags(
                holder.txtTaskName.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG
        );
      }
    }

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          selectedPosition = holder.getAdapterPosition();
          holder.itemView.requestFocus();
          Log.d("Show itemView request focus: ", holder.itemView.toString());
          notifyDataSetChanged();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
//    TODO: Change color red_700 to another color
    if (position == selectedPosition) {

      holder.itemView.setBackgroundResource(R.color.selected_white);
    } else {
      holder.itemView.setBackgroundColor(Color.TRANSPARENT);
    }
  }


  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public int getItemCount() {
    return dataGet.getList().size();
  }


  public int getSelectedPosition() {
    return this.selectedPosition;
  }

}
