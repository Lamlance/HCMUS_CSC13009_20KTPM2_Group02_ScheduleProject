package com.honaglam.scheduleproject.Task;


import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;


public class TaskViewHolder extends RecyclerView.ViewHolder {

  TextView txtTaskName;
  TextView txtCountPomodoro;
  CheckBox checkBoxCompleteTask;
  ImageButton imgBtnDeleteTask;

  OnClickPositionCallBack deleteTaskCallback = null;

  OnClickPositionCallBack checkTaskCallback = null;
  OnClickPositionCallBack moveToHistoryCallback = null;
  ImageButton imgBtnEditTask;
  ImageButton imgBtnMoveToHistory;

  public interface OnClickPositionCallBack {
    void clickAtPosition(int position) throws NotImplementedError;
    default void onClickCheckPosition(int pos, boolean isChecked){
      OnClickPositionCallBack.this.clickAtPosition(pos);
    }
  }

  public TaskViewHolder(@NonNull View itemView,
                        OnClickPositionCallBack deleteTaskCallback,
                        OnClickPositionCallBack checkTaskCallback,
                        OnClickPositionCallBack editTaskCallback,
                        OnClickPositionCallBack moveToHistoryCallback) {
    super(itemView);

    this.txtTaskName = itemView.findViewById(R.id.txtTaskName);
    this.txtCountPomodoro = itemView.findViewById(R.id.txtCountPomodoro);
    this.checkBoxCompleteTask = itemView.findViewById(R.id.checkBoxCompleteTask);
    this.imgBtnDeleteTask = itemView.findViewById(R.id.imgBtnDeleteTask);
    this.imgBtnEditTask = itemView.findViewById(R.id.imgBtnEditTask);
    this.imgBtnMoveToHistory = itemView.findViewById(R.id.imgBtnMoveToHistory);
    this.deleteTaskCallback = deleteTaskCallback;
    this.checkTaskCallback = checkTaskCallback;
    this.moveToHistoryCallback = moveToHistoryCallback;

    imgBtnEditTask.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int position = getAdapterPosition();
          editTaskCallback.clickAtPosition(position);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    this.imgBtnDeleteTask.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int position = getAdapterPosition();
          deleteTaskCallback.clickAtPosition(position);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    this.imgBtnMoveToHistory.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int position = getAdapterPosition();
            moveToHistoryCallback.clickAtPosition(position);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    this.checkBoxCompleteTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        checkTaskCallback.onClickCheckPosition(TaskViewHolder.this.getAdapterPosition(),b);
      }
    });
  }


}