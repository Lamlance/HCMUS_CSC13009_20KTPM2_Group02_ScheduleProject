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

import java.util.Calendar;
import java.util.Locale;

import kotlin.NotImplementedError;

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

    public interface SelectDateCallBackInterface {
        void clickDate(int date, int month, int year) throws NotImplementedError;
    }

    /*
     * SUN = 0
     * MON = 1
     * TUE ....
     */

    private int clickedPos = -1;
    private int weekDateOfFirstDayOfMoth;
    public SelectDateCallBackInterface selectDateCallBack = null;
    public Calendar calendar = Calendar.getInstance();

    Context context;


    private static final String[] WEEKDAY_NAMES = new String[]{
            "SU", "MO", "TU", "WE", "TH", "FR", "SA"
    };

    public CalendarRecyclerViewAdapter(Context context) {
        this.context = context;
        clickedPos = dateToPos(calendar.get(Calendar.DATE));
        weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth();
        clickedPos = dateToPos(calendar.get(Calendar.DATE));
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
                    selectDateCallBack.clickDate(date, calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.txtDate.setTextColor((clickedPos == position) ? Color.WHITE : Color.BLACK);
        holder.txtDate.setBackgroundColor((clickedPos == position) ? Color.BLUE : Color.WHITE);

        if (position < 7) {
            holder.txtDate.setText(WEEKDAY_NAMES[position]);
            return;
        }
        int date = posToDate(position);
        String dateStr = (date <= 0) ? "!" : String.format(Locale.getDefault(), "%d", date);
        holder.txtDate.setText(dateStr);

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
    }

    public void decreaseMonth() {
        int oldSize = getItemCount();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DATE, 1);
        weekDateOfFirstDayOfMoth = getFirstDayOfWeekOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        int newSize = getItemCount();
        clickedPos = dateToPos(1);
        this.notifyItemRangeChanged(0, Math.max(oldSize, newSize));
    }

    public String getSelectDateString() {
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format(Locale.getDefault(), "%d/%d/%d", posToDate(clickedPos), month, year);
    }
}
