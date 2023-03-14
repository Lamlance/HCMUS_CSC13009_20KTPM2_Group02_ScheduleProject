package com.honaglam.scheduleproject.Reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import java.util.List;

public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderViewHolder> {

  Context context;
  List<ReminderData> reminderDataList;

  public ReminderRecyclerAdapter(Context context, List<ReminderData> reminderData){
    this.context = context;
    this.reminderDataList = reminderData;
  }

  @NonNull
  @Override
  public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ReminderViewHolder reminderViewHolder = new ReminderViewHolder(
            LayoutInflater.from(context).inflate(R.layout.reminder_recycler_item, parent, false));
    return reminderViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
    holder.txtId.setText(reminderDataList.get(position).Name);
  }

  @Override
  public int getItemCount() {
    return reminderDataList.size();
  }
}
