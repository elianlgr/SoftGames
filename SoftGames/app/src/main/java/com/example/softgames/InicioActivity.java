package com.example.softgames;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class InicioActivity extends AppCompatActivity {

    private static final int TOTAL_RANURAS_UI = 3;

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

        Button btnGestionarPartidaInicio = findViewById(R.id.btnGestionarPartidaInicio);
        if (btnGestionarPartidaInicio != null) {
            btnGestionarPartidaInicio.setOnClickListener(v -> mostrarSelectorRanuraParaGestionar());
        }

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
        // Clic normal: iniciar sesión si existe o crear nueva partida si está libre
        boton.setOnClickListener(v -> {
            if (adminArchivos.iniciarSesion(indice)) {
                Toast.makeText(this, "Bienvenido de nuevo, " + adminArchivos.getArchivoActual().nombre, Toast.LENGTH_SHORT).show();
                guardarSesionPersistente(indice);
                iniciarJuego();
            } else {
                mostrarDialogoCreacion(boton, indice);
            }
        });

        // Caso de Uso CU-04 (Paso 1): Mantener presionado el botón de una ranura con partida existente
        boton.setOnLongClickListener(v -> {
            ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
            if (archivo != null && archivo.nombre != null && !archivo.nombre.trim().isEmpty()) {
                mostrarDialogoGestionPartida(indice);
            } else {
                Toast.makeText(this, "Ranura vacía. Tócala para crear una nueva partida.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    /**
     * Caso de Uso CU-04 (Paso 1 alternativo):
     * Permite seleccionar la opción de gestionar partida desde el botón dedicado.
     */
    private void mostrarSelectorRanuraParaGestionar() {
        List<Integer> ocupadas = adminArchivos.obtenerRanurasOcupadas(TOTAL_RANURAS_UI);
        if (ocupadas.isEmpty()) {
            Toast.makeText(this, "No existe ningún archivo de guardado creado aún.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ocupadas.size() == 1) {
            mostrarDialogoGestionPartida(ocupadas.get(0));
            return;
        }

        String[] etiquetas = new String[ocupadas.size()];
        for (int i = 0; i < ocupadas.size(); i++) {
            int slot = ocupadas.get(i);
            ArchivoGuardado arch = adminArchivos.getArchivo(slot);
            String nombre = (arch != null && arch.nombre != null) ? arch.nombre : ("PARTIDA " + (slot + 1));
            etiquetas[i] = "Ranura " + (slot + 1) + ": " + nombre;
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona la partida a gestionar")
                .setItems(etiquetas, (dialog, which) -> {
                    int slotElegido = ocupadas.get(which);
                    mostrarDialogoGestionPartida(slotElegido);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Caso de Uso CU-04 (Paso 2):
     * Muestra el cuadro de diálogo de gestión con las opciones:
     * "Iniciar partida", "Copiar partida", "Borrar partida" y "Cancelar".
     */
    private void mostrarDialogoGestionPartida(int indice) {
        ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
        if (archivo == null) {
            return;
        }

        String[] opciones = {"Iniciar partida", "Copiar partida", "Borrar partida", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle("Gestionar: " + archivo.nombre)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        // Iniciar partida
                        if (adminArchivos.iniciarSesion(indice)) {
                            Toast.makeText(this, "Bienvenido de nuevo, " + adminArchivos.getArchivoActual().nombre, Toast.LENGTH_SHORT).show();
                            guardarSesionPersistente(indice);
                            iniciarJuego();
                        }
                    } else if (which == 1) {
                        // Flujo alternativo 3.1: Copiar partida
                        iniciarFlujoCopiarPartida(indice);
                    } else if (which == 2) {
                        // Flujo normal paso 3: Borrar partida
                        mostrarDialogoConfirmarBorrado(indice);
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Caso de Uso CU-04 (Flujo normal pasos 4, 5 y 6):
     * Muestra confirmación antes de eliminar el archivo .json y restablece el botón a su estado libre.
     */
    private void mostrarDialogoConfirmarBorrado(int indice) {
        ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
        if (archivo == null) {
            return;
        }
        String nombreBorrar = archivo.nombre;

        new AlertDialog.Builder(this)
                .setTitle("Borrar partida")
                .setMessage("¿Estás seguro de que deseas eliminar el archivo de guardado \"" + nombreBorrar + "\"?")
                .setPositiveButton("Borrar partida", (dialog, which) -> {
                    if (adminArchivos.borrarArchivo(indice)) {
                        actualizarTextosBotones();
                        Toast.makeText(this, "Archivo \"" + nombreBorrar + "\" eliminado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el archivo de guardado", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Caso de Uso CU-04 (Flujo alternativo 3.1 y 3.1.1):
     * Verifica ranuras libres y permite clonar la partida en otra ranura con un nuevo nombre.
     */
    private void iniciarFlujoCopiarPartida(int indiceOrigen) {
        ArchivoGuardado archivoOrigen = adminArchivos.getArchivo(indiceOrigen);
        if (archivoOrigen == null) {
            return;
        }

        List<Integer> ranurasLibres = adminArchivos.obtenerRanurasLibres(TOTAL_RANURAS_UI);

        // Flujo alternativo 3.1.1: No existen ranuras libres para copiar
        if (ranurasLibres.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sin espacios disponibles")
                    .setMessage("No hay espacios disponibles para copiar la partida. Debes borrar una partida antes de copiar.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        float densidad = getResources().getDisplayMetrics().density;
        int padH = (int) (20 * densidad);
        int padV = (int) (12 * densidad);

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(padH, padV, padH, padV);

        TextView tvElegirRanura = new TextView(this);
        tvElegirRanura.setText("Selecciona la ranura destino disponible:");
        tvElegirRanura.setTextColor(0xFF0D1B2A);
        tvElegirRanura.setTypeface(null, Typeface.BOLD);
        tvElegirRanura.setTextSize(14f);
        contenedor.addView(tvElegirRanura);

        final RadioGroup radioGroupRanuras = new RadioGroup(this);
        LinearLayout.LayoutParams paramsRg = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsRg.topMargin = (int) (6 * densidad);
        paramsRg.bottomMargin = (int) (12 * densidad);
        radioGroupRanuras.setLayoutParams(paramsRg);

        for (int i = 0; i < ranurasLibres.size(); i++) {
            int slotLibre = ranurasLibres.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(1000 + slotLibre);
            rb.setTag(slotLibre);
            rb.setText("Ranura " + (slotLibre + 1) + " (PARTIDA " + (slotLibre + 1) + " - Libre)");
            rb.setTextColor(0xFF0D1B2A);
            radioGroupRanuras.addView(rb);
            if (i == 0) {
                rb.setChecked(true);
            }
        }
        contenedor.addView(radioGroupRanuras);

        TextView tvNombreCopia = new TextView(this);
        tvNombreCopia.setText("Nombre para la copia:");
        tvNombreCopia.setTextColor(0xFF0D1B2A);
        tvNombreCopia.setTypeface(null, Typeface.BOLD);
        tvNombreCopia.setTextSize(14f);
        contenedor.addView(tvNombreCopia);

        final EditText inputNombreCopia = new EditText(this);
        inputNombreCopia.setHint("Escribe un nombre distinto para la copia");
        String sugerencia = archivoOrigen.nombre + "_copia";
        if (!adminArchivos.existeNombreArchivo(sugerencia)) {
            inputNombreCopia.setText(sugerencia);
            inputNombreCopia.setSelection(sugerencia.length());
        }
        contenedor.addView(inputNombreCopia);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Copiar partida: " + archivoOrigen.nombre)
                .setView(contenedor)
                .setPositiveButton("Copiar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btnConfirmarCopia = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnConfirmarCopia.setOnClickListener(v -> {
                String nuevoNombre = inputNombreCopia.getText().toString().trim();
                if (nuevoNombre.isEmpty()) {
                    inputNombreCopia.setError("Ingresa un nombre para la copia");
                    Toast.makeText(this, "El nombre de la copia no puede estar vacío", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Flujo alternativo 3.1.2: El nombre ingresado para la copia ya existe
                if (adminArchivos.existeNombreArchivo(nuevoNombre)) {
                    inputNombreCopia.setError("Este nombre ya está ocupado en otra ranura");
                    Toast.makeText(this,
                            "El nombre ingresado ya existe. Ingresa un nombre distinto que no esté ocupado.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                int checkedId = radioGroupRanuras.getCheckedRadioButtonId();
                RadioButton rbSeleccionado = radioGroupRanuras.findViewById(checkedId);
                int ranuraDestino = (rbSeleccionado != null && rbSeleccionado.getTag() instanceof Integer)
                        ? (Integer) rbSeleccionado.getTag()
                        : ranurasLibres.get(0);

                if (adminArchivos.copiarArchivo(indiceOrigen, ranuraDestino, nuevoNombre)) {
                    actualizarTextosBotones();
                    Toast.makeText(this,
                            "Partida copiada en Ranura " + (ranuraDestino + 1) + ": " + nuevoNombre,
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Error al copiar el archivo de guardado", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void mostrarDialogoCreacion(Button boton, int indice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear archivo de guardado");

        final EditText input = new EditText(this);
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
