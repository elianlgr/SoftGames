package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

        // Conectar cada tarjeta de categoría
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
                // Solo Lógica está implementada funcionalmente
                if ("Lógica".equalsIgnoreCase(nombreCategoria)) {
                    Intent intent = new Intent(SeleccionarCategoriaActivity.this, ConfigurarPartidaActivity.class);
                    intent.putExtra("categoria", nombreCategoria);
                    startActivity(intent);
                } else {
                    // Muestra el mensaje de aviso tipo "[Categoría] en construcción"
                    Toast.makeText(SeleccionarCategoriaActivity.this, nombreCategoria + " en construcción", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
