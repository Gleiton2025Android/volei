package com.example.joguim;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {
    private static final String TAG = "PlayerAdapter";
    private List<Player> players = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private Context context;

    public PlayerAdapter() {
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Criando novo ViewHolder");
        context = parent.getContext();
        databaseHelper = new DatabaseHelper(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        Log.d(TAG, "Vinculando jogador na posição " + position + ": " + player.getName());
        
        // Configurar nome
        holder.textViewPlayerName.setText(player.getName());
        
        // Configurar posição
        holder.textViewPosition.setText("Posição: " + player.getPosition().getDisplayName());
        
        // Configurar nível
        holder.textViewLevel.setText("Nível: " + player.getLevel());

        // Configurar clique no item
        holder.itemView.setOnClickListener(v -> showPlayerOptionsDialog(player));
    }

    private void showPlayerOptionsDialog(Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Opções do Jogador")
               .setItems(new String[]{"Editar", "Excluir"}, (dialog, which) -> {
                   if (which == 0) {
                       showEditPlayerDialog(player);
                   } else {
                       showDeleteConfirmationDialog(player);
                   }
               })
               .show();
    }

    private void showEditPlayerDialog(Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_player, null);

        EditText editTextPlayerName = dialogView.findViewById(R.id.editTextPlayerName);
        Spinner spinnerPosition = dialogView.findViewById(R.id.spinnerPosition);
        SeekBar seekBarLevel = dialogView.findViewById(R.id.seekBarLevel);
        TextView textViewLevel = dialogView.findViewById(R.id.textViewLevel);

        // Preencher dados atuais
        editTextPlayerName.setText(player.getName());
        
        // Configurar spinner
        String[] positions = new String[Player.Position.values().length];
        for (int i = 0; i < Player.Position.values().length; i++) {
            positions[i] = Player.Position.values()[i].getDisplayName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
            android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);
        spinnerPosition.setSelection(player.getPosition().ordinal());

        // Configurar seekbar
        seekBarLevel.setProgress(player.getLevel() - 1);
        textViewLevel.setText("Nível: " + player.getLevel());
        seekBarLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewLevel.setText("Nível: " + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(dialogView)
               .setTitle("Editar Jogador")
               .setPositiveButton("Salvar", (dialog, which) -> {
                   String name = editTextPlayerName.getText().toString();
                   if (name.isEmpty()) {
                       Toast.makeText(context, "Por favor, informe o nome do jogador", Toast.LENGTH_SHORT).show();
                       return;
                   }

                   player.setName(name);
                   player.setPosition(Player.Position.values()[spinnerPosition.getSelectedItemPosition()]);
                   player.setLevel(seekBarLevel.getProgress() + 1);

                   databaseHelper.updatePlayer(player);
                   notifyDataSetChanged();
                   Toast.makeText(context, "Jogador atualizado com sucesso!", Toast.LENGTH_SHORT).show();
               })
               .setNegativeButton("Cancelar", null)
               .show();
    }

    private void showDeleteConfirmationDialog(Player player) {
        new AlertDialog.Builder(context)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir o jogador " + player.getName() + "?")
            .setPositiveButton("Sim", (dialog, which) -> {
                databaseHelper.deletePlayer(player.getId());
                players.remove(player);
                notifyDataSetChanged();
                Toast.makeText(context, "Jogador excluído com sucesso!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Não", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public void setPlayers(List<Player> players) {
        Log.d(TAG, "Atualizando lista de jogadores: " + (players != null ? players.size() : 0) + " jogadores");
        if (players != null) {
            this.players = new ArrayList<>(players);
            notifyDataSetChanged();
        }
    }

    public void addPlayer(Player player) {
        if (player != null) {
            Log.d(TAG, "Adicionando jogador: " + player.getName());
            players.add(player);
            notifyItemInserted(players.size() - 1);
        }
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPlayerName;
        TextView textViewPosition;
        TextView textViewLevel;

        PlayerViewHolder(View itemView) {
            super(itemView);
            textViewPlayerName = itemView.findViewById(R.id.textViewPlayerName);
            textViewPosition = itemView.findViewById(R.id.textViewPosition);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
        }
    }
} 