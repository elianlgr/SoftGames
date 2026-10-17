package com.example.softgames;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EstadisticasActivity extends AppCompatActivity {

    private static class InfoCategoria {
        final String nombre;
        final int iconoRes;

        InfoCategoria(String nombre, int iconoRes) {
            this.nombre = nombre;
            this.iconoRes = iconoRes;
        }
    }

    private LinearLayout contenedorTarjetasCategorias;
    private TextView tvArchivoActivoEstadisticas;
    private TextView tvPromedioGeneralEstadisticas;
    private TextView tvCategoriasJugadasEstadisticas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        View btnVolverTop = findViewById(R.id.btnVolverEstadisticasTop);
        Button btnVolverBottom = findViewById(R.id.btnVolverEstadisticasBottom);
        contenedorTarjetasCategorias = findViewById(R.id.contenedorTarjetasCategorias);
        tvArchivoActivoEstadisticas = findViewById(R.id.tvArchivoActivoEstadisticas);
        tvPromedioGeneralEstadisticas = findViewById(R.id.tvPromedioGeneralEstadisticas);
        tvCategoriasJugadasEstadisticas = findViewById(R.id.tvCategoriasJugadasEstadisticas);

        if (btnVolverTop != null) {
            btnVolverTop.setOnClickListener(v -> finish());
        }
        if (btnVolverBottom != null) {
            btnVolverBottom.setOnClickListener(v -> finish());
        }

        cargarYMostrarEstadisticas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarYMostrarEstadisticas();
    }

    private void cargarYMostrarEstadisticas() {
        // Flujo normal paso 5: Deserializar los datos del archivo activo y mostrar el historial
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
            String nombreArchivoJson = ArchivoGuardado.obtenerNombreArchivo(usuarioActual, slotActivo);
            archivoActivo.deserializar(this, nombreArchivoJson);
        }

        String nombrePartidaMostrar = (archivoActivo.nombre != null && !archivoActivo.nombre.trim().isEmpty())
                ? archivoActivo.nombre.trim().toUpperCase(Locale.ROOT)
                : "PARTIDA " + (slotActivo + 1);

        String htmlArchivo = "Archivo activo: <b>" + nombrePartidaMostrar + "</b>";
        tvArchivoActivoEstadisticas.setText(Html.fromHtml(htmlArchivo, Html.FROM_HTML_MODE_LEGACY));

        Estadisticas stats = new Estadisticas(archivoActivo);

        List<InfoCategoria> categoriasBase = obtenerCategoriasOficiales();
        List<InfoCategoria> categoriasConPartidas = new ArrayList<>();
        List<InfoCategoria> categoriasSinPartidas = new ArrayList<>();

        double sumaPromediosCategorias = 0.0;
        int totalCategoriasJugadas = 0;

        for (InfoCategoria cat : categoriasBase) {
            Map<String, Double> minijuegosCat = stats.obtenerMinijuegosPorCategoria(cat.nombre);
            if (!minijuegosCat.isEmpty()) {
                categoriasConPartidas.add(cat);
                totalCategoriasJugadas++;
                double mejorPuntajeCat = calcularPuntajeRepresentativo(minijuegosCat);
                sumaPromediosCategorias += mejorPuntajeCat;
            } else {
                categoriasSinPartidas.add(cat);
            }
        }

        double promedioGeneral = totalCategoriasJugadas > 0
                ? (sumaPromediosCategorias / totalCategoriasJugadas)
                : 0.0;

        tvPromedioGeneralEstadisticas.setText(
                String.format(Locale.US, "Promedio General: %.1f pts", promedioGeneral));
        tvCategoriasJugadasEstadisticas.setText(
                "Categorías jugadas: " + totalCategoriasJugadas + " / " + categoriasBase.size());

        contenedorTarjetasCategorias.removeAllViews();

        // Mostrar primero las categorías con partidas registradas y luego las que aún no tienen
        List<InfoCategoria> ordenFinal = new ArrayList<>(categoriasConPartidas);
        ordenFinal.addAll(categoriasSinPartidas);

        for (int i = 0; i < ordenFinal.size(); i++) {
            InfoCategoria cat = ordenFinal.get(i);
            Map<String, Double> minijuegosCat = stats.obtenerMinijuegosPorCategoria(cat.nombre);
            boolean esUltima = (i == ordenFinal.size() - 1);
            View tarjeta = crearTarjetaCategoria(cat, minijuegosCat, esUltima);
            contenedorTarjetasCategorias.addView(tarjeta);
        }
    }

    private double calcularPuntajeRepresentativo(Map<String, Double> minijuegosCat) {
        double max = 0.0;
        for (Double val : minijuegosCat.values()) {
            if (val != null && val > max) {
                max = val;
            }
        }
        return max;
    }

    private View crearTarjetaCategoria(InfoCategoria cat, Map<String, Double> minijuegosCat, boolean esUltima) {
        LinearLayout tarjeta = new LinearLayout(this);
        LinearLayout.LayoutParams paramsTarjeta = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (!esUltima) {
            paramsTarjeta.bottomMargin = dpToPx(12);
        }
        tarjeta.setLayoutParams(paramsTarjeta);
        tarjeta.setOrientation(LinearLayout.HORIZONTAL);
        tarjeta.setGravity(Gravity.CENTER_VERTICAL);
        tarjeta.setBackgroundResource(R.drawable.bg_categoria_pill);
        tarjeta.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        // Ícono de categoría a la izquierda
        ImageView ivIcono = new ImageView(this);
        LinearLayout.LayoutParams paramsIcono = new LinearLayout.LayoutParams(dpToPx(38), dpToPx(38));
        paramsIcono.setMarginEnd(dpToPx(14));
        ivIcono.setLayoutParams(paramsIcono);
        ivIcono.setImageResource(cat.iconoRes);
        tarjeta.addView(ivIcono);

        // Contenedor derecho
        LinearLayout colDerecha = new LinearLayout(this);
        colDerecha.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        colDerecha.setOrientation(LinearLayout.VERTICAL);

        boolean tienePartidas = !minijuegosCat.isEmpty();

        if (tienePartidas) {
            double puntajeCat = calcularPuntajeRepresentativo(minijuegosCat);
            int porcentaje = (int) Math.round(Math.min(100.0, Math.max(0.0, puntajeCat * 10.0)));

            // Fila 1: Nombre de categoría + Insignia "X.X / 10 pts"
            LinearLayout filaSuperior = new LinearLayout(this);
            filaSuperior.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            filaSuperior.setOrientation(LinearLayout.HORIZONTAL);
            filaSuperior.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvNombre = new TextView(this);
            LinearLayout.LayoutParams paramsNombre = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            paramsNombre.setMarginEnd(dpToPx(8));
            tvNombre.setLayoutParams(paramsNombre);
            tvNombre.setText(cat.nombre);
            tvNombre.setTextColor(0xFF0D1B2A);
            tvNombre.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tvNombre.setTypeface(null, Typeface.BOLD);
            filaSuperior.addView(tvNombre);

            TextView tvBadge = new TextView(this);
            tvBadge.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvBadge.setText(String.format(Locale.US, "%.1f / 10 pts", puntajeCat));
            tvBadge.setTextColor(0xFFFFFFFF);
            tvBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvBadge.setTypeface(null, Typeface.BOLD);
            tvBadge.setBackgroundResource(R.drawable.bg_badge_verde);
            tvBadge.setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4));
            filaSuperior.addView(tvBadge);

            colDerecha.addView(filaSuperior);

            // Fila 2: Barra de progreso + Porcentaje (ej. 86%)
            LinearLayout filaProgreso = new LinearLayout(this);
            LinearLayout.LayoutParams paramsFilaProg = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsFilaProg.topMargin = dpToPx(8);
            filaProgreso.setLayoutParams(paramsFilaProg);
            filaProgreso.setOrientation(LinearLayout.HORIZONTAL);
            filaProgreso.setGravity(Gravity.CENTER_VERTICAL);

            ProgressBar progressBar = new ProgressBar(
                    this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams paramsBar = new LinearLayout.LayoutParams(
                    0, dpToPx(10), 1.0f);
            progressBar.setLayoutParams(paramsBar);
            progressBar.setMax(100);
            progressBar.setProgress(porcentaje);
            progressBar.setProgressDrawable(getDrawable(R.drawable.progress_bar_estadisticas));
            filaProgreso.addView(progressBar);

            TextView tvPorcentaje = new TextView(this);
            LinearLayout.LayoutParams paramsPorc = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsPorc.setMarginStart(dpToPx(10));
            tvPorcentaje.setLayoutParams(paramsPorc);
            tvPorcentaje.setText(porcentaje + "%");
            tvPorcentaje.setTextColor(0xFF0D1B2A);
            tvPorcentaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvPorcentaje.setTypeface(null, Typeface.BOLD);
            filaProgreso.addView(tvPorcentaje);

            colDerecha.addView(filaProgreso);

            // Fila 3: Desglose de minijuegos jugados (ej. "Lógica 1: 8.6 | Lógica 2: 7.5")
            StringBuilder sbDesglose = new StringBuilder();
            for (Map.Entry<String, Double> entry : minijuegosCat.entrySet()) {
                if (sbDesglose.length() > 0) {
                    sbDesglose.append(" | ");
                }
                sbDesglose.append(entry.getKey())
                        .append(": ")
                        .append(String.format(Locale.US, "%.1f", entry.getValue()));
            }

            TextView tvDesglose = new TextView(this);
            LinearLayout.LayoutParams paramsDesglose = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsDesglose.topMargin = dpToPx(6);
            tvDesglose.setLayoutParams(paramsDesglose);
            tvDesglose.setText(sbDesglose.toString());
            tvDesglose.setTextColor(0xFF4A3F31);
            tvDesglose.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            colDerecha.addView(tvDesglose);

        } else {
            // Flujo alternativo 2.1: Categoría sin puntuaciones registradas aún (0.0 pts)
            TextView tvNombre = new TextView(this);
            tvNombre.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvNombre.setText(cat.nombre);
            tvNombre.setTextColor(0xFF7A6F5D);
            tvNombre.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tvNombre.setTypeface(null, Typeface.BOLD);
            colDerecha.addView(tvNombre);

            TextView tvSinPartidas = new TextView(this);
            LinearLayout.LayoutParams paramsSin = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsSin.topMargin = dpToPx(3);
            tvSinPartidas.setLayoutParams(paramsSin);
            tvSinPartidas.setText("Sin partidas registradas aún (0.0 pts)");
            tvSinPartidas.setTextColor(0xFF7A6F5D);
            tvSinPartidas.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            colDerecha.addView(tvSinPartidas);

            View lineaPunteada = new View(this);
            LinearLayout.LayoutParams paramsLinea = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(5));
            paramsLinea.topMargin = dpToPx(6);
            lineaPunteada.setLayoutParams(paramsLinea);
            lineaPunteada.setBackgroundResource(R.drawable.bg_linea_punteada);
            lineaPunteada.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            colDerecha.addView(lineaPunteada);
        }

        tarjeta.addView(colDerecha);
        return tarjeta;
    }

    private List<InfoCategoria> obtenerCategoriasOficiales() {
        List<InfoCategoria> lista = new ArrayList<>();
        lista.add(new InfoCategoria("Lógica", R.drawable.ic_cat_puzzle));
        lista.add(new InfoCategoria("Programación", R.drawable.ic_cat_code));
        lista.add(new InfoCategoria("Introducción a la ingeniería", R.drawable.ic_cat_gears));
        lista.add(new InfoCategoria("Matemáticas discretas", R.drawable.ic_cat_math));
        lista.add(new InfoCategoria("Análisis y diseño de algoritmos", R.drawable.ic_cat_flowchart));
        lista.add(new InfoCategoria("Ingeniería de requerimientos", R.drawable.ic_cat_req));
        lista.add(new InfoCategoria("Análisis y diseño orientado a objetos", R.drawable.ic_cat_diagram));
        lista.add(new InfoCategoria("Programación orientada a objetos", R.drawable.ic_cat_cube));
        return lista;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
