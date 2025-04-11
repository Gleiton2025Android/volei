package com.example.joguim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SelectablePlayerAdapter extends RecyclerView.Adapter<SelectablePlayerAdapter.ViewHolder> {
    private List<Player> players;
    private List<Player> selectedPlayers;

    public SelectablePlayerAdapter() {
        this.players = new ArrayList<>();
        this.selectedPlayers = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_player, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.textViewPlayerName.setText(player.getName());
        holder.textViewPlayerInfo.setText(
            player.getPosition().name() + " - Nível " + player.getLevel());
        
        holder.checkBoxPlayer.setChecked(selectedPlayers.contains(player));
        holder.checkBoxPlayer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPlayers.add(player);
            } else {
                selectedPlayers.remove(player);
            }
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public void setPlayers(List<Player> players) {
        this.players = new ArrayList<>(players);
        this.selectedPlayers.clear();
        notifyDataSetChanged();
    }

    public List<Player> getSelectedPlayers() {
        return new ArrayList<>(selectedPlayers);
    }

    public void removePlayers(List<Player> playersToRemove) {
        players.removeAll(playersToRemove);
        selectedPlayers.removeAll(playersToRemove);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxPlayer;
        TextView textViewPlayerName;
        TextView textViewPlayerInfo;

        ViewHolder(View itemView) {
            super(itemView);
            checkBoxPlayer = itemView.findViewById(R.id.checkBoxPlayer);
            textViewPlayerName = itemView.findViewById(R.id.textViewPlayerName);
            textViewPlayerInfo = itemView.findViewById(R.id.textViewPlayerInfo);
        }
    }
} 