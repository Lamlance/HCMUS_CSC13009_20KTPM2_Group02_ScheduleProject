package com.honaglam.scheduleproject.Reminder;

import android.content.Context;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlin.NotImplementedError;

public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderViewHolder> {
  public interface ReminderListGetter{
    List<ReminderData> get() throws NotImplementedError;
  }

  public interface DeleteListCallBack{
    void deletePos(int pos) throws NotImplementedError;
  }

  Context context;
  ReminderListGetter reminderListGetter = null;
  DeleteListCallBack deleteListCallBack = null;
  public int selectedItemPos = -1;
  public int swipePos = -1;
  public ReminderRecyclerAdapter(
          Context context,
          ReminderListGetter getter,
          DeleteListCallBack deleteListCallBack){
    this.context = context;
    this.reminderListGetter = getter;
    this.deleteListCallBack = deleteListCallBack;
  }

  @NonNull
  @Override
  public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ReminderViewHolder reminderViewHolder = new ReminderViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_recycler_item, parent, false));
    reminderViewHolder.setSelectItemCallback(new ReminderItemClickCallBack());
    return reminderViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
    ReminderData data = reminderListGetter.get().get(position);
    String dateFormat = DateFormat.getDateTimeInstance().format(new Date(data.RemindTime));
    holder.txtId.setText(dateFormat);
    holder.txtName.setText(data.Name);

    holder.itemView.setBackgroundColor(position == selectedItemPos ?
            ResourcesCompat.getColor(context.getResources(), R.color.selected_white, null) :
            Color.TRANSPARENT);
    holder.btnDeleteReminder.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try{
          deleteListCallBack.deletePos(selectedItemPos);
        }catch (Exception ignore){}
      }
    });
    holder.openMenu(selectedItemPos == position);
  }


  @Override
  public int getItemCount() {
    return reminderListGetter.get().size();
  }
  class ReminderItemClickCallBack implements ReminderViewHolder.SelectItemCallBack{
    @Override
    public void onClickPos(int pos) {
      int oldPos = selectedItemPos;
      selectedItemPos = pos;
      int listSize = reminderListGetter.get().size();

      if(oldPos >= 0 && oldPos < listSize){
        notifyItemChanged(oldPos);
      }
      if(selectedItemPos >= 0 && selectedItemPos < listSize){
        notifyItemChanged(selectedItemPos);
      }


    }
  }
}
