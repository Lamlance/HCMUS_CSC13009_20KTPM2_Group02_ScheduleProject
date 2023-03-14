package com.honaglam.scheduleproject.Reminder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honaglam.scheduleproject.R;

public class ReminderViewHolder extends RecyclerView.ViewHolder{
  TextView txtId;
  public ReminderViewHolder(@NonNull View itemView) {
    super(itemView);
    txtId = itemView.findViewById(R.id.txtRecyclerRemindersId);
  }
}
