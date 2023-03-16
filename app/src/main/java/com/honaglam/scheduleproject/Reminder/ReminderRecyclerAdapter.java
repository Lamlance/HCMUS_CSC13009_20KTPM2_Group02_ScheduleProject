package com.honaglam.scheduleproject.Reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import java.util.List;

import kotlin.NotImplementedError;

public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderViewHolder> {

  public interface ReminderListGetter{
    List<ReminderData> get() throws NotImplementedError;
  }

  Context context;
  ReminderListGetter reminderListGetter = null;

  public ReminderRecyclerAdapter(Context context, ReminderListGetter getter){
    this.context = context;
    this.reminderListGetter = getter;
  }

  @NonNull
  @Override
  public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ReminderViewHolder reminderViewHolder = new ReminderViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_recycler_item, parent, false));
    return reminderViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
    ReminderData data = reminderListGetter.get().get(position);
    holder.txtId.setText(data.Name);
  }

  @Override
  public int getItemCount() {
    return reminderListGetter.get().size();
  }
}
