package com.example.softgames;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class FinMaratonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_maraton);

        double puntajeTotal = 0.0;
        int mejorRacha = 0;
        int totalAciertos = 0;
        int dificultadAlcanzada = 1;
        int erroresAcumulados = 3;
        boolean modoGeneral = true;
        ArrayList<String> categoriasSeleccionadas = new ArrayList<>();
        ArrayList<String> categoriasResumen = new ArrayList<>();
        int[] resueltosPorCategoria = null;
        double[] puntosPorCategoria = null;

        Intent intent = getIntent();
        if (intent != null) {
            puntajeTotal = intent.getDoubleExtra("puntajeTotal", 0.0);
            mejorRacha = intent.getIntExtra("mejorRacha", 0);
            totalAciertos = intent.getIntExtra("totalAciertos", 0);
            dificultadAlcanzada = intent.getIntExtra("dificultadAlcanzada", 1);
            erroresAcumulados = intent.getIntExtra("erroresAcumulados", 3);
            modoGeneral = intent.getBooleanExtra("modoGeneral", true);
            ArrayList<String> recibidas = intent.getStringArrayListExtra("categoriasSeleccionadas");
            if (recibidas != null) {
                categoriasSeleccionadas = recibidas;
            }
            ArrayList<String> resumenRecibido = intent.getStringArrayListExtra("categoriasResumen");
            if (resumenRecibido != null) {
                categoriasResumen = resumenRecibido;
            }
            resueltosPorCategoria = intent.getIntArrayExtra("resueltosPorCategoria");
            puntosPorCategoria = intent.getDoubleArrayExtra("puntosPorCategoria");
        }

        final boolean modoGeneralFinal = modoGeneral;
        final ArrayList<String> categoriasFinal = categoriasSeleccionadas;

        String textoDificultad;
        if (dificultadAlcanzada == 1) {
            textoDificultad = "Fácil";
        } else if (dificultadAlcanzada == 2) {
            textoDificultad = "Normal";
        } else {
            textoDificultad = "Difícil";
        }

        TextView tvSubtituloErroresMaraton = findViewById(R.id.tvSubtituloErroresMaraton);
        TextView tvBadgeRecordMaraton = findViewById(R.id.tvBadgeRecordMaraton);
        TextView tvPuntajeTotalMaraton = findViewById(R.id.tvPuntajeTotalMaraton);
        TextView tvRachaLogradaMaraton = findViewById(R.id.tvRachaLogradaMaraton);
        TextView tvDificultadAlcanzadaMaraton = findViewById(R.id.tvDificultadAlcanzadaMaraton);
        TextView tvRecordAnteriorMaraton = findViewById(R.id.tvRecordAnteriorMaraton);
        TextView tvTituloDesgloseMaraton = findViewById(R.id.tvTituloDesgloseMaraton);
        LinearLayout llDesgloseCategoriasMaraton = findViewById(R.id.llDesgloseCategoriasMaraton);
        TextView tvEstadoGuardadoMaraton = findViewById(R.id.tvEstadoGuardadoMaraton);
        Button btnJugarDeNuevoMaraton = findViewById(R.id.btnJugarDeNuevoMaraton);
        Button btnVolverMenuMaraton = findViewById(R.id.btnVolverMenuMaraton);

        if (erroresAcumulados >= 3) {
            tvSubtituloErroresMaraton.setText("3 errores acumulados  •  ✕  ✕  ✕");
        } else {
            tvSubtituloErroresMaraton.setText("¡Banco completado!  •  Errores: " + erroresAcumulados + " / 3");
        }

        tvPuntajeTotalMaraton.setText(String.format(Locale.US, "%.1f pts", puntajeTotal));
        int rachaMostrar = Math.max(mejorRacha, totalAciertos);
        tvRachaLogradaMaraton.setText(rachaMostrar + (rachaMostrar == 1 ? " acierto" : " aciertos"));
        tvDificultadAlcanzadaMaraton.setText(textoDificultad);

        if (tvTituloDesgloseMaraton != null) {
            tvTituloDesgloseMaraton.setText(modoGeneral
                    ? "PUNTOS Y PROBLEMAS POR CATEGORÍA (MODO GENERAL)"
                    : "PUNTOS Y PROBLEMAS POR CATEGORÍA SELECCIONADA");
        }

        renderizarDesglosePorCategoria(
                llDesgloseCategoriasMaraton,
                categoriasResumen,
                resueltosPorCategoria,
                puntosPorCategoria
        );

        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        int slotActivo = prefs.getInt("slotActivo", 0);
        String usuarioActual = prefs.getString("usuarioActual", "default");

        AdministradorArchivos admin = new AdministradorArchivos(this, usuarioActual);
        ArchivoGuardado archivoActivo = null;
        if (admin.iniciarSesion(slotActivo)) {
            archivoActivo = admin.getArchivoActual();
        }
        if (archivoActivo == null) {
            archivoActivo = new ArchivoGuardado(slotActivo, "PARTIDA " + (slotActivo + 1), usuarioActual);
        }

        String nombrePartidaMostrar = (archivoActivo.nombre != null && !archivoActivo.nombre.trim().isEmpty())
                ? archivoActivo.nombre.trim().toUpperCase(Locale.ROOT)
                : "PARTIDA " + (slotActivo + 1);

        Estadisticas stats = new Estadisticas(archivoActivo);
        String claveMaraton = "Maratón - Récord";
        double recordAnterior = stats.obtenerPuntuacion(claveMaraton);

        tvRecordAnteriorMaraton.setText(String.format(Locale.US, "%.1f pts", recordAnterior));

        boolean esNuevoRecord = (puntajeTotal > recordAnterior) || (recordAnterior == 0.0 && puntajeTotal > 0.0);
        if (esNuevoRecord) {
            tvBadgeRecordMaraton.setText("★  ¡NUEVO RÉCORD!");
        } else {
            tvBadgeRecordMaraton.setText("★  MARATÓN FINALIZADO");
        }

        if (puntajeTotal >= recordAnterior) {
            stats.registrarPuntuacion(claveMaraton, puntajeTotal);
        }
        boolean guardadoExitoso = stats.guardar(this);

        if (guardadoExitoso) {
            String htmlGuardado = "Récord guardado automáticamente en <b>" + nombrePartidaMostrar + "</b>";
            tvEstadoGuardadoMaraton.setText(Html.fromHtml(htmlGuardado, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvEstadoGuardadoMaraton.setText("Aviso: Récord conservado en memoria de la sesión activa");
            Toast.makeText(this,
                    "No se pudo escribir el archivo .json. El puntaje se conserva en memoria durante la sesión activa.",
                    Toast.LENGTH_LONG).show();
        }

        btnJugarDeNuevoMaraton.setOnClickListener(v -> {
            Intent nuevoMaraton = new Intent(FinMaratonActivity.this, ModoMaratonActivity.class);
            nuevoMaraton.putExtra("modoGeneral", modoGeneralFinal);
            nuevoMaraton.putStringArrayListExtra("categoriasSeleccionadas", categoriasFinal);
            startActivity(nuevoMaraton);
            finish();
        });

        btnVolverMenuMaraton.setOnClickListener(v -> finish());
    }

    /**
     * Paso 9 del flujo normal:
     * Muestra la cantidad de puntos acumulados y la cantidad de problemas resueltos
     * en cada categoría seleccionada.
     */
    private void renderizarDesglosePorCategoria(
            LinearLayout contenedor,
            ArrayList<String> categorias,
            int[] resueltos,
            double[] puntos) {

        if (contenedor == null) return;
        contenedor.removeAllViews();

        if (categorias == null || categorias.isEmpty()) {
            categorias = new ArrayList<>();
            categorias.add("Lógica");
            categorias.add("Programación");
            categorias.add("Análisis y diseño de algoritmos");
        }

        float densidad = getResources().getDisplayMetrics().density;

        for (int i = 0; i < categorias.size(); i++) {
            String nombreCat = categorias.get(i);
            int cantResueltos = (resueltos != null && i < resueltos.length) ? resueltos[i] : 0;
            double cantPuntos = (puntos != null && i < puntos.length) ? puntos[i] : 0.0;

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setBackgroundResource(R.drawable.bg_pill_crema);
            int padH = (int) (12 * densidad);
            int padV = (int) (9 * densidad);
            fila.setPadding(padH, padV, padH, padV);

            LinearLayout.LayoutParams paramsFila = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            paramsFila.bottomMargin = (int) (6 * densidad);
            fila.setLayoutParams(paramsFila);

            // Columna izquierda: Nombre de la categoría + Problemas resueltos
            LinearLayout colIzq = new LinearLayout(this);
            colIzq.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams paramsIzq = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            colIzq.setLayoutParams(paramsIzq);

            TextView tvNombreCat = new TextView(this);
            tvNombreCat.setText(nombreCat);
            tvNombreCat.setTextColor(0xFF0D1B2A);
            tvNombreCat.setTextSize(13.5f);
            tvNombreCat.setTypeface(null, Typeface.BOLD);

            String textoProblemas = cantResueltos + (cantResueltos == 1 ? " problema resuelto" : " problemas resueltos");
            TextView tvProblemasCat = new TextView(this);
            tvProblemasCat.setText(textoProblemas);
            tvProblemasCat.setTextColor(0xFF415A77);
            tvProblemasCat.setTextSize(12f);

            colIzq.addView(tvNombreCat);
            colIzq.addView(tvProblemasCat);

            // Insignia derecha: Puntos acumulados en esa categoría
            TextView tvPuntosCat = new TextView(this);
            tvPuntosCat.setText(String.format(Locale.US, "%.1f pts", cantPuntos));
            tvPuntosCat.setTextSize(12.5f);
            tvPuntosCat.setTypeface(null, Typeface.BOLD);
            tvPuntosCat.setGravity(Gravity.CENTER);
            int badgePadH = (int) (10 * densidad);
            int badgePadV = (int) (4 * densidad);
            tvPuntosCat.setPadding(badgePadH, badgePadV, badgePadH, badgePadV);

            if (cantResueltos > 0) {
                tvPuntosCat.setBackgroundResource(R.drawable.bg_badge_verde);
                tvPuntosCat.setTextColor(0xFFFFFFFF);
            } else {
                tvPuntosCat.setBackgroundResource(R.drawable.bg_chip_unselected);
                tvPuntosCat.setTextColor(0xFF415A77);
            }

            fila.addView(colIzq);
            fila.addView(tvPuntosCat);
            contenedor.addView(fila);
        }
    }
}
