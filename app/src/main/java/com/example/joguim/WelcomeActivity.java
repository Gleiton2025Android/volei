package com.example.joguim;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    private TextView textViewUsername;
    private Button buttonLogout;
    private Button buttonAddMatch;
    private Button buttonAddPlayer;
    private Button buttonEditProfile;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MatchesFragment matchesFragment;
    private PlayersFragment playersFragment;
    private TeamsFragment teamsFragment;
    private Calendar selectedDate = Calendar.getInstance();
    private int selectedHour = 0;
    private int selectedMinute = 0;
    private DatabaseHelper databaseHelper;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_welcome);

            initializeViews();
            if (databaseHelper == null || userId <= 0) {
                Toast.makeText(this, "Erro ao inicializar o banco de dados", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            setupFragments();
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao iniciar a tela: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            databaseHelper = new DatabaseHelper(this);

            textViewUsername = findViewById(R.id.textViewUsername);
            buttonLogout = findViewById(R.id.buttonLogout);
            buttonAddMatch = findViewById(R.id.buttonAddMatch);
            buttonAddPlayer = findViewById(R.id.buttonAddPlayer);
            buttonEditProfile = findViewById(R.id.buttonEditProfile);
            viewPager = findViewById(R.id.viewPager);
            tabLayout = findViewById(R.id.tabLayout);

            String username = getIntent().getStringExtra("username");
            if (username == null || username.isEmpty()) {
                Log.e("WelcomeActivity", "Username não fornecido");
                Toast.makeText(this, "Erro: usuário não identificado", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            textViewUsername.setText("Olá, " + username + "!");
            userId = databaseHelper.getUserId(username);
            
            if (userId <= 0) {
                Log.e("WelcomeActivity", "UserId inválido: " + userId);
                Toast.makeText(this, "Erro: usuário não encontrado", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());

            Log.d("WelcomeActivity", "Views inicializadas com sucesso. UserId: " + userId);
        } catch (Exception e) {
            Log.e("WelcomeActivity", "Erro ao inicializar views: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao inicializar aplicativo", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupFragments() {
        try {
            matchesFragment = new MatchesFragment();
            matchesFragment.setUserId(userId);
            playersFragment = new PlayersFragment();
            teamsFragment = new TeamsFragment();
            teamsFragment.setUserId(userId);

            ViewPagerAdapter adapter = new ViewPagerAdapter(this);
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Partidas");
                        break;
                    case 1:
                        tab.setText("Jogadores");
                        break;
                    case 2:
                        tab.setText("Times");
                        break;
                }
            }).attach();

            // Aguardar os fragmentos serem criados antes de carregar os dados
            viewPager.post(() -> {
                // Adicionar um pequeno delay para garantir que os fragmentos estejam prontos
                viewPager.postDelayed(() -> {
                    loadInitialData();
                }, 500);
            });

        } catch (Exception e) {
            Log.e("WelcomeActivity", "Erro ao configurar fragmentos: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao configurar fragmentos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadInitialData() {
        try {
            if (databaseHelper != null && userId > 0) {
                // Carregar partidas
                List<Match> matches = databaseHelper.getMatches(userId);
                if (matches != null) {
                    Log.d("WelcomeActivity", "Carregadas " + matches.size() + " partidas");
                    if (matchesFragment != null) {
                        matchesFragment.setMatches(matches);
                    } else {
                        Log.e("WelcomeActivity", "matchesFragment é null");
                    }
                } else {
                    Log.e("WelcomeActivity", "Lista de partidas é null");
                }

                // Carregar jogadores
                List<Player> players = databaseHelper.getAllPlayers();
                if (players != null) {
                    Log.d("WelcomeActivity", "Carregados " + players.size() + " jogadores");
                    if (playersFragment != null) {
                        playersFragment.setPlayers(players);
                    } else {
                        Log.e("WelcomeActivity", "playersFragment é null");
                    }
                } else {
                    Log.e("WelcomeActivity", "Lista de jogadores é null");
                }
            } else {
                Log.e("WelcomeActivity", "databaseHelper ou userId inválidos. userId: " + userId);
            }
        } catch (Exception e) {
            Log.e("WelcomeActivity", "Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        // Configurar o botão de adicionar partida
        buttonAddMatch.setOnClickListener(v -> showAddMatchDialog());

        // Configurar o botão de adicionar jogador
        buttonAddPlayer.setOnClickListener(v -> showAddPlayerDialog());

        // Configurar o botão de logout
        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return matchesFragment;
                case 1:
                    return playersFragment;
                case 2:
                    return teamsFragment;
                default:
                    throw new IllegalStateException("Posição inválida: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private void showAddMatchDialog() {
        showMatchDialog(null);
    }

    private void showEditMatchDialog(Match match) {
        showMatchDialog(match);
    }

    public void showMatchDialog(Match match) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_match, null);
        
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        Button buttonDatePicker = dialogView.findViewById(R.id.buttonDatePicker);
        Button buttonTimePicker = dialogView.findViewById(R.id.buttonTimePicker);
        TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);

        Calendar selectedDateTime = Calendar.getInstance();
        if (match != null) {
            selectedDateTime.setTime(match.getDateTime());
            editTextLocation.setText(match.getLocation());
            editTextDescription.setText(match.getDescription());
        }

        // Atualizar o texto de data e hora
        updateDateTimeText(textViewDateTime, selectedDateTime);

        buttonDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);
                    updateDateTimeText(textViewDateTime, selectedDateTime);
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        buttonTimePicker.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeText(textViewDateTime, selectedDateTime);
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        String dialogTitle = match != null ? "Editar Partida" : "Nova Partida";
        String buttonText = match != null ? "Salvar" : "Adicionar";

        builder.setView(dialogView)
                .setTitle(dialogTitle)
                .setPositiveButton(buttonText, (dialog, which) -> {
                    String location = editTextLocation.getText().toString();
                    String description = editTextDescription.getText().toString();

                    if (location.isEmpty()) {
                        Toast.makeText(this, "Por favor, informe o local da partida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (match != null) {
                        // Atualizar partida existente
                        match.setDateTime(selectedDateTime.getTime());
                        match.setLocation(location);
                        match.setDescription(description);
                        databaseHelper.updateMatch(match);
                        matchesFragment.updateMatch(match);
                        Toast.makeText(this, "Partida atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Criar nova partida
                        Match newMatch = new Match(selectedDateTime.getTime(), location, description);
                        long matchId = databaseHelper.addMatch(newMatch, userId);
                        if (matchId != -1) {
                            newMatch.setId(matchId);
                            matchesFragment.addMatch(newMatch);
                            Toast.makeText(this, "Partida adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erro ao adicionar partida", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateDateTimeText(TextView textView, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textView.setText("Data e hora: " + sdf.format(calendar.getTime()));
    }

    private void showAddPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_player, null);

        EditText editTextPlayerName = dialogView.findViewById(R.id.editTextPlayerName);
        Spinner spinnerPosition = dialogView.findViewById(R.id.spinnerPosition);
        SeekBar seekBarLevel = dialogView.findViewById(R.id.seekBarLevel);
        TextView textViewLevel = dialogView.findViewById(R.id.textViewLevel);

        // Configurar o Spinner de posições
        String[] positions = new String[Player.Position.values().length];
        for (int i = 0; i < Player.Position.values().length; i++) {
            positions[i] = Player.Position.values()[i].getDisplayName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);

        // Configurar o SeekBar de nível
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
                .setTitle("Cadastrar Jogador")
                .setPositiveButton("Cadastrar", (dialog, which) -> {
                    String name = editTextPlayerName.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Por favor, informe o nome do jogador", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Player.Position position = Player.Position.values()[spinnerPosition.getSelectedItemPosition()];
                    int level = seekBarLevel.getProgress() + 1;

                    Player player = new Player(name, position, level);
                    long playerId = databaseHelper.addPlayer(player);
                    if (playerId != -1) {
                        player.setId(playerId);
                        playersFragment.addPlayer(player);
                        Toast.makeText(this, "Jogador cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar jogador", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditProfileDialog() {
        try {
            // Carregar dados atuais do usuário
            User currentUser = databaseHelper.getUser(userId);
            if (currentUser == null) {
                Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);

            EditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
            EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);
            Spinner spinnerPosition = dialogView.findViewById(R.id.spinnerPosition);
            SeekBar seekBarLevel = dialogView.findViewById(R.id.seekBarLevel);
            TextView textViewLevel = dialogView.findViewById(R.id.textViewLevel);

            // Preencher dados atuais
            editTextUsername.setText(currentUser.getUsername());
            
            // Configurar spinner de posições
            String[] positions = new String[Player.Position.values().length];
            for (int i = 0; i < Player.Position.values().length; i++) {
                positions[i] = Player.Position.values()[i].getDisplayName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, positions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPosition.setAdapter(adapter);
            
            // Selecionar posição atual
            spinnerPosition.setSelection(currentUser.getPosition().ordinal());

            // Configurar nível atual
            seekBarLevel.setProgress(currentUser.getLevel() - 1);
            textViewLevel.setText("Nível: " + currentUser.getLevel());

            // Listener para atualização do nível
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
                .setTitle("Editar Perfil")
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String username = editTextUsername.getText().toString();
                    String password = editTextPassword.getText().toString();
                    Player.Position position = Player.Position.values()[spinnerPosition.getSelectedItemPosition()];
                    int level = seekBarLevel.getProgress() + 1;

                    if (username.isEmpty()) {
                        Toast.makeText(WelcomeActivity.this, 
                            "Por favor, informe o nome de usuário", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Atualizar usuário
                    databaseHelper.updateUser(userId, username, password, level, position);
                    
                    // Atualizar nome exibido
                    textViewUsername.setText("Olá, " + username + "!");
                    
                    Toast.makeText(WelcomeActivity.this, 
                        "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
        } catch (Exception e) {
            Log.e("WelcomeActivity", "Erro ao mostrar diálogo de edição: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao abrir edição de perfil", Toast.LENGTH_SHORT).show();
        }
    }
} 