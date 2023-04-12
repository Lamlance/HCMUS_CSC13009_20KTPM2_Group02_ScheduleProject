package com.honaglam.scheduleproject.TimerViews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.TimerService;

public class TimerFloatingButton extends FloatingActionButton {
  private static final int STATE_WORK = R.attr.state_work;
  private static final int STATE_SHORT = R.attr.state_short;
  private static final int STATE_LONG = R.attr.state_long;
  private static final int[] STATE_THEME_ARRAY = new int[]{
          R.attr.pomodoro_theme0,R.attr.pomodoro_theme1,R.attr.pomodoro_theme2,R.attr.pomodoro_theme3,
          R.attr.pomodoro_theme4,R.attr.pomodoro_theme5,R.attr.pomodoro_theme6,R.attr.pomodoro_theme7
  };
  private int currState = TimerService.WORK_STATE;
  int currTheme = 0;

  public TimerFloatingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }
  public void setPomodoroState(int state){
    currState = state;
    refreshDrawableState();
  }
  public void setPomodoroTheme(int theme){
    currTheme = theme;
    refreshDrawableState();
  }

  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace+4+STATE_THEME_ARRAY.length);

    Log.i("DRAW_POMODORO_STATE","Draw state: " + currState);
    Log.i("DRAW_POMODORO_STATE","Draw theme: " + currTheme);

    if(currState == TimerService.SHORT_BREAK_STATE){
      mergeDrawableStates(drawableState,new int[]{STATE_SHORT,STATE_THEME_ARRAY[currTheme]});
    } else if (currState == TimerService.LONG_BREAK_STATE) {
      mergeDrawableStates(drawableState,new int[]{STATE_LONG,STATE_THEME_ARRAY[currTheme]});
    }else{
      mergeDrawableStates(drawableState,new int[]{STATE_WORK,STATE_THEME_ARRAY[currTheme]});
    }


    return drawableState;
  }
}
