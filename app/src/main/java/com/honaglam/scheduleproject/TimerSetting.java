package com.honaglam.scheduleproject;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.media.Ringtone;
import android.media.RingtoneManager;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.honaglam.scheduleproject.TimerViews.TimerThemeIconImage;
import com.honaglam.scheduleproject.UserSetting.UserTimerSettings;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimerSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimerSetting extends DialogFragment {
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private NumberPicker pomodoroTimePicker;
  private NumberPicker shortBreakPicker;
  private NumberPicker longBreakPicker;
  private SwitchMaterial autoStartBreakSwitch;
  private SwitchMaterial autoStartPomodoroSwitch;
  private Slider longBreakIntervalSlider;
  private TextView soundPicker;
  private Uri selectedUri;
  private Button confirmButton;
  private Button cancelButton;

  GridLayout gridViewIcon;
  UserTimerSettings userTimerSettings;

  int selectedTheme;
  private ActivityResultLauncher<Intent> soundPickerLauncher;

  public TimerSetting() {
    // Required empty public constructor
  }

  public static final String TIMER_SETTING_REQUEST_KEY = "timer_setting_request";
  public static final String TIMER_SETTING_RESULT_KEY = "timer_user_setting";

  // TODO: Rename and change types and number of parameters
  public static TimerSetting newInstance(UserTimerSettings userSettings) {
    TimerSetting fragment = new TimerSetting();
    Bundle args = new Bundle();
    args.putSerializable(TIMER_SETTING_RESULT_KEY, userSettings);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      userTimerSettings = (UserTimerSettings) getArguments().getSerializable(TIMER_SETTING_RESULT_KEY);
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_timer_setting, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);


    int minVal = 0;
    int maxVal = 120;
    // Set time for pomodoro work time and watch changed valued
    pomodoroTimePicker = view.findViewById(R.id.workTime);
    pomodoroTimePicker.setMinValue(minVal);
    pomodoroTimePicker.setMaxValue(maxVal);
    pomodoroTimePicker.setValue(25);

    // Set time for short break time and watch changed valued
    shortBreakPicker = view.findViewById(R.id.shortBreak);
    shortBreakPicker.setMinValue(minVal);
    shortBreakPicker.setMaxValue(maxVal);
    shortBreakPicker.setValue(5);

    // Set time for long break time and watch changed valued
    longBreakPicker = view.findViewById(R.id.longBreak);
    longBreakPicker.setMinValue(minVal);
    longBreakPicker.setMaxValue(maxVal);
    longBreakPicker.setValue(10);

    // Set auto start breaks to true
    autoStartBreakSwitch = view.findViewById(R.id.autoStartBreak);
    autoStartBreakSwitch.setChecked(true);

    // Set auto start pomodoro to true
    autoStartPomodoroSwitch = view.findViewById(R.id.autoStartPomodoro);
    autoStartPomodoroSwitch.setChecked(true);

    // Set long break interval default to 4
    longBreakIntervalSlider = view.findViewById(R.id.longBreakInterval);
    longBreakIntervalSlider.setValue(4);

    // Set sound for timer notifications
    soundPicker = view.findViewById(R.id.soundPicker);
    Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone defaultRingtone = RingtoneManager.getRingtone(getContext(), defaultUri);
    String defaultName = defaultRingtone.getTitle(getContext());
    soundPicker.setText(defaultName);
    selectedUri = defaultUri;
    soundPicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri);
        soundPickerLauncher.launch(intent);
      }
    });
    // TODO: CREATE A HANDLER TO GRANT PERMISSION TO OPEN CUSTOM RINGTONE
    soundPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
              public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                  Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                  if (uri == null) {
                    // If the user didn't pick a ringtone, use the default ringtone
                    uri = defaultUri;
                  }
                  selectedUri = uri;
                  Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
                  String name = ringtone.getTitle(getContext());
                  soundPicker.setText(name);
                }
              }
            });
    // Pass the setting to the timer Fragment upon confirm pressed
    confirmButton = view.findViewById(R.id.confirmButton);
    confirmButton.setOnClickListener(new OnConfirmSetting());
    cancelButton = view.findViewById(R.id.cancelButton);
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TimerSetting.this.dismiss();
      }
    });

    gridViewIcon = view.findViewById(R.id.layoutGridThemeIcon);
    int iconId = 0;
    for (int i = 0; i < gridViewIcon.getChildCount(); i++){
      View child = gridViewIcon.getChildAt(i);
      if(child instanceof AppCompatImageView){
        child.setOnClickListener(new OnThemeSelected(iconId));
        iconId++;
      }
    }


    setUserPref(userTimerSettings);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


    return super.onCreateDialog(savedInstanceState);
  }

  class OnConfirmSetting implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      long workTime = pomodoroTimePicker.getValue() * (long) 1000;
      long shortBreak = shortBreakPicker.getValue() * (long) 1000;
      long longBreak = longBreakPicker.getValue() * (long) 1000;
      boolean autoStartBreak = autoStartBreakSwitch.isChecked();
      boolean autoStartPomodoro = autoStartPomodoroSwitch.isChecked();
      long longBreakInterVal = (long) longBreakIntervalSlider.getValue();
      Log.d("autoStartBreak: ", String.valueOf(autoStartBreak));
      Log.d("autoStartPomodoro: ", String.valueOf(autoStartPomodoro));
      Log.d("longBreakInterVal: ", String.valueOf(longBreakInterVal));
      Uri final_uri = selectedUri;
      Bundle result = new Bundle();
      result.putSerializable(TIMER_SETTING_RESULT_KEY, new UserTimerSettings(
              workTime, shortBreak, longBreak,
              final_uri, autoStartBreak, autoStartPomodoro, longBreakInterVal,selectedTheme));
      getParentFragmentManager().setFragmentResult(TIMER_SETTING_REQUEST_KEY, result);
      TimerSetting.this.dismiss();
    }
  }

  class OnThemeSelected implements View.OnClickListener{
    int theme;
    OnThemeSelected(int themeId){
      theme = themeId;
    }
    @Override
    public void onClick(View view) {
      TimerSetting.this.selectedTheme = theme;
    }
  }

  private void setUserPref(UserTimerSettings settings) {
    pomodoroTimePicker.setValue((int) (settings.workMillis / 1000));
    shortBreakPicker.setValue((int) (settings.shortBreakMillis / 1000));
    longBreakPicker.setValue((int) (settings.longBreakMillis / 1000));

    longBreakIntervalSlider.setValue(settings.longBreakInterValSetting);

    autoStartBreakSwitch.setChecked(settings.autoStartBreakSetting);
    autoStartPomodoroSwitch.setChecked(settings.autoStartPomodoroSetting);

    selectedUri = settings.alarmUri;
    if (selectedUri != null) {
      Ringtone ringtone = RingtoneManager.getRingtone(getContext(), selectedUri);
      String name = ringtone.getTitle(getContext());
    }

    selectedTheme = settings.prefTheme;
  }

}