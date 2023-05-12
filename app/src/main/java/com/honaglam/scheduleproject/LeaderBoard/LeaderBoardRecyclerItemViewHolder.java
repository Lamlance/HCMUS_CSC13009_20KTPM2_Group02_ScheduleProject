package com.honaglam.scheduleproject.LeaderBoard;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.honaglam.scheduleproject.R;

public class LeaderBoardRecyclerItemViewHolder extends RecyclerView.ViewHolder{
  protected TextView txtRank;
  protected TextView txtUserName;
  protected TextView txtScore;
  public LeaderBoardRecyclerItemViewHolder(@NonNull View itemView) {
    super(itemView);
    txtRank = itemView.findViewById(R.id.txtLeaderboardNumber);
    txtUserName = itemView.findViewById(R.id.txtLeaderboardUsername);
    txtScore = itemView.findViewById(R.id.txtLeaderboardTotalTime);
  }
}
