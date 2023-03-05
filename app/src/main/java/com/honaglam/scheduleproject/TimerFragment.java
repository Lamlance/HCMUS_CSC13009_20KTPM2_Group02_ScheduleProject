package com.honaglam.scheduleproject;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
  protected TimerService timerService;
  private long millisRemain = 1;
  public synchronized void setMillisRemain(long millisRemain) {
    this.millisRemain = millisRemain;
  }
  public synchronized long getMillisRemain(){
    return millisRemain;
  }

  // TODO: Rename and change types and number of parameters
  public static TimerFragment newInstance(String param1, String param2) {
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_timer, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    txtTimer = getView().findViewById(R.id.txtTimer);
    ((MainActivity)getActivity()).bindTimerService(new TimerConnectionService());


    btnTimer = getView().findViewById(R.id.btnTimerStart);
    btnTimer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(timerService != null){
          timerService.startTimer();
          ((MainActivity)getActivity()).runOnUiThread(new UpdateTimeUI());
        }
      }
    });


  }

  class UpdateTimeUI implements Runnable{
    @Override
    public void run() {
      txtTimer.setText(String.format("%d",getMillisRemain()));
      if(millisRemain > 0){
        txtTimer.postDelayed(new UpdateTimeUI(),1000);
      }else {
        millisRemain = 1;
      }
    }
  }

  class TimerConnectionService implements ServiceConnection{
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      timerService = ((TimerService.LocalBinder)iBinder).getService();
      timerService.tickCallBack = new TimerService.TimerTickCallBack() {
        @Override
        public void call(long remainMillis) throws Exception {
          setMillisRemain(remainMillis);
        }
      };
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
  }
}