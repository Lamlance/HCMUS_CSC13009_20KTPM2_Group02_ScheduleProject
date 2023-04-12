package com.honaglam.scheduleproject.TimerViews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.TimerService;

public class TimerViewGroupLinear extends LinearLayout {
  private static final int[] STATE_WORK = {R.attr.state_work};
  private static final int[] STATE_SHORT = {R.attr.state_short};
  private static final int[] STATE_LONG = {R.attr.state_long};
  private static final int[] STATE_THEME_ARRAY = new int[]{
          R.attr.pomodoro_theme0,R.attr.pomodoro_theme1,R.attr.pomodoro_theme2,R.attr.pomodoro_theme3,
          R.attr.pomodoro_theme4,R.attr.pomodoro_theme5,R.attr.pomodoro_theme6,R.attr.pomodoro_theme7
  };
  private int currState = TimerService.WORK_STATE;
  private int currTheme = 0;
  public TimerViewGroupLinear(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  public void setPomodoroState(int state){
    currState = state;
    //Log.i("SET_POMODORO_STATE","Draw state: " + currState);
    refreshDrawableState();
  }
  public void setPomodoroTheme(int theme){
    currTheme = theme;
    refreshDrawableState();
  }
  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace+3);
    //Log.i("DRAW_POMODORO_STATE","Draw state: " + currState);
    if(currState == TimerService.SHORT_BREAK_STATE){
      mergeDrawableStates(drawableState,STATE_SHORT);
    } else if (currState == TimerService.LONG_BREAK_STATE) {
      mergeDrawableStates(drawableState,STATE_LONG);
    }else{
      mergeDrawableStates(drawableState,STATE_WORK);
    }

    if(currTheme >= 0 && currTheme < STATE_THEME_ARRAY.length){
      mergeDrawableStates(drawableState,new int[]{STATE_THEME_ARRAY[currTheme]});
    }

    return drawableState;
  }

}
