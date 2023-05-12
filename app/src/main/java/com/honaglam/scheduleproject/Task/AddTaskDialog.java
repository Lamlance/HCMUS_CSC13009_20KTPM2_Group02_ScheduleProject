package com.honaglam.scheduleproject.Task;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;
import com.honaglam.scheduleproject.Task.TaskData;

import java.time.LocalDate;

public class AddTaskDialog extends Dialog {

  public interface AddTaskDialogListener {
    void onDataPassed(ReminderTaskFireBase.Task taskData);
  }

  EditText editTxtTaskName;

  EditText editTextCountPomodoro;
  Button btnUp;
  Button btnDown;
  Button btnSave;
  Button btnCancel;
  TaskData tempData = new TaskData(
          "",0,0,
          -1,false,0,0,0);
  AddTaskDialogListener listener;

  public AddTaskDialog(@NonNull Context context, AddTaskDialogListener listener) {
    super(context);
    this.listener = listener;
    setContentView(R.layout.add_task_dialog);


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

        ReminderTaskFireBase.Task task = new ReminderTaskFireBase.Task();

        task.title = taskName;
        task.loops = countPomodoro;

        listener.onDataPassed(task);
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


  public AddTaskDialog(@NonNull Context context,
                       AddTaskDialogListener listener,
                       ReminderTaskFireBase.Task oldData) {
    super(context);
    this.listener = listener;
    setContentView(R.layout.add_task_dialog);

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

    tempData.taskName = oldData.title;
    tempData.numberPomodoros = oldData.loops;

    editTxtTaskName.setText(tempData.taskName);
    editTextCountPomodoro.setText(Integer.toString(tempData.numberPomodoros));

    btnUp.setOnClickListener(view -> {
      tempData.numberPomodoros++;
      editTextCountPomodoro.setText(Integer.toString(tempData.numberPomodoros));
    });

    btnDown.setOnClickListener(view -> {
      if (tempData.numberPomodoros == 1) return;
      tempData.numberPomodoros--;
      editTextCountPomodoro.setText(Integer.toString(tempData.numberPomodoros));
    });


    btnSave.setOnClickListener(view -> {
      String taskName = editTxtTaskName.getText().toString();
      int countPomodoro = Integer.parseInt(editTextCountPomodoro.getText().toString());

      if (taskName.equals("")) {
        dismiss();
        return;
      }

      oldData.title = taskName;
      oldData.loops = countPomodoro;

      listener.onDataPassed(oldData);
      dismiss();
    });

    btnCancel.setOnClickListener(view -> dismiss());
  }
}
