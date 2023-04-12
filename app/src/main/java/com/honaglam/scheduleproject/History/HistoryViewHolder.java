package com.honaglam.scheduleproject.History;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.Task.TaskViewHolder;

import kotlin.NotImplementedError;

public class HistoryViewHolder extends RecyclerView.ViewHolder{
  TextView txtTaskNameHistory;
  TextView txtCountPomodoroHistory;
  ImageButton imgBtnMoveToTodoTask;
  HistoryViewHolder.OnClickPositionCallBack moveToTodoTaskCallback = null;


  public interface OnClickPositionCallBack {
    void clickAtPosition(int position) throws NotImplementedError;
  }

  public HistoryViewHolder(@NonNull View itemView,
                           HistoryViewHolder.OnClickPositionCallBack moveToTodoTaskCallback) {
    super(itemView);
    this.txtTaskNameHistory = itemView.findViewById(R.id.txtTaskNameHistory);
    this.txtCountPomodoroHistory = itemView.findViewById(R.id.txtCountPomodoroHistory);
    this.imgBtnMoveToTodoTask = itemView.findViewById(R.id.imgBtnMoveToTodoTask);
    this.moveToTodoTaskCallback = moveToTodoTaskCallback;

    this.imgBtnMoveToTodoTask.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int position = getAdapterPosition();
          moveToTodoTaskCallback.clickAtPosition(position);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}
