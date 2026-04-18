package com.group.groupProject.score;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.groupProject.R;
import com.group.groupProject.score.model.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private final List<LeaderboardEntry> items = new ArrayList<>();

    public void submitItems(List<LeaderboardEntry> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = items.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        private final TextView rankTextView;
        private final TextView playerNameTextView;
        private final TextView levelTextView;
        private final TextView scoreTextView;

        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.tv_rank);
            playerNameTextView = itemView.findViewById(R.id.tv_player_name);
            levelTextView = itemView.findViewById(R.id.tv_level);
            scoreTextView = itemView.findViewById(R.id.tv_score);
        }

        void bind(LeaderboardEntry entry) {
            rankTextView.setText(itemView.getContext().getString(
                    R.string.label_rank,
                    entry.rank > 0 ? String.valueOf(entry.rank) : itemView.getContext().getString(R.string.unknown_value)
            ));
            playerNameTextView.setText(itemView.getContext().getString(
                    R.string.label_player_name,
                    TextUtils.isEmpty(entry.playerName) ? itemView.getContext().getString(R.string.unknown_value) : entry.playerName
            ));
            levelTextView.setText(itemView.getContext().getString(R.string.label_level, entry.level));
            scoreTextView.setText(itemView.getContext().getString(R.string.label_score, entry.score));
        }
    }
}


