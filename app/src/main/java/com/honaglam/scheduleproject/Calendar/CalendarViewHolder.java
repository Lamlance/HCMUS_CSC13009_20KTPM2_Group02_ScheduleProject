package com.honaglam.scheduleproject.Calendar;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder {
  TextView txtDate;
  public CalendarViewHolder(@NonNull View itemView) {
    super(itemView);
    txtDate = itemView.findViewById(R.id.txtCalendarDateItem);
  }
}

