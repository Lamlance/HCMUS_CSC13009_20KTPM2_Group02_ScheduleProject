package com.honaglam.scheduleproject.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    ReminderTaskFireBase.GetInstance(uuid,"Stats").addTimeTodayTask(state,time);
  }

  public void SetGetStatsCompleted(GetStatsDataCallBack completedCallBack){
    getStats30DayCompleted = completedCallBack;
  }

  public void get30Stats30DaysBefore(){
    ReminderTaskFireBase.GetInstance(uuid,"Stats").getTimeStats30DaysBefore(new StatsSearchResult(getStats30DayCompleted));
  }

  static class StatsSearchResult implements ReminderTaskFireBase.GetStatsCompletedCallBack{
    @Nullable GetStatsDataCallBack getStats30DayCompleted;
    StatsSearchResult(@Nullable GetStatsDataCallBack callBack){
      getStats30DayCompleted = callBack;
    }
    @Override
    public void onCompleted(@NonNull List<ReminderTaskFireBase.TimerStats> stats, long startTime, long endTime) {
      if(getStats30DayCompleted == null){
        return;
      }
      List<ReminderTaskFireBase.TimerStats> statsList = new LinkedList<>(stats);

      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(startTime);

      for(;calendar.getTimeInMillis() <= endTime;calendar.add(Calendar.DATE,1)){
        long timeInMillis = calendar.getTimeInMillis();
        if(stats.stream().anyMatch(s -> s.createDate == timeInMillis)){
          continue;
        }
        statsList.add(ReminderTaskFireBase.TimerStats.GetEmptyStatsAt(timeInMillis));
      }
      List<ReminderTaskFireBase.TimerStats> sortedList = statsList.stream()
              .sorted(Comparator.comparingLong(s->s.createDate*-1))
              .collect(Collectors.toList());


      getStats30DayCompleted.onGet(sortedList);

    }
  }

}
