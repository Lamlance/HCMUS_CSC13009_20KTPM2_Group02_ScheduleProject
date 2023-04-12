package com.honaglam.scheduleproject.History;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.Calendar.CalendarViewHolder;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.Task.TaskData;
import com.honaglam.scheduleproject.Task.TaskRecyclerViewAdapter;
import com.honaglam.scheduleproject.Task.TaskViewHolder;

import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryViewHolder>{
  Context context;
  LayoutInflater inflater;
  private int selectedPosition = -1;

  HistoryViewHolder.OnClickPositionCallBack moveToTodoTaskCallback;

  public interface GetListCallback {
    public List<TaskData> getList();
  }
  GetListCallback dataGet;

  public HistoryRecyclerViewAdapter(
          Context context,
          GetListCallback callback,
          HistoryViewHolder.OnClickPositionCallBack moveToTodoTaskCallback
  ) {
    this.context = context;
    dataGet = callback;
    this.moveToTodoTaskCallback = moveToTodoTaskCallback;
  }

  @NonNull
  @Override
  public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    HistoryViewHolder viewHolder = new HistoryViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.task_history, parent, false),
            moveToTodoTaskCallback
    );
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
    Log.i("HISTORY_NAME","Task pos " + position);

    TaskData taskData = dataGet.getList().get(position);

    Log.i("HISTORY_NAME","Task name " + taskData.taskName);
    holder.txtTaskNameHistory.setText(taskData.taskName);
    holder.txtCountPomodoroHistory.setText(taskData.numberCompletedPomodoros + "/ " + taskData.numberPomodoros);
  }


  @Override
  public long getItemId(int i) {
    return i;
  }
  @Override
  public int getItemCount() {
    return dataGet.getList().size();
  }

//  public int getSelectedPosition() {
//    return this.selectedPosition;
//  }
}