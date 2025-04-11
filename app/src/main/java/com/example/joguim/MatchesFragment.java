package com.example.joguim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {
    private static final String TAG = "MatchesFragment";
    private RecyclerView recyclerViewMatches;
    private MatchAdapter matchAdapter;
    private List<Match> pendingMatches;
    private DatabaseHelper databaseHelper;
    private long userId;

    public void setUserId(long userId) {
        this.userId = userId;
        Log.d(TAG, "UserId definido: " + userId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            pendingMatches = new ArrayList<>();
            databaseHelper = new DatabaseHelper(requireContext());
            Log.d(TAG, "Fragment criado com userId: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar fragment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);
        
        try {
            recyclerViewMatches = view.findViewById(R.id.recyclerViewMatches);
            recyclerViewMatches.setLayoutManager(new LinearLayoutManager(getContext()));
            
            if (userId <= 0) {
                Log.e(TAG, "UserId inválido ao criar adapter: " + userId);
                return view;
            }

            matchAdapter = new MatchAdapter(requireContext(), String.valueOf(userId));
            matchAdapter.setOnMatchClickListener(new MatchAdapter.OnMatchClickListener() {
                @Override
                public void onMatchStatusChanged(Match match, Match.MatchStatus newStatus) {
                    try {
                        match.setStatus(newStatus);
                        databaseHelper.updateMatch(match);
                        matchAdapter.notifyDataSetChanged();
                        String message = newStatus == Match.MatchStatus.CONFIRMED ? 
                            "Partida confirmada!" : "Partida cancelada!";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao mudar status da partida: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMatchEdit(Match match) {
                    try {
                        if (match != null && getActivity() instanceof WelcomeActivity) {
                            ((WelcomeActivity) getActivity()).showMatchDialog(match);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao editar partida: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMatchDelete(Match match) {
                    try {
                        // Primeiro, deletar os times associados à partida
                        databaseHelper.deleteTeamsForMatch(match.getId());
                        
                        // Depois, deletar a partida
                        databaseHelper.deleteMatch(match.getId());
                        
                        // Atualizar a interface
                        matchAdapter.removeMatch(match);
                        Toast.makeText(requireContext(), "Partida excluída com sucesso!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao excluir partida: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Erro ao excluir partida", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            recyclerViewMatches.setAdapter(matchAdapter);

            // Se houver partidas pendentes, exiba-as
            if (!pendingMatches.isEmpty()) {
                Log.d(TAG, "Exibindo " + pendingMatches.size() + " partidas pendentes");
                matchAdapter.setMatches(pendingMatches);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar view: " + e.getMessage());
            e.printStackTrace();
        }

        return view;
    }

    public void setMatches(List<Match> matches) {
        try {
            Log.d(TAG, "Definindo " + (matches != null ? matches.size() : 0) + " partidas");
            if (matchAdapter != null) {
                matchAdapter.setMatches(new ArrayList<>(matches));
            } else {
                pendingMatches = new ArrayList<>(matches);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao definir partidas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addMatch(Match match) {
        try {
            Log.d(TAG, "Adicionando partida: " + match.getLocation());
            if (matchAdapter != null) {
                matchAdapter.addMatch(match);
            } else {
                pendingMatches.add(match);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adicionar partida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateMatch(Match match) {
        try {
            Log.d(TAG, "Atualizando partida: " + match.getLocation());
            if (matchAdapter != null) {
                matchAdapter.updateMatch(match);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar partida: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 