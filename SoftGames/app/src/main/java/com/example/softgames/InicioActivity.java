package com.example.softgames;

import android.content.Intent;
import android.content.SharedPreferences; // Necesario para la memoria permanente
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InicioActivity extends AppCompatActivity {

    private AdministradorArchivos adminArchivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adminArchivos = new AdministradorArchivos(this);

        // 1. REVISAR SI YA EXISTE UNA SESIÓN ACTIVA (Permanencia)
        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        int slotActivo = prefs.getInt("slotActivo", -1);

        if (slotActivo != -1) {
            // Si ya hay sesión, carga el archivo directo y salta esta pantalla
            adminArchivos.iniciarSesion(slotActivo);
            iniciarJuego();
            return; // Detiene la ejecución aquí
        }

        // 2. SI NO HAY SESIÓN, MUESTRA LOS BOTONES
        setContentView(R.layout.activity_inicio);

        // Configuramos tus 5 botones originales
        configurarBoton(findViewById(R.id.btnSlot0), 0);
        configurarBoton(findViewById(R.id.btnSlot1), 1);
        configurarBoton(findViewById(R.id.btnSlot2), 2);

        Button btnAjustesInicio = findViewById(R.id.btnAjustesInicio);
        btnAjustesInicio.setOnClickListener(v -> {
            // Nota que ahora dice InicioActivity.this
            Intent intent = new Intent(InicioActivity.this, AjustesActivity.class);
            startActivity(intent);
        });
    }

    private void configurarBoton(Button boton, int indice) {
        boton.setOnClickListener(v -> {
            // Intentamos iniciar sesion (deserializar)
            if (adminArchivos.iniciarSesion(indice)) {
                Toast.makeText(this, "Bienvenido de nuevo, " + adminArchivos.getArchivoActual().nombre, Toast.LENGTH_SHORT).show();
                guardarSesionPersistente(indice); // <-- GUARDA LA DECISIÓN PERMANENTEMENTE
                iniciarJuego();
            } else {
                // Caso de uso: Crear archivo de guardado
                mostrarDialogoCreacion(indice);
            }
        });
    }

    private void mostrarDialogoCreacion(int indice) {
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
        finish();
    }
}