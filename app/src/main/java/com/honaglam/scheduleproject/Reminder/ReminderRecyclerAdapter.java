package com.honaglam.scheduleproject.Reminder;

import android.content.Context;
import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

  Context context;
  ReminderListGetter reminderListGetter = null;

  public int selectedItemPos = -1;
  public ReminderRecyclerAdapter(Context context, ReminderListGetter getter){
    this.context = context;
    this.reminderListGetter = getter;
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


  }

  @Override
  public int getItemCount() {
    return reminderListGetter.get().size();
  }
  class ReminderItemClickCallBack implements ReminderViewHolder.SelectItemCallBack{
    @Override
    public void onClickPos(int pos) throws NotImplementedError {
      selectedItemPos = pos;
    }
  }
}