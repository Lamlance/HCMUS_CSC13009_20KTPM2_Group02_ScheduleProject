package com.honaglam.scheduleproject.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.CalendarFragment;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import kotlin.NotImplementedError;

public class CalendarRecyclerViewAdapterFB extends RecyclerView.Adapter<CalendarViewHolder>{
  public interface SelectDateCallBack{
    void onSelectDate(int date,int month,int year,int weekDate);
  }


  public static final String[] WEEKDAY_NAMES = new String[]{
          "SU", "MO", "TU", "WE", "TH", "FR", "SA"
  };
  public static final String[] WEEKDAY_NAMES_MEDIUM = new String[]{
          "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
  };
  public static final String[] WEEKDAY_NAMES_FULL = new String[]{
          "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
  };

  public static int getFirstDayOfWeekOfMonth() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
  }
  public static int getFirstDayOfWeekOfMonth(int year, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
  }


  private int clickedPos = -1;
  private int weekDateOfFirstDayOfMoth;
  public Calendar calendar = Calendar.getInstance();
  SelectDateCallBack onSelectDateCallBack;


  Integer txtPrimaryColor = null;
  Integer highlightColor = null;
  Integer bgPrimaryColor = null;
  Integer tertiaryColor = null;


  CalendarFragment.ReminderInDateGetter reminderInDateGetter;
  Context context;



  public CalendarRecyclerViewAdapterFB(Context context,CalendarFragment.ReminderInDateGetter getter){
    this.context = context;
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth();
    clickedPos = dateToPos(calendar.get(Calendar.DATE));
    reminderInDateGetter = getter;
  }



  private int dateToPos(int date) {
    int pos = date + 7 + weekDateOfFirstDayOfMoth - 1;
    return pos;
  }
  private int posToDate(int pos) {
    int date = pos - 7 - weekDateOfFirstDayOfMoth + 1;
    return date;
  }



  private void getTxtPrimaryColor(CalendarViewHolder holder){
    TypedValue textTypedValue = new TypedValue();
    Resources.Theme textTheme = holder.itemView.getContext().getTheme();
    textTheme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, textTypedValue, true);
    txtPrimaryColor = textTypedValue.data;
  }
  private void getTertiaryColor(CalendarViewHolder holder){
    TypedValue tertTypedValue = new TypedValue();
    Resources.Theme tertTextTheme = holder.itemView.getContext().getTheme();
    tertTextTheme.resolveAttribute(com.google.android.material.R.attr.colorTertiary, tertTypedValue, true);
    tertiaryColor = tertTypedValue.data;
  }
  private void getHighlightColor(CalendarViewHolder holder){
    TypedValue highlightedTypedValue = new TypedValue();
    Resources.Theme highlightedTextTheme = holder.itemView.getContext().getTheme();
    highlightedTextTheme.resolveAttribute(com.google.android.material.R.attr.colorTertiary, highlightedTypedValue, true);
    highlightColor = highlightedTypedValue.data;
  }
  private void getBgPrimaryColor(CalendarViewHolder holder){
    TypedValue bgTypedValue = new TypedValue();
    Resources.Theme bgTheme = holder.itemView.getContext().getTheme();
    bgTheme.resolveAttribute(com.google.android.material.R.attr.backgroundColor, bgTypedValue, true);
    bgPrimaryColor = bgTypedValue.data;
  }



  public void increaseMonth() {
    int oldSize = getItemCount();
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.DATE, 1);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    int newSize = getItemCount();
    clickedPos = dateToPos(1);

    //this.notifyItemRangeChanged(0, Math.max(oldSize, newSize));
  }
  public void decreaseMonth() {
    int oldSize = getItemCount();
    calendar.add(Calendar.MONTH, -1);
    calendar.set(Calendar.DATE, 1);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    int newSize = getItemCount();
    clickedPos = dateToPos(1);

    //this.notifyItemRangeChanged(0, Math.max(oldSize, newSize));


  }



  @NonNull
  @Override
  public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    CalendarViewHolder viewHolder = new CalendarViewHolder(
            LayoutInflater.from(context).inflate(R.layout.calendar_date_item, parent, false),
            new OnSelectDateCallBack()
    );
    getTxtPrimaryColor(viewHolder);
    getTertiaryColor(viewHolder);
    getHighlightColor(viewHolder);
    getBgPrimaryColor(viewHolder);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
    holder.txtDate.setBackgroundColor(
            (clickedPos == position) ? Color.rgb(159, 62, 65) : bgPrimaryColor
    );
    if (position < 7) {
      holder.txtDate.setText(WEEKDAY_NAMES[position]);
      return;
    }
    int weekDate = position % 7;
    int date = posToDate(position);

    String dateStr = (date <= 0) ? "!" : String.format(Locale.getDefault(), "%d", date);

    holder.txtDate.setText(dateStr);


    if (reminderInDateGetter.getReminder(date).size() > 0) {
      holder.txtDate.setBackgroundColor(
              (clickedPos == position) ? Color.rgb(159, 62, 65) : highlightColor
      );
      holder.txtDate.setTextColor(Color.WHITE);
    }

  }

  @Override
  public int getItemCount() {
    return (calendar.getActualMaximum(Calendar.DATE) + weekDateOfFirstDayOfMoth + 7);
  }



  class OnSelectDateCallBack implements CalendarViewHolder.OnClickPositionCallBack{
    @Override
    public void clickAtPosition(int position) {
      int oldPos = clickedPos;
      int date = posToDate(position);

      if (date <= 0) {
        return;
      }

      clickedPos = position;
      calendar.set(Calendar.DATE, date);
      notifyItemChanged(oldPos);
      notifyItemChanged(clickedPos);

      if(onSelectDateCallBack != null){
        onSelectDateCallBack.onSelectDate(
                date,
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.DAY_OF_WEEK)
        );
      }
    }
  }

}
