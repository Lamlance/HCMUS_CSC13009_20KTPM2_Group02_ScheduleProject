package com.honaglam.scheduleproject.TimerViews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.TimerService;

public class TimerFloatingButton extends FloatingActionButton {
  private static final int[] STATE_WORK = {R.attr.state_work};
  private static final int[] STATE_SHORT = {R.attr.state_short};
  private static final int[] STATE_LONG = {R.attr.state_long};

  private int currState = TimerService.WORK_STATE;

  public TimerFloatingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }
  public void setPomodoroState(int state){
    currState = state;
    Log.i("SET_POMODORO_STATE","Draw state: " + currState);

    refreshDrawableState();
  }

  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace+3);
    Log.i("DRAW_POMODORO_STATE","Draw state: " + currState);
    if(currState == TimerService.SHORT_BREAK_STATE){
      mergeDrawableStates(drawableState,STATE_SHORT);
    } else if (currState == TimerService.LONG_BREAK_STATE) {
      mergeDrawableStates(drawableState,STATE_LONG);
    }else{
      mergeDrawableStates(drawableState,STATE_WORK);
    }
    return drawableState;
  }
}
