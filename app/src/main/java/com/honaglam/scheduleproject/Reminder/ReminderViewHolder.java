package com.honaglam.scheduleproject.Reminder;

import android.animation.LayoutTransition;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;

public class ReminderViewHolder extends RecyclerView.ViewHolder{
  TextView txtId;
  TextView txtName;
  ConstraintLayout layoutMenu;
  SelectItemCallBack callBack;
  ImageButton btnDeleteReminder;
  public ReminderViewHolder(@NonNull View itemView) {
    super(itemView);
    txtName = itemView.findViewById(R.id.txtRecyclerRemindersId);
    txtId = itemView.findViewById(R.id.txtRecyclerRemindersTime);
    layoutMenu = itemView.findViewById(R.id.layoutReminderMenu);
    btnDeleteReminder = itemView.findViewById(R.id.btnDeleteReminder);
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

  public void openMenu(boolean isOpen){
    layoutMenu.setVisibility(isOpen ? View.VISIBLE : View.GONE);
  }

  public interface SelectItemCallBack {
    public void onClickPos(int pos) throws NotImplementedError;
  }
}
