package com.example.joguim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PlayersFragment extends Fragment {
    private static final String TAG = "PlayersFragment";
    private RecyclerView recyclerViewPlayers;
    private PlayerAdapter playerAdapter;
    private List<Player> pendingPlayers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_players, container, false);

        recyclerViewPlayers = view.findViewById(R.id.recyclerViewPlayers);
        recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        playerAdapter = new PlayerAdapter();
        recyclerViewPlayers.setAdapter(playerAdapter);

        // Se houver jogadores pendentes, exibi-los
        if (!pendingPlayers.isEmpty()) {
            Log.d(TAG, "Exibindo " + pendingPlayers.size() + " jogadores pendentes");
            playerAdapter.setPlayers(pendingPlayers);
        }

        return view;
    }

    public void setPlayers(List<Player> players) {
        Log.d(TAG, "setPlayers chamado com " + (players != null ? players.size() : 0) + " jogadores");
        if (players != null) {
            if (playerAdapter != null) {
                playerAdapter.setPlayers(new ArrayList<>(players));
                playerAdapter.notifyDataSetChanged();
                Log.d(TAG, "Lista de jogadores atualizada no adapter");
            } else {
                pendingPlayers = new ArrayList<>(players);
                Log.d(TAG, "Jogadores armazenados para exibição posterior");
            }
        }
    }

    public void addPlayer(Player player) {
        Log.d(TAG, "addPlayer chamado para jogador: " + player.getName());
        if (player != null) {
            if (playerAdapter != null) {
                playerAdapter.addPlayer(player);
                playerAdapter.notifyItemInserted(playerAdapter.getItemCount() - 1);
                Log.d(TAG, "Jogador adicionado ao adapter");
            } else {
                pendingPlayers.add(player);
                Log.d(TAG, "Jogador armazenado para adição posterior");
            }
        }
    }
} 