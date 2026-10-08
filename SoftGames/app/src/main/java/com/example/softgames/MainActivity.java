package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnModoQuiz = findViewById(R.id.btnModoQuiz);
        Button btnFreePlay = findViewById(R.id.btnFreePlay);
        Button btnMaraton = findViewById(R.id.btnMaraton);
        Button btnRondaAleatoria = findViewById(R.id.btnRondaAleatoria);

        btnModoQuiz.setOnClickListener(v -> mostrarMensaje("Modo Quiz en construcción"));
        btnFreePlay.setOnClickListener(v -> mostrarDialogoCategorias());
        btnMaraton.setOnClickListener(v -> mostrarMensaje("Maratón en construcción"));
        btnRondaAleatoria.setOnClickListener(v -> mostrarMensaje("Ronda Aleatoria en construcción"));
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoCategorias() {
        String[] categorias = {"Programación", "Introducción a la ingeniería", "Lógica"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Selecciona una Categoría")
                .setItems(categorias, (dialog, which) -> {
                    mostrarDialogoMinijuegos(categorias[which]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoMinijuegos(String categoria) {
        // En un caso real esto se llenaría dinámicamente según la categoría
        String[] minijuegos;
        if (categoria.equals("Lógica")) {
            minijuegos = new String[]{"Lógica 1"};
        } else {
            minijuegos = new String[]{"Próximamente..."};
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Minijuegos de " + categoria)
                .setItems(minijuegos, (dialog, which) -> {
                    if (minijuegos[which].equals("Lógica 1")) {
                        mostrarDialogoDificultad();
                    } else {
                        mostrarMensaje("Este minijuego aún no está disponible");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoDificultad() {
        String[] dificultades = {"Fácil", "Medio", "Difícil"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Selecciona la Dificultad")
                .setItems(dificultades, (dialog, which) -> {
                    // which: 0=Facil (dificultad 1), 1=Medio (dificultad 2), 2=Dificil (dificultad 3)
                    mostrarDialogoCantidadPreguntas(which + 1);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoCantidadPreguntas(int dificultad) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Número de preguntas");
        builder.setMessage("Ingresa la cantidad (Máximo 20):");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText("5"); // Por defecto
        builder.setView(input);

        builder.setPositiveButton("Empezar", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int cantidad = Integer.parseInt(inputText);
                if (cantidad > 0 && cantidad <= 20) {
                    Intent intent = new Intent(MainActivity.this, MinijuegoLogica1.class);
                    intent.putExtra("dificultad", dificultad);
                    intent.putExtra("totalPreguntas", cantidad);
                    startActivity(intent);
                } else {
                    mostrarMensaje("Por favor ingresa un número entre 1 y 20");
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_ajustes) {
            Intent intent = new Intent(this, AjustesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}