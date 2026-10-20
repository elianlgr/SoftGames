package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MinijuegoLogica1 extends Minijuego {

    public static class ReactivoFormula {
        public final String plantillaFormula; // Contiene "[ ? ]" donde va la incógnita
        public final boolean resultadoSiVerdadero; // Valor de la fórmula si ? = V
        public final boolean resultadoSiFalso;     // Valor de la fórmula si ? = F

        public ReactivoFormula(String plantillaFormula, boolean resultadoSiVerdadero, boolean resultadoSiFalso) {
            this.plantillaFormula = plantillaFormula;
            this.resultadoSiVerdadero = resultadoSiVerdadero;
            this.resultadoSiFalso = resultadoSiFalso;
        }
    }

    private final List<ReactivoFormula> bancoFormulas = new ArrayList<>();
    private ReactivoFormula reactivoActual;
    private boolean resultadoEsperadoActual = true; // Lo que se pide en "Resultado esperado"
    private boolean respuestaCorrectaActual = true; // Qué valor de ? produce ese resultado
    private Boolean opcionSeleccionada = null;      // null si aún no selecciona V o F
    private boolean ordenBotonesInvertido = false;  // Para aleatorizar posición de opciones

    private int preguntasRespondidas = 0;
    private int totalPreguntas = 5;
    private int respuestasCorrectas = 0;
    private double puntosTotales = 0.0;
    private int sumaSegundosRestantes = 0;
    private String categoria = "Lógica";
    private String subtema = "Lógica 2";

    private TextView tvProgresoLogica2;
    private TextView textoFormula;
    private TextView tvSeleccionActualFormula;
    private TextView tvResultadoEsperadoFormula;
    private Button btnOpcionIzquierda;
    private Button btnOpcionDerecha;
    private Button btnComprobarFormula;
    private final Random random = new Random();

    public MinijuegoLogica1(EnrutadorMinijuego enrutador, int dificultad) {
        super(1200, enrutador);
        this.dificultad = dificultad;
    }

    public MinijuegoLogica1() {
        super(1200, null);
        this.dificultad = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            if (getIntent().hasExtra("categoria")) {
                this.categoria = getIntent().getStringExtra("categoria");
            }
            if (getIntent().hasExtra("minijuego")) {
                this.subtema = getIntent().getStringExtra("minijuego");
            }
            this.dificultad = getIntent().getIntExtra("dificultad", 1);
            this.totalPreguntas = getIntent().getIntExtra("totalPreguntas", 5);
        }
        this.nombre = this.subtema;

        setContentView(R.layout.logic_minigame1_layout);

        tvProgresoLogica2 = findViewById(R.id.tvProgresoLogica2);
        textoFormula = findViewById(R.id.textoFormula);
        tvSeleccionActualFormula = findViewById(R.id.tvSeleccionActualFormula);
        tvResultadoEsperadoFormula = findViewById(R.id.tvResultadoEsperadoFormula);
        btnOpcionIzquierda = findViewById(R.id.trueButton);
        btnOpcionDerecha = findViewById(R.id.falseButton);
        btnComprobarFormula = findViewById(R.id.btnComprobarFormula);

        inicializarYBarajarBanco();

        btnOpcionIzquierda.setOnClickListener(v -> {
            boolean valorBotonIzq = !ordenBotonesInvertido;
            seleccionarValor(valorBotonIzq);
        });

        btnOpcionDerecha.setOnClickListener(v -> {
            boolean valorBotonDer = ordenBotonesInvertido;
            seleccionarValor(valorBotonDer);
        });

        btnComprobarFormula.setOnClickListener(v -> comprobarRespuestaSeleccionada());

        cargarSiguienteReactivo();
        init();
    }

    private void inicializarYBarajarBanco() {
        bancoFormulas.clear();
        bancoFormulas.addAll(obtenerBancoPorDificultad(dificultad));
    }

    public static List<ReactivoFormula> obtenerBancoPorDificultad(int dificultadNivel) {
        List<ReactivoFormula> lista = new ArrayList<>();

        if (dificultadNivel == 1) {
            lista.add(new ReactivoFormula("[ ? ]  ∧  V", true, false));
            lista.add(new ReactivoFormula("[ ? ]  ∨  F", true, false));
            lista.add(new ReactivoFormula("V  ∧  ¬[ ? ]", false, true));
            lista.add(new ReactivoFormula("¬( F  ∨  [ ? ] )", false, true));
            lista.add(new ReactivoFormula("( V  ∧  [ ? ] )  ∨  F", true, false));
            lista.add(new ReactivoFormula("¬[ ? ]  ∧  V", false, true));
            lista.add(new ReactivoFormula("F  ∨  [ ? ]", true, false));
            lista.add(new ReactivoFormula("¬( [ ? ]  ∧  V )", false, true));
        } else if (dificultadNivel == 2) {
            lista.add(new ReactivoFormula("¬( ¬V  ∨  [ ? ] )", false, true));
            lista.add(new ReactivoFormula("( [ ? ]  ∧  V )  ∧  V", true, false));
            lista.add(new ReactivoFormula("( V  ∧  F )  ∨  ( [ ? ]  ∧  V )", true, false));
            lista.add(new ReactivoFormula("( [ ? ]  ∨  F )  ∧  ¬F", true, false));
            lista.add(new ReactivoFormula("¬( [ ? ]  ∧  V )  ∨  F", false, true));
            lista.add(new ReactivoFormula("( F  ∨  ¬[ ? ] )  ∧  V", false, true));
            lista.add(new ReactivoFormula("¬( F  ∧  V )  ∧  [ ? ]", true, false));
            lista.add(new ReactivoFormula("( ¬F  ∧  [ ? ] )  ∨  F", true, false));
        } else {
            lista.add(new ReactivoFormula("( [ ? ]  ∧  F )  ∨  ( ¬[ ? ]  ∧  V )", false, true));
            lista.add(new ReactivoFormula("( [ ? ]  ∨  ¬[ ? ] )  ∧  ( V  ∧  ¬[ ? ] )", false, true));
            lista.add(new ReactivoFormula("¬( ( [ ? ]  ∧  V )  ∨  F )  ∧  V", false, true));
            lista.add(new ReactivoFormula("( ¬[ ? ]  ∨  F )  ∧  ( V  ∨  [ ? ] )", false, true));
            lista.add(new ReactivoFormula("( ( [ ? ]  ∧  V )  ∨  F )  ∧  ¬F", true, false));
            lista.add(new ReactivoFormula("¬( ¬[ ? ]  ∨  F )  ∧  V", true, false));
            lista.add(new ReactivoFormula("( ( V  ∨  F )  ∧  ¬[ ? ] )  ∨  F", false, true));
            lista.add(new ReactivoFormula("( ¬F  ∧  [ ? ] )  ∧  ( V  ∨  ¬[ ? ] )", true, false));
        }

        Collections.shuffle(lista);
        return lista;
    }

    private void cargarSiguienteReactivo() {
        opcionSeleccionada = null;

        if (bancoFormulas.isEmpty()) {
            inicializarYBarajarBanco();
        }

        int idx = preguntasRespondidas % bancoFormulas.size();
        if (idx == 0 && preguntasRespondidas > 0) {
            Collections.shuffle(bancoFormulas, random);
        }
        reactivoActual = bancoFormulas.get(idx);

        // Elegir aleatoriamente si pedimos que el resultado sea VERDADERO (V) o FALSO (F)
        resultadoEsperadoActual = random.nextBoolean();
        if (reactivoActual.resultadoSiVerdadero == resultadoEsperadoActual) {
            respuestaCorrectaActual = true;
        } else {
            respuestaCorrectaActual = false;
        }

        // Aleatorizar también el orden de los dos botones de opción
        ordenBotonesInvertido = random.nextBoolean();
        configurarBotonesOpciones();

        tvProgresoLogica2.setText(categoria + " • " + (preguntasRespondidas + 1) + " / " + totalPreguntas);
        textoFormula.setText(reactivoActual.plantillaFormula);

        tvSeleccionActualFormula.setText("Selecciona una opción abajo para reemplazar [ ? ]");
        tvSeleccionActualFormula.setTextColor(0xFF415A77);
        tvSeleccionActualFormula.setBackgroundResource(R.drawable.bg_drop_zone_empty);

        if (resultadoEsperadoActual) {
            tvResultadoEsperadoFormula.setText("VERDADERO (V)");
            tvResultadoEsperadoFormula.setBackgroundResource(R.drawable.bg_badge_verde);
        } else {
            tvResultadoEsperadoFormula.setText("FALSO (F)");
            tvResultadoEsperadoFormula.setBackgroundResource(R.drawable.bg_bloque_codigo);
        }

        btnComprobarFormula.setAlpha(0.75f);
    }

    private void configurarBotonesOpciones() {
        if (!ordenBotonesInvertido) {
            btnOpcionIzquierda.setText("VERDADERO\n(V)");
            btnOpcionDerecha.setText("FALSO\n(F)");
        } else {
            btnOpcionIzquierda.setText("FALSO\n(F)");
            btnOpcionDerecha.setText("VERDADERO\n(V)");
        }
        btnOpcionIzquierda.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnOpcionDerecha.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnOpcionIzquierda.setAlpha(0.9f);
        btnOpcionDerecha.setAlpha(0.9f);
    }

    private void seleccionarValor(boolean valorElegido) {
        opcionSeleccionada = valorElegido;

        String simbolo = valorElegido ? "[ V ]" : "[ F ]";
        String etiqueta = valorElegido ? "Opción seleccionada: VERDADERO (V)" : "Opción seleccionada: FALSO (F)";

        textoFormula.setText(reactivoActual.plantillaFormula.replace("[ ? ]", simbolo));
        tvSeleccionActualFormula.setText(etiqueta);
        tvSeleccionActualFormula.setTextColor(0xFFFFFFFF);
        tvSeleccionActualFormula.setBackgroundResource(R.drawable.bg_drop_zone_snapped);

        boolean seleccionoIzquierdo = (!ordenBotonesInvertido && valorElegido) || (ordenBotonesInvertido && !valorElegido);
        if (seleccionoIzquierdo) {
            btnOpcionIzquierda.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnOpcionIzquierda.setAlpha(1.0f);
            btnOpcionDerecha.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnOpcionDerecha.setAlpha(0.65f);
        } else {
            btnOpcionDerecha.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnOpcionDerecha.setAlpha(1.0f);
            btnOpcionIzquierda.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnOpcionIzquierda.setAlpha(0.65f);
        }

        btnComprobarFormula.setAlpha(1.0f);
    }

    private void comprobarRespuestaSeleccionada() {
        if (opcionSeleccionada == null) {
            Toast.makeText(this, "Selecciona primero una opción antes de comprobar", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean esCorrecto = (opcionSeleccionada == respuestaCorrectaActual);
        EventoMinijuego eventoPregunta = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        double puntosPregunta = eventoPregunta.obtenerPuntos();
        int segsRestantes = Math.max(0, (int) ((frameTimer * 16L) / 1000L));
        sumaSegundosRestantes += segsRestantes;

        if (esCorrecto) {
            puntosTotales += puntosPregunta;
            String ptsStr = String.format(Locale.US, "%.1f", puntosPregunta);
            Toast.makeText(this, "¡Respuesta correcta! +" + ptsStr + " pts", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Respuesta incorrecta. Puntos: 0", Toast.LENGTH_SHORT).show();
        }
        avanzarSiguientePregunta(esCorrecto);
    }

    @Override
    protected void tiempoAgotado() {
        avanzarSiguientePregunta(false);
    }

    private void avanzarSiguientePregunta(boolean fueCorrecta) {
        if (fueCorrecta) {
            respuestasCorrectas++;
        }
        preguntasRespondidas++;

        if (preguntasRespondidas < totalPreguntas) {
            cargarSiguienteReactivo();
            reiniciarMinijuego();
        } else {
            if (temporizadorReal != null) {
                temporizadorReal.cancel();
            }
            double promedio = totalPreguntas > 0 ? (puntosTotales / totalPreguntas) : 0.0;
            int tiempoPromedio = totalPreguntas > 0 ? Math.round((float) sumaSegundosRestantes / totalPreguntas) : 0;

            Intent resumenIntent = new Intent(this, ResumenPartidaActivity.class);
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

    @Override
    public void dibujarMinijuego() {
    }
}
