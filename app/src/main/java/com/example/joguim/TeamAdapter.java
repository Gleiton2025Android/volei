package com.example.joguim;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
    private List<Team> teams = new ArrayList<>();
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnTeamDeletedListener onTeamDeletedListener;
    private SimpleDateFormat dateFormat;

    public interface OnTeamDeletedListener {
        void onTeamDeleted(Team team);
    }

    public TeamAdapter(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    }

    public void setOnTeamDeletedListener(OnTeamDeletedListener listener) {
        this.onTeamDeletedListener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        
        // Obter informações da partida
        Match match = databaseHelper.getMatchById(team.getMatchId());
        String matchInfo = "";
        if (match != null) {
            matchInfo = match.getLocation() + " - " + dateFormat.format(match.getDateTime());
        }
        
        holder.textViewMatchInfo.setText(matchInfo);
        holder.textViewTeamNumber.setText("Time " + team.getTeamNumber());
        
        // Criar lista de jogadores
        StringBuilder players = new StringBuilder();
        for (Player player : team.getPlayers()) {
            if (players.length() > 0) {
                players.append("\n");
            }
            players.append("• ").append(player.getName())
                  .append(" (").append(player.getPosition().getDisplayName()).append(")");
        }
        holder.textViewPlayers.setText(players.toString());

        holder.buttonDeleteTeam.setOnClickListener(v -> {
            // Criar diálogo de confirmação
            new AlertDialog.Builder(context)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir este time?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    if (databaseHelper.deleteTeam(team.getId())) {
                        teams.remove(position);
                        notifyItemRemoved(position);
                        if (onTeamDeletedListener != null) {
                            onTeamDeletedListener.onTeamDeleted(team);
                        }
                        Toast.makeText(context, "Time excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao excluir time", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
        });
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        notifyDataSetChanged();
    }

    public void addTeam(Team team) {
        teams.add(team);
        notifyItemInserted(teams.size() - 1);
    }

    public void clearTeams() {
        teams.clear();
        notifyDataSetChanged();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMatchInfo;
        TextView textViewTeamNumber;
        TextView textViewPlayers;
        ImageButton buttonDeleteTeam;

        TeamViewHolder(View itemView) {
            super(itemView);
            textViewMatchInfo = itemView.findViewById(R.id.textViewMatchInfo);
            textViewTeamNumber = itemView.findViewById(R.id.textViewTeamNumber);
            textViewPlayers = itemView.findViewById(R.id.textViewPlayers);
            buttonDeleteTeam = itemView.findViewById(R.id.buttonDeleteTeam);
        }
    }
} 