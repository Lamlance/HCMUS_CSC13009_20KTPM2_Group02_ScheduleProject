package com.honaglam.scheduleproject.Reminder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class ReminderRecyclerAdapterFB extends RecyclerView.Adapter<ReminderViewHolder>{
  public interface TaskListGetter{
    List<ReminderTaskFireBase.Task> getList();
  }
  public interface ItemAction{
    void onAction(int position);
  }


  int selectedPos = -1;
  Context context;
  TaskListGetter taskListGetter;
  ItemAction itemClickAction;
  ItemAction deleteClickAction;

  ReminderRecyclerAdapterFB(Context context,TaskListGetter getter){
    this.context = context;
    this.taskListGetter = getter;
  }

  public void callItemAction(ItemAction action){
    if(action != null){
      try{
        action.onAction(selectedPos);
      }catch (Exception e ){
        e.printStackTrace();
      }
    }
  }
  public void setItemClickAction(ItemAction action){
    itemClickAction = action;
  }
  public void setDeleteClickAction(ItemAction action) {deleteClickAction = action;}

  @NonNull
  @Override
  public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ReminderViewHolder viewHolder = new ReminderViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_recycler_item, parent, false)
    );
    viewHolder.setSelectItemCallback((pos) -> {
      selectedPos = pos;
      callItemAction(itemClickAction);
    });
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
    holder.btnDeleteReminder.setOnClickListener((clickedView) -> {
      callItemAction(deleteClickAction);
    });
    holder.itemView.setBackgroundColor(position == selectedPos ?
            ResourcesCompat.getColor(context.getResources(), R.color.selected_white, null) :
            Color.TRANSPARENT);
  }

  @Override
  public int getItemCount() {
    return 0;
  }
}
