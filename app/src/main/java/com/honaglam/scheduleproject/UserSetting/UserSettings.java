package com.honaglam.scheduleproject.UserSetting;

import android.app.Application;

public class UserSettings extends Application {
    public static final String PREFERENCES = "preferences";
    public static final String CUSTOM_THEME = "customTheme";
    public static final String LIGHT_THEME = "lightTheme";
    public static final String DARK_THEME = "darkTheme";
    public static final String WORKING_TIMER_SETTING = "WorkingTimerSetting";
    public static final String SHORT_BREAK_TIMER_SETTING = "shortBreakTimerSetting";
    public static final String LONG_BREAK_TIMER_SETTING = "longBreakTimerSetting";
    public static final String RINGTONE_SETTING = "ringtoneSetting";

    private String customTheme;

    public String getCustomTheme() {
        return customTheme;
    }

    public void setCustomTheme(String customTheme) {
        this.customTheme = customTheme;
    }

}
