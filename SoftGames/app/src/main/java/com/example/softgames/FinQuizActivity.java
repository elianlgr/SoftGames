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

/**
 * Pantalla de resultados finales del Modo Quiz (CU-S3-01: Superar el modo Quiz).
 * Paso 7 del flujo normal:
 * - Muestra la puntuación final general del usuario (1.0 a 10.0) junto con la puntuación
 *   y aciertos obtenidos en cada una de las categorías evaluadas.
 * - Registra y serializa automáticamente el progreso en el archivo .json activo.
 */
public class FinQuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_quiz);

        int dificultad = 1;
        int preguntasPorCategoria = 10;
        int aciertosTotales = 0;
        int totalReactivos = 30;
        double promedioGeneral = 0.0;
        ArrayList<String> categoriasEvaluadas = new ArrayList<>();
        int[] aciertosPorCategoria = null;
        double[] promediosPorCategoria = null;

        Intent intent = getIntent();
        if (intent != null) {
            dificultad = intent.getIntExtra("dificultad", 1);
            preguntasPorCategoria = intent.getIntExtra("preguntasPorCategoria", 10);
            aciertosTotales = intent.getIntExtra("aciertosTotales", 0);
            totalReactivos = intent.getIntExtra("totalReactivos", 30);
            promedioGeneral = intent.getDoubleExtra("promedioGeneral", 0.0);
            ArrayList<String> recibidas = intent.getStringArrayListExtra("categoriasEvaluadas");
            if (recibidas != null) {
                categoriasEvaluadas = recibidas;
            }
            aciertosPorCategoria = intent.getIntArrayExtra("aciertosPorCategoria");
            promediosPorCategoria = intent.getDoubleArrayExtra("promediosPorCategoria");
        }

        final int dificultadFinal = dificultad;
        final int pregPorCatFinal = preguntasPorCategoria;

        String textoDificultad;
        if (dificultad == 1) {
            textoDificultad = "Fácil";
        } else if (dificultad == 2) {
            textoDificultad = "Normal";
        } else {
            textoDificultad = "Difícil";
        }

        TextView tvSubtituloConfigQuiz = findViewById(R.id.tvSubtituloConfigQuiz);
        TextView tvBadgeRecordQuiz = findViewById(R.id.tvBadgeRecordQuiz);
        TextView tvPuntajeFinalQuiz = findViewById(R.id.tvPuntajeFinalQuiz);
        TextView tvAciertosTotalesQuiz = findViewById(R.id.tvAciertosTotalesQuiz);
        TextView tvDificultadEvaluadaQuiz = findViewById(R.id.tvDificultadEvaluadaQuiz);
        TextView tvRecordAnteriorQuiz = findViewById(R.id.tvRecordAnteriorQuiz);
        LinearLayout llDesgloseCategoriasQuiz = findViewById(R.id.llDesgloseCategoriasQuiz);
        TextView tvEstadoGuardadoQuiz = findViewById(R.id.tvEstadoGuardadoQuiz);
        Button btnJugarDeNuevoQuiz = findViewById(R.id.btnJugarDeNuevoQuiz);
        Button btnVolverMenuQuiz = findViewById(R.id.btnVolverMenuQuiz);

        tvSubtituloConfigQuiz.setText("Dificultad: " + textoDificultad
                + "  •  " + preguntasPorCategoria + " preguntas por categoría");
        tvPuntajeFinalQuiz.setText(String.format(Locale.US, "%.1f / 10", promedioGeneral));
        tvAciertosTotalesQuiz.setText(aciertosTotales + " / " + totalReactivos);
        tvDificultadEvaluadaQuiz.setText(textoDificultad);

        renderizarDesglosePorCategoria(
                llDesgloseCategoriasQuiz,
                categoriasEvaluadas,
                preguntasPorCategoria,
                aciertosPorCategoria,
                promediosPorCategoria
        );

        // Guardar automáticamente la puntuación del Quiz general y de cada categoría en el archivo .json
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
        String claveQuizGeneral = "Modo Quiz - General";
        double recordAnterior = stats.obtenerPuntuacion(claveQuizGeneral);
        tvRecordAnteriorQuiz.setText(String.format(Locale.US, "%.1f pts", recordAnterior));

        boolean esNuevoRecord = (promedioGeneral > recordAnterior) || (recordAnterior == 0.0 && promedioGeneral > 0.0);
        if (esNuevoRecord) {
            tvBadgeRecordQuiz.setText("★  ¡NUEVO RÉCORD EN QUIZ!");
        } else {
            tvBadgeRecordQuiz.setText("★  QUIZ COMPLETADO");
        }

        if (promedioGeneral >= recordAnterior) {
            stats.registrarPuntuacion(claveQuizGeneral, promedioGeneral);
        }

        if (categoriasEvaluadas != null && promediosPorCategoria != null) {
            for (int i = 0; i < categoriasEvaluadas.size() && i < promediosPorCategoria.length; i++) {
                String claveCatQuiz = categoriasEvaluadas.get(i) + " - Modo Quiz";
                double prevCat = stats.obtenerPuntuacion(claveCatQuiz);
                if (promediosPorCategoria[i] >= prevCat) {
                    stats.registrarPuntuacion(claveCatQuiz, promediosPorCategoria[i]);
                }
            }
        }

        boolean guardadoExitoso = stats.guardar(this);
        if (guardadoExitoso) {
            String htmlGuardado = "Puntuación guardada automáticamente en <b>" + nombrePartidaMostrar + "</b>";
            tvEstadoGuardadoQuiz.setText(Html.fromHtml(htmlGuardado, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvEstadoGuardadoQuiz.setText("Aviso: Puntuación conservada en memoria de la sesión activa");
            Toast.makeText(this,
                    "No se pudo escribir el archivo .json. La puntuación se conserva en memoria durante la sesión activa.",
                    Toast.LENGTH_LONG).show();
        }

        btnJugarDeNuevoQuiz.setOnClickListener(v -> {
            Intent nuevoQuiz = new Intent(FinQuizActivity.this, ModoQuizActivity.class);
            nuevoQuiz.putExtra("dificultad", dificultadFinal);
            nuevoQuiz.putExtra("preguntasPorCategoria", pregPorCatFinal);
            startActivity(nuevoQuiz);
            finish();
        });

        btnVolverMenuQuiz.setOnClickListener(v -> finish());
    }

    private void renderizarDesglosePorCategoria(
            LinearLayout contenedor,
            ArrayList<String> categorias,
            int preguntasPorCategoria,
            int[] aciertos,
            double[] promedios) {

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
            int cantAciertos = (aciertos != null && i < aciertos.length) ? aciertos[i] : 0;
            double promCat = (promedios != null && i < promedios.length) ? promedios[i] : 0.0;

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

            // Columna izquierda: Nombre de la categoría + Aciertos en esa categoría
            LinearLayout colIzq = new LinearLayout(this);
            colIzq.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams paramsIzq = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            colIzq.setLayoutParams(paramsIzq);

            TextView tvNombreCat = new TextView(this);
            tvNombreCat.setText((i + 1) + ". " + nombreCat);
            tvNombreCat.setTextColor(0xFF0D1B2A);
            tvNombreCat.setTextSize(13.5f);
            tvNombreCat.setTypeface(null, Typeface.BOLD);

            String textoAciertos = "Aciertos: " + cantAciertos + " / " + preguntasPorCategoria;
            TextView tvAciertosCat = new TextView(this);
            tvAciertosCat.setText(textoAciertos);
            tvAciertosCat.setTextColor(0xFF415A77);
            tvAciertosCat.setTextSize(12f);

            colIzq.addView(tvNombreCat);
            colIzq.addView(tvAciertosCat);

            // Insignia derecha: Puntuación promedio de esa categoría
            TextView tvPromedioCat = new TextView(this);
            tvPromedioCat.setText(String.format(Locale.US, "%.1f / 10", promCat));
            tvPromedioCat.setTextSize(12.5f);
            tvPromedioCat.setTypeface(null, Typeface.BOLD);
            tvPromedioCat.setGravity(Gravity.CENTER);
            int badgePadH = (int) (10 * densidad);
            int badgePadV = (int) (4 * densidad);
            tvPromedioCat.setPadding(badgePadH, badgePadV, badgePadH, badgePadV);

            if (cantAciertos > 0) {
                tvPromedioCat.setBackgroundResource(R.drawable.bg_badge_verde);
                tvPromedioCat.setTextColor(0xFFFFFFFF);
            } else {
                tvPromedioCat.setBackgroundResource(R.drawable.bg_chip_unselected);
                tvPromedioCat.setTextColor(0xFF415A77);
            }

            fila.addView(colIzq);
            fila.addView(tvPromedioCat);
            contenedor.addView(fila);
        }
    }
}
