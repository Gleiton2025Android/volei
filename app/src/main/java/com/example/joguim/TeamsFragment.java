package com.example.joguim;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TeamsFragment extends Fragment {
    private static final String TAG = "TeamsFragment";
    private RecyclerView recyclerViewTeams;
    private TeamAdapter teamAdapter;
    private Button buttonGenerateTeams;
    private DatabaseHelper databaseHelper;
    private long userId;
    private List<Match> matches;
    private List<Player> selectedPlayers;
    private int currentTeamNumber = 1;
    private int teamSize = 0;
    private Match selectedMatch;

    public void setUserId(long userId) {
        this.userId = userId;
        Log.d(TAG, "UserId definido: " + userId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            databaseHelper = new DatabaseHelper(requireContext());
            Log.d(TAG, "Fragment criado com userId: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar fragment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teams, container, false);
        
        try {
            recyclerViewTeams = view.findViewById(R.id.recyclerViewTeams);
            recyclerViewTeams.setLayoutManager(new LinearLayoutManager(getContext()));
            
            teamAdapter = new TeamAdapter(requireContext());
            teamAdapter.setOnTeamDeletedListener(team -> {
                // Atualizar a interface após a exclusão do time
                loadAllTeams();
            });
            recyclerViewTeams.setAdapter(teamAdapter);

            buttonGenerateTeams = view.findViewById(R.id.buttonGenerateTeams);
            buttonGenerateTeams.setOnClickListener(v -> showTeamGenerationDialog());

            loadAllTeams();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar view: " + e.getMessage());
            e.printStackTrace();
        }

        return view;
    }

    private void showTeamGenerationDialog() {
        try {
            Log.d(TAG, "Iniciando diálogo de geração de times");
            
            // Resetar variáveis
            currentTeamNumber = 1;
            teamSize = 0;
            selectedMatch = null;
            
            matches = databaseHelper.getMatches(userId);
            Log.d(TAG, "Total de partidas carregadas: " + matches.size());
            
            if (matches.isEmpty()) {
                Toast.makeText(requireContext(), 
                    "Não há partidas cadastradas", Toast.LENGTH_SHORT).show();
                return;
            }

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_manual_team_selection, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Gerar Times");
            builder.setView(dialogView);

            // Configurar Spinner de partidas
            Spinner spinnerMatch = dialogView.findViewById(R.id.spinnerMatch);
            List<String> matchDescriptions = new ArrayList<>();
            
            // Adicionar apenas partidas com jogadores confirmados
            List<Match> validMatches = new ArrayList<>();
            for (Match match : matches) {
                int confirmedCount = databaseHelper.getConfirmedPlayersCount(match.getId());
                Log.d(TAG, "Verificando partida " + match.getId() + 
                    " (" + match.getLocation() + "): " + confirmedCount + " jogadores confirmados");
                
                if (confirmedCount > 0) {
                    validMatches.add(match);
                    String description = match.getLocation() + " - " + match.getFormattedDateTime() + 
                        " (" + confirmedCount + " confirmados)";
                    matchDescriptions.add(description);
                    Log.d(TAG, "Partida adicionada à lista válida: " + description);
                }
            }
            
            if (validMatches.isEmpty()) {
                Toast.makeText(requireContext(), 
                    "Não há partidas com jogadores confirmados", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Nenhuma partida válida encontrada");
                return;
            }
            
            Log.d(TAG, "Total de partidas válidas: " + validMatches.size());
            matches = validMatches; // Atualizar lista de partidas para conter apenas as válidas

            ArrayAdapter<String> matchAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, matchDescriptions);
            matchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMatch.setAdapter(matchAdapter);

            // Configurar RadioGroup
            RadioGroup radioGroupTeamSize = dialogView.findViewById(R.id.radioGroupTeamSize);

            // Configurar RecyclerView de jogadores
            RecyclerView recyclerViewPlayers = dialogView.findViewById(R.id.recyclerViewPlayers);
            recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
            SelectablePlayerAdapter playerAdapter = new SelectablePlayerAdapter();
            recyclerViewPlayers.setAdapter(playerAdapter);

            // Botões
            Button buttonAddToTeam = dialogView.findViewById(R.id.buttonAddToTeam);
            Button buttonConfirmTeams = dialogView.findViewById(R.id.buttonConfirmTeams);
            
            // Desabilitar botões inicialmente
            buttonAddToTeam.setEnabled(false);
            buttonConfirmTeams.setEnabled(false);

            // Criar o diálogo
            AlertDialog dialog = builder.create();

            // Atualizar jogadores quando uma partida é selecionada
            spinnerMatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedMatch = matches.get(position);
                    Log.d(TAG, "Partida selecionada: " + selectedMatch.getId() + 
                        " (" + selectedMatch.getLocation() + ")");
                    
                    List<Player> confirmedPlayers = databaseHelper.getConfirmedPlayersForMatch(selectedMatch.getId());
                    Log.d(TAG, "Jogadores confirmados carregados: " + confirmedPlayers.size() + 
                        " para partida " + selectedMatch.getId());
                    
                    if (confirmedPlayers.isEmpty()) {
                        Log.e(TAG, "Lista de jogadores confirmados está vazia para partida " + 
                            selectedMatch.getId());
                        Toast.makeText(requireContext(), 
                            "Nenhum jogador confirmado para esta partida", Toast.LENGTH_SHORT).show();
                    } else {
                        StringBuilder playerNames = new StringBuilder();
                        for (Player player : confirmedPlayers) {
                            playerNames.append(player.getName()).append(", ");
                        }
                        Log.d(TAG, "Jogadores confirmados: " + playerNames.toString());
                    }
                    
                    playerAdapter.setPlayers(confirmedPlayers);
                    
                    // Habilitar botão de adicionar se houver jogadores e tamanho do time selecionado
                    boolean shouldEnableButton = teamSize > 0 && !confirmedPlayers.isEmpty();
                    buttonAddToTeam.setEnabled(shouldEnableButton);
                    Log.d(TAG, "Estado do botão Adicionar: " + shouldEnableButton + 
                        " (teamSize=" + teamSize + ", hasPlayers=" + !confirmedPlayers.isEmpty() + ")");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "Nenhuma partida selecionada");
                }
            });

            // Atualizar tamanho do time quando selecionado
            radioGroupTeamSize.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radioButtonDupla) {
                    teamSize = 2;
                } else if (checkedId == R.id.radioButtonQuarteto) {
                    teamSize = 4;
                } else if (checkedId == R.id.radioButtonSexteto) {
                    teamSize = 6;
                }
                
                Log.d(TAG, "Tamanho do time selecionado: " + teamSize);
                
                // Habilitar botão de adicionar se houver jogadores
                boolean hasPlayers = playerAdapter.getItemCount() > 0;
                boolean shouldEnableButton = teamSize > 0 && hasPlayers;
                buttonAddToTeam.setEnabled(shouldEnableButton);
                Log.d(TAG, "Estado do botão Adicionar após seleção de tamanho: " + shouldEnableButton + 
                    " (teamSize=" + teamSize + ", hasPlayers=" + hasPlayers + ")");
            });

            // Adicionar jogadores ao time atual
            buttonAddToTeam.setOnClickListener(v -> {
                if (teamSize == 0) {
                    Toast.makeText(requireContext(), 
                        "Selecione o tamanho dos times", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Player> selectedPlayers = playerAdapter.getSelectedPlayers();
                Log.d(TAG, "Jogadores selecionados: " + selectedPlayers.size());
                
                if (selectedPlayers.size() != teamSize) {
                    Toast.makeText(requireContext(), 
                        "Selecione exatamente " + teamSize + " jogadores para o time", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Criar e salvar o time
                Team team = new Team(selectedMatch.getId(), currentTeamNumber);
                for (Player player : selectedPlayers) {
                    team.addPlayer(player);
                    Log.d(TAG, "Adicionando jogador ao time: " + player.getName());
                }

                long teamId = databaseHelper.saveTeam(team);
                if (teamId != -1) {
                    team.setId(teamId);
                    teamAdapter.addTeam(team);
                    currentTeamNumber++;
                    
                    // Remover jogadores selecionados da lista
                    playerAdapter.removePlayers(selectedPlayers);
                    
                    // Atualizar estado dos botões
                    buttonAddToTeam.setEnabled(playerAdapter.getItemCount() >= teamSize);
                    buttonConfirmTeams.setEnabled(playerAdapter.getItemCount() < teamSize);
                    
                    Log.d(TAG, "Time " + team.getTeamNumber() + " criado com sucesso");
                    Toast.makeText(requireContext(), 
                        "Time " + team.getTeamNumber() + " criado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Erro ao salvar o time");
                    Toast.makeText(requireContext(), 
                        "Erro ao criar o time", Toast.LENGTH_SHORT).show();
                }
            });

            // Confirmar todos os times
            buttonConfirmTeams.setOnClickListener(v -> {
                if (playerAdapter.getItemCount() > 0) {
                    Toast.makeText(requireContext(), 
                        "Ainda há " + playerAdapter.getItemCount() + 
                        " jogadores não alocados em times", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                loadAllTeams(); // Recarregar times após finalizar
                Log.d(TAG, "Times confirmados e diálogo fechado");
            });

            dialog.show();
            Log.d(TAG, "Diálogo de geração de times exibido");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar diálogo: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), 
                "Erro ao abrir diálogo de geração de times", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllTeams() {
        try {
            Log.d(TAG, "Iniciando carregamento de todos os times");
            
            List<Match> matches = databaseHelper.getMatches(userId);
            List<Team> allTeams = new ArrayList<>();

            for (Match match : matches) {
                List<Team> matchTeams = databaseHelper.getTeamsForMatch(match.getId());
                allTeams.addAll(matchTeams);
                Log.d(TAG, "Partida " + match.getId() + ": " + matchTeams.size() + " times carregados");
            }

            teamAdapter.setTeams(allTeams);
            Log.d(TAG, "Total de times carregados: " + allTeams.size());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar times: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addTeam(Team team) {
        if (teamAdapter != null) {
            teamAdapter.addTeam(team);
        }
    }

    public void updateTeams() {
        loadAllTeams();
    }
} 