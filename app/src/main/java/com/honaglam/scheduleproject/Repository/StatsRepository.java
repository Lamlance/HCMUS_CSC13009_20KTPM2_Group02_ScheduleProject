package com.honaglam.scheduleproject.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class StatsRepository {
  public interface GetStatsDataCallBack{
    void onGet(@NonNull List<ReminderTaskFireBase.TimerStats> stats);
  }

  String uuid;
  public StatsRepository(String id){
    uuid = id;
  }
  @Nullable GetStatsDataCallBack getStats30DayCompleted;

  public void addTimeTodayTask(int state,long time){
    ReminderTaskFireBase.GetInstance(uuid).addTimeTodayTask(state,time);
  }

  public void SetGetStatsCompleted(GetStatsDataCallBack completedCallBack){
    getStats30DayCompleted = completedCallBack;
  }

  public void get30Stats30DaysBefore(){
    ReminderTaskFireBase.GetInstance(uuid).getTimeStats30DaysBefore(new StatsSearchResult(getStats30DayCompleted));
  }

  static class StatsSearchResult implements ReminderTaskFireBase.GetStatsCompletedCallBack{
    @Nullable GetStatsDataCallBack getStats30DayCompleted;
    StatsSearchResult(@Nullable GetStatsDataCallBack callBack){
      getStats30DayCompleted = callBack;
    }
    @Override
    public void onCompleted(@NonNull List<ReminderTaskFireBase.TimerStats> stats, long startTime, long endTime) {
      if(getStats30DayCompleted != null){
        getStats30DayCompleted.onGet(stats);
      }
    }
  }

}
