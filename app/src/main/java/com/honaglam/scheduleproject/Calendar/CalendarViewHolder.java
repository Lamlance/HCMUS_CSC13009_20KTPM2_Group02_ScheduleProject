package com.honaglam.scheduleproject.Calendar;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honaglam.scheduleproject.R;
import kotlin.NotImplementedError;


public class CalendarViewHolder extends RecyclerView.ViewHolder {
  TextView txtDate;
  OnClickPositionCallBack clickPositionCallBack = null;
  public interface OnClickPositionCallBack{
    void clickAtPosition(int position) throws NotImplementedError;
  }
  public CalendarViewHolder(@NonNull View itemView,OnClickPositionCallBack callBack) {
    super(itemView);

    this.clickPositionCallBack = callBack;

    txtDate = itemView.findViewById(R.id.txtCalendarDateItem);
    this.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int position = getAdapterPosition();
          if(position >= 0){
            clickPositionCallBack.clickAtPosition(position);
          }
        }catch (Exception e ){}
      }
    });
  }
}

