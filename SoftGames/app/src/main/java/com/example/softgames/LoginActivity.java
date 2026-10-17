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
            String identificador = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (identificador.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            org.json.JSONArray cuentas = RegistroActivity.cargarCuentas(LoginActivity.this);
            String usuarioAutenticado = null;
            boolean cuentaEncontrada = false;

            for (int i = 0; i < cuentas.length(); i++) {
                org.json.JSONObject cuenta = cuentas.optJSONObject(i);
                if (cuenta != null) {
                    String regUsuario = cuenta.optString("usuario", "");
                    String regCorreo = cuenta.optString("correo", "");
                    String regPassword = cuenta.optString("password", "");

                    if (regUsuario.equalsIgnoreCase(identificador) || regCorreo.equalsIgnoreCase(identificador)) {
                        cuentaEncontrada = true;
                        if (regPassword.equals(password)) {
                            usuarioAutenticado = regUsuario;
                        }
                        break;
                    }
                }
            }

            if (cuentaEncontrada && usuarioAutenticado == null) {
                Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cuentaEncontrada) {
                // Verificar compatibilidad con partidas previas existentes en el dispositivo
                boolean tienePartidaPrevia = false;
                for (int i = 0; i < 5; i++) {
                    java.io.File archivoPrevio = new java.io.File(getFilesDir(), ArchivoGuardado.obtenerNombreArchivo(identificador, i));
                    if (archivoPrevio.exists()) {
                        tienePartidaPrevia = true;
                        break;
                    }
                }
                if (tienePartidaPrevia) {
                    usuarioAutenticado = identificador;
                } else {
                    Toast.makeText(LoginActivity.this, "La cuenta no existe. Por favor, crea una cuenta primero", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(LoginActivity.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

            // Guardamos el usuario actual en la sesión
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("usuarioActual", usuarioAutenticado);
            editor.remove("slotActivo");
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        });

        btnCrearCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }
}