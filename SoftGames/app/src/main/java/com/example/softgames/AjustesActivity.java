package com.example.softgames;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AjustesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    }

    private void cerrarSesion() {
        // 1. Borramos la sesión de la memoria
        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("slotActivo");
        editor.apply();

        // 2. Redirigimos al LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);

        // Estas banderas (flags) son cruciales: cierran todas las pantallas anteriores de golpe.
        // Así evitas que el usuario presione el botón "Atrás" del celular y regrese al juego sin sesión.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}