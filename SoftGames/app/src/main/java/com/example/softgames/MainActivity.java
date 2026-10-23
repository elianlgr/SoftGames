package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Botón Volver
        View btnVolver = findViewById(R.id.btnVolverMain);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // Botón Ajustes (Engrane)
        View btnAjustes = findViewById(R.id.btnAjustesMain);
        if (btnAjustes != null) {
            btnAjustes.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AjustesActivity.class);
                startActivity(intent);
            });
        }

        // 4 Modos de juego
        View cardModoQuiz = findViewById(R.id.cardModoQuiz);
        View cardFreePlay = findViewById(R.id.cardFreePlay);
        View cardMaraton = findViewById(R.id.cardMaraton);
        View cardRondaAleatoria = findViewById(R.id.cardRondaAleatoria);

        if (cardModoQuiz != null) {
            cardModoQuiz.setOnClickListener(v -> mostrarMensaje("Modo Quiz en construcción"));
        }

        if (cardFreePlay != null) {
            cardFreePlay.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SeleccionarCategoriaActivity.class);
                startActivity(intent);
            });
        }

        if (cardMaraton != null) {
            cardMaraton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ConfigurarMaratonActivity.class);
                startActivity(intent);
            });
        }

        if (cardRondaAleatoria != null) {
            cardRondaAleatoria.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RondaAleatoriaActivity.class);
                startActivity(intent);
            });
        }

        View btnVerEstadisticasMain = findViewById(R.id.btnVerEstadisticasMain);
        if (btnVerEstadisticasMain != null) {
            btnVerEstadisticasMain.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EstadisticasActivity.class);
                startActivity(intent);
            });
        }
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
