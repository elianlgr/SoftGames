package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ConfigurarPartidaActivity extends AppCompatActivity {

    private String categoria = "Lógica";
    private String minijuegoSeleccionado = "Lógica 1";
    private int dificultadSeleccionada = 1; // 1: Fácil, 2: Medio, 3: Difícil

    private TextView chipMinijuego1;
    private TextView chipMinijuego2;
    private TextView chipMinijuego3;

    private TextView chipFacil;
    private TextView chipMedio;
    private TextView chipDificil;

    private EditText etCantidadPreguntas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_partida);

        if (getIntent() != null && getIntent().hasExtra("categoria")) {
            categoria = getIntent().getStringExtra("categoria");
        }

        View btnVolver = findViewById(R.id.btnVolverConfig);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        chipMinijuego1 = findViewById(R.id.chipMinijuego1);
        chipMinijuego2 = findViewById(R.id.chipMinijuego2);
        chipMinijuego3 = findViewById(R.id.chipMinijuego3);

        chipFacil = findViewById(R.id.chipFacil);
        chipMedio = findViewById(R.id.chipMedio);
        chipDificil = findViewById(R.id.chipDificil);

        etCantidadPreguntas = findViewById(R.id.etCantidadPreguntas);
        Button btnEmpezar = findViewById(R.id.btnEmpezarPartida);

        configurarChipsMinijuegos();
        configurarChipsDificultad();

        btnEmpezar.setOnClickListener(v -> {
            iniciarPartida();
        });
    }

    private void configurarChipsMinijuegos() {
        // Adaptar nombres según la categoría seleccionada
        if ("Programación".equalsIgnoreCase(categoria)) {
            chipMinijuego1.setText("Variables 1");
            chipMinijuego2.setText("Condicionales 1");
            chipMinijuego3.setText("Bucles 1");
        } else if ("Introducción a la ingeniería".equalsIgnoreCase(categoria)) {
            chipMinijuego1.setText("Diagramas 1");
            chipMinijuego2.setText("Requerimientos 1");
            chipMinijuego3.setText("Procesos 1");
        } else {
            chipMinijuego1.setText("Lógica 1");
            chipMinijuego2.setText("Lógica 2");
            chipMinijuego3.setText("Lógica 3");
        }

        minijuegoSeleccionado = chipMinijuego1.getText().toString();

        chipMinijuego1.setOnClickListener(v -> seleccionarChipMinijuego(chipMinijuego1));
        chipMinijuego2.setOnClickListener(v -> seleccionarChipMinijuego(chipMinijuego2));
        chipMinijuego3.setOnClickListener(v -> seleccionarChipMinijuego(chipMinijuego3));
    }

    private void seleccionarChipMinijuego(TextView chipSeleccionado) {
        minijuegoSeleccionado = chipSeleccionado.getText().toString();

        actualizarEstiloChip(chipMinijuego1, chipMinijuego1 == chipSeleccionado);
        actualizarEstiloChip(chipMinijuego2, chipMinijuego2 == chipSeleccionado);
        actualizarEstiloChip(chipMinijuego3, chipMinijuego3 == chipSeleccionado);
    }

    private void configurarChipsDificultad() {
        chipFacil.setOnClickListener(v -> {
            dificultadSeleccionada = 1;
            actualizarDificultadUI();
        });

        chipMedio.setOnClickListener(v -> {
            dificultadSeleccionada = 2;
            actualizarDificultadUI();
        });

        chipDificil.setOnClickListener(v -> {
            dificultadSeleccionada = 3;
            actualizarDificultadUI();
        });
    }

    private void actualizarDificultadUI() {
        actualizarEstiloChip(chipFacil, dificultadSeleccionada == 1);
        actualizarEstiloChip(chipMedio, dificultadSeleccionada == 2);
        actualizarEstiloChip(chipDificil, dificultadSeleccionada == 3);
    }

    private void actualizarEstiloChip(TextView chip, boolean seleccionado) {
        if (seleccionado) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(0xFFFFFFFF);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(0xFF142232);
        }
    }

    private void iniciarPartida() {
        String inputStr = etCantidadPreguntas.getText().toString().trim();
        int cantidad = 5;

        if (!inputStr.isEmpty()) {
            try {
                cantidad = Integer.parseInt(inputStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingresa un número válido de preguntas", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (cantidad < 1 || cantidad > 20) {
            Toast.makeText(this, "Por favor ingresa un número de preguntas entre 1 y 20", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es Lógica 1, lanzamos el minijuego ya implementado
        if ("Lógica 1".equals(minijuegoSeleccionado)) {
            Intent intent = new Intent(this, MinijuegoLogica1.class);
            intent.putExtra("dificultad", dificultadSeleccionada);
            intent.putExtra("totalPreguntas", cantidad);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "El minijuego " + minijuegoSeleccionado + " estará disponible próximamente", Toast.LENGTH_SHORT).show();
        }
    }
}
