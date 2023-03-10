package com.honaglam.scheduleproject.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

import java.util.Calendar;
import java.util.Locale;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
  
  // Return first day of week of a identified month and year
  public static int getFirstDayOfWeekOfMonth(int year, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month - 1);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
  }

  // Return first day of week of current month
  public static int getFirstDayOfWeekOfMonth() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
  }

  /*
   * SUN = 0
   * MON = 1
   * TUE ....
   */

  private int weekDateOfFirstDayOfMoth;

  public Calendar calendar = Calendar.getInstance();

  Context context;

  private static final String[] WEEKDAY_NAMES = new String[] {
      "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
  };

  public CalendarRecyclerViewAdapter(Context context) {
    this.context = context;
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth();
  }

  private int getDaysInMonths() {
    int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return days;
  }

  @NonNull
  @Override
  public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new CalendarViewHolder(LayoutInflater.from(context).inflate(R.layout.calendar_date_item, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
    if (position < 7) {
      holder.txtDate.setText(WEEKDAY_NAMES[position]);
      return;
    }
    int date = position - 7 - weekDateOfFirstDayOfMoth + 1;
    String dateStr = (date <= 0) ? "!" : String.format(Locale.getDefault(), "%d", date);
    holder.txtDate.setText(dateStr);
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        Toast.makeText(view.getContext(), String.format("Date: %d/%d/%d", date, month, year), Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public int getItemCount() {
    return (getDaysInMonths() + weekDateOfFirstDayOfMoth + 7);
  }
}
