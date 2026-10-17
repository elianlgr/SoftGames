package com.example.softgames;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InicioActivity extends AppCompatActivity {

    private AdministradorArchivos adminArchivos;
    private Button btnSlot0;
    private Button btnSlot1;
    private Button btnSlot2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adminArchivos = new AdministradorArchivos(this);

        setContentView(R.layout.activity_inicio);

        btnSlot0 = findViewById(R.id.btnSlot0);
        btnSlot1 = findViewById(R.id.btnSlot1);
        btnSlot2 = findViewById(R.id.btnSlot2);

        configurarBoton(btnSlot0, 0);
        configurarBoton(btnSlot1, 1);
        configurarBoton(btnSlot2, 2);

        actualizarTextosBotones();

        Button btnAjustesInicio = findViewById(R.id.btnAjustesInicio);
        btnAjustesInicio.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, AjustesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adminArchivos != null) {
            adminArchivos.cargarArchivos();
            actualizarTextosBotones();
        }
    }

    private void actualizarTextosBotones() {
        actualizarTextoBoton(btnSlot0, 0);
        actualizarTextoBoton(btnSlot1, 1);
        actualizarTextoBoton(btnSlot2, 2);
    }

    private void actualizarTextoBoton(Button boton, int indice) {
        if (boton == null) return;
        ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
        if (archivo != null && archivo.nombre != null && !archivo.nombre.trim().isEmpty()) {
            boton.setText(archivo.nombre.toUpperCase());
            boton.setBackgroundResource(R.drawable.bg_btn_continuar);
        } else {
            boton.setText("PARTIDA " + (indice + 1));
            if (indice == 0) {
                boton.setBackgroundResource(R.drawable.bg_btn_continuar);
            } else {
                boton.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            }
        }
        boton.setTextColor(0xFFFFFFFF);
    }

    private void configurarBoton(Button boton, int indice) {
        boton.setOnClickListener(v -> {
            if (adminArchivos.iniciarSesion(indice)) {
                Toast.makeText(this, "Bienvenido de nuevo, " + adminArchivos.getArchivoActual().nombre, Toast.LENGTH_SHORT).show();
                guardarSesionPersistente(indice);
                iniciarJuego();
            } else {
                mostrarDialogoCreacion(boton, indice);
            }
        });
    }

    private void mostrarDialogoCreacion(Button boton, int indice) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Crear archivo de guardado");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Escribe el nombre del archivo");
        builder.setView(input);

        builder.setPositiveButton("Continuar", (dialog, which) -> {
            String nombreNuevo = input.getText().toString().trim();
            if (nombreNuevo.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (adminArchivos.crearArchivo(nombreNuevo, indice)) {
                Toast.makeText(this, "Archivo creado: " + nombreNuevo, Toast.LENGTH_SHORT).show();
                actualizarTextoBoton(boton, indice);
                guardarSesionPersistente(indice);
                iniciarJuego();
            } else {
                Toast.makeText(this, "Error: El nombre de archivo ya existe o hubo un fallo", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Método que registra la ranura en la memoria del teléfono
    private void guardarSesionPersistente(int indice) {
        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("slotActivo", indice);
        editor.apply();
    }

    private void iniciarJuego() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}