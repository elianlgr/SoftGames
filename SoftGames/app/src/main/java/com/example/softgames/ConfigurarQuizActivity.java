package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Pantalla de configuración del Modo Quiz (CU-S3-01: Superar el modo Quiz).
 * Flujo normal pasos 2 a 5:
 * - Muestra las dificultades disponibles (Fácil, Normal, Difícil).
 * - Pide el número de preguntas por categoría (10 a 20, con 10 seleccionado por defecto).
 * - Inicia la evaluación secuencial aleatoria por categorías.
 */
public class ConfigurarQuizActivity extends AppCompatActivity {

    private int dificultadSeleccionada = 1; // 1: Fácil, 2: Normal, 3: Difícil
    private int cantidadPorDefecto = 10;

    private TextView chipQuizFacil;
    private TextView chipQuizNormal;
    private TextView chipQuizDificil;

    private EditText etPreguntasPorCategoriaQuiz;
    private TextView chipCantQuiz10;
    private TextView chipCantQuiz15;
    private TextView chipCantQuiz20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_quiz);

        View btnVolver = findViewById(R.id.btnVolverConfigQuiz);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        chipQuizFacil = findViewById(R.id.chipQuizFacil);
        chipQuizNormal = findViewById(R.id.chipQuizNormal);
        chipQuizDificil = findViewById(R.id.chipQuizDificil);

        etPreguntasPorCategoriaQuiz = findViewById(R.id.etPreguntasPorCategoriaQuiz);
        chipCantQuiz10 = findViewById(R.id.chipCantQuiz10);
        chipCantQuiz15 = findViewById(R.id.chipCantQuiz15);
        chipCantQuiz20 = findViewById(R.id.chipCantQuiz20);

        Button btnContinuarQuiz = findViewById(R.id.btnContinuarQuiz);

        configurarChipsDificultad();
        configurarChipsCantidad();

        btnContinuarQuiz.setOnClickListener(v -> iniciarModoQuiz());
    }

    private void configurarChipsDificultad() {
        chipQuizFacil.setOnClickListener(v -> {
            dificultadSeleccionada = 1;
            actualizarDificultadUI();
        });

        chipQuizNormal.setOnClickListener(v -> {
            dificultadSeleccionada = 2;
            actualizarDificultadUI();
        });

        chipQuizDificil.setOnClickListener(v -> {
            dificultadSeleccionada = 3;
            actualizarDificultadUI();
        });
    }

    private void actualizarDificultadUI() {
        actualizarEstiloChip(chipQuizFacil, dificultadSeleccionada == 1);
        actualizarEstiloChip(chipQuizNormal, dificultadSeleccionada == 2);
        actualizarEstiloChip(chipQuizDificil, dificultadSeleccionada == 3);
    }

    private void configurarChipsCantidad() {
        chipCantQuiz10.setOnClickListener(v -> seleccionarCantidadRapida(10));
        chipCantQuiz15.setOnClickListener(v -> seleccionarCantidadRapida(15));
        chipCantQuiz20.setOnClickListener(v -> seleccionarCantidadRapida(20));
    }

    private void seleccionarCantidadRapida(int cantidad) {
        cantidadPorDefecto = cantidad;
        etPreguntasPorCategoriaQuiz.setText(String.valueOf(cantidad));
        actualizarEstiloChip(chipCantQuiz10, cantidad == 10);
        actualizarEstiloChip(chipCantQuiz15, cantidad == 15);
        actualizarEstiloChip(chipCantQuiz20, cantidad == 20);
    }

    private void actualizarEstiloChip(TextView chip, boolean seleccionado) {
        if (chip == null) return;
        if (seleccionado) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(0xFFFFFFFF);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(0xFF142232);
        }
    }

    private void iniciarModoQuiz() {
        String inputStr = etPreguntasPorCategoriaQuiz.getText().toString().trim();
        int preguntasPorCat = 10; // Por default, el número seleccionado es 10 (Paso 4)

        if (!inputStr.isEmpty()) {
            try {
                preguntasPorCat = Integer.parseInt(inputStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingresa un número válido de preguntas (10 a 20)", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (preguntasPorCat < 10 || preguntasPorCat > 20) {
            Toast.makeText(this,
                    "En el Modo Quiz el número de preguntas por categoría debe ser entre 10 y 20",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ModoQuizActivity.class);
        intent.putExtra("dificultad", dificultadSeleccionada);
        intent.putExtra("preguntasPorCategoria", preguntasPorCat);
        startActivity(intent);
        finish();
    }
}
