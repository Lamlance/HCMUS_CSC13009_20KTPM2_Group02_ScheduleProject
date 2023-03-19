package com.honaglam.scheduleproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.honaglam.scheduleproject.Model.TaskData;

public class AddTaskDialog extends Dialog {

    public interface AddTaskDialogListener {
        void onDataPassed(TaskData taskData);
    }

    EditText editTxtTaskName;

    EditText editTextCountPomodoro;
    Button btnUp;
    Button btnDown;
    Button btnSave;
    Button btnCancel;

    AddTaskDialogListener listener;

    public AddTaskDialog(@NonNull Context context, AddTaskDialogListener listener) {
        super(context);
        this.listener = listener;
        setContentView(R.layout.add_task_dialog);

        TaskData tempData = new TaskData("");

        // Set width for dialog
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setAttributes(lp);

        editTxtTaskName = findViewById(R.id.editTxtTaskName);
        editTextCountPomodoro = findViewById(R.id.editTxtCountPomodoro);
        btnSave = findViewById(R.id.btnSaveTask);
        btnCancel = findViewById(R.id.btnCancelAddTask);
        btnDown = findViewById(R.id.btnDown);
        btnUp = findViewById(R.id.btnUp);

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempData.numberPomodoros++;
                editTextCountPomodoro.setText(Integer.toString(tempData.numberPomodoros));
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tempData.numberPomodoros == 1) return;
                tempData.numberPomodoros--;
                editTextCountPomodoro.setText(Integer.toString(tempData.numberPomodoros));
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskName = editTxtTaskName.getText().toString();
                int countPomodoro = Integer.parseInt(editTextCountPomodoro.getText().toString());

                if (taskName.equals("")) {
                    dismiss();
                    return;
                }

                tempData.taskName = taskName;
                tempData.numberPomodoros = countPomodoro;

                listener.onDataPassed(tempData);
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
