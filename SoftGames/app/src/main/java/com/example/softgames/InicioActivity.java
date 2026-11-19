package com.example.softgames;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
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
            // Ranura con partida creada -> color azul marino
            boton.setText(archivo.nombre.toUpperCase());
            boton.setBackgroundResource(R.drawable.bg_btn_continuar);
        } else {
            // Ranura vacía (o recién eliminada) -> color verde salvia en todas las ranuras
            boton.setText("PARTIDA " + (indice + 1));
            boton.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
        }
        boton.setTextColor(0xFFFFFFFF);
    }

    private void configurarBoton(Button boton, int indice) {
        // Clic corto normal: lleva directamente al archivo de guardado (o crea uno nuevo si la ranura está vacía)
        boton.setOnClickListener(v -> {
            if (adminArchivos.iniciarSesion(indice)) {
                Toast.makeText(this, "Bienvenido de nuevo, " + adminArchivos.getArchivoActual().nombre, Toast.LENGTH_SHORT).show();
                guardarSesionPersistente(indice);
                iniciarJuego();
            } else {
                mostrarDialogoCreacion(boton, indice);
            }
        });

        // Caso de Uso CU-04 (Paso 1): Dejar presionado por más tiempo el botón de una partida existente abre el menú
        boton.setOnLongClickListener(v -> {
            ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
            if (archivo != null && archivo.nombre != null && !archivo.nombre.trim().isEmpty()) {
                mostrarDialogoGestionPartida(indice);
            } else {
                Toast.makeText(this, "Partida vacía. Tócala para crear una nueva partida.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    /**
     * Crea una ventana modal personalizada con la estética y paleta de colores de SoftGames:
     * Crema cálido (#E9DFCB / #FAF5EC), Arena (#D8C4A5), Azul Marino (#142232 / #0D1B2A) y Verde Salvia (#708274).
     */
    private Dialog crearDialogoEstilizado(LinearLayout contenidoTarjeta) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contenidoTarjeta);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int anchoPantalla = getResources().getDisplayMetrics().widthPixels;
            int anchoDialogo = (int) (anchoPantalla * 0.88f);
            dialog.getWindow().setLayout(anchoDialogo, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    private LinearLayout crearContenedorBaseDialogo() {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setGravity(Gravity.CENTER_HORIZONTAL);
        tarjeta.setBackgroundResource(R.drawable.bg_card_ajustes);
        int padH = dp(22);
        int padV = dp(24);
        tarjeta.setPadding(padH, padV, padH, padV);
        return tarjeta;
    }

    private AppCompatButton crearBotonEstilizado(String texto, int drawableRes, int colorTexto, int margenInferiorDp) {
        AppCompatButton btn = new AppCompatButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        if (margenInferiorDp > 0) {
            params.bottomMargin = dp(margenInferiorDp);
        }
        btn.setLayoutParams(params);
        btn.setText(texto);
        btn.setTextColor(colorTexto);
        btn.setTextSize(14.5f);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setAllCaps(false);
        btn.setBackgroundResource(drawableRes);
        return btn;
    }

    /**
     * Caso de Uso CU-04 (Paso 2):
     * Menú emergente con la paleta de colores de SoftGames al dejar presionada una partida existente.
     */
    private void mostrarDialogoGestionPartida(int indice) {
        ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
        if (archivo == null) {
            return;
        }

        LinearLayout tarjeta = crearContenedorBaseDialogo();

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Gestionar: " + archivo.nombre);
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(21f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvSubtitulo = new TextView(this);
        tvSubtitulo.setText("PARTIDA " + (indice + 1) + " • Selecciona una acción");
        tvSubtitulo.setTextColor(0xFF415A77);
        tvSubtitulo.setTextSize(13f);
        tvSubtitulo.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsSub = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsSub.topMargin = dp(4);
        paramsSub.bottomMargin = dp(18);
        tvSubtitulo.setLayoutParams(paramsSub);
        tarjeta.addView(tvSubtitulo);

        Dialog dialog = crearDialogoEstilizado(tarjeta);

        // Opción 1: Copiar partida (Verde salvia #708274)
        AppCompatButton btnCopiar = crearBotonEstilizado(
                "COPIAR PARTIDA", R.drawable.bg_btn_crear_cuenta, 0xFFFFFFFF, 12);
        btnCopiar.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarFlujoCopiarPartida(indice);
        });
        tarjeta.addView(btnCopiar);

        // Opción 2: Borrar partida (Terracota / Rojo elegante #BA3A3A)
        AppCompatButton btnBorrar = crearBotonEstilizado(
                "BORRAR PARTIDA", R.drawable.bg_btn_cerrar_sesion, 0xFFFFFFFF, 12);
        btnBorrar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogoConfirmarBorrado(indice);
        });
        tarjeta.addView(btnBorrar);

        // Opción 3: Cancelar (Arena #D8C4A5 con texto Azul Marino #142232)
        AppCompatButton btnCancelar = crearBotonEstilizado(
                "CANCELAR", R.drawable.bg_btn_ajustes, 0xFF142232, 0);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        tarjeta.addView(btnCancelar);

        dialog.show();
    }

    /**
     * Caso de Uso CU-04 (Flujo normal pasos 4, 5 y 6):
     * Muestra confirmación con la estética de SoftGames antes de eliminar el archivo .json.
     */
    private void mostrarDialogoConfirmarBorrado(int indice) {
        ArchivoGuardado archivo = adminArchivos.getArchivo(indice);
        if (archivo == null) {
            return;
        }
        String nombreBorrar = archivo.nombre;

        LinearLayout tarjeta = crearContenedorBaseDialogo();

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Borrar partida");
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(21f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvMensaje = new TextView(this);
        tvMensaje.setText("¿Estás seguro de que deseas eliminar la partida \""
                + nombreBorrar + "\"?");
        tvMensaje.setTextColor(0xFF0D1B2A);
        tvMensaje.setTextSize(14f);
        tvMensaje.setGravity(Gravity.CENTER);
        tvMensaje.setBackgroundResource(R.drawable.bg_categoria_pill);
        tvMensaje.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams paramsMsg = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsMsg.topMargin = dp(14);
        paramsMsg.bottomMargin = dp(20);
        tvMensaje.setLayoutParams(paramsMsg);
        tarjeta.addView(tvMensaje);

        Dialog dialog = crearDialogoEstilizado(tarjeta);

        LinearLayout filaBotones = new LinearLayout(this);
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);
        filaBotones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        AppCompatButton btnCancelar = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        paramsCancelar.rightMargin = dp(6);
        btnCancelar.setLayoutParams(paramsCancelar);
        btnCancelar.setText("CANCELAR");
        btnCancelar.setTextColor(0xFF142232);
        btnCancelar.setTextSize(14f);
        btnCancelar.setTypeface(null, Typeface.BOLD);
        btnCancelar.setBackgroundResource(R.drawable.bg_btn_ajustes);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        AppCompatButton btnConfirmarBorrar = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsBorrar = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        paramsBorrar.leftMargin = dp(6);
        btnConfirmarBorrar.setLayoutParams(paramsBorrar);
        btnConfirmarBorrar.setText("BORRAR");
        btnConfirmarBorrar.setTextColor(0xFFFFFFFF);
        btnConfirmarBorrar.setTextSize(14f);
        btnConfirmarBorrar.setTypeface(null, Typeface.BOLD);
        btnConfirmarBorrar.setBackgroundResource(R.drawable.bg_btn_cerrar_sesion);
        btnConfirmarBorrar.setOnClickListener(v -> {
            if (adminArchivos.borrarArchivo(indice)) {
                actualizarTextosBotones();
                Toast.makeText(this, "Partida \"" + nombreBorrar + "\" eliminada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo eliminar la partida", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        filaBotones.addView(btnCancelar);
        filaBotones.addView(btnConfirmarBorrar);
        tarjeta.addView(filaBotones);

        dialog.show();
    }

    /**
     * Caso de Uso CU-04 (Flujo alternativo 3.1 y 3.1.1):
     * Ventana personalizada con la estética de SoftGames para elegir partida destino y nombre de copia.
     */
    private void iniciarFlujoCopiarPartida(int indiceOrigen) {
        ArchivoGuardado archivoOrigen = adminArchivos.getArchivo(indiceOrigen);
        if (archivoOrigen == null) {
            return;
        }

        List<Integer> ranurasLibres = adminArchivos.obtenerRanurasLibres(TOTAL_RANURAS_UI);

        // Flujo alternativo 3.1.1: No existen espacios libres para copiar
        if (ranurasLibres.isEmpty()) {
            mostrarDialogoAvisoSinEspacio();
            return;
        }

        LinearLayout tarjeta = crearContenedorBaseDialogo();

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Copiar partida: " + archivoOrigen.nombre);
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(20f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvElegirRanura = new TextView(this);
        tvElegirRanura.setText("Selecciona la partida destino:");
        tvElegirRanura.setTextColor(0xFF142232);
        tvElegirRanura.setTypeface(null, Typeface.BOLD);
        tvElegirRanura.setTextSize(14f);
        LinearLayout.LayoutParams paramsLabel1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsLabel1.topMargin = dp(16);
        paramsLabel1.bottomMargin = dp(8);
        tvElegirRanura.setLayoutParams(paramsLabel1);
        tarjeta.addView(tvElegirRanura);

        // Lista de tarjetas seleccionables con la paleta de SoftGames (Verde Salvia #708274 vs Crema #FAF5EC)
        final int[] ranuraSeleccionada = {ranurasLibres.get(0)};
        final List<TextView> chipsRanuras = new ArrayList<>();

        for (int i = 0; i < ranurasLibres.size(); i++) {
            final int slotLibre = ranurasLibres.get(i);
            TextView chipRanura = new TextView(this);
            LinearLayout.LayoutParams paramsChip = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(46)
            );
            paramsChip.bottomMargin = dp(8);
            chipRanura.setLayoutParams(paramsChip);
            chipRanura.setGravity(Gravity.CENTER_VERTICAL);
            chipRanura.setPadding(dp(16), 0, dp(16), 0);
            chipRanura.setTextSize(14f);
            chipRanura.setTypeface(null, Typeface.BOLD);
            chipRanura.setTag(slotLibre);

            chipRanura.setOnClickListener(v -> {
                ranuraSeleccionada[0] = slotLibre;
                actualizarEstiloChipsRanura(chipsRanuras, slotLibre);
            });

            chipsRanuras.add(chipRanura);
            tarjeta.addView(chipRanura);
        }
        actualizarEstiloChipsRanura(chipsRanuras, ranuraSeleccionada[0]);

        TextView tvNombreCopia = new TextView(this);
        tvNombreCopia.setText("Nombre para la copia:");
        tvNombreCopia.setTextColor(0xFF142232);
        tvNombreCopia.setTypeface(null, Typeface.BOLD);
        tvNombreCopia.setTextSize(14f);
        LinearLayout.LayoutParams paramsLabel2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsLabel2.topMargin = dp(8);
        paramsLabel2.bottomMargin = dp(8);
        tvNombreCopia.setLayoutParams(paramsLabel2);
        tarjeta.addView(tvNombreCopia);

        final EditText inputNombreCopia = new EditText(this);
        LinearLayout.LayoutParams paramsInput = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        paramsInput.bottomMargin = dp(20);
        inputNombreCopia.setLayoutParams(paramsInput);
        inputNombreCopia.setHint("Escribe un nombre distinto");
        inputNombreCopia.setHintTextColor(0xFF7A6F5D);
        inputNombreCopia.setTextColor(0xFF142232);
        inputNombreCopia.setTextSize(15f);
        inputNombreCopia.setSingleLine(true);
        inputNombreCopia.setBackgroundResource(R.drawable.bg_input_preguntas);
        inputNombreCopia.setPadding(dp(16), 0, dp(16), 0);

        String sugerencia = archivoOrigen.nombre + "_copia";
        if (!adminArchivos.existeNombreArchivo(sugerencia)) {
            inputNombreCopia.setText(sugerencia);
            inputNombreCopia.setSelection(sugerencia.length());
        }
        tarjeta.addView(inputNombreCopia);

        Dialog dialog = crearDialogoEstilizado(tarjeta);

        LinearLayout filaBotones = new LinearLayout(this);
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);
        filaBotones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        AppCompatButton btnCancelar = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        paramsCancelar.rightMargin = dp(6);
        btnCancelar.setLayoutParams(paramsCancelar);
        btnCancelar.setText("CANCELAR");
        btnCancelar.setTextColor(0xFF142232);
        btnCancelar.setTextSize(14f);
        btnCancelar.setTypeface(null, Typeface.BOLD);
        btnCancelar.setBackgroundResource(R.drawable.bg_btn_ajustes);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        AppCompatButton btnConfirmarCopia = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsCopiar = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        paramsCopiar.leftMargin = dp(6);
        btnConfirmarCopia.setLayoutParams(paramsCopiar);
        btnConfirmarCopia.setText("COPIAR");
        btnConfirmarCopia.setTextColor(0xFFFFFFFF);
        btnConfirmarCopia.setTextSize(14f);
        btnConfirmarCopia.setTypeface(null, Typeface.BOLD);
        btnConfirmarCopia.setBackgroundResource(R.drawable.bg_btn_continuar);
        btnConfirmarCopia.setOnClickListener(v -> {
            String nuevoNombre = inputNombreCopia.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                inputNombreCopia.setError("Ingresa un nombre para la copia");
                Toast.makeText(this, "El nombre de la copia no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            // Flujo alternativo 3.1.2: El nombre ingresado para la copia ya existe
            if (adminArchivos.existeNombreArchivo(nuevoNombre)) {
                inputNombreCopia.setError("Este nombre ya está ocupado en otra partida");
                Toast.makeText(this,
                        "El nombre ingresado ya existe. Ingresa un nombre distinto que no esté ocupado.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            int ranuraDestino = ranuraSeleccionada[0];
            if (adminArchivos.copiarArchivo(indiceOrigen, ranuraDestino, nuevoNombre)) {
                actualizarTextosBotones();
                Toast.makeText(this,
                        "Partida copiada en PARTIDA " + (ranuraDestino + 1) + ": " + nuevoNombre,
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Error al copiar la partida", Toast.LENGTH_SHORT).show();
            }
        });

        filaBotones.addView(btnCancelar);
        filaBotones.addView(btnConfirmarCopia);
        tarjeta.addView(filaBotones);

        dialog.show();
    }

    private void actualizarEstiloChipsRanura(List<TextView> chips, int slotSeleccionado) {
        for (TextView chip : chips) {
            int slot = (Integer) chip.getTag();
            boolean activo = (slot == slotSeleccionado);
            String textoBase = "PARTIDA " + (slot + 1);
            if (activo) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(0xFFFFFFFF);
                chip.setText("✓   " + textoBase);
            } else {
                chip.setBackgroundResource(R.drawable.bg_categoria_pill);
                chip.setTextColor(0xFF142232);
                chip.setText("○   " + textoBase);
            }
        }
    }

    private void mostrarDialogoAvisoSinEspacio() {
        LinearLayout tarjeta = crearContenedorBaseDialogo();

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Sin espacios disponibles");
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(20f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvMensaje = new TextView(this);
        tvMensaje.setText("Todas las partidas están ocupadas.\nDebes borrar una partida antes de poder crear una copia.");
        tvMensaje.setTextColor(0xFF0D1B2A);
        tvMensaje.setTextSize(14f);
        tvMensaje.setGravity(Gravity.CENTER);
        tvMensaje.setBackgroundResource(R.drawable.bg_categoria_pill);
        tvMensaje.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams paramsMsg = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsMsg.topMargin = dp(14);
        paramsMsg.bottomMargin = dp(18);
        tvMensaje.setLayoutParams(paramsMsg);
        tarjeta.addView(tvMensaje);

        Dialog dialog = crearDialogoEstilizado(tarjeta);

        AppCompatButton btnAceptar = crearBotonEstilizado(
                "ENTENDIDO", R.drawable.bg_btn_continuar, 0xFFFFFFFF, 0);
        btnAceptar.setOnClickListener(v -> dialog.dismiss());
        tarjeta.addView(btnAceptar);

        dialog.show();
    }

    private void mostrarDialogoCreacion(Button boton, int indice) {
        LinearLayout tarjeta = crearContenedorBaseDialogo();

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Crear partida");
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(20f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvSub = new TextView(this);
        tvSub.setText("PARTIDA " + (indice + 1) + " • Ingresa el nombre de tu partida");
        tvSub.setTextColor(0xFF415A77);
        tvSub.setTextSize(13f);
        tvSub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsSub = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsSub.topMargin = dp(4);
        paramsSub.bottomMargin = dp(16);
        tvSub.setLayoutParams(paramsSub);
        tarjeta.addView(tvSub);

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams paramsInput = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        paramsInput.bottomMargin = dp(20);
        input.setLayoutParams(paramsInput);
        input.setHint("Escribe el nombre de la partida");
        input.setHintTextColor(0xFF7A6F5D);
        input.setTextColor(0xFF142232);
        input.setTextSize(15f);
        input.setSingleLine(true);
        input.setBackgroundResource(R.drawable.bg_input_preguntas);
        input.setPadding(dp(16), 0, dp(16), 0);
        tarjeta.addView(input);

        Dialog dialog = crearDialogoEstilizado(tarjeta);

        LinearLayout filaBotones = new LinearLayout(this);
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);
        filaBotones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        AppCompatButton btnCancelar = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        paramsCancelar.rightMargin = dp(6);
        btnCancelar.setLayoutParams(paramsCancelar);
        btnCancelar.setText("CANCELAR");
        btnCancelar.setTextColor(0xFF142232);
        btnCancelar.setTextSize(14f);
        btnCancelar.setTypeface(null, Typeface.BOLD);
        btnCancelar.setBackgroundResource(R.drawable.bg_btn_ajustes);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        AppCompatButton btnContinuar = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsContinuar = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        paramsContinuar.leftMargin = dp(6);
        btnContinuar.setLayoutParams(paramsContinuar);
        btnContinuar.setText("CONTINUAR");
        btnContinuar.setTextColor(0xFFFFFFFF);
        btnContinuar.setTextSize(14f);
        btnContinuar.setTypeface(null, Typeface.BOLD);
        btnContinuar.setBackgroundResource(R.drawable.bg_btn_continuar);
        btnContinuar.setOnClickListener(v -> {
            String nombreNuevo = input.getText().toString().trim();
            if (nombreNuevo.isEmpty()) {
                input.setError("El nombre no puede estar vacío");
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (adminArchivos.crearArchivo(nombreNuevo, indice)) {
                actualizarTextosBotones();
                Toast.makeText(this, "Partida creada con éxito: " + nombreNuevo, Toast.LENGTH_SHORT).show();
                guardarSesionPersistente(indice);
                dialog.dismiss();
                iniciarJuego();
            } else {
                input.setError("Este nombre ya existe");
                Toast.makeText(this, "El nombre de la partida ya existe, escribe otro nombre", Toast.LENGTH_LONG).show();
            }
        });

        filaBotones.addView(btnCancelar);
        filaBotones.addView(btnContinuar);
        tarjeta.addView(filaBotones);

        dialog.show();
    }

    private void guardarSesionPersistente(int indice) {
        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("slotActivo", indice);
        editor.putString("usuarioActual", adminArchivos.getUsuario());
        editor.apply();
    }

    private void iniciarJuego() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private int dp(int valorDp) {
        return Math.round(valorDp * getResources().getDisplayMetrics().density);
    }
}
