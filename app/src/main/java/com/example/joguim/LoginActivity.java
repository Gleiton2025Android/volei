package com.example.joguim;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.widget.ArrayAdapter;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (databaseHelper.checkUser(username, password)) {
                    Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
    }

    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);

        EditText editTextUsername = dialogView.findViewById(R.id.editTextRegisterUsername);
        EditText editTextPassword = dialogView.findViewById(R.id.editTextRegisterPassword);
        Spinner spinnerPosition = dialogView.findViewById(R.id.spinnerRegisterPosition);
        SeekBar seekBarLevel = dialogView.findViewById(R.id.seekBarRegisterLevel);
        TextView textViewLevel = dialogView.findViewById(R.id.textViewRegisterLevel);

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
                .setTitle("Cadastrar")
                .setPositiveButton("Cadastrar", (dialog, which) -> {
                    String username = editTextUsername.getText().toString();
                    String password = editTextPassword.getText().toString();
                    Player.Position position = Player.Position.values()[spinnerPosition.getSelectedItemPosition()];
                    int level = seekBarLevel.getProgress() + 1;

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(LoginActivity.this, 
                            "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long userId = databaseHelper.addUser(username, password, level, position);
                    if (userId != -1) {
                        Toast.makeText(LoginActivity.this, 
                            "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            "Erro ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
} 