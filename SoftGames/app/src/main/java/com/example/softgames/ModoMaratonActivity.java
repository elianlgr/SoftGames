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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ModoMaratonActivity extends Minijuego {

    private static final int TIPO_BLOQUES_CODIGO = 0;
    private static final int TIPO_DIAGRAMA_VENN = 1;
    private static final int TIPO_FORMULA_BOOLEANA = 2;

    private static class RetoMaraton {
        final int tipo;
        final int dificultad;
        final String categoria;
        final MinijuegoBloques.PreguntaBloque preguntaBloque;
        final MinijuegoLogicaAlgoritmos.PreguntaVenn preguntaVenn;
        final MinijuegoLogica1.ReactivoFormula preguntaFormula;

        RetoMaraton(int dificultad, String categoria, MinijuegoBloques.PreguntaBloque p) {
            this.tipo = TIPO_BLOQUES_CODIGO;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.preguntaBloque = p;
            this.preguntaVenn = null;
            this.preguntaFormula = null;
        }

        RetoMaraton(int dificultad, String categoria, MinijuegoLogicaAlgoritmos.PreguntaVenn p) {
            this.tipo = TIPO_DIAGRAMA_VENN;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.preguntaBloque = null;
            this.preguntaVenn = p;
            this.preguntaFormula = null;
        }

        RetoMaraton(int dificultad, String categoria, MinijuegoLogica1.ReactivoFormula p) {
            this.tipo = TIPO_FORMULA_BOOLEANA;
            this.dificultad = dificultad;
            this.categoria = categoria;
            this.preguntaBloque = null;
            this.preguntaVenn = null;
            this.preguntaFormula = p;
        }
    }

    // Configuración de categorías (Paso 2 del Caso de Uso)
    private boolean modoGeneral = true;
    private ArrayList<String> categoriasSeleccionadas = new ArrayList<>();
    private final Map<String, Integer> problemasResueltosPorCat = new LinkedHashMap<>();
    private final Map<String, Double> puntosAcumuladosPorCat = new LinkedHashMap<>();

    // Estado del Modo Maratón (Flujo normal pasos 3 a 9)
    private int erroresAcumulados = 0; // Máximo 3 errores permitidos
    private int rachaActual = 0;
    private int mejorRachaPartida = 0;
    private int totalAciertos = 0;
    private int preguntasJugadas = 0;
    private int dificultadMaximaAlcanzada = 1;
    private double puntajeAcumulado = 0.0;

    private final List<RetoMaraton> colaFacil = new ArrayList<>();
    private final List<RetoMaraton> colaNormal = new ArrayList<>();
    private final List<RetoMaraton> colaDificil = new ArrayList<>();
    private int idxFacil = 0;
    private int idxNormal = 0;
    private int idxDificil = 0;

    private RetoMaraton retoActual;
    private final Random random = new Random();

    // Vistas HUD
    private TextView tvHudEstadoMaraton;
    private TextView tvBadgeDificultadMaraton;
    private TextView tvEnunciadoMaraton;
    private Button btnComprobarMaraton;

    // Paneles dinámicos
    private View panelRetoBloquesMaraton;
    private View panelRetoVennMaraton;
    private View panelRetoFormulaMaraton;

    // Sub-vistas Modo A: Bloques de Código
    private TextView tvCodigoAntesMaraton;
    private TextView tvCodigoDespuesMaraton;
    private TextView zonaDropCodigoMaraton;
    private final TextView[] vistasBloquesCodigo = new TextView[4];
    private final List<String> opcionesCodigoMezcladas = new ArrayList<>();
    private int indiceCodigoEnganchado = -1;

    // Sub-vistas Modo B: Diagramas de Venn
    private LinearLayout zonaDropVennMaraton;
    private TextView tvEstadoDropVennMaraton;
    private VennDiagramView vennRespuestaMaraton;
    private final View[] tarjetasVenn = new View[3];
    private final VennDiagramView[] diagramasVenn = new VennDiagramView[3];
    private final List<Integer> opcionesVennMezcladas = new ArrayList<>();
    private final String[] etiquetasVenn = {"Opción a)", "Opción b)", "Opción c)"};
    private int indiceVennEnganchado = -1;

    // Sub-vistas Modo C: Fórmulas Booleanas
    private TextView tvFormulaMaraton;
    private TextView tvResultadoEsperadoMaraton;
    private Button btnFormulaOpIzqMaraton;
    private Button btnFormulaOpDerMaraton;
    private boolean resultadoEsperadoFormula = true;
    private boolean respuestaCorrectaFormula = true;
    private Boolean seleccionFormula = null;
    private boolean botonesFormulaInvertidos = false;

    public ModoMaratonActivity() {
        super(1200, null);
        this.dificultad = 1;
        this.nombre = "Maratón";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_maraton);

        Intent intent = getIntent();
        if (intent != null) {
            modoGeneral = intent.getBooleanExtra("modoGeneral", true);
            ArrayList<String> recibidas = intent.getStringArrayListExtra("categoriasSeleccionadas");
            if (recibidas != null) {
                categoriasSeleccionadas = recibidas;
            }
        }

        inicializarContadoresPorCategoria();

        tvHudEstadoMaraton = findViewById(R.id.tvHudEstadoMaraton);
        tvBadgeDificultadMaraton = findViewById(R.id.tvBadgeDificultadMaraton);
        tvEnunciadoMaraton = findViewById(R.id.tvEnunciadoMaraton);
        btnComprobarMaraton = findViewById(R.id.btnComprobarMaraton);

        panelRetoBloquesMaraton = findViewById(R.id.panelRetoBloquesMaraton);
        panelRetoVennMaraton = findViewById(R.id.panelRetoVennMaraton);
        panelRetoFormulaMaraton = findViewById(R.id.panelRetoFormulaMaraton);

        // Inicializar vistas de Bloques de Código
        tvCodigoAntesMaraton = findViewById(R.id.tvCodigoAntesMaraton);
        tvCodigoDespuesMaraton = findViewById(R.id.tvCodigoDespuesMaraton);
        zonaDropCodigoMaraton = findViewById(R.id.zonaDropCodigoMaraton);
        vistasBloquesCodigo[0] = findViewById(R.id.bloqueMaraton1);
        vistasBloquesCodigo[1] = findViewById(R.id.bloqueMaraton2);
        vistasBloquesCodigo[2] = findViewById(R.id.bloqueMaraton3);
        vistasBloquesCodigo[3] = findViewById(R.id.bloqueMaraton4);

        // Inicializar vistas de Diagramas de Venn
        zonaDropVennMaraton = findViewById(R.id.zonaDropVennMaraton);
        tvEstadoDropVennMaraton = findViewById(R.id.tvEstadoDropVennMaraton);
        vennRespuestaMaraton = findViewById(R.id.vennRespuestaMaraton);
        tarjetasVenn[0] = findViewById(R.id.cardVennMaratonA);
        tarjetasVenn[1] = findViewById(R.id.cardVennMaratonB);
        tarjetasVenn[2] = findViewById(R.id.cardVennMaratonC);
        diagramasVenn[0] = findViewById(R.id.vennMaratonA);
        diagramasVenn[1] = findViewById(R.id.vennMaratonB);
        diagramasVenn[2] = findViewById(R.id.vennMaratonC);

        // Inicializar vistas de Fórmulas Booleanas
        tvFormulaMaraton = findViewById(R.id.tvFormulaMaraton);
        tvResultadoEsperadoMaraton = findViewById(R.id.tvResultadoEsperadoMaraton);
        btnFormulaOpIzqMaraton = findViewById(R.id.btnFormulaOpIzqMaraton);
        btnFormulaOpDerMaraton = findViewById(R.id.btnFormulaOpDerMaraton);

        configurarInteraccionesBloquesCodigo();
        configurarInteraccionesVenn();
        configurarInteraccionesFormula();

        btnComprobarMaraton.setOnClickListener(v -> comprobarRetoActual());

        construirColasAleatoriasPorDificultad();
        actualizarHudMaraton();
        cargarSiguienteRetoMaraton();
        init();
    }

    private void inicializarContadoresPorCategoria() {
        problemasResueltosPorCat.clear();
        puntosAcumuladosPorCat.clear();

        if (modoGeneral || categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            String[] todas = {"Lógica", "Programación", "Análisis y diseño de algoritmos"};
            for (String cat : todas) {
                problemasResueltosPorCat.put(cat, 0);
                puntosAcumuladosPorCat.put(cat, 0.0);
            }
        } else {
            for (String cat : categoriasSeleccionadas) {
                problemasResueltosPorCat.put(cat, 0);
                puntosAcumuladosPorCat.put(cat, 0.0);
            }
        }
    }

    private void construirColasAleatoriasPorDificultad() {
        colaFacil.clear();
        colaNormal.clear();
        colaDificil.clear();

        cargarNivelEnCola(1, colaFacil);
        cargarNivelEnCola(2, colaNormal);
        cargarNivelEnCola(3, colaDificil);
    }

    private void cargarNivelEnCola(int nivel, List<RetoMaraton> destino) {
        boolean incluirTodo = modoGeneral || categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty();
        boolean incProgramacion = incluirTodo || categoriasSeleccionadas.contains("Programación");
        boolean incLogica = incluirTodo || categoriasSeleccionadas.contains("Lógica");
        boolean incAlgoritmos = incluirTodo || categoriasSeleccionadas.contains("Análisis y diseño de algoritmos");

        if (incProgramacion || incAlgoritmos) {
            List<MinijuegoBloques.PreguntaBloque> bancoBloques = MinijuegoBloques.obtenerBancoPorDificultad(nivel);
            for (int i = 0; i < bancoBloques.size(); i++) {
                String catAsignada;
                if (incProgramacion && incAlgoritmos) {
                    catAsignada = (i < 6) ? "Programación" : "Análisis y diseño de algoritmos";
                } else if (incProgramacion) {
                    catAsignada = "Programación";
                } else {
                    catAsignada = "Análisis y diseño de algoritmos";
                }
                destino.add(new RetoMaraton(nivel, catAsignada, bancoBloques.get(i)));
            }
        }

        if (incLogica || incAlgoritmos) {
            List<MinijuegoLogicaAlgoritmos.PreguntaVenn> bancoVenn = MinijuegoLogicaAlgoritmos.obtenerBancoPorDificultad(nivel);
            for (int i = 0; i < bancoVenn.size(); i++) {
                String catAsignada;
                if (incLogica && incAlgoritmos) {
                    catAsignada = (i < 4) ? "Lógica" : "Análisis y diseño de algoritmos";
                } else if (incLogica) {
                    catAsignada = "Lógica";
                } else {
                    catAsignada = "Análisis y diseño de algoritmos";
                }
                destino.add(new RetoMaraton(nivel, catAsignada, bancoVenn.get(i)));
            }
        }

        if (incLogica) {
            for (MinijuegoLogica1.ReactivoFormula pf : MinijuegoLogica1.obtenerBancoPorDificultad(nivel)) {
                destino.add(new RetoMaraton(nivel, "Lógica", pf));
            }
        }

        // Respaldo de seguridad si ninguna condición coincidió
        if (destino.isEmpty()) {
            for (MinijuegoBloques.PreguntaBloque pb : MinijuegoBloques.obtenerBancoPorDificultad(nivel)) {
                destino.add(new RetoMaraton(nivel, "Programación", pb));
            }
            for (MinijuegoLogicaAlgoritmos.PreguntaVenn pv : MinijuegoLogicaAlgoritmos.obtenerBancoPorDificultad(nivel)) {
                destino.add(new RetoMaraton(nivel, "Análisis y diseño de algoritmos", pv));
            }
            for (MinijuegoLogica1.ReactivoFormula pf : MinijuegoLogica1.obtenerBancoPorDificultad(nivel)) {
                destino.add(new RetoMaraton(nivel, "Lógica", pf));
            }
        }

        Collections.shuffle(destino, random);
    }

    /**
     * Progresión gradual de dificultad en Modo Maratón:
     * - Primeras 12 preguntas: Fácil (1)
     * - Preguntas 13 a 26: Normal (2)
     * - Pregunta 27 en adelante: Difícil (3)
     */
    private int calcularDificultadActual() {
        if (preguntasJugadas < 12) {
            return 1;
        } else if (preguntasJugadas < 26) {
            return 2;
        } else {
            return 3;
        }
    }

    private RetoMaraton extraerSiguienteReto(int nivel) {
        if (nivel == 1) {
            if (idxFacil >= colaFacil.size()) {
                Collections.shuffle(colaFacil, random);
                idxFacil = 0;
            }
            return colaFacil.get(idxFacil++);
        } else if (nivel == 2) {
            if (idxNormal >= colaNormal.size()) {
                Collections.shuffle(colaNormal, random);
                idxNormal = 0;
            }
            return colaNormal.get(idxNormal++);
        } else {
            if (idxDificil >= colaDificil.size()) {
                Collections.shuffle(colaDificil, random);
                idxDificil = 0;
            }
            return colaDificil.get(idxDificil++);
        }
    }

    private void actualizarHudMaraton() {
        int vidasRestantes = Math.max(0, 3 - erroresAcumulados);
        StringBuilder corazones = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) corazones.append(" ");
            corazones.append(i < vidasRestantes ? "♥" : "♡");
        }
        String ptsStr = String.format(Locale.US, "%.1f pts", puntajeAcumulado);
        tvHudEstadoMaraton.setText(corazones + "  •  Racha: " + rachaActual + "  •  " + ptsStr);
    }

    private void cargarSiguienteRetoMaraton() {
        this.dificultad = calcularDificultadActual();
        if (this.dificultad > dificultadMaximaAlcanzada) {
            dificultadMaximaAlcanzada = this.dificultad;
        }

        retoActual = extraerSiguienteReto(this.dificultad);

        String nombreDif = (dificultad == 1) ? "Fácil" : (dificultad == 2 ? "Normal" : "Difícil");
        btnComprobarMaraton.setAlpha(0.75f);
        tvBadgeDificultadMaraton.setText(retoActual.categoria + " • Dificultad: " + nombreDif);

        if (retoActual.tipo == TIPO_BLOQUES_CODIGO) {
            panelRetoBloquesMaraton.setVisibility(View.VISIBLE);
            panelRetoVennMaraton.setVisibility(View.GONE);
            panelRetoFormulaMaraton.setVisibility(View.GONE);

            tvEnunciadoMaraton.setText(retoActual.preguntaBloque.enunciado);
            prepararRetoBloques(retoActual.preguntaBloque);

        } else if (retoActual.tipo == TIPO_DIAGRAMA_VENN) {
            panelRetoBloquesMaraton.setVisibility(View.GONE);
            panelRetoVennMaraton.setVisibility(View.VISIBLE);
            panelRetoFormulaMaraton.setVisibility(View.GONE);

            tvEnunciadoMaraton.setText(retoActual.preguntaVenn.enunciado);
            prepararRetoVenn(retoActual.preguntaVenn);

        } else {
            panelRetoBloquesMaraton.setVisibility(View.GONE);
            panelRetoVennMaraton.setVisibility(View.GONE);
            panelRetoFormulaMaraton.setVisibility(View.VISIBLE);

            tvEnunciadoMaraton.setText("Selecciona el valor para [ ? ] que cumple el resultado esperado:");
            prepararRetoFormula(retoActual.preguntaFormula);
        }
    }

    // ==================== MODO A: BLOQUES DE CÓDIGO ====================
    private void prepararRetoBloques(MinijuegoBloques.PreguntaBloque p) {
        indiceCodigoEnganchado = -1;
        zonaDropCodigoMaraton.setText("⬇ Arrastra el bloque aquí ⬇");
        zonaDropCodigoMaraton.setTextColor(0xFF415A77);
        zonaDropCodigoMaraton.setBackgroundResource(R.drawable.bg_drop_zone_empty);

        tvCodigoAntesMaraton.setText(p.codigoAntes);
        if (p.codigoDespues == null || p.codigoDespues.isEmpty()) {
            tvCodigoDespuesMaraton.setVisibility(View.GONE);
        } else {
            tvCodigoDespuesMaraton.setVisibility(View.VISIBLE);
            tvCodigoDespuesMaraton.setText(p.codigoDespues);
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
                    ClipData data = ClipData.newPlainText("indice_codigo", String.valueOf(idx));
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadow, v, 0);
                    v.setAlpha(0.35f);
                    return true;
                }
                return false;
            });
            bloque.setOnClickListener(v -> engancharBloqueCodigo(idx));
        }

        zonaDropCodigoMaraton.setOnClickListener(v -> {
            if (indiceCodigoEnganchado != -1) {
                vistasBloquesCodigo[indiceCodigoEnganchado].setVisibility(View.VISIBLE);
                vistasBloquesCodigo[indiceCodigoEnganchado].setAlpha(1.0f);
                indiceCodigoEnganchado = -1;
                zonaDropCodigoMaraton.setText("⬇ Arrastra el bloque aquí ⬇");
                zonaDropCodigoMaraton.setTextColor(0xFF415A77);
                zonaDropCodigoMaraton.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                btnComprobarMaraton.setAlpha(0.75f);
            }
        });

        zonaDropCodigoMaraton.setOnDragListener((v, event) -> {
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

        zonaDropCodigoMaraton.setText(opcionesCodigoMezcladas.get(idx));
        zonaDropCodigoMaraton.setTextColor(0xFFFFFFFF);
        zonaDropCodigoMaraton.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        btnComprobarMaraton.setAlpha(1.0f);
    }

    // ==================== MODO B: DIAGRAMAS DE VENN ====================
    private void prepararRetoVenn(MinijuegoLogicaAlgoritmos.PreguntaVenn p) {
        indiceVennEnganchado = -1;
        zonaDropVennMaraton.setBackgroundResource(R.drawable.bg_drop_zone_empty);
        tvEstadoDropVennMaraton.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
        tvEstadoDropVennMaraton.setTextColor(0xFF415A77);
        vennRespuestaMaraton.setVisibility(View.GONE);

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
                    ClipData data = ClipData.newPlainText("indice_venn", String.valueOf(idx));
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadow, v, 0);
                    v.setAlpha(0.35f);
                    return true;
                }
                return false;
            });
            card.setOnClickListener(v -> engancharTarjetaVenn(idx));
        }

        zonaDropVennMaraton.setOnClickListener(v -> {
            if (indiceVennEnganchado != -1) {
                tarjetasVenn[indiceVennEnganchado].setVisibility(View.VISIBLE);
                tarjetasVenn[indiceVennEnganchado].setAlpha(1.0f);
                indiceVennEnganchado = -1;
                zonaDropVennMaraton.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                tvEstadoDropVennMaraton.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
                tvEstadoDropVennMaraton.setTextColor(0xFF415A77);
                vennRespuestaMaraton.setVisibility(View.GONE);
                btnComprobarMaraton.setAlpha(0.75f);
            }
        });

        zonaDropVennMaraton.setOnDragListener((v, event) -> {
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

        zonaDropVennMaraton.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        tvEstadoDropVennMaraton.setText(etiquetasVenn[idx] + " colocada (Toca para cambiar)");
        tvEstadoDropVennMaraton.setTextColor(0xFFFFFFFF);
        vennRespuestaMaraton.setDiagramType(opcionesVennMezcladas.get(idx));
        vennRespuestaMaraton.setVisibility(View.VISIBLE);
        btnComprobarMaraton.setAlpha(1.0f);
    }

    // ==================== MODO C: FÓRMULAS BOOLEANAS ====================
    private void prepararRetoFormula(MinijuegoLogica1.ReactivoFormula pf) {
        seleccionFormula = null;
        resultadoEsperadoFormula = random.nextBoolean();
        respuestaCorrectaFormula = (pf.resultadoSiVerdadero == resultadoEsperadoFormula);

        botonesFormulaInvertidos = random.nextBoolean();
        if (!botonesFormulaInvertidos) {
            btnFormulaOpIzqMaraton.setText("VERDADERO\n(V)");
            btnFormulaOpDerMaraton.setText("FALSO\n(F)");
        } else {
            btnFormulaOpIzqMaraton.setText("FALSO\n(F)");
            btnFormulaOpDerMaraton.setText("VERDADERO\n(V)");
        }
        btnFormulaOpIzqMaraton.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnFormulaOpDerMaraton.setBackgroundResource(R.drawable.bg_bloque_codigo);
        btnFormulaOpIzqMaraton.setAlpha(0.9f);
        btnFormulaOpDerMaraton.setAlpha(0.9f);

        tvFormulaMaraton.setText(pf.plantillaFormula);
        if (resultadoEsperadoFormula) {
            tvResultadoEsperadoMaraton.setText("VERDADERO (V)");
            tvResultadoEsperadoMaraton.setBackgroundResource(R.drawable.bg_badge_verde);
        } else {
            tvResultadoEsperadoMaraton.setText("FALSO (F)");
            tvResultadoEsperadoMaraton.setBackgroundResource(R.drawable.bg_bloque_codigo);
        }
    }

    private void configurarInteraccionesFormula() {
        btnFormulaOpIzqMaraton.setOnClickListener(v -> seleccionarValorFormula(!botonesFormulaInvertidos));
        btnFormulaOpDerMaraton.setOnClickListener(v -> seleccionarValorFormula(botonesFormulaInvertidos));
    }

    private void seleccionarValorFormula(boolean valor) {
        seleccionFormula = valor;
        String reemplazo = valor ? "[ V ]" : "[ F ]";
        tvFormulaMaraton.setText(retoActual.preguntaFormula.plantillaFormula.replace("[ ? ]", reemplazo));

        boolean izqElegido = (!botonesFormulaInvertidos && valor) || (botonesFormulaInvertidos && !valor);
        if (izqElegido) {
            btnFormulaOpIzqMaraton.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnFormulaOpIzqMaraton.setAlpha(1.0f);
            btnFormulaOpDerMaraton.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnFormulaOpDerMaraton.setAlpha(0.65f);
        } else {
            btnFormulaOpDerMaraton.setBackgroundResource(R.drawable.bg_btn_crear_cuenta);
            btnFormulaOpDerMaraton.setAlpha(1.0f);
            btnFormulaOpIzqMaraton.setBackgroundResource(R.drawable.bg_bloque_codigo);
            btnFormulaOpIzqMaraton.setAlpha(0.65f);
        }
        btnComprobarMaraton.setAlpha(1.0f);
    }

    // ==================== VALIDACIÓN Y FLUJO DEL MARATÓN ====================
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
        procesarResultadoTurno(false);
    }

    private void procesarResultadoTurno(boolean fueCorrecta) {
        preguntasJugadas++;

        if (fueCorrecta) {
            EventoMinijuego evento = new EventoMinijuego(true, maxTime, frameTimer);
            double puntosGanados = evento.obtenerPuntos();
            puntajeAcumulado += puntosGanados;
            rachaActual++;
            totalAciertos++;
            if (rachaActual > mejorRachaPartida) {
                mejorRachaPartida = rachaActual;
            }

            if (retoActual != null && retoActual.categoria != null) {
                String cat = retoActual.categoria;
                int prevResueltos = problemasResueltosPorCat.containsKey(cat) ? problemasResueltosPorCat.get(cat) : 0;
                double prevPuntos = puntosAcumuladosPorCat.containsKey(cat) ? puntosAcumuladosPorCat.get(cat) : 0.0;
                problemasResueltosPorCat.put(cat, prevResueltos + 1);
                puntosAcumuladosPorCat.put(cat, prevPuntos + puntosGanados);
            }

            String ptsStr = String.format(Locale.US, "%.1f", puntosGanados);
            Toast.makeText(this, "¡Correcto! +" + ptsStr + " pts (Racha: " + rachaActual + ")", Toast.LENGTH_SHORT).show();
        } else {
            // Flujo alternativo 5.1: Incrementar errores y mostrar intentos restantes
            erroresAcumulados++;
            rachaActual = 0;
            int intentosRestantes = Math.max(0, 3 - erroresAcumulados);
            if (intentosRestantes > 0) {
                Toast.makeText(this,
                        "Respuesta incorrecta. Intentos restantes: " + intentosRestantes,
                        Toast.LENGTH_SHORT).show();
            }
        }

        actualizarHudMaraton();

        // Paso 8 del flujo normal: Al cometer el tercer error, finalizar la partida del modo Maratón
        if (erroresAcumulados >= 3) {
            finalizarMaraton();
        } else {
            cargarSiguienteRetoMaraton();
            reiniciarMinijuego();
        }
    }

    private void finalizarMaraton() {
        if (temporizadorReal != null) {
            temporizadorReal.cancel();
        }

        ArrayList<String> categoriasResumen = new ArrayList<>(problemasResueltosPorCat.keySet());
        int[] resueltosPorCategoria = new int[categoriasResumen.size()];
        double[] puntosPorCategoria = new double[categoriasResumen.size()];

        for (int i = 0; i < categoriasResumen.size(); i++) {
            String cat = categoriasResumen.get(i);
            Integer res = problemasResueltosPorCat.get(cat);
            Double pts = puntosAcumuladosPorCat.get(cat);
            resueltosPorCategoria[i] = (res != null) ? res : 0;
            puntosPorCategoria[i] = (pts != null) ? pts : 0.0;
        }

        Intent finIntent = new Intent(this, FinMaratonActivity.class);
        finIntent.putExtra("puntajeTotal", puntajeAcumulado);
        finIntent.putExtra("mejorRacha", mejorRachaPartida);
        finIntent.putExtra("totalAciertos", totalAciertos);
        finIntent.putExtra("dificultadAlcanzada", dificultadMaximaAlcanzada);
        finIntent.putExtra("erroresAcumulados", erroresAcumulados);
        finIntent.putExtra("modoGeneral", modoGeneral);
        finIntent.putStringArrayListExtra("categoriasSeleccionadas", categoriasSeleccionadas);
        finIntent.putStringArrayListExtra("categoriasResumen", categoriasResumen);
        finIntent.putExtra("resueltosPorCategoria", resueltosPorCategoria);
        finIntent.putExtra("puntosPorCategoria", puntosPorCategoria);
        startActivity(finIntent);
        finish();
    }

    @Override
    public void dibujarMinijuego() {
    }
}
