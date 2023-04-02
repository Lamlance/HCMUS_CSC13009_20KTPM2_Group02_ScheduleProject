package com.honaglam.scheduleproject.Reminder;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.honaglam.scheduleproject.R;

import kotlin.NotImplementedError;

public class ReminderAddDialog extends Dialog {
    public interface ReminderDataCallBack{
        void onSubmit(String name,int hour24h,int minute) throws NotImplementedError;
    }

    Animation shakeAnim;

    ReminderDataCallBack dataCallBack = null;
    public ReminderAddDialog(@NonNull Context context,ReminderDataCallBack dataCallBack) {
        super(context);
        this.dataCallBack = dataCallBack;
        this.shakeAnim = AnimationUtils.loadAnimation(context,R.anim.shake);
        this.shakeAnim.setInterpolator(new CycleInterpolator(7));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_reminder_add);

        TimePicker timePicker = findViewById(R.id.timeReminderDialog);
        timePicker.setIs24HourView(true);
        EditText editText = findViewById(R.id.txtEditNameReminder);
        ((Button)findViewById(R.id.btnAddReminderDialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editText.getText().toString();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                if(name.isEmpty()){
                    editText.startAnimation(shakeAnim);
                    editText.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getContext(),R.color.red_700)
                    ));
                    Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                try{
                    dataCallBack.onSubmit(name,hour,minute);
                }catch (Exception ignore){}
                ReminderAddDialog.this.dismiss();
            }
        });

        ((Button)findViewById(R.id.btnCancelReminderDialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
}
