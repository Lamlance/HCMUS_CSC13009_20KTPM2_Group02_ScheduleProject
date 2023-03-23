package com.honaglam.scheduleproject.Task;

import android.view.View;
import android.widget.CheckBox;
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


    public interface OnClickPositionCallBack {
        void clickAtPosition(int position) throws NotImplementedError;
    }

    public TaskViewHolder(@NonNull View itemView, OnClickPositionCallBack deleteTaskCallback, OnClickPositionCallBack checkTaskCallback) {
        super(itemView);
        this.txtTaskName = itemView.findViewById(R.id.txtTaskName);
        this.txtCountPomodoro = itemView.findViewById(R.id.txtCountPomodoro);
        this.checkBoxCompleteTask = itemView.findViewById(R.id.checkBoxCompleteTask);
        this.imgBtnDeleteTask = itemView.findViewById(R.id.imgBtnDeleteTask);

        this.deleteTaskCallback = deleteTaskCallback;

        this.checkTaskCallback = checkTaskCallback;


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

        this.checkBoxCompleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int position = getAdapterPosition();
                    checkTaskCallback.clickAtPosition(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}