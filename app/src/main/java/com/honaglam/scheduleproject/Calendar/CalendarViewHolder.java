package com.honaglam.scheduleproject.Calendar;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
  TextView txtDate;
  public CalendarViewHolder(@NonNull View itemView) {
    super(itemView);
    itemView.setOnClickListener(this);
    txtDate = itemView.findViewById(R.id.txtCalendarDateItem);
  }

  @Override
  public void onClick(View view) {
    int position = getAdapterPosition();
    // Toast.makeText(view.getContext(), "Da chon duoc ngay " + position, Toast.LENGTH_SHORT).show();
  }
}

