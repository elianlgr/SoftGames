package com.example.softgames;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ResumenPartidaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_partida);

        String categoria = "Lógica";
        String minijuego = "Lógica 1";
        int dificultad = 1;
        int respuestasCorrectas = 0;
        int totalPreguntas = 5;
        double puntajeObtenido = 0.0;
        int tiempoPromedioSegundos = 0;

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("categoria")) {
                categoria = intent.getStringExtra("categoria");
            }
            if (intent.hasExtra("minijuego")) {
                minijuego = intent.getStringExtra("minijuego");
            }
            dificultad = intent.getIntExtra("dificultad", 1);
            respuestasCorrectas = intent.getIntExtra("respuestasCorrectas", 0);
            totalPreguntas = intent.getIntExtra("totalPreguntas", 5);
            puntajeObtenido = intent.getDoubleExtra("puntajeObtenido", 0.0);
            tiempoPromedioSegundos = intent.getIntExtra("tiempoPromedioSegundos", 0);
        }

        String textoDificultad;
        if (dificultad == 0) {
            textoDificultad = "Aleatoria";
        } else if (dificultad == 1) {
            textoDificultad = "Fácil";
        } else if (dificultad == 2) {
            textoDificultad = "Medio";
        } else {
            textoDificultad = "Difícil";
        }

        TextView tvSubtituloResumen = findViewById(R.id.tvSubtituloResumen);
        TextView tvBadgeRecordResumen = findViewById(R.id.tvBadgeRecordResumen);
        TextView tvPuntajeObtenidoResumen = findViewById(R.id.tvPuntajeObtenidoResumen);
        TextView tvRespuestasCorrectasResumen = findViewById(R.id.tvRespuestasCorrectasResumen);
        TextView tvTiempoPromedioResumen = findViewById(R.id.tvTiempoPromedioResumen);
        TextView tvPuntuacionAnteriorResumen = findViewById(R.id.tvPuntuacionAnteriorResumen);
        TextView tvEstadoGuardadoResumen = findViewById(R.id.tvEstadoGuardadoResumen);
        Button btnVerEstadisticasResumen = findViewById(R.id.btnVerEstadisticasResumen);
        Button btnVolverMenuResumen = findViewById(R.id.btnVolverMenuResumen);

        tvSubtituloResumen.setText(categoria + " • " + minijuego + " (" + textoDificultad + ")");
        tvPuntajeObtenidoResumen.setText(String.format(Locale.US, "%.1f / 10", puntajeObtenido));
        tvRespuestasCorrectasResumen.setText(respuestasCorrectas + " / " + totalPreguntas);
        tvTiempoPromedioResumen.setText(tiempoPromedioSegundos + "s");

        // Flujo normal pasos 1, 2 y 3: Comparar con puntuación previa y serializar automáticamente
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
        String claveMinijuego = categoria + " - " + minijuego;
        double puntuacionAnterior = stats.obtenerPuntuacion(claveMinijuego);

        tvPuntuacionAnteriorResumen.setText(String.format(Locale.US, "%.1f pts", puntuacionAnterior));

        boolean esNuevoRecord = (puntajeObtenido > puntuacionAnterior) || (puntuacionAnterior == 0.0 && puntajeObtenido > 0.0);
        if (esNuevoRecord) {
            tvBadgeRecordResumen.setText("★  ¡NUEVO RÉCORD!");
        } else {
            tvBadgeRecordResumen.setText("★  PARTIDA FINALIZADA");
        }

        boolean guardadoExitoso = true;
        if (puntajeObtenido >= puntuacionAnterior) {
            stats.registrarPuntuacion(claveMinijuego, puntajeObtenido);
            guardadoExitoso = stats.guardar(this);
        } else {
            // Igual serializamos el archivo para garantizar que exista en almacenamiento interno
            guardadoExitoso = stats.guardar(this);
        }

        if (guardadoExitoso) {
            String htmlTexto = "Progreso guardado automáticamente en <b>" + nombrePartidaMostrar + "</b>";
            tvEstadoGuardadoResumen.setText(Html.fromHtml(htmlTexto, Html.FROM_HTML_MODE_LEGACY));
        } else {
            // Flujo alternativo 3.1: Error de escritura en almacenamiento interno
            tvEstadoGuardadoResumen.setText("Aviso: Puntos conservados en memoria de la sesión activa");
            Toast.makeText(this,
                    "No se pudo escribir el archivo .json. Los puntos se conservarán en memoria durante la sesión activa.",
                    Toast.LENGTH_LONG).show();
        }

        btnVerEstadisticasResumen.setOnClickListener(v -> {
            Intent statsIntent = new Intent(ResumenPartidaActivity.this, EstadisticasActivity.class);
            startActivity(statsIntent);
        });

        btnVolverMenuResumen.setOnClickListener(v -> finish());
    }
}
