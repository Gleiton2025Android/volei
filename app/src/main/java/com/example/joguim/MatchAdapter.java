package com.example.joguim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private List<Match> matches;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private OnMatchClickListener listener;
    private DatabaseHelper databaseHelper;
    private long userId;
    private Context context;

    public MatchAdapter(Context context, String userId) {
        this.matches = new ArrayList<>();
        this.databaseHelper = new DatabaseHelper(context);
        this.userId = Long.parseLong(userId);
        this.context = context;
    }

    public interface OnMatchClickListener {
        void onMatchStatusChanged(Match match, Match.MatchStatus newStatus);
        void onMatchEdit(Match match);
        void onMatchDelete(Match match);
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);
        
        // Configurar data e local
        holder.textViewDateTime.setText(dateFormat.format(match.getDateTime()));
        holder.textViewLocation.setText(match.getLocation());
        
        // Configurar descrição (se houver)
        if (match.getDescription() != null && !match.getDescription().isEmpty()) {
            holder.textViewDescription.setVisibility(View.VISIBLE);
            holder.textViewDescription.setText(match.getDescription());
        } else {
            holder.textViewDescription.setVisibility(View.GONE);
        }

        // Configurar status
        String statusText;
        int statusColor;
        switch (match.getStatus()) {
            case CONFIRMED:
                statusText = "Confirmada";
                statusColor = holder.itemView.getContext().getColor(R.color.status_confirmed);
                break;
            case CANCELLED:
                statusText = "Cancelada";
                statusColor = holder.itemView.getContext().getColor(R.color.status_cancelled);
                break;
            default:
                statusText = "Pendente";
                statusColor = holder.itemView.getContext().getColor(R.color.status_pending);
                break;
        }
        holder.textViewStatus.setText(statusText);
        holder.textViewStatus.setBackgroundColor(statusColor);

        // Configurar número de confirmações
        int confirmedCount = databaseHelper.getConfirmedPlayersCount(match.getId());
        holder.textViewConfirmedCount.setText(confirmedCount + " jogador(es) confirmado(s)");

        // Configurar botão de presença
        if (match.getStatus() == Match.MatchStatus.CANCELLED) {
            holder.buttonConfirmPresence.setVisibility(View.GONE);
        } else {
            holder.buttonConfirmPresence.setVisibility(View.VISIBLE);
            boolean isConfirmed = databaseHelper.isPlayerConfirmed(match.getId(), userId);
            updatePresenceButton(holder.buttonConfirmPresence, isConfirmed);

            holder.buttonConfirmPresence.setOnClickListener(v -> {
                boolean newConfirmation = !isConfirmed;
                databaseHelper.confirmPresence(match.getId(), userId, newConfirmation);
                updatePresenceButton(holder.buttonConfirmPresence, newConfirmation);
                notifyItemChanged(position);
            });
        }

        // Configurar cliques nos botões de status
        holder.layoutButtons.setVisibility(match.getStatus() == Match.MatchStatus.PENDING ? 
            View.VISIBLE : View.GONE);

        holder.buttonConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMatchStatusChanged(match, Match.MatchStatus.CONFIRMED);
            }
        });

        holder.buttonCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMatchStatusChanged(match, Match.MatchStatus.CANCELLED);
            }
        });

        // Configurar clique no item
        holder.itemView.setOnClickListener(v -> {
            showOptionsDialog(match);
        });
    }

    private void showOptionsDialog(Match match) {
        String[] options = {"Editar", "Excluir"};
        
        new AlertDialog.Builder(context)
            .setTitle("Opções da Partida")
            .setItems(options, (dialog, which) -> {
                if (which == 0) { // Editar
                    if (listener != null) {
                        listener.onMatchEdit(match);
                    }
                } else if (which == 1) { // Excluir
                    showDeleteConfirmationDialog(match);
                }
            })
            .show();
    }

    private void showDeleteConfirmationDialog(Match match) {
        new AlertDialog.Builder(context)
            .setTitle("Excluir Partida")
            .setMessage("Tem certeza que deseja excluir esta partida?")
            .setPositiveButton("Sim", (dialog, which) -> {
                if (listener != null) {
                    listener.onMatchDelete(match);
                }
            })
            .setNegativeButton("Não", null)
            .show();
    }

    private void updatePresenceButton(Button button, boolean isConfirmed) {
        if (isConfirmed) {
            button.setText("Remover Presença");
            button.setBackgroundColor(button.getContext().getColor(R.color.status_cancelled));
        } else {
            button.setText("Confirmar Presença");
            button.setBackgroundColor(button.getContext().getColor(R.color.status_confirmed));
        }
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
        notifyDataSetChanged();
    }

    public void addMatch(Match match) {
        matches.add(match);
        notifyItemInserted(matches.size() - 1);
    }

    public void updateMatch(Match match) {
        int position = -1;
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getId() == match.getId()) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            matches.set(position, match);
            notifyItemChanged(position);
        }
    }

    public void removeMatch(Match match) {
        int position = -1;
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getId() == match.getId()) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            matches.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnMatchClickListener(OnMatchClickListener listener) {
        this.listener = listener;
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDateTime;
        TextView textViewLocation;
        TextView textViewDescription;
        TextView textViewStatus;
        TextView textViewConfirmedCount;
        LinearLayout layoutButtons;
        Button buttonConfirm;
        Button buttonCancel;
        Button buttonConfirmPresence;

        MatchViewHolder(View itemView) {
            super(itemView);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewConfirmedCount = itemView.findViewById(R.id.textViewConfirmedCount);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
            buttonConfirm = itemView.findViewById(R.id.buttonConfirm);
            buttonCancel = itemView.findViewById(R.id.buttonCancel);
            buttonConfirmPresence = itemView.findViewById(R.id.buttonConfirmPresence);
        }
    }
} 