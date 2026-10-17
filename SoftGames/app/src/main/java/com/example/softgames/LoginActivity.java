package com.example.softgames;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        String usuarioActual = prefs.getString("usuarioActual", null);

        // Si ya hay un usuario que inició sesión, vamos a la selección de partidas
        if (usuarioActual != null && !usuarioActual.trim().isEmpty()) {
            Intent intent = new Intent(this, InicioActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

                // Guardamos el usuario actual en la sesión
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("usuarioActual", usuario);
                editor.remove("slotActivo");
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCrearCuenta.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Pantalla de registro en construcción", Toast.LENGTH_SHORT).show();
        });
    }
}