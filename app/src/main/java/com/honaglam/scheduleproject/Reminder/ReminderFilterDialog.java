package com.honaglam.scheduleproject.Reminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.honaglam.scheduleproject.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kotlin.NotImplementedError;

public class ReminderFilterDialog extends Dialog {
  public interface OnSelectFromToDate {
    public void onSelect(long fromDate, long toDate) throws NotImplementedError;
  }

  TextView txtFromDate;
  TextView txtToDate;
  long fromDate = -1;
  long toDate = -1;
  DatePicker datePicker;
  OnSelectFromToDate selectFromToDate = null;

  public ReminderFilterDialog(@NonNull Context context) {
    super(context);
  }

  public ReminderFilterDialog(@NonNull Context context,OnSelectFromToDate callBack) {
    super(context);
    selectFromToDate = callBack;
  }

  public void setOnFromToDateCallBack(@NonNull OnSelectFromToDate callBack){
    selectFromToDate = callBack;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_reminder_filter);

    txtFromDate = findViewById(R.id.txtFilterFromDate);
    txtFromDate.setPaintFlags(txtFromDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    txtFromDate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new ReminderDatePicker(ReminderFilterDialog.this.getContext(), true).show();
      }
    });

    txtToDate = findViewById(R.id.txtFilterToDate);
    txtToDate.setPaintFlags(txtToDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    txtToDate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new ReminderDatePicker(ReminderFilterDialog.this.getContext(), false).show();
      }
    });

    findViewById(R.id.btnFilterAccept).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(selectFromToDate != null){
          try {
            selectFromToDate.onSelect(fromDate,toDate);
          }catch (Exception ignore){}
        }
        ReminderFilterDialog.this.dismiss();
      }
    });

    findViewById(R.id.btnFilterClear).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        fromDate = -1;
        toDate = -1;
        txtFromDate.setText(R.string.date_format);
        txtToDate.setText(R.string.date_format);
        if(selectFromToDate != null){
          try {
            selectFromToDate.onSelect(fromDate,toDate);
          }catch (Exception ignore){}
        }
        ReminderFilterDialog.this.dismiss();
      }
    });
  }

  class ReminderDatePicker extends DatePickerDialog {
    public ReminderDatePicker(@NonNull Context context, boolean isFrom) {
      super(context);
      this.setOnDateSetListener(new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int date) {
          Calendar calendar = Calendar.getInstance();
          calendar.set(year, month, date, isFrom ? 0 : 23,isFrom ? 0 : 59,isFrom ? 0 : 59);
          long time = calendar.getTimeInMillis();
          SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
          if (isFrom) {
            fromDate = time;
            txtFromDate.setText(dateFormat.format(new Date(time)));
          } else {
            toDate = calendar.getTimeInMillis();
            txtToDate.setText(dateFormat.format(new Date(time)));
          }
        }
      });
    }
  }

}
