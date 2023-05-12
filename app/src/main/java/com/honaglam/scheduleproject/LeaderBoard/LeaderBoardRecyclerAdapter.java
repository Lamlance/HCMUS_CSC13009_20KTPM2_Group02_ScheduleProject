package com.honaglam.scheduleproject.LeaderBoard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.LeaderboardFragment;
import com.honaglam.scheduleproject.R;
import com.honaglam.scheduleproject.ReminderTaskFireBase;

import java.util.Locale;


public class LeaderBoardRecyclerAdapter extends RecyclerView.Adapter<LeaderBoardRecyclerItemViewHolder> {
  LayoutInflater inflater;
  LeaderboardFragment.ScoreBoardListGetter getter;
  public LeaderBoardRecyclerAdapter(LayoutInflater layoutInflater, LeaderboardFragment.ScoreBoardListGetter scoreGetter){
    inflater = layoutInflater;
    getter = scoreGetter;
  }

  @NonNull
  @Override
  public LeaderBoardRecyclerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LeaderBoardRecyclerItemViewHolder viewHolder = new LeaderBoardRecyclerItemViewHolder(
            inflater.inflate(R.layout.leaderboard_item,parent,false)
    );
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull LeaderBoardRecyclerItemViewHolder holder, int position) {
    ReminderTaskFireBase.ScoreBoard scoreBoard = getter.getList().get(position);
    if(scoreBoard == null){
      return;
    }
    holder.txtRank.setText(String.format(Locale.getDefault(),"#%d",position+1));
    holder.txtScore.setText(String.valueOf(scoreBoard.score));
    holder.txtUserName.setText(scoreBoard.userId);
  }

  @Override
  public int getItemCount() {
    return getter.getList().size();
  }
}
