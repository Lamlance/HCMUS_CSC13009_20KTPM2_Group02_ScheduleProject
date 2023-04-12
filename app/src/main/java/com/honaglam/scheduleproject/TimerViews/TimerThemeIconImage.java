package com.honaglam.scheduleproject.TimerViews;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.R;

public class TimerThemeIconImage extends androidx.appcompat.widget.AppCompatImageView {
  private static final int[] STATE_WORK = {R.attr.state_work};
  private static final int[] STATE_SHORT = {R.attr.state_short};
  private static final int[] STATE_LONG = {R.attr.state_long};
  private static final int[] STATE_THEME_ARRAY = new int[]{
          R.attr.pomodoro_theme0,R.attr.pomodoro_theme1,R.attr.pomodoro_theme2,R.attr.pomodoro_theme3,
          R.attr.pomodoro_theme4,R.attr.pomodoro_theme5,R.attr.pomodoro_theme6,R.attr.pomodoro_theme7
  };
  int currTheme = 0;
  public TimerThemeIconImage(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context,attrs);
  }
  public void setPomodoroTheme(int theme){
    currTheme = theme;
    refreshDrawableState();
  }

  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace+1);
    if(currTheme >= 0 && currTheme < STATE_THEME_ARRAY.length){
      mergeDrawableStates(drawableState,new int[]{STATE_THEME_ARRAY[currTheme]});
    }
    return drawableState;
  }
}
