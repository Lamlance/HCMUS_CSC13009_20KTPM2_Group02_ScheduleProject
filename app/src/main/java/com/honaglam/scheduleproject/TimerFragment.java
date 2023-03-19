package com.honaglam.scheduleproject;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//import android.os.Handler;
import android.os.IBinder;
//import android.os.Looper;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TimerFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  // TODO: Rename and change types of parameters

  private TextView txtTimer;
  private Button btnTimer;
  private Button btnGiveUp;
  private Button btnSkip;
  private Button timerSetting;

  // TODO: Rename and change types and number of parameters
  public static TimerFragment newInstance() {
    TimerFragment fragment = new TimerFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public TimerFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_timer, container, false);
  }
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    txtTimer = getView().findViewById(R.id.txtTimer);
    txtTimer.setTextSize(50);

    btnTimer = getView().findViewById(R.id.btnTimerStart);
    btnTimer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity)getActivity()).startTimer();
      }
    });

    btnGiveUp = getView().findViewById(R.id.btnTimerGiveUp);
    btnGiveUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity)getActivity()).resetTimer();
      }
    });

    ((MainActivity)getActivity()).setTimerOnTickCallBack(new TimerService.TimerTickCallBack() {
      @Override
      public void call(long remainMillis) throws Exception {
       UpdateTimeUI(remainMillis);
      }
    });

    timerSetting = getView().findViewById(R.id.btnTimerSetting);
    timerSetting.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).switchFragment_TimerSetting();
      }
    });

    btnSkip = getView().findViewById((R.id.btnSkip));
    btnSkip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          ((MainActivity) getActivity()).skip();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    UpdateTimeUI(((MainActivity)getActivity()).getCurrentRemainMillis());

  }

  public void UpdateTimeUI(long millisRemain) {
    int seconds = ((int) millisRemain / 1000) % 60;
    int minutes = (int) millisRemain / (60 * 1000);
    txtTimer.setText(String.format("%d:%02d", minutes, seconds));
  }
}

