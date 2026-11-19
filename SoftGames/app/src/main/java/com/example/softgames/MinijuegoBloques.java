package com.example.softgames;

import android.content.ClipData;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinijuegoBloques extends Minijuego {

    public static class PreguntaBloque {
        public final String subtituloArea;
        public final String enunciado;
        public final String codigoAntes;
        public final String codigoDespues;
        public final String respuestaCorrecta;
        public final String[] distractores;

        public PreguntaBloque(String subtituloArea, String enunciado, String codigoAntes,
                              String codigoDespues, String respuestaCorrecta,
                              String d1, String d2, String d3) {
            this.subtituloArea = subtituloArea;
            this.enunciado = enunciado;
            this.codigoAntes = codigoAntes;
            this.codigoDespues = codigoDespues;
            this.respuestaCorrecta = respuestaCorrecta;
            this.distractores = new String[]{d1, d2, d3};
        }
    }

    private String categoria = "Programación";
    private String subtema = "Variables 1";
    private int preguntasRespondidas = 0;
    private int totalPreguntas = 5;
    private int respuestasCorrectas = 0;
    private double puntosTotales = 0.0;
    private int sumaSegundosRestantes = 0;

    private List<PreguntaBloque> bancoActual = new ArrayList<>();
    private PreguntaBloque preguntaActual;
    private List<String> opcionesMezcladas = new ArrayList<>();

    private int indiceBloqueEnganchado = -1;
    private boolean validandoRespuesta = false;

    private TextView tvProgresoBloques;
    private TextView tvEnunciadoBloques;
    private TextView tvSubtituloArea;
    private TextView tvCodigoAntes;
    private TextView tvCodigoDespues;
    private TextView zonaRespuestaBloque;
    private final TextView[] vistasBloques = new TextView[4];
    private Button btnComprobarBloque;

    public MinijuegoBloques() {
        super(1200, null);
        this.dificultad = 1;
    }

    public MinijuegoBloques(EnrutadorMinijuego enrutador, int dificultad) {
        super(1200, enrutador);
        this.dificultad = dificultad;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            String catIntent = getIntent().getStringExtra("categoria");
            if (catIntent != null && !catIntent.isEmpty()) {
                this.categoria = catIntent;
            }
            String subIntent = getIntent().getStringExtra("minijuego");
            if (subIntent != null && !subIntent.isEmpty()) {
                this.subtema = subIntent;
            }
            this.dificultad = getIntent().getIntExtra("dificultad", 1);
            this.totalPreguntas = getIntent().getIntExtra("totalPreguntas", 5);
        }
        this.nombre = this.subtema;

        setContentView(R.layout.activity_minijuego_bloques);

        tvProgresoBloques = findViewById(R.id.tvProgresoBloques);
        tvEnunciadoBloques = findViewById(R.id.tvEnunciadoBloques);
        tvSubtituloArea = findViewById(R.id.tvSubtituloArea);
        tvCodigoAntes = findViewById(R.id.tvCodigoAntes);
        tvCodigoDespues = findViewById(R.id.tvCodigoDespues);
        zonaRespuestaBloque = findViewById(R.id.zonaRespuestaBloque);
        vistasBloques[0] = findViewById(R.id.bloqueOpcion0);
        vistasBloques[1] = findViewById(R.id.bloqueOpcion1);
        vistasBloques[2] = findViewById(R.id.bloqueOpcion2);
        vistasBloques[3] = findViewById(R.id.bloqueOpcion3);
        btnComprobarBloque = findViewById(R.id.btnComprobarBloque);

        inicializarBancoPreguntas();
        configurarArrastreYZonaRespuesta();
        mostrarSiguientePregunta();

        btnComprobarBloque.setOnClickListener(v -> verificarRespuestaEnganchada());

        init();
    }

    private void configurarArrastreYZonaRespuesta() {
        for (int i = 0; i < vistasBloques.length; i++) {
            final int index = i;
            TextView vistaBloque = vistasBloques[i];

            vistaBloque.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (v.getVisibility() != View.VISIBLE) {
                        return false;
                    }
                    ClipData data = ClipData.newPlainText("indiceBloque", String.valueOf(index));
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                    v.setAlpha(0.25f);
                    return true;
                }
                return false;
            });
        }

        zonaRespuestaBloque.setOnClickListener(v -> {
            if (indiceBloqueEnganchado != -1) {
                devolverBloqueAlMenu(indiceBloqueEnganchado);
                limpiarZonaRespuesta();
            }
        });

        zonaRespuestaBloque.setOnDragListener((v, event) -> {
            View vistaArrastrada = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_hover);
                    zonaRespuestaBloque.animate().scaleX(1.03f).scaleY(1.03f).setDuration(120).start();
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    zonaRespuestaBloque.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    if (indiceBloqueEnganchado == -1) {
                        zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                    } else {
                        zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    zonaRespuestaBloque.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    int nuevoIndice = Integer.parseInt(item.getText().toString());

                    if (indiceBloqueEnganchado != -1 && indiceBloqueEnganchado != nuevoIndice) {
                        devolverBloqueAlMenu(indiceBloqueEnganchado);
                    }

                    engancharBloqueEnArea(nuevoIndice);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    zonaRespuestaBloque.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    if (!event.getResult() && vistaArrastrada != null) {
                        vistaArrastrada.setVisibility(View.VISIBLE);
                        vistaArrastrada.setAlpha(0.0f);
                        vistaArrastrada.setTranslationY(-20f);
                        vistaArrastrada.animate()
                                .alpha(1.0f)
                                .translationY(0f)
                                .setDuration(200)
                                .start();
                    }
                    return true;

                default:
                    return false;
            }
        });
    }

    private void engancharBloqueEnArea(int indice) {
        indiceBloqueEnganchado = indice;
        String textoBloque = opcionesMezcladas.get(indice);

        vistasBloques[indice].setVisibility(View.INVISIBLE);
        vistasBloques[indice].setAlpha(1.0f);

        zonaRespuestaBloque.setText(textoBloque);
        zonaRespuestaBloque.setTextColor(0xFFFFFFFF);
        zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_snapped);

        btnComprobarBloque.setAlpha(1.0f);
    }

    private void devolverBloqueAlMenu(int indice) {
        if (indice >= 0 && indice < vistasBloques.length) {
            TextView vista = vistasBloques[indice];
            vista.setVisibility(View.VISIBLE);
            vista.setAlpha(1.0f);
        }
    }

    private void limpiarZonaRespuesta() {
        validandoRespuesta = false;
        indiceBloqueEnganchado = -1;
        zonaRespuestaBloque.setText("⬇ Arrastra el bloque aquí ⬇");
        zonaRespuestaBloque.setTextColor(0xFF415A77);
        zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_empty);
        btnComprobarBloque.setAlpha(0.75f);
    }

    private void mostrarSiguientePregunta() {
        limpiarZonaRespuesta();

        if (bancoActual.isEmpty()) {
            inicializarBancoPreguntas();
        }

        int idx = preguntasRespondidas % bancoActual.size();
        preguntaActual = bancoActual.get(idx);

        tvProgresoBloques.setText(categoria + " • " + (preguntasRespondidas + 1) + " / " + totalPreguntas);
        tvSubtituloArea.setText(preguntaActual.subtituloArea);
        tvEnunciadoBloques.setText(preguntaActual.enunciado);
        tvCodigoAntes.setText(preguntaActual.codigoAntes);

        if (preguntaActual.codigoDespues == null || preguntaActual.codigoDespues.isEmpty()) {
            tvCodigoDespues.setVisibility(View.GONE);
        } else {
            tvCodigoDespues.setVisibility(View.VISIBLE);
            tvCodigoDespues.setText(preguntaActual.codigoDespues);
        }

        opcionesMezcladas.clear();
        opcionesMezcladas.add(preguntaActual.respuestaCorrecta);
        Collections.addAll(opcionesMezcladas, preguntaActual.distractores);
        Collections.shuffle(opcionesMezcladas);

        for (int i = 0; i < 4; i++) {
            vistasBloques[i].setVisibility(View.VISIBLE);
            vistasBloques[i].setAlpha(1.0f);
            vistasBloques[i].setText(opcionesMezcladas.get(i));
        }
    }

    private void verificarRespuestaEnganchada() {
        if (validandoRespuesta) return;
        if (indiceBloqueEnganchado == -1) {
            return;
        }
        validandoRespuesta = true;
        if (temporizadorReal != null) {
            temporizadorReal.cancel();
        }

        String seleccion = opcionesMezcladas.get(indiceBloqueEnganchado);
        boolean esCorrecto = seleccion.equals(preguntaActual.respuestaCorrecta);

        EventoMinijuego eventoPregunta = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        double puntosPregunta = eventoPregunta.obtenerPuntos();
        int segsRestantes = Math.max(0, (int) ((frameTimer * 16L) / 1000L));
        sumaSegundosRestantes += segsRestantes;

        AjustesActivity.reproducirRetroalimentacion(this, btnComprobarBloque, esCorrecto);

        if (esCorrecto) {
            puntosTotales += puntosPregunta;
            zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_correct);
            zonaRespuestaBloque.setTextColor(0xFFFFFFFF);
            zonaRespuestaBloque.setText("✓  " + seleccion);
        } else {
            zonaRespuestaBloque.setBackgroundResource(R.drawable.bg_drop_zone_error);
            zonaRespuestaBloque.setTextColor(0xFFFFFFFF);
            zonaRespuestaBloque.setText("✕  " + seleccion);
        }

        zonaRespuestaBloque.postDelayed(() -> avanzarTurno(esCorrecto), 550);
    }

    @Override
    protected void tiempoAgotado() {
        avanzarTurno(false);
    }

    private void avanzarTurno(boolean fueCorrecta) {
        if (fueCorrecta) {
            respuestasCorrectas++;
        }
        preguntasRespondidas++;

        if (preguntasRespondidas < totalPreguntas) {
            mostrarSiguientePregunta();
            reiniciarMinijuego();
        } else {
            if (temporizadorReal != null) {
                temporizadorReal.cancel();
            }
            double promedio = totalPreguntas > 0 ? (puntosTotales / totalPreguntas) : 0.0;
            int tiempoPromedio = totalPreguntas > 0 ? Math.round((float) sumaSegundosRestantes / totalPreguntas) : 0;

            android.content.Intent resumenIntent = new android.content.Intent(this, ResumenPartidaActivity.class);
            resumenIntent.putExtra("categoria", categoria);
            resumenIntent.putExtra("minijuego", subtema);
            resumenIntent.putExtra("dificultad", dificultad);
            resumenIntent.putExtra("respuestasCorrectas", respuestasCorrectas);
            resumenIntent.putExtra("totalPreguntas", totalPreguntas);
            resumenIntent.putExtra("puntajeObtenido", promedio);
            resumenIntent.putExtra("tiempoPromedioSegundos", tiempoPromedio);
            startActivity(resumenIntent);
            finish();
        }
    }

    private void inicializarBancoPreguntas() {
        bancoActual.clear();
        bancoActual.addAll(obtenerBancoPorCategoriaYDificultad(categoria, dificultad));
    }

    public static List<PreguntaBloque> obtenerBancoPorCategoriaYDificultad(String categoriaNombre, int dificultadNivel) {
        return BancoPreguntasCategorias.obtenerBanco(categoriaNombre, dificultadNivel);
    }

    public static List<PreguntaBloque> obtenerBancoPorDificultad(int dificultadNivel) {
        return BancoPreguntasCategorias.obtenerBanco("Programación", dificultadNivel);
    }

    @Override
    public void dibujarMinijuego() {
    }
}
