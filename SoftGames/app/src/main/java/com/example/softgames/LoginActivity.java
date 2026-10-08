package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vinculamos las variables con los elementos del diseño XML
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCrearCuenta = findViewById(R.id.tvCrearCuenta);

        // Lógica del botón Iniciar Sesión
        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validación básica prioritaria: comprobar que hayan escrito algo
            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí en el futuro puedes conectar con una base de datos.
                // Por ahora, si escriben cualquier cosa, los deja pasar.
                Toast.makeText(LoginActivity.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

                // Transición a la pantalla de las partidas (InicioActivity)
                Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
                startActivity(intent);
                finish(); // Cerramos el login para que no puedan regresar con el botón de "Atrás"
            }
        });

        // Lógica temporal para el botón de crear cuenta
        tvCrearCuenta.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Pantalla de registro en construcción", Toast.LENGTH_SHORT).show();
        });
    }
}