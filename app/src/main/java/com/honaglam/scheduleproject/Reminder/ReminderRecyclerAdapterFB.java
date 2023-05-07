package com.honaglam.scheduleproject.Reminder;

import android.content.Context;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.CalendarFragment;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReminderRecyclerAdapterFB extends RecyclerView.Adapter<ReminderViewHolder> {
  static final String[] WEEK_DAY_NAMES_SHORT = new String[]{
          "SU", "MO", "TU", "WE", "TH", "FR", "SA"
  };

  public interface ItemAction {
    void onAction(int position, @Nullable ReminderTaskFireBase.Reminder reminder);
  }


  int selectedPos = -1;
  Context context;
  ItemAction itemClickAction;
  ItemAction deleteClickAction;

  CalendarFragment.ReminderInDateGetter reminderInDateGetter;


  public ReminderRecyclerAdapterFB(Context context, CalendarFragment.ReminderInDateGetter getter) {
    this.context = context;
    reminderInDateGetter = getter;
  }


  public void callItemAction(ItemAction action, ReminderTaskFireBase.Reminder reminder) {
    if (action != null) {
      try {
        action.onAction(selectedPos,reminder);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setItemClickAction(ItemAction action) {
    itemClickAction = action;
  }

  public void setDeleteClickAction(ItemAction action) {
    deleteClickAction = action;
  }


  @NonNull
  @Override
  public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ReminderViewHolder viewHolder = new ReminderViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_recycler_item, parent, false)
    );
    viewHolder.setSelectItemCallback((pos) -> {
      selectedPos = pos;
      callItemAction(itemClickAction,null);
    });
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
    ReminderTaskFireBase.Reminder data = reminderInDateGetter.getReminder().get(position);

    if(data == null){
      return;
    }

    holder.btnDeleteReminder.setOnClickListener((clickedView) -> {
      callItemAction(deleteClickAction,data);
    });
    holder.itemView.setBackgroundColor(position == selectedPos ?
            ResourcesCompat.getColor(context.getResources(), R.color.selected_white, null) :
            Color.TRANSPARENT);

    if (data.weekDates != null) {
      String weekDateNames = "";
      for (Integer wD : data.weekDates) {
        weekDateNames += WEEK_DAY_NAMES_SHORT[wD-1] + "-";
      }
      weekDateNames = weekDateNames.substring(0,weekDateNames.length() - 1);
      holder.txtId.setText(String.format(Locale.getDefault(),"Weekly: %s",weekDateNames));
    } else {
      String dateFormat = DateFormat.getDateTimeInstance().format(new Date(data.reminderTime));
      holder.txtId.setText(dateFormat);
    }

    holder.txtName.setText(data.title);

  }

  @Override
  public int getItemCount() {
    List<ReminderTaskFireBase.Reminder> reminders = reminderInDateGetter.getReminder();
    return reminders.size();
  }
}
