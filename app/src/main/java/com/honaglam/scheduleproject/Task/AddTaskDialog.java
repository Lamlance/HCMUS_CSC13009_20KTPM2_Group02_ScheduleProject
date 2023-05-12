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
  ReminderTaskFireBase.Task tempData = new ReminderTaskFireBase.Task();
  AddTaskDialogListener listener;

  public AddTaskDialog(@NonNull Context context, AddTaskDialogListener listener) {
    super(context);
    this.listener = listener;
    setContentView(R.layout.add_task_dialog);

    tempData.loopsDone = 0;
    tempData.loops = 1;
    tempData.id = "";
    tempData.title = "";

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

    btnUp.setOnClickListener(view -> {
      tempData.loops++;
      editTextCountPomodoro.setText(Integer.toString(tempData.loops));
    });

    btnDown.setOnClickListener(view -> {
      if (tempData.loops == 1) return;
      tempData.loops--;
      editTextCountPomodoro.setText(Integer.toString(tempData.loops));
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

    btnCancel.setOnClickListener(view -> dismiss());
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

    tempData.title = oldData.title;
    tempData.loops = oldData.loops;

    editTxtTaskName.setText(tempData.title);
    editTextCountPomodoro.setText(Integer.toString(tempData.loops));

    btnUp.setOnClickListener(view -> {
      tempData.loops++;
      editTextCountPomodoro.setText(Integer.toString(tempData.loops));
    });

    btnDown.setOnClickListener(view -> {
      if (tempData.loops == 1) return;
      tempData.loops--;
      editTextCountPomodoro.setText(Integer.toString(tempData.loops));
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
