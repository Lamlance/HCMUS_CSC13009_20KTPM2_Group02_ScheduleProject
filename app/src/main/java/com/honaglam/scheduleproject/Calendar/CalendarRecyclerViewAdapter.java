package com.honaglam.scheduleproject.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.Reminder.ReminderData;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import kotlin.NotImplementedError;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    // Return first day of week of a identified month and year
    public static int getFirstDayOfWeekOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
    }

    // Return first day of week of current month
    public static int getFirstDayOfWeekOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
    }

  public interface SelectDateCallBackInterface {
    void clickDate(int date, int month, int year, int weekDay,List<ReminderData> reminders) throws NotImplementedError;
  }

  public interface GetReminderInMonth {
    List<ReminderData> getReminderInMonth(int year, int month) throws NotImplementedError;
  }

    /*
     * SUN = 0
     * MON = 1
     * TUE ....
     */

  private int clickedPos = -1;
  private int weekDateOfFirstDayOfMoth;
  private SelectDateCallBackInterface selectDateCallBack = null;
  private GetReminderInMonth getReminderInMonth = null;
  public Calendar calendar = Calendar.getInstance();

    Context context;


    public static final String[] WEEKDAY_NAMES = new String[]{
            "SU", "MO", "TU", "WE", "TH", "FR", "SA"
    };
    public static final String[] WEEKDAY_NAMES_MEDIUM = new String[]{
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    };
    public static final String[] WEEKDAY_NAMES_FULL = new String[]{
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
    };

  @NonNull Map<Integer, List<ReminderData>> reminderByDates = new HashMap<>();

  public CalendarRecyclerViewAdapter(Context context, GetReminderInMonth getReminderInMonth) {
    this.context = context;
    clickedPos = dateToPos(calendar.get(Calendar.DATE));
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth();
    clickedPos = dateToPos(calendar.get(Calendar.DATE));

    this.getReminderInMonth = getReminderInMonth;
    getSetAllReminderInMonth();
  }

  private void getSetAllReminderInMonth() {
    try {
      List<ReminderData> reminders = getReminderInMonth.getReminderInMonth(
              calendar.get(Calendar.YEAR),
              calendar.get(Calendar.MONTH));
      reminderByDates = reminders.stream().collect(
              Collectors.groupingBy(ReminderData::getMyDate)
      );
    } catch (Exception ignore) {
    }
  }
  private void callSelectDateCallBack(){
    if (this.selectDateCallBack != null) {
      try {
        this.selectDateCallBack.clickDate(
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.DAY_OF_WEEK),
                reminderByDates.get(calendar.get(Calendar.DATE)));
      } catch (Exception ignore) {
      }
    }
  }
  public void setSelectDateCallBack(SelectDateCallBackInterface callBack) {
    this.selectDateCallBack = callBack;
    if (this.selectDateCallBack != null) {
      try {
        this.selectDateCallBack.clickDate(
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.DAY_OF_WEEK),
                reminderByDates.get(calendar.get(Calendar.DATE)));
      } catch (Exception ignore) {
      }
    }
  }

    private int getDaysInMonths() {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CalendarViewHolder viewHolder = new CalendarViewHolder(
                LayoutInflater.from(context).inflate(R.layout.calendar_date_item, parent, false),
                new OnClickAtPosition()
        );

        return viewHolder;
    }

    class OnClickAtPosition implements CalendarViewHolder.OnClickPositionCallBack {
        @Override
        public void clickAtPosition(int position) throws NotImplementedError {
            int oldPos = clickedPos;
            int date = posToDate(position);

      if (date <= 0) {
        return;
      }
      clickedPos = position;
      calendar.set(Calendar.DATE, date);
      notifyItemChanged(oldPos);
      notifyItemChanged(clickedPos);
      if (selectDateCallBack != null) {
        try {
          selectDateCallBack.clickDate(
                  date,
                  calendar.get(Calendar.MONTH),
                  calendar.get(Calendar.YEAR),
                  calendar.get(Calendar.DAY_OF_WEEK),
                  reminderByDates.get(date));
        } catch (Exception e) {
        }
      }
    }
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
    holder.txtDate.setTextColor((clickedPos == position) ? Color.WHITE : Color.BLACK);
    holder.txtDate.setBackgroundColor((clickedPos == position) ? Color.rgb(0, 109, 59) : Color.WHITE);

    if (position < 7) {
      holder.txtDate.setText(WEEKDAY_NAMES[position]);
      return;
    }

    int date = posToDate(position);
    String dateStr = (date <= 0) ? "!" : String.format(Locale.getDefault(), "%d", date);
    holder.txtDate.setText(dateStr);
    if(reminderByDates.get(date) != null){
      holder.txtDate.setBackgroundColor((clickedPos == position) ? Color.rgb(0, 109, 59) : Color.RED);
    }

  }

    private int dateToPos(int date) {
        int pos = date + 7 + weekDateOfFirstDayOfMoth - 1;
        return pos;
    }

    private int posToDate(int pos) {
        int date = pos - 7 - weekDateOfFirstDayOfMoth + 1;
        return date;
    }

    @Override
    public int getItemCount() {
        return (getDaysInMonths() + weekDateOfFirstDayOfMoth + 7);
    }

  public void increaseMonth() {
    int oldSize = getItemCount();
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.DATE, 1);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    int newSize = getItemCount();
    clickedPos = dateToPos(1);
    this.notifyItemRangeChanged(0, Math.max(oldSize, newSize));

    getSetAllReminderInMonth();
    callSelectDateCallBack();
  }

  public void decreaseMonth() {
    int oldSize = getItemCount();
    calendar.add(Calendar.MONTH, -1);
    calendar.set(Calendar.DATE, 1);
    weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    int newSize = getItemCount();
    clickedPos = dateToPos(1);
    this.notifyItemRangeChanged(0, Math.max(oldSize, newSize));

    getSetAllReminderInMonth();
    callSelectDateCallBack();
  }

    public String getSelectDateString() {
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format(Locale.getDefault(), "%d/%d/%d", posToDate(clickedPos), month, year);
    }
}
