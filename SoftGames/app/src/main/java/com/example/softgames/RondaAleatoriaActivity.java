package com.example.softgames;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Caso de Uso CU-03 (Sprint 2): Jugar Ronda Aleatoria.
 * Genera automáticamente una partida rápida seleccionando al azar preguntas de
 * distintas categorías de ingeniería de software, distintos minijuegos y
 * dificultades aleatorias (Fácil, Normal, Difícil) sin requerir configuración manual previa.
 */
public class RondaAleatoriaActivity extends Minijuego {

    public static final int TIPO_BLOQUES_CODIGO = 0;
    public static final int TIPO_DIAGRAMA_VENN = 1;
    public static final int TIPO_FORMULA_BOOLEANA = 2;

    public static class RetoAleatorio {
        public final int tipo;
        public final int dificultad; // 1: Fácil, 2: Normal, 3: Difícil
        public final String categoria;
        public final String nombreMinijuego;
        public final MinijuegoBloques.PreguntaBloque preguntaBloque;
        public final MinijuegoLogicaAlgoritmos.PreguntaVenn preguntaVenn;
        public final MinijuegoLogica1.ReactivoFormula preguntaFormula;

        public RetoAleatorio(int dificultad, String categoria, String nombreMinijuego,
                             MinijuegoBloques.PreguntaBloque p) {
            this.tipo = TIPO_BLOQUES_CODIGO;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.nombreMinijuego = nombreMinijuego;
            this.preguntaBloque = p;
            this.preguntaVenn = null;
            this.preguntaFormula = null;
        }

        public RetoAleatorio(int dificultad, String categoria, String nombreMinijuego,
                             MinijuegoLogicaAlgoritmos.PreguntaVenn p) {
            this.tipo = TIPO_DIAGRAMA_VENN;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.nombreMinijuego = nombreMinijuego;
            this.preguntaBloque = null;
            this.preguntaVenn = p;
            this.preguntaFormula = null;
        }

        public RetoAleatorio(int dificultad, String categoria, String nombreMinijuego,
                             MinijuegoLogica1.ReactivoFormula p) {
            this.tipo = TIPO_FORMULA_BOOLEANA;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.nombreMinijuego = nombreMinijuego;
            this.preguntaBloque = null;
            this.preguntaVenn = null;
            this.preguntaFormula = p;
        }
    }

    private int totalPreguntas = 10;
    private int preguntasRespondidas = 0;
    private int respuestasCorrectas = 0;
    private double puntosTotales = 0.0;
    private int sumaSegundosRestantes = 0;

    // Registro por categoría para actualizar estadísticas individuales al concluir la ronda
    private final Map<String, Double> mejorPuntajePorCategoria = new LinkedHashMap<>();

    private final List<RetoAleatorio> listaRetosRonda = new ArrayList<>();
    private RetoAleatorio retoActual;
    private final Random random = new Random();

    // Vistas HUD
    private TextView tvHudEstadoRonda;
    private TextView tvBadgeCategoriaDificultadRonda;
    private TextView tvEnunciadoRonda;
    private Button btnComprobarRonda;

    // Paneles dinámicos
    private View panelRetoBloquesRonda;
    private View panelRetoVennRonda;
    private View panelRetoFormulaRonda;

    // Sub-vistas Modo A: Bloques de Código
    private TextView tvCodigoAntesRonda;
    private TextView tvCodigoDespuesRonda;
    private TextView zonaDropCodigoRonda;
    private final TextView[] vistasBloquesCodigo = new TextView[4];
    private final List<String> opcionesCodigoMezcladas = new ArrayList<>();
    private int indiceCodigoEnganchado = -1;

    // Sub-vistas Modo B: Diagramas de Venn
    private LinearLayout zonaDropVennRonda;
    private TextView tvEstadoDropVennRonda;
    private VennDiagramView vennRespuestaRonda;
    private final View[] tarjetasVenn = new View[3];
    private final VennDiagramView[] diagramasVenn = new VennDiagramView[3];
    private final List<Integer> opcionesVennMezcladas = new ArrayList<>();
    private final String[] etiquetasVenn = {"Opción a)", "Opción b)", "Opción c)"};
    private int indiceVennEnganchado = -1;

    // Sub-vistas Modo C: Fórmulas Booleanas
    private TextView tvFormulaRonda;
    private TextView tvResultadoEsperadoRonda;
    private Button btnFormulaOpIzqRonda;
    private Button btnFormulaOpDerRonda;
    private boolean resultadoEsperadoFormula = true;
    private boolean respuestaCorrectaFormula = true;
    private Boolean seleccionFormula = null;
    private boolean botonesFormulaInvertidos = false;

    public RondaAleatoriaActivity() {
        super(1200, null);
        this.dificultad = 1;
        this.nombre = "Ronda Aleatoria";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ronda_aleatoria);

        if (getIntent() != null) {
            totalPreguntas = getIntent().getIntExtra("totalPreguntas", 10);
            if (totalPreguntas < 1) {
                totalPreguntas = 10;
            }
        }

        tvHudEstadoRonda = findViewById(R.id.tvHudEstadoRonda);
        tvBadgeCategoriaDificultadRonda = findViewById(R.id.tvBadgeCategoriaDificultadRonda);
        tvEnunciadoRonda = findViewById(R.id.tvEnunciadoRonda);
        btnComprobarRonda = findViewById(R.id.btnComprobarRonda);

        panelRetoBloquesRonda = findViewById(R.id.panelRetoBloquesRonda);
        panelRetoVennRonda = findViewById(R.id.panelRetoVennRonda);
        panelRetoFormulaRonda = findViewById(R.id.panelRetoFormulaRonda);

        // Inicializar vistas Modo A: Bloques de Código
        tvCodigoAntesRonda = findViewById(R.id.tvCodigoAntesRonda);
        tvCodigoDespuesRonda = findViewById(R.id.tvCodigoDespuesRonda);
        zonaDropCodigoRonda = findViewById(R.id.zonaDropCodigoRonda);
        vistasBloquesCodigo[0] = findViewById(R.id.bloqueRonda1);
        vistasBloquesCodigo[1] = findViewById(R.id.bloqueRonda2);
        vistasBloquesCodigo[2] = findViewById(R.id.bloqueRonda3);
        vistasBloquesCodigo[3] = findViewById(R.id.bloqueRonda4);

        // Inicializar vistas Modo B: Diagramas de Venn
        zonaDropVennRonda = findViewById(R.id.zonaDropVennRonda);
        tvEstadoDropVennRonda = findViewById(R.id.tvEstadoDropVennRonda);
        vennRespuestaRonda = findViewById(R.id.vennRespuestaRonda);
        tarjetasVenn[0] = findViewById(R.id.cardVennRondaA);
        tarjetasVenn[1] = findViewById(R.id.cardVennRondaB);
        tarjetasVenn[2] = findViewById(R.id.cardVennRondaC);
        diagramasVenn[0] = findViewById(R.id.vennRondaA);
        diagramasVenn[1] = findViewById(R.id.vennRondaB);
        diagramasVenn[2] = findViewById(R.id.vennRondaC);

        // Inicializar vistas Modo C: Fórmulas Booleanas
        tvFormulaRonda = findViewById(R.id.tvFormulaRonda);
        tvResultadoEsperadoRonda = findViewById(R.id.tvResultadoEsperadoRonda);
        btnFormulaOpIzqRonda = findViewById(R.id.btnFormulaOpIzqRonda);
        btnFormulaOpDerRonda = findViewById(R.id.btnFormulaOpDerRonda);

        configurarInteraccionesBloquesCodigo();
        configurarInteraccionesVenn();
        configurarInteraccionesFormula();

        btnComprobarRonda.setOnClickListener(v -> comprobarRetoActual());

        // Paso 2 del Flujo Normal: Generar automáticamente la partida rápida aleatoria
        listaRetosRonda.clear();
        listaRetosRonda.addAll(generarPartidaAleatoria(totalPreguntas, random));

        actualizarHudRonda();
        cargarSiguienteReto();
        init();
    }

    /**
     * Genera una lista aleatoria de retos combinando distintas categorías,
     * distintos minijuegos y niveles de dificultad aleatorios (1: Fácil, 2: Normal, 3: Difícil).
     */
    public static List<RetoAleatorio> generarPartidaAleatoria(int cantidadPreguntas, Random rng) {
        Random generador = (rng != null) ? rng : new Random();
        List<RetoAleatorio> bancoCompleto = new ArrayList<>();

        for (int nivel = 1; nivel <= 3; nivel++) {
            List<MinijuegoBloques.PreguntaBloque> bloques = MinijuegoBloques.obtenerBancoPorDificultad(nivel);
            for (int i = 0; i < bloques.size(); i++) {
                String cat = (i % 2 == 0) ? "Programación" : "Análisis y diseño de algoritmos";
                bancoCompleto.add(new RetoAleatorio(nivel, cat, "Bloques de Código", bloques.get(i)));
            }

            List<MinijuegoLogicaAlgoritmos.PreguntaVenn> venns = MinijuegoLogicaAlgoritmos.obtenerBancoPorDificultad(nivel);
            for (int i = 0; i < venns.size(); i++) {
                String cat = (i % 2 == 0) ? "Lógica" : "Análisis y diseño de algoritmos";
                bancoCompleto.add(new RetoAleatorio(nivel, cat, "Diagramas de Venn", venns.get(i)));
            }

            List<MinijuegoLogica1.ReactivoFormula> formulas = MinijuegoLogica1.obtenerBancoPorDificultad(nivel);
            for (MinijuegoLogica1.ReactivoFormula pf : formulas) {
                bancoCompleto.add(new RetoAleatorio(nivel, "Lógica", "Fórmulas Booleanas", pf));
            }
        }

        Collections.shuffle(bancoCompleto, generador);

        int total = Math.max(1, cantidadPreguntas);
        List<RetoAleatorio> seleccion = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            seleccion.add(bancoCompleto.get(i % bancoCompleto.size()));
        }
        return seleccion;
    }

    private void actualizarHudRonda() {
        int numActual = Math.min(preguntasRespondidas + 1, totalPreguntas);
        double promActual = preguntasRespondidas > 0 ? (puntosTotales / preguntasRespondidas) : 0.0;
        String promStr = String.format(Locale.US, "%.1f", promActual);
        tvHudEstadoRonda.setText("Pregunta " + numActual + " / " + totalPreguntas
                + "  •  Aciertos: " + respuestasCorrectas
                + "  •  Prom: " + promStr);
    }

    private void cargarSiguienteReto() {
        if (listaRetosRonda.isEmpty()) {
            listaRetosRonda.addAll(generarPartidaAleatoria(totalPreguntas, random));
        }

        retoActual = listaRetosRonda.get(preguntasRespondidas % listaRetosRonda.size());
        this.dificultad = retoActual.dificultad;

        String nombreDif = (retoActual.dificultad == 1)
                ? "Fácil"
                : (retoActual.dificultad == 2 ? "Normal" : "Difícil");

        btnComprobarRonda.setAlpha(0.75f);
        tvBadgeCategoriaDificultadRonda.setText(
                retoActual.categoria + " • " + retoActual.nombreMinijuego + " • Dificultad: " + nombreDif);

        if (retoActual.tipo == TIPO_BLOQUES_CODIGO) {
            panelRetoBloquesRonda.setVisibility(View.VISIBLE);
            panelRetoVennRonda.setVisibility(View.GONE);
            panelRetoFormulaRonda.setVisibility(View.GONE);

            tvEnunciadoRonda.setText(retoActual.preguntaBloque.enunciado);
            prepararRetoBloques(retoActual.preguntaBloque);

        } else if (retoActual.tipo == TIPO_DIAGRAMA_VENN) {
            panelRetoBloquesRonda.setVisibility(View.GONE);
            panelRetoVennRonda.setVisibility(View.VISIBLE);
            panelRetoFormulaRonda.setVisibility(View.GONE);

            tvEnunciadoRonda.setText(retoActual.preguntaVenn.enunciado);
            prepararRetoVenn(retoActual.preguntaVenn);

        } else {
            panelRetoBloquesRonda.setVisibility(View.GONE);
            panelRetoVennRonda.setVisibility(View.GONE);
            panelRetoFormulaRonda.setVisibility(View.VISIBLE);

            tvEnunciadoRonda.setText("Selecciona el valor para [ ? ] que cumple el resultado esperado:");
            prepararRetoFormula(retoActual.preguntaFormula);
        }
    }

    // ==================== MODO A: BLOQUES DE CÓDIGO ====================
    private void prepararRetoBloques(MinijuegoBloques.PreguntaBloque p) {
        indiceCodigoEnganchado = -1;
        zonaDropCodigoRonda.setText("⬇ Arrastra el bloque aquí ⬇");
        zonaDropCodigoRonda.setTextColor(0xFF415A77);
        zonaDropCodigoRonda.setBackgroundResource(R.drawable.bg_drop_zone_empty);

        tvCodigoAntesRonda.setText(p.codigoAntes);
        if (p.codigoDespues == null || p.codigoDespues.isEmpty()) {
            tvCodigoDespuesRonda.setVisibility(View.GONE);
        } else {
            tvCodigoDespuesRonda.setVisibility(View.VISIBLE);
            tvCodigoDespuesRonda.setText(p.codigoDespues);
        }

        opcionesCodigoMezcladas.clear();
        opcionesCodigoMezcladas.add(p.respuestaCorrecta);
        Collections.addAll(opcionesCodigoMezcladas, p.distractores);
        Collections.shuffle(opcionesCodigoMezcladas, random);

        for (int i = 0; i < 4; i++) {
            vistasBloquesCodigo[i].setVisibility(View.VISIBLE);
            vistasBloquesCodigo[i].setAlpha(1.0f);
            vistasBloquesCodigo[i].setText(opcionesCodigoMezcladas.get(i));
        }
    }

    private void configurarInteraccionesBloquesCodigo() {
        for (int i = 0; i < vistasBloquesCodigo.length; i++) {
            final int idx = i;
            TextView bloque = vistasBloquesCodigo[i];
            bloque.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("indice_codigo_ronda", String.valueOf(idx));
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadow, v, 0);
                    v.setAlpha(0.35f);
                    return true;
                }
                return false;
            });
            bloque.setOnClickListener(v -> engancharBloqueCodigo(idx));
        }

        zonaDropCodigoRonda.setOnClickListener(v -> {
            if (indiceCodigoEnganchado != -1) {
                vistasBloquesCodigo[indiceCodigoEnganchado].setVisibility(View.VISIBLE);
                vistasBloquesCodigo[indiceCodigoEnganchado].setAlpha(1.0f);
                indiceCodigoEnganchado = -1;
                zonaDropCodigoRonda.setText("⬇ Arrastra el bloque aquí ⬇");
                zonaDropCodigoRonda.setTextColor(0xFF415A77);
                zonaDropCodigoRonda.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                btnComprobarRonda.setAlpha(0.75f);
            }
        });

        zonaDropCodigoRonda.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundResource(R.drawable.bg_drop_zone_hover);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundResource(indiceCodigoEnganchado == -1
                            ? R.drawable.bg_drop_zone_empty
                            : R.drawable.bg_drop_zone_snapped);
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData clip = event.getClipData();
                    if (clip != null && clip.getItemCount() > 0) {
                        int idxSoltado = Integer.parseInt(clip.getItemAt(0).getText().toString());
                        engancharBloqueCodigo(idxSoltado);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    View dragged = (View) event.getLocalState();
                    if (!event.getResult() && dragged != null) {
                        dragged.setAlpha(1.0f);
                    }
                    return true;
                default:
                    return false;
            }
        });
    }

    private void engancharBloqueCodigo(int idx) {
        if (indiceCodigoEnganchado != -1 && indiceCodigoEnganchado != idx) {
            vistasBloquesCodigo[indiceCodigoEnganchado].setVisibility(View.VISIBLE);
            vistasBloquesCodigo[indiceCodigoEnganchado].setAlpha(1.0f);
        }
        indiceCodigoEnganchado = idx;
        vistasBloquesCodigo[idx].setVisibility(View.INVISIBLE);
        vistasBloquesCodigo[idx].setAlpha(1.0f);

        zonaDropCodigoRonda.setText(opcionesCodigoMezcladas.get(idx));
        zonaDropCodigoRonda.setTextColor(0xFFFFFFFF);
        zonaDropCodigoRonda.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        btnComprobarRonda.setAlpha(1.0f);
    }

    // ==================== MODO B: DIAGRAMAS DE VENN ====================
    private void prepararRetoVenn(MinijuegoLogicaAlgoritmos.PreguntaVenn p) {
        indiceVennEnganchado = -1;
        zonaDropVennRonda.setBackgroundResource(R.drawable.bg_drop_zone_empty);
        tvEstadoDropVennRonda.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
        tvEstadoDropVennRonda.setTextColor(0xFF415A77);
        vennRespuestaRonda.setVisibility(View.GONE);

        opcionesVennMezcladas.clear();
        opcionesVennMezcladas.add(p.tipoCorrecto);
        opcionesVennMezcladas.add(p.distractor1);
        opcionesVennMezcladas.add(p.distractor2);
        Collections.shuffle(opcionesVennMezcladas, random);

        for (int i = 0; i < 3; i++) {
            tarjetasVenn[i].setVisibility(View.VISIBLE);
            tarjetasVenn[i].setAlpha(1.0f);
            diagramasVenn[i].setDiagramType(opcionesVennMezcladas.get(i));
        }
    }

    private void configurarInteraccionesVenn() {
        for (int i = 0; i < tarjetasVenn.length; i++) {
            final int idx = i;
            View card = tarjetasVenn[i];
            card.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("indice_venn_ronda", String.valueOf(idx));
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadow, v, 0);
                    v.setAlpha(0.35f);
                    return true;
                }
                return false;
            });
            card.setOnClickListener(v -> engancharTarjetaVenn(idx));
        }

        zonaDropVennRonda.setOnClickListener(v -> {
            if (indiceVennEnganchado != -1) {
                tarjetasVenn[indiceVennEnganchado].setVisibility(View.VISIBLE);
                tarjetasVenn[indiceVennEnganchado].setAlpha(1.0f);
                indiceVennEnganchado = -1;
                zonaDropVennRonda.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                tvEstadoDropVennRonda.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
                tvEstadoDropVennRonda.setTextColor(0xFF415A77);
                vennRespuestaRonda.setVisibility(View.GONE);
                btnComprobarRonda.setAlpha(0.75f);
            }
        });

        zonaDropVennRonda.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundResource(R.drawable.bg_drop_zone_hover);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundResource(indiceVennEnganchado == -1
                            ? R.drawable.bg_drop_zone_empty
                            : R.drawable.bg_drop_zone_snapped);
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData clip = event.getClipData();
                    if (clip != null && clip.getItemCount() > 0) {
                        int idxSoltado = Integer.parseInt(clip.getItemAt(0).getText().toString());
                        engancharTarjetaVenn(idxSoltado);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    View dragged = (View) event.getLocalState();
                    if (!event.getResult() && dragged != null) {
                        dragged.setAlpha(1.0f);
                    }
                    return true;
                default:
                    return false;
            }
        });
    }

    private void engancharTarjetaVenn(int idx) {
        if (indiceVennEnganchado != -1 && indiceVennEnganchado != idx) {
            tarjetasVenn[indiceVennEnganchado].setVisibility(View.VISIBLE);
            tarjetasVenn[indiceVennEnganchado].setAlpha(1.0f);
        }
        indiceVennEnganchado = idx;
        tarjetasVenn[idx].setVisibility(View.INVISIBLE);
        tarjetasVenn[idx].setAlpha(1.0f);

        zonaDropVennRonda.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        tvEstadoDropVennRonda.setText(etiquetasVenn[idx] + " colocada (Toca para cambiar)");
        tvEstadoDropVennRonda.setTextColor(0xFFFFFFFF);
        vennRespuestaRonda.setDiagramType(opcionesVennMezcladas.get(idx));
        vennRespuestaRonda.setVisibility(View.VISIBLE);
        btnComprobarRonda.setAlpha(1.0f);
    }

    // ==================== MODO C: FÓRMULAS BOOLEANAS ====================
    private void prepararRetoFormula(MinijuegoLogica1.ReactivoFormula pf) {
        seleccionFormula = null;
        resultadoEsperadoFormula = random.nextBoolean();
        respuestaCorrectaFormula = (pf.resultadoSiVerdadero == resultadoEsperadoFormula);

        botonesFormulaInvertidos = random.nextBoolean();
        if (!botonesFormulaInvertidos) {
            btnFormulaOpIzqRonda.setText("VERDADERO\n(V)");
            btnFormulaOpDerRonda.setText("FALSO\n(F)");
        } else {
            btnFormulaOpIzqRonda.setText("FALSO\n(F)");
            btnFormulaOpDerRonda.setText("VERDADERO\n(V)");
        }
        btnFormulaOpIzqRonda.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnFormulaOpDerRonda.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnFormulaOpIzqRonda.setAlpha(0.9f);
        btnFormulaOpDerRonda.setAlpha(0.9f);

        tvFormulaRonda.setText(pf.plantillaFormula);
        if (resultadoEsperadoFormula) {
            tvResultadoEsperadoRonda.setText("VERDADERO (V)");
            tvResultadoEsperadoRonda.setBackgroundResource(R.drawable.bg_badge_verde);
        } else {
            tvResultadoEsperadoRonda.setText("FALSO (F)");
            tvResultadoEsperadoRonda.setBackgroundResource(R.drawable.bg_bloque_codigo);
        }
    }

    private void configurarInteraccionesFormula() {
        btnFormulaOpIzqRonda.setOnClickListener(v -> seleccionarValorFormula(!botonesFormulaInvertidos));
        btnFormulaOpDerRonda.setOnClickListener(v -> seleccionarValorFormula(botonesFormulaInvertidos));
    }

    private void seleccionarValorFormula(boolean valor) {
        seleccionFormula = valor;
        String reemplazo = valor ? "[ V ]" : "[ F ]";
        tvFormulaRonda.setText(retoActual.preguntaFormula.plantillaFormula.replace("[ ? ]", reemplazo));

        boolean izqElegido = (!botonesFormulaInvertidos && valor) || (botonesFormulaInvertidos && !valor);
        if (izqElegido) {
            btnFormulaOpIzqRonda.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnFormulaOpIzqRonda.setAlpha(1.0f);
            btnFormulaOpDerRonda.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnFormulaOpDerRonda.setAlpha(0.65f);
        } else {
            btnFormulaOpDerRonda.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnFormulaOpDerRonda.setAlpha(1.0f);
            btnFormulaOpIzqRonda.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnFormulaOpIzqRonda.setAlpha(0.65f);
        }
        btnComprobarRonda.setAlpha(1.0f);
    }

    // ==================== VALIDACIÓN Y FLUJO DE LA RONDA ALEATORIA ====================
    private void comprobarRetoActual() {
        boolean esCorrecto;

        if (retoActual.tipo == TIPO_BLOQUES_CODIGO) {
            if (indiceCodigoEnganchado == -1) {
                Toast.makeText(this, "Arrastra o selecciona un bloque de código primero", Toast.LENGTH_SHORT).show();
                return;
            }
            String elegida = opcionesCodigoMezcladas.get(indiceCodigoEnganchado);
            esCorrecto = elegida.equals(retoActual.preguntaBloque.respuestaCorrecta);

        } else if (retoActual.tipo == TIPO_DIAGRAMA_VENN) {
            if (indiceVennEnganchado == -1) {
                Toast.makeText(this, "Arrastra o selecciona una opción de diagrama de Venn", Toast.LENGTH_SHORT).show();
                return;
            }
            int tipoElegido = opcionesVennMezcladas.get(indiceVennEnganchado);
            esCorrecto = (tipoElegido == retoActual.preguntaVenn.tipoCorrecto);

        } else {
            if (seleccionFormula == null) {
                Toast.makeText(this, "Selecciona VERDADERO (V) o FALSO (F) primero", Toast.LENGTH_SHORT).show();
                return;
            }
            esCorrecto = (seleccionFormula == respuestaCorrectaFormula);
        }

        procesarResultadoTurno(esCorrecto);
    }

    @Override
    protected void tiempoAgotado() {
        // Flujo alternativo 4.1: Se agota el tiempo del turno -> 0.0 pts y avanza a la siguiente pregunta
        procesarResultadoTurno(false);
    }

    private void procesarResultadoTurno(boolean fueCorrecta) {
        EventoMinijuego evento = new EventoMinijuego(fueCorrecta, maxTime, frameTimer);
        double puntosGanados = evento.obtenerPuntos();
        int segsRestantes = Math.max(0, (int) ((frameTimer * 16L) / 1000L));
        sumaSegundosRestantes += segsRestantes;

        if (fueCorrecta) {
            respuestasCorrectas++;
            puntosTotales += puntosGanados;

            if (retoActual != null && retoActual.categoria != null) {
                Double prevMejor = mejorPuntajePorCategoria.get(retoActual.categoria);
                if (prevMejor == null || puntosGanados > prevMejor) {
                    mejorPuntajePorCategoria.put(retoActual.categoria, puntosGanados);
                }
            }

            String ptsStr = String.format(Locale.US, "%.1f", puntosGanados);
            Toast.makeText(this, "¡Respuesta correcta! +" + ptsStr + " pts", Toast.LENGTH_SHORT).show();
        } else {
            // Flujo alternativo 4.1: Asignar 0.0 puntos a esa pregunta y avanzar al siguiente problema
            Toast.makeText(this, "Respuesta incorrecta. Puntos: 0.0", Toast.LENGTH_SHORT).show();
        }

        preguntasRespondidas++;
        actualizarHudRonda();

        if (preguntasRespondidas < totalPreguntas) {
            cargarSiguienteReto();
            reiniciarMinijuego();
        } else {
            finalizarRondaAleatoria();
        }
    }

    /**
     * Paso 6 del Flujo Normal:
     * Calcula el promedio obtenido, guarda automáticamente el progreso en el archivo .json activo
     * y muestra la pantalla de resumen de partida.
     */
    private void finalizarRondaAleatoria() {
        if (temporizadorReal != null) {
            temporizadorReal.cancel();
        }

        double promedio = totalPreguntas > 0 ? (puntosTotales / totalPreguntas) : 0.0;
        int tiempoPromedio = totalPreguntas > 0 ? Math.round((float) sumaSegundosRestantes / totalPreguntas) : 0;

        // Registrar las puntuaciones alcanzadas por categoría durante la Ronda Aleatoria
        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        int slotActivo = prefs.getInt("slotActivo", 0);
        String usuarioActual = prefs.getString("usuarioActual", "default");

        AdministradorArchivos admin = new AdministradorArchivos(this, usuarioActual);
        ArchivoGuardado archivoActivo = null;
        if (admin.iniciarSesion(slotActivo)) {
            archivoActivo = admin.getArchivoActual();
        }
        if (archivoActivo != null) {
            Estadisticas stats = new Estadisticas(archivoActivo);
            for (Map.Entry<String, Double> entry : mejorPuntajePorCategoria.entrySet()) {
                String claveCat = entry.getKey() + " - Ronda Aleatoria";
                double prev = stats.obtenerPuntuacion(claveCat);
                if (entry.getValue() > prev) {
                    stats.registrarPuntuacion(claveCat, entry.getValue());
                }
            }
            stats.guardar(this);
        }

        Intent resumenIntent = new Intent(this, ResumenPartidaActivity.class);
        resumenIntent.putExtra("categoria", "Ronda Aleatoria");
        resumenIntent.putExtra("minijuego", "Mix de Categorías");
        resumenIntent.putExtra("dificultad", 0); // 0 indica Dificultad Aleatoria
        resumenIntent.putExtra("respuestasCorrectas", respuestasCorrectas);
        resumenIntent.putExtra("totalPreguntas", totalPreguntas);
        resumenIntent.putExtra("puntajeObtenido", promedio);
        resumenIntent.putExtra("tiempoPromedioSegundos", tiempoPromedio);
        startActivity(resumenIntent);
        finish();
    }

    @Override
    public void dibujarMinijuego() {
    }
}
