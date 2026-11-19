package com.example.softgames;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Caso de Uso CU-S3-01: Superar el modo Quiz.
 * Flujo normal paso 6 y 7:
 * - Escoge cada categoría en orden aleatorio e inicia la serie de preguntas configurada (10 a 20).
 * - Repite "Solución de minijuego" hasta completar las preguntas de esa categoría y pasa a la siguiente.
 * - Una vez respondidas las preguntas de todas las categorías, calcula la puntuación final y el
 *   desglose obtenido en cada categoría.
 */
public class ModoQuizActivity extends Minijuego {

    public static final int TIPO_BLOQUES_CODIGO = 0;
    public static final int TIPO_DIAGRAMA_VENN = 1;
    public static final int TIPO_FORMULA_BOOLEANA = 2;

    public static class RetoQuiz {
        public final int tipo;
        public final int dificultad;
        public final String categoria;
        public final int indiceCategoria; // 1..N
        public final int totalCategorias;
        public final int numeroPreguntaEnCategoria; // 1..M
        public final int preguntasPorCategoria;
        public final MinijuegoBloques.PreguntaBloque preguntaBloque;
        public final MinijuegoLogicaAlgoritmos.PreguntaVenn preguntaVenn;
        public final MinijuegoLogica1.ReactivoFormula preguntaFormula;

        public RetoQuiz(int dificultad, String categoria, int indiceCategoria, int totalCategorias,
                        int numeroPreguntaEnCategoria, int preguntasPorCategoria,
                        MinijuegoBloques.PreguntaBloque p) {
            this.tipo = TIPO_BLOQUES_CODIGO;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.indiceCategoria = indiceCategoria;
            this.totalCategorias = totalCategorias;
            this.numeroPreguntaEnCategoria = numeroPreguntaEnCategoria;
            this.preguntasPorCategoria = preguntasPorCategoria;
            this.preguntaBloque = p;
            this.preguntaVenn = null;
            this.preguntaFormula = null;
        }

        public RetoQuiz(int dificultad, String categoria, int indiceCategoria, int totalCategorias,
                        int numeroPreguntaEnCategoria, int preguntasPorCategoria,
                        MinijuegoLogicaAlgoritmos.PreguntaVenn p) {
            this.tipo = TIPO_DIAGRAMA_VENN;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.indiceCategoria = indiceCategoria;
            this.totalCategorias = totalCategorias;
            this.numeroPreguntaEnCategoria = numeroPreguntaEnCategoria;
            this.preguntasPorCategoria = preguntasPorCategoria;
            this.preguntaBloque = null;
            this.preguntaVenn = p;
            this.preguntaFormula = null;
        }

        public RetoQuiz(int dificultad, String categoria, int indiceCategoria, int totalCategorias,
                        int numeroPreguntaEnCategoria, int preguntasPorCategoria,
                        MinijuegoLogica1.ReactivoFormula p) {
            this.tipo = TIPO_FORMULA_BOOLEANA;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.indiceCategoria = indiceCategoria;
            this.totalCategorias = totalCategorias;
            this.numeroPreguntaEnCategoria = numeroPreguntaEnCategoria;
            this.preguntasPorCategoria = preguntasPorCategoria;
            this.preguntaBloque = null;
            this.preguntaVenn = null;
            this.preguntaFormula = p;
        }
    }

    private int dificultadQuiz = 1;
    private int preguntasPorCategoria = 10;
    private int indiceRetoGlobal = 0;
    private int aciertosTotales = 0;
    private double sumaPuntosTotales = 0.0;

    private final List<String> ordenCategoriasAleatorias = new ArrayList<>();
    private final List<RetoQuiz> colaRetosQuiz = new ArrayList<>();
    private final Map<String, Integer> aciertosPorCategoria = new LinkedHashMap<>();
    private final Map<String, Double> sumaPuntosPorCategoria = new LinkedHashMap<>();

    private RetoQuiz retoActual;
    private final Random random = new Random();

    // Vistas HUD
    private TextView tvHudEstadoQuiz;
    private TextView tvBadgeCategoriaQuiz;
    private TextView tvEnunciadoQuiz;
    private Button btnComprobarQuiz;

    // Paneles dinámicos
    private View panelRetoBloquesQuiz;
    private View panelRetoVennQuiz;
    private View panelRetoFormulaQuiz;

    // Sub-vistas Modo A: Bloques de Código
    private TextView tvCodigoAntesQuiz;
    private TextView tvCodigoDespuesQuiz;
    private TextView zonaDropCodigoQuiz;
    private final TextView[] vistasBloquesCodigo = new TextView[4];
    private final List<String> opcionesCodigoMezcladas = new ArrayList<>();
    private int indiceCodigoEnganchado = -1;

    // Sub-vistas Modo B: Diagramas de Venn
    private LinearLayout zonaDropVennQuiz;
    private TextView tvEstadoDropVennQuiz;
    private VennDiagramView vennRespuestaQuiz;
    private final View[] tarjetasVenn = new View[3];
    private final VennDiagramView[] diagramasVenn = new VennDiagramView[3];
    private final List<Integer> opcionesVennMezcladas = new ArrayList<>();
    private final String[] etiquetasVenn = {"Opción a)", "Opción b)", "Opción c)"};
    private int indiceVennEnganchado = -1;

    // Sub-vistas Modo C: Fórmulas Booleanas
    private TextView tvFormulaQuiz;
    private TextView tvResultadoEsperadoQuiz;
    private Button btnFormulaOpIzqQuiz;
    private Button btnFormulaOpDerQuiz;
    private boolean resultadoEsperadoFormula = true;
    private boolean respuestaCorrectaFormula = true;
    private Boolean seleccionFormula = null;
    private boolean botonesFormulaInvertidos = false;
    private boolean validandoRespuesta = false;

    public ModoQuizActivity() {
        super(1200, null);
        this.dificultad = 1;
        this.nombre = "Modo Quiz";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_quiz);

        if (getIntent() != null) {
            dificultadQuiz = getIntent().getIntExtra("dificultad", 1);
            preguntasPorCategoria = getIntent().getIntExtra("preguntasPorCategoria", 10);
            if (preguntasPorCategoria < 1) {
                preguntasPorCategoria = 10;
            }
        }
        this.dificultad = dificultadQuiz;

        tvHudEstadoQuiz = findViewById(R.id.tvHudEstadoQuiz);
        tvBadgeCategoriaQuiz = findViewById(R.id.tvBadgeCategoriaQuiz);
        tvEnunciadoQuiz = findViewById(R.id.tvEnunciadoQuiz);
        btnComprobarQuiz = findViewById(R.id.btnComprobarQuiz);

        panelRetoBloquesQuiz = findViewById(R.id.panelRetoBloquesQuiz);
        panelRetoVennQuiz = findViewById(R.id.panelRetoVennQuiz);
        panelRetoFormulaQuiz = findViewById(R.id.panelRetoFormulaQuiz);

        // Inicializar vistas Modo A: Bloques de Código
        tvCodigoAntesQuiz = findViewById(R.id.tvCodigoAntesQuiz);
        tvCodigoDespuesQuiz = findViewById(R.id.tvCodigoDespuesQuiz);
        zonaDropCodigoQuiz = findViewById(R.id.zonaDropCodigoQuiz);
        vistasBloquesCodigo[0] = findViewById(R.id.bloqueQuiz1);
        vistasBloquesCodigo[1] = findViewById(R.id.bloqueQuiz2);
        vistasBloquesCodigo[2] = findViewById(R.id.bloqueQuiz3);
        vistasBloquesCodigo[3] = findViewById(R.id.bloqueQuiz4);

        // Inicializar vistas Modo B: Diagramas de Venn
        zonaDropVennQuiz = findViewById(R.id.zonaDropVennQuiz);
        tvEstadoDropVennQuiz = findViewById(R.id.tvEstadoDropVennQuiz);
        vennRespuestaQuiz = findViewById(R.id.vennRespuestaQuiz);
        tarjetasVenn[0] = findViewById(R.id.cardVennQuizA);
        tarjetasVenn[1] = findViewById(R.id.cardVennQuizB);
        tarjetasVenn[2] = findViewById(R.id.cardVennQuizC);
        diagramasVenn[0] = findViewById(R.id.vennQuizA);
        diagramasVenn[1] = findViewById(R.id.vennQuizB);
        diagramasVenn[2] = findViewById(R.id.vennQuizC);

        // Inicializar vistas Modo C: Fórmulas Booleanas
        tvFormulaQuiz = findViewById(R.id.tvFormulaQuiz);
        tvResultadoEsperadoQuiz = findViewById(R.id.tvResultadoEsperadoQuiz);
        btnFormulaOpIzqQuiz = findViewById(R.id.btnFormulaOpIzqQuiz);
        btnFormulaOpDerQuiz = findViewById(R.id.btnFormulaOpDerQuiz);

        configurarInteraccionesBloquesCodigo();
        configurarInteraccionesVenn();
        configurarInteraccionesFormula();

        btnComprobarQuiz.setOnClickListener(v -> comprobarRetoActual());

        // Paso 6 del Flujo Normal: Escoger las categorías en orden aleatorio y generar sus preguntas
        colaRetosQuiz.clear();
        ordenCategoriasAleatorias.clear();
        colaRetosQuiz.addAll(generarRetosQuiz(dificultadQuiz, preguntasPorCategoria, random, ordenCategoriasAleatorias));

        aciertosPorCategoria.clear();
        sumaPuntosPorCategoria.clear();
        for (String cat : ordenCategoriasAleatorias) {
            aciertosPorCategoria.put(cat, 0);
            sumaPuntosPorCategoria.put(cat, 0.0);
        }

        cargarSiguienteRetoQuiz();
        actualizarHudQuiz();
        init();
    }

    /**
     * Genera la secuencia completa del Modo Quiz recorriendo las categorías en orden aleatorio
     * y seleccionando N preguntas por cada categoría en la dificultad indicada.
     */
    public static List<RetoQuiz> generarRetosQuiz(
            int dificultad,
            int preguntasPorCategoria,
            Random rng,
            List<String> ordenCategoriasSalida) {

        Random generador = (rng != null) ? rng : new Random();
        int nivel = Math.max(1, Math.min(3, dificultad));
        int cantPorCat = Math.max(1, preguntasPorCategoria);

        List<String> categorias = new ArrayList<>(BancoPreguntasCategorias.CATEGORIAS_OFICIALES);
        Collections.shuffle(categorias, generador);

        if (ordenCategoriasSalida != null) {
            ordenCategoriasSalida.clear();
            ordenCategoriasSalida.addAll(categorias);
        }

        List<RetoQuiz> resultado = new ArrayList<>(categorias.size() * cantPorCat);
        int totalCats = categorias.size();

        for (int idxCat = 0; idxCat < totalCats; idxCat++) {
            String nombreCat = categorias.get(idxCat);
            int numCatVisible = idxCat + 1;

            List<RetoQuiz> bancoCategoria = construirBancoParaCategoria(
                    nivel, nombreCat, numCatVisible, totalCats, cantPorCat, generador);

            for (int numPreg = 1; numPreg <= cantPorCat; numPreg++) {
                RetoQuiz base = bancoCategoria.get((numPreg - 1) % bancoCategoria.size());
                if (base.tipo == TIPO_BLOQUES_CODIGO) {
                    resultado.add(new RetoQuiz(
                            nivel, nombreCat, numCatVisible, totalCats, numPreg, cantPorCat, base.preguntaBloque));
                } else if (base.tipo == TIPO_DIAGRAMA_VENN) {
                    resultado.add(new RetoQuiz(
                            nivel, nombreCat, numCatVisible, totalCats, numPreg, cantPorCat, base.preguntaVenn));
                } else {
                    resultado.add(new RetoQuiz(
                            nivel, nombreCat, numCatVisible, totalCats, numPreg, cantPorCat, base.preguntaFormula));
                }
            }
        }

        return resultado;
    }

    private static List<RetoQuiz> construirBancoParaCategoria(
            int nivel,
            String categoria,
            int numCatVisible,
            int totalCats,
            int cantPorCat,
            Random generador) {

        List<RetoQuiz> pool = new ArrayList<>();

        List<MinijuegoBloques.PreguntaBloque> bloques = BancoPreguntasCategorias.obtenerBanco(categoria, nivel);
        for (MinijuegoBloques.PreguntaBloque pb : bloques) {
            pool.add(new RetoQuiz(nivel, categoria, numCatVisible, totalCats, 1, cantPorCat, pb));
        }

        if ("Lógica".equalsIgnoreCase(categoria) || "Matemáticas discretas".equalsIgnoreCase(categoria)) {
            List<MinijuegoLogicaAlgoritmos.PreguntaVenn> venns = MinijuegoLogicaAlgoritmos.obtenerBancoPorDificultad(nivel);
            for (MinijuegoLogicaAlgoritmos.PreguntaVenn pv : venns) {
                pool.add(new RetoQuiz(nivel, categoria, numCatVisible, totalCats, 1, cantPorCat, pv));
            }
            List<MinijuegoLogica1.ReactivoFormula> formulas = MinijuegoLogica1.obtenerBancoPorDificultad(nivel);
            for (MinijuegoLogica1.ReactivoFormula pf : formulas) {
                pool.add(new RetoQuiz(nivel, categoria, numCatVisible, totalCats, 1, cantPorCat, pf));
            }
        }

        Collections.shuffle(pool, generador);
        return pool;
    }

    private void actualizarHudQuiz() {
        if (retoActual == null) return;
        double promActual = indiceRetoGlobal > 0 ? (sumaPuntosTotales / indiceRetoGlobal) : 0.0;
        String promStr = String.format(Locale.US, "%.1f", promActual);

        tvHudEstadoQuiz.setText("Cat " + retoActual.indiceCategoria + "/" + retoActual.totalCategorias
                + "  •  Pregunta " + retoActual.numeroPreguntaEnCategoria + "/" + retoActual.preguntasPorCategoria
                + "  •  Prom: " + promStr);
    }

    private void cargarSiguienteRetoQuiz() {
        validandoRespuesta = false;
        if (colaRetosQuiz.isEmpty()) {
            colaRetosQuiz.addAll(generarRetosQuiz(dificultadQuiz, preguntasPorCategoria, random, ordenCategoriasAleatorias));
        }

        retoActual = colaRetosQuiz.get(indiceRetoGlobal % colaRetosQuiz.size());
        this.dificultad = retoActual.dificultad;

        String nombreDif = (retoActual.dificultad == 1)
                ? "Fácil"
                : (retoActual.dificultad == 2 ? "Normal" : "Difícil");

        btnComprobarQuiz.setAlpha(0.75f);
        tvBadgeCategoriaQuiz.setText(
                "Categoría " + retoActual.indiceCategoria + " de " + retoActual.totalCategorias
                        + ": " + retoActual.categoria + "  •  Dificultad: " + nombreDif);

        if (retoActual.tipo == TIPO_BLOQUES_CODIGO) {
            panelRetoBloquesQuiz.setVisibility(View.VISIBLE);
            panelRetoVennQuiz.setVisibility(View.GONE);
            panelRetoFormulaQuiz.setVisibility(View.GONE);

            tvEnunciadoQuiz.setText(retoActual.preguntaBloque.enunciado);
            prepararRetoBloques(retoActual.preguntaBloque);

        } else if (retoActual.tipo == TIPO_DIAGRAMA_VENN) {
            panelRetoBloquesQuiz.setVisibility(View.GONE);
            panelRetoVennQuiz.setVisibility(View.VISIBLE);
            panelRetoFormulaQuiz.setVisibility(View.GONE);

            tvEnunciadoQuiz.setText(retoActual.preguntaVenn.enunciado);
            prepararRetoVenn(retoActual.preguntaVenn);

        } else {
            panelRetoBloquesQuiz.setVisibility(View.GONE);
            panelRetoVennQuiz.setVisibility(View.GONE);
            panelRetoFormulaQuiz.setVisibility(View.VISIBLE);

            tvEnunciadoQuiz.setText("Selecciona el valor para [ ? ] que cumple el resultado esperado:");
            prepararRetoFormula(retoActual.preguntaFormula);
        }
    }

    // ==================== MODO A: BLOQUES DE CÓDIGO ====================
    private void prepararRetoBloques(MinijuegoBloques.PreguntaBloque p) {
        indiceCodigoEnganchado = -1;
        zonaDropCodigoQuiz.setText("⬇ Arrastra el bloque aquí ⬇");
        zonaDropCodigoQuiz.setTextColor(0xFF415A77);
        zonaDropCodigoQuiz.setBackgroundResource(R.drawable.bg_drop_zone_empty);

        tvCodigoAntesQuiz.setText(p.codigoAntes);
        if (p.codigoDespues == null || p.codigoDespues.isEmpty()) {
            tvCodigoDespuesQuiz.setVisibility(View.GONE);
        } else {
            tvCodigoDespuesQuiz.setVisibility(View.VISIBLE);
            tvCodigoDespuesQuiz.setText(p.codigoDespues);
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
                    ClipData data = ClipData.newPlainText("indice_codigo_quiz", String.valueOf(idx));
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadow, v, 0);
                    v.setAlpha(0.35f);
                    return true;
                }
                return false;
            });
            bloque.setOnClickListener(v -> engancharBloqueCodigo(idx));
        }

        zonaDropCodigoQuiz.setOnClickListener(v -> {
            if (indiceCodigoEnganchado != -1) {
                vistasBloquesCodigo[indiceCodigoEnganchado].setVisibility(View.VISIBLE);
                vistasBloquesCodigo[indiceCodigoEnganchado].setAlpha(1.0f);
                indiceCodigoEnganchado = -1;
                zonaDropCodigoQuiz.setText("⬇ Arrastra el bloque aquí ⬇");
                zonaDropCodigoQuiz.setTextColor(0xFF415A77);
                zonaDropCodigoQuiz.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                btnComprobarQuiz.setAlpha(0.75f);
            }
        });

        zonaDropCodigoQuiz.setOnDragListener((v, event) -> {
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

        zonaDropCodigoQuiz.setText(opcionesCodigoMezcladas.get(idx));
        zonaDropCodigoQuiz.setTextColor(0xFFFFFFFF);
        zonaDropCodigoQuiz.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        btnComprobarQuiz.setAlpha(1.0f);
    }

    // ==================== MODO B: DIAGRAMAS DE VENN ====================
    private void prepararRetoVenn(MinijuegoLogicaAlgoritmos.PreguntaVenn p) {
        indiceVennEnganchado = -1;
        zonaDropVennQuiz.setBackgroundResource(R.drawable.bg_drop_zone_empty);
        tvEstadoDropVennQuiz.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
        tvEstadoDropVennQuiz.setTextColor(0xFF415A77);
        vennRespuestaQuiz.setVisibility(View.GONE);

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
                    ClipData data = ClipData.newPlainText("indice_venn_quiz", String.valueOf(idx));
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadow, v, 0);
                    v.setAlpha(0.35f);
                    return true;
                }
                return false;
            });
            card.setOnClickListener(v -> engancharTarjetaVenn(idx));
        }

        zonaDropVennQuiz.setOnClickListener(v -> {
            if (indiceVennEnganchado != -1) {
                tarjetasVenn[indiceVennEnganchado].setVisibility(View.VISIBLE);
                tarjetasVenn[indiceVennEnganchado].setAlpha(1.0f);
                indiceVennEnganchado = -1;
                zonaDropVennQuiz.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                tvEstadoDropVennQuiz.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
                tvEstadoDropVennQuiz.setTextColor(0xFF415A77);
                vennRespuestaQuiz.setVisibility(View.GONE);
                btnComprobarQuiz.setAlpha(0.75f);
            }
        });

        zonaDropVennQuiz.setOnDragListener((v, event) -> {
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

        zonaDropVennQuiz.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        tvEstadoDropVennQuiz.setText(etiquetasVenn[idx] + " colocada (Toca para cambiar)");
        tvEstadoDropVennQuiz.setTextColor(0xFFFFFFFF);
        vennRespuestaQuiz.setDiagramType(opcionesVennMezcladas.get(idx));
        vennRespuestaQuiz.setVisibility(View.VISIBLE);
        btnComprobarQuiz.setAlpha(1.0f);
    }

    // ==================== MODO C: FÓRMULAS BOOLEANAS ====================
    private void prepararRetoFormula(MinijuegoLogica1.ReactivoFormula pf) {
        seleccionFormula = null;
        resultadoEsperadoFormula = random.nextBoolean();
        respuestaCorrectaFormula = (pf.resultadoSiVerdadero == resultadoEsperadoFormula);

        botonesFormulaInvertidos = random.nextBoolean();
        if (!botonesFormulaInvertidos) {
            btnFormulaOpIzqQuiz.setText("VERDADERO\n(V)");
            btnFormulaOpDerQuiz.setText("FALSO\n(F)");
        } else {
            btnFormulaOpIzqQuiz.setText("FALSO\n(F)");
            btnFormulaOpDerQuiz.setText("VERDADERO\n(V)");
        }
        btnFormulaOpIzqQuiz.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnFormulaOpDerQuiz.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnFormulaOpIzqQuiz.setAlpha(0.9f);
        btnFormulaOpDerQuiz.setAlpha(0.9f);

        tvFormulaQuiz.setText(pf.plantillaFormula);
        tvFormulaQuiz.setTextColor(0xFF0D1B2A);
        tvFormulaQuiz.setBackgroundColor(0x00000000);
        tvFormulaQuiz.setPadding(0, 0, 0, 0);
        if (resultadoEsperadoFormula) {
            tvResultadoEsperadoQuiz.setText("VERDADERO (V)");
            tvResultadoEsperadoQuiz.setBackgroundResource(R.drawable.bg_badge_verde);
        } else {
            tvResultadoEsperadoQuiz.setText("FALSO (F)");
            tvResultadoEsperadoQuiz.setBackgroundResource(R.drawable.bg_bloque_codigo);
        }
    }

    private void configurarInteraccionesFormula() {
        btnFormulaOpIzqQuiz.setOnClickListener(v -> seleccionarValorFormula(!botonesFormulaInvertidos));
        btnFormulaOpDerQuiz.setOnClickListener(v -> seleccionarValorFormula(botonesFormulaInvertidos));
    }

    private void seleccionarValorFormula(boolean valor) {
        seleccionFormula = valor;
        String reemplazo = valor ? "[ V ]" : "[ F ]";
        tvFormulaQuiz.setText(retoActual.preguntaFormula.plantillaFormula.replace("[ ? ]", reemplazo));

        boolean izqElegido = (!botonesFormulaInvertidos && valor) || (botonesFormulaInvertidos && !valor);
        if (izqElegido) {
            btnFormulaOpIzqQuiz.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnFormulaOpIzqQuiz.setAlpha(1.0f);
            btnFormulaOpDerQuiz.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnFormulaOpDerQuiz.setAlpha(0.65f);
        } else {
            btnFormulaOpDerQuiz.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnFormulaOpDerQuiz.setAlpha(1.0f);
            btnFormulaOpIzqQuiz.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnFormulaOpIzqQuiz.setAlpha(0.65f);
        }
        btnComprobarQuiz.setAlpha(1.0f);
    }

    // ==================== VALIDACIÓN Y FLUJO DEL MODO QUIZ ====================
    private void comprobarRetoActual() {
        if (validandoRespuesta) return;
        boolean esCorrecto;

        if (retoActual.tipo == TIPO_BLOQUES_CODIGO) {
            if (indiceCodigoEnganchado == -1) {
                return;
            }
            String elegida = opcionesCodigoMezcladas.get(indiceCodigoEnganchado);
            esCorrecto = elegida.equals(retoActual.preguntaBloque.respuestaCorrecta);
            zonaDropCodigoQuiz.setBackgroundResource(esCorrecto ? R.drawable.bg_drop_zone_correct : R.drawable.bg_drop_zone_error);
            zonaDropCodigoQuiz.setTextColor(0xFFFFFFFF);
            zonaDropCodigoQuiz.setText((esCorrecto ? "✓  " : "✕  ") + elegida);

        } else if (retoActual.tipo == TIPO_DIAGRAMA_VENN) {
            if (indiceVennEnganchado == -1) {
                return;
            }
            int tipoElegido = opcionesVennMezcladas.get(indiceVennEnganchado);
            esCorrecto = (tipoElegido == retoActual.preguntaVenn.tipoCorrecto);
            zonaDropVennQuiz.setBackgroundResource(esCorrecto ? R.drawable.bg_drop_zone_correct : R.drawable.bg_drop_zone_error);
            tvEstadoDropVennQuiz.setTextColor(0xFFFFFFFF);
            tvEstadoDropVennQuiz.setText((esCorrecto ? "✓  " : "✕  ") + etiquetasVenn[indiceVennEnganchado]);

        } else {
            if (seleccionFormula == null) {
                return;
            }
            esCorrecto = (seleccionFormula == respuestaCorrectaFormula);
            String simb = seleccionFormula ? "[ V ]" : "[ F ]";
            String formulaConValor = retoActual.preguntaFormula.plantillaFormula.replace("[ ? ]", simb);
            tvFormulaQuiz.setBackgroundResource(esCorrecto ? R.drawable.bg_drop_zone_correct : R.drawable.bg_drop_zone_error);
            tvFormulaQuiz.setTextColor(0xFFFFFFFF);
            int pad = Math.round(12 * getResources().getDisplayMetrics().density);
            tvFormulaQuiz.setPadding(pad, pad / 2, pad, pad / 2);
            tvFormulaQuiz.setText((esCorrecto ? "✓  " : "✕  ") + formulaConValor);
        }

        validandoRespuesta = true;
        if (temporizadorReal != null) {
            temporizadorReal.cancel();
        }
        final boolean resultadoFinal = esCorrecto;
        btnComprobarQuiz.postDelayed(() -> procesarResultadoTurno(resultadoFinal), 550);
    }

    @Override
    protected void tiempoAgotado() {
        // Flujo alternativo 6.1: Se agota el tiempo -> 0 puntos y avanza al siguiente problema
        procesarResultadoTurno(false);
    }

    private void procesarResultadoTurno(boolean fueCorrecta) {
        EventoMinijuego evento = new EventoMinijuego(fueCorrecta, maxTime, frameTimer);
        double puntosGanados = evento.obtenerPuntos();
        String catActual = (retoActual != null && retoActual.categoria != null) ? retoActual.categoria : "Lógica";

        AjustesActivity.reproducirRetroalimentacion(this, btnComprobarQuiz, fueCorrecta);

        if (fueCorrecta) {
            aciertosTotales++;
            sumaPuntosTotales += puntosGanados;

            int prevAciertos = aciertosPorCategoria.containsKey(catActual) ? aciertosPorCategoria.get(catActual) : 0;
            double prevPuntos = sumaPuntosPorCategoria.containsKey(catActual) ? sumaPuntosPorCategoria.get(catActual) : 0.0;
            aciertosPorCategoria.put(catActual, prevAciertos + 1);
            sumaPuntosPorCategoria.put(catActual, prevPuntos + puntosGanados);
        }

        indiceRetoGlobal++;

        if (indiceRetoGlobal < colaRetosQuiz.size()) {
            cargarSiguienteRetoQuiz();
            actualizarHudQuiz();
            reiniciarMinijuego();
        } else {
            finalizarModoQuiz();
        }
    }

    /**
     * Paso 7 del Flujo Normal:
     * Una vez respondidas las preguntas de todas las categorías, calcula la puntuación final
     * y muestra la puntuación obtenida en cada categoría.
     */
    private void finalizarModoQuiz() {
        if (temporizadorReal != null) {
            temporizadorReal.cancel();
        }

        int totalReactivos = colaRetosQuiz.size();
        double promedioGeneral = totalReactivos > 0 ? (sumaPuntosTotales / totalReactivos) : 0.0;

        ArrayList<String> listaCats = new ArrayList<>(ordenCategoriasAleatorias);
        int[] arrAciertosCat = new int[listaCats.size()];
        double[] arrPromediosCat = new double[listaCats.size()];

        for (int i = 0; i < listaCats.size(); i++) {
            String cat = listaCats.get(i);
            int aciertosCat = aciertosPorCategoria.containsKey(cat) ? aciertosPorCategoria.get(cat) : 0;
            double sumaCat = sumaPuntosPorCategoria.containsKey(cat) ? sumaPuntosPorCategoria.get(cat) : 0.0;
            arrAciertosCat[i] = aciertosCat;
            arrPromediosCat[i] = preguntasPorCategoria > 0 ? (sumaCat / preguntasPorCategoria) : 0.0;
        }

        Intent finIntent = new Intent(this, FinQuizActivity.class);
        finIntent.putExtra("dificultad", dificultadQuiz);
        finIntent.putExtra("preguntasPorCategoria", preguntasPorCategoria);
        finIntent.putExtra("aciertosTotales", aciertosTotales);
        finIntent.putExtra("totalReactivos", totalReactivos);
        finIntent.putExtra("promedioGeneral", promedioGeneral);
        finIntent.putStringArrayListExtra("categoriasEvaluadas", listaCats);
        finIntent.putExtra("aciertosPorCategoria", arrAciertosCat);
        finIntent.putExtra("promediosPorCategoria", arrPromediosCat);
        startActivity(finIntent);
        finish();
    }

    @Override
    public void dibujarMinijuego() {
    }
}
