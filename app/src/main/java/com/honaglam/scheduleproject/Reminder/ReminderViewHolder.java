package com.honaglam.scheduleproject.Reminder;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;

public class ReminderViewHolder extends RecyclerView.ViewHolder{
  TextView txtId;
  TextView txtName;
  int selectedId = -1;
  SelectItemCallBack callBack;
  public ReminderViewHolder(@NonNull View itemView) {
    super(itemView);
    txtName = itemView.findViewById(R.id.txtRecyclerRemindersId);
    txtId = itemView.findViewById(R.id.txtRecyclerRemindersTime);
  }

  public void  setSelectItemCallback(SelectItemCallBack callBack){
    this.callBack = callBack;
    try {
      callBack.onClickPos(0);
    }catch (Exception ignore){}

    itemView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
          case MotionEvent.ACTION_DOWN:{
            try {
              callBack.onClickPos(getAdapterPosition());
            }catch (Exception ignore){}
            break;
          }
          case MotionEvent.ACTION_UP:{
            view.performClick();
            break;
          }
        }
        return true;
      }
    });
  }

  public interface SelectItemCallBack {
    public void onClickPos(int pos) throws NotImplementedError;
  }
}
