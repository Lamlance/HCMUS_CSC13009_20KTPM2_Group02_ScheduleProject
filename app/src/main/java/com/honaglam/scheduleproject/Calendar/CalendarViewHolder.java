package com.honaglam.scheduleproject.Calendar;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;


import kotlin.NotImplementedError;


public class CalendarViewHolder extends RecyclerView.ViewHolder {

  TextView txtDate;
  OnClickPositionCallBack clickPositionCallBack = null;
  FrameLayout layoutItemAll;

  public interface OnClickPositionCallBack {
    void clickAtPosition(int position) throws NotImplementedError;
  }

  public CalendarViewHolder(@NonNull View itemView, OnClickPositionCallBack callBack) {
    super(itemView);

    this.clickPositionCallBack = callBack;

    txtDate = itemView.findViewById(R.id.txtCalendarDateItem);
    layoutItemAll = itemView.findViewById(R.id.layoutCalendarItemAll);
    this.itemView.setOnClickListener(view -> {
      try {
        int position = getAdapterPosition();
        if (position >= 0) {
          clickPositionCallBack.clickAtPosition(position);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}


