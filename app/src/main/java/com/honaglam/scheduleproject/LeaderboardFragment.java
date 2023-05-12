package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.honaglam.scheduleproject.LeaderBoard.LeaderBoardRecyclerAdapter;
import com.honaglam.scheduleproject.Repository.ScoreRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class LeaderboardFragment extends Fragment {
  static ScoreRepository scoreRepository;

  Context context;
  RecyclerView recyclerViewLeaderList;
  LeaderBoardRecyclerAdapter leaderBoardRecyclerAdapter;

  List<ReminderTaskFireBase.ScoreBoard> scoreBoardList = new LinkedList<>();

  public class ScoreBoardListGetter{
    @NonNull public List<ReminderTaskFireBase.ScoreBoard> getList(){
      return scoreBoardList;
    }
  }

  public LeaderboardFragment() {
    // Required empty public constructor
  }

  public static LeaderboardFragment newInstance(String uid) {
    LeaderboardFragment fragment = new LeaderboardFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    if(scoreRepository == null){
      scoreRepository = new ScoreRepository(uid);
    }

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    scoreRepository.SetOnRankingResultCallBack(new GetRankingUserCallBack());
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    this.context = context;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    leaderBoardRecyclerAdapter = new LeaderBoardRecyclerAdapter(inflater,new ScoreBoardListGetter());
    View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recyclerViewLeaderList = view.findViewById(R.id.recyclerLeaderboard);
    recyclerViewLeaderList.setLayoutManager(new LinearLayoutManager(context));
    recyclerViewLeaderList.setAdapter(leaderBoardRecyclerAdapter);

    scoreRepository.getTop10Ranking();
  }


  class GetRankingUserCallBack implements ScoreRepository.OnRankingResultCallBack{
    @Override
    public void onResult(List<ReminderTaskFireBase.ScoreBoard> scoreList) {
      Collections.reverse(scoreList);
      scoreBoardList.clear();
      scoreBoardList.addAll(scoreList);

      leaderBoardRecyclerAdapter.notifyDataSetChanged();
    }
  }
}