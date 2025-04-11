package com.example.joguim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextNewUsername;
    private EditText editTextNewPassword;
    private Spinner spinnerPosition;
    private SeekBar seekBarLevel;
    private TextView textViewLevel;
    private Button buttonRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        editTextNewUsername = findViewById(R.id.editTextNewUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        seekBarLevel = findViewById(R.id.seekBarLevel);
        textViewLevel = findViewById(R.id.textViewLevel);
        buttonRegister = findViewById(R.id.buttonRegister);

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

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextNewUsername.getText().toString();
                String password = editTextNewPassword.getText().toString();
                Player.Position position = Player.Position.values()[spinnerPosition.getSelectedItemPosition()];
                int level = seekBarLevel.getProgress() + 1;

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    long userId = databaseHelper.addUser(username, password, level, position);
                    if (userId != -1) {
                        Toast.makeText(RegisterActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Erro ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, "Nome de usuário já existe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 