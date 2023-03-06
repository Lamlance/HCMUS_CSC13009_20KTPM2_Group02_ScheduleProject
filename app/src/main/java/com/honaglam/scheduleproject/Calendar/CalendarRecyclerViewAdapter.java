package com.honaglam.scheduleproject.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import java.util.Calendar;
import java.util.Locale;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
  public static int getFirstDayOfWeekOfMonth(int year, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
  }
  public static int getFirstDayOfWeekOfMonth() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
  }

  /*
   * SUN = 0
   * MON = 1
   * TUE ....
   * */

  private int weekDateOfFirstDayOfMoth;

  public Calendar calendar = Calendar.getInstance();
  Context context;
  int selectedDate;
  private static final String[] WEEKDAY_NAMES = new String[]{
          "SUN","MON","TUE","WED","THU","FRI","SAT"
  };

  public CalendarRecyclerViewAdapter(Context context){
    this.context = context;
    selectedDate = calendar.get(Calendar.DATE);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth();

  }
  private int getDaysInMonths(){
    int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return days;
  }
  @NonNull
  @Override
  public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new CalendarViewHolder(LayoutInflater.from(context).inflate(R.layout.calendar_date_item,parent,false));
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
    if(position < 7){
      holder.txtDate.setText(WEEKDAY_NAMES[position]);
      return;
    }
    int date = position -7 - weekDateOfFirstDayOfMoth + 1;
    String dateStr = (date <= 0) ? "!" : String.format(Locale.getDefault(),"%d",date);
    holder.txtDate.setText(dateStr);

    if(selectedDate == date){
      holder.txtDate.setTextColor(Color.WHITE);
      holder.txtDate.setBackgroundColor(Color.BLUE);
    }
  }

  @Override
  public int getItemCount() {
    return (getDaysInMonths() + weekDateOfFirstDayOfMoth + 7);
  }

  public void increaseMonth(){
    calendar.add(Calendar.MONTH,1);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH)
    );
    selectedDate = 1;
    this.notifyDataSetChanged();
  }
  public void decreaseMonth(){
    calendar.add(Calendar.MONTH,-1);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH)
    );
    selectedDate = 1;
    this.notifyDataSetChanged();
  }
  public String getSelectDateString(){
    int month = calendar.get(Calendar.MONTH) + 1;
    int year = calendar.get(Calendar.YEAR);
    return String.format(Locale.getDefault(),"%d/%d/%d",selectedDate,month,year);
  }
}
