package com.honaglam.scheduleproject.Repository;

import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.List;

public class ScoreRepository {
  public interface OnRankingResultCallBack{
    void onResult(List<ReminderTaskFireBase.ScoreBoard> scoreBoardList);
  }
  OnRankingResultCallBack onRankingResultCallBack;
  String uid = "";
  public ScoreRepository(String id){
    uid = id;
  }

  public void SetOnRankingResultCallBack(OnRankingResultCallBack callBack){
    onRankingResultCallBack = callBack;
  }

  public void getTop10Ranking(){
    if(onRankingResultCallBack == null){
      return;
    }

    ReminderTaskFireBase.GetInstance(uid,"score").getTop10Score(scores -> {
      onRankingResultCallBack.onResult(scores);
    });
  }
}
