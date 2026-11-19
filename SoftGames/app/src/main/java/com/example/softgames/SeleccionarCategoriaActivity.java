package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Selector de las 8 categorías curriculares de Ingeniería de Software (CU-S1-03, CU-S3-03 y CU-S3-04).
 * Todas las categorías se encuentran habilitadas y redirigen a ConfigurarPartidaActivity.
 */
public class SeleccionarCategoriaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_categoria);

        View btnVolver = findViewById(R.id.btnVolverCategoria);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        Button btnCancelar = findViewById(R.id.btnCancelarCategoria);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> finish());
        }

        // Conectar las 8 tarjetas de categoría curricular
        conectarCategoria(R.id.cardProgramacion, "Programación");
        conectarCategoria(R.id.cardIngenieria, "Introducción a la ingeniería");
        conectarCategoria(R.id.cardLogica, "Lógica");
        conectarCategoria(R.id.cardMatematicas, "Matemáticas discretas");
        conectarCategoria(R.id.cardRequerimientos, "Ingeniería de requerimientos");
        conectarCategoria(R.id.cardAnalisisDiseno, "Análisis y diseño orientado a objetos");
        conectarCategoria(R.id.cardPOO, "Programación orientada a objetos");
        conectarCategoria(R.id.cardAlgoritmos, "Análisis y diseño de algoritmos");
    }

    private void conectarCategoria(int viewId, String nombreCategoria) {
        View card = findViewById(viewId);
        if (card != null) {
            card.setOnClickListener(v -> {
                Intent intent = new Intent(SeleccionarCategoriaActivity.this, ConfigurarPartidaActivity.class);
                intent.putExtra("categoria", nombreCategoria);
                startActivity(intent);
            });
        }
    }
}
