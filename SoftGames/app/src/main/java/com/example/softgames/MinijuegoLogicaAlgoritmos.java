package com.example.softgames;

import android.content.ClipData;
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
import java.util.List;

public class MinijuegoLogicaAlgoritmos extends Minijuego {

    public static class PreguntaVenn {
        public final String subtitulo;
        public final String enunciado;
        public final int tipoCorrecto;
        public final int distractor1;
        public final int distractor2;

        public PreguntaVenn(String subtitulo, String enunciado, int tipoCorrecto, int distractor1, int distractor2) {
            this.subtitulo = subtitulo;
            this.enunciado = enunciado;
            this.tipoCorrecto = tipoCorrecto;
            this.distractor1 = distractor1;
            this.distractor2 = distractor2;
        }
    }

    private String categoria = "Lógica";
    private String subtema = "Lógica 1";
    private int preguntasRespondidas = 0;
    private int totalPreguntas = 5;
    private int respuestasCorrectas = 0;
    private double puntosTotales = 0.0;
    private int sumaSegundosRestantes = 0;

    private final List<PreguntaVenn> bancoVenn = new ArrayList<>();
    private PreguntaVenn preguntaActual;
    private final List<Integer> tiposMezclados = new ArrayList<>();
    private final String[] etiquetasOpciones = {"Opción a)", "Opción b)", "Opción c)"};

    private int indiceBloqueEnganchado = -1;

    private TextView tvProgresoLogicaAlg;
    private TextView tvSubtituloVenn;
    private TextView tvEnunciadoVenn;
    private LinearLayout zonaDropVenn;
    private TextView tvEstadoDropVenn;
    private VennDiagramView vennRespuestaEnganchada;

    private final View[] tarjetasOpciones = new View[3];
    private final VennDiagramView[] diagramasOpciones = new VennDiagramView[3];
    private Button btnComprobarLogicaAlg;

    public MinijuegoLogicaAlgoritmos() {
        super(1200, null);
        this.dificultad = 1;
    }

    public MinijuegoLogicaAlgoritmos(EnrutadorMinijuego enrutador, int dificultad) {
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

        setContentView(R.layout.activity_minijuego_logica_algoritmos);

        tvProgresoLogicaAlg = findViewById(R.id.tvProgresoLogicaAlg);
        tvSubtituloVenn = findViewById(R.id.tvSubtituloVenn);
        tvEnunciadoVenn = findViewById(R.id.tvEnunciadoVenn);
        zonaDropVenn = findViewById(R.id.zonaDropVenn);
        tvEstadoDropVenn = findViewById(R.id.tvEstadoDropVenn);
        vennRespuestaEnganchada = findViewById(R.id.vennRespuestaEnganchada);

        tarjetasOpciones[0] = findViewById(R.id.cardOpcionA);
        tarjetasOpciones[1] = findViewById(R.id.cardOpcionB);
        tarjetasOpciones[2] = findViewById(R.id.cardOpcionC);

        diagramasOpciones[0] = findViewById(R.id.vennOpcionA);
        diagramasOpciones[1] = findViewById(R.id.vennOpcionB);
        diagramasOpciones[2] = findViewById(R.id.vennOpcionC);

        btnComprobarLogicaAlg = findViewById(R.id.btnComprobarLogicaAlg);

        inicializarBancoVenn();
        configurarDragAndDrop();
        mostrarSiguientePregunta();

        btnComprobarLogicaAlg.setOnClickListener(v -> validarRespuesta());

        init();
    }

    private void configurarDragAndDrop() {
        for (int i = 0; i < tarjetasOpciones.length; i++) {
            final int index = i;
            View tarjeta = tarjetasOpciones[i];

            tarjeta.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (v.getVisibility() != View.VISIBLE) {
                        return false;
                    }
                    ClipData data = ClipData.newPlainText("opcionVenn", String.valueOf(index));
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                    v.setAlpha(0.25f);
                    return true;
                }
                return false;
            });
        }

        // Tocar la zona de respuesta desengancha el bloque y lo devuelve a su lugar original
        zonaDropVenn.setOnClickListener(v -> {
            if (indiceBloqueEnganchado != -1) {
                restaurarTarjetaEnMenu(indiceBloqueEnganchado);
                limpiarZonaDrop();
            }
        });

        zonaDropVenn.setOnDragListener((v, event) -> {
            View vistaArrastrada = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    zonaDropVenn.setBackgroundResource(R.drawable.bg_drop_zone_hover);
                    zonaDropVenn.animate().scaleX(1.02f).scaleY(1.02f).setDuration(120).start();
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    zonaDropVenn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    if (indiceBloqueEnganchado == -1) {
                        zonaDropVenn.setBackgroundResource(R.drawable.bg_drop_zone_empty);
                    } else {
                        zonaDropVenn.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    zonaDropVenn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    int nuevoIndice = Integer.parseInt(item.getText().toString());

                    if (indiceBloqueEnganchado != -1 && indiceBloqueEnganchado != nuevoIndice) {
                        restaurarTarjetaEnMenu(indiceBloqueEnganchado);
                    }

                    // Paso 4 del Flujo Normal: Enganchar gráficamente el bloque de Diagrama de Venn
                    engancharDiagramaEnRespuesta(nuevoIndice);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    zonaDropVenn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    // Flujo Alternativo 3.1: Soltar el bloque fuera del área permitida lo regresa a su posición
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

    private void engancharDiagramaEnRespuesta(int indice) {
        indiceBloqueEnganchado = indice;
        int tipoDiagrama = tiposMezclados.get(indice);

        tarjetasOpciones[indice].setVisibility(View.INVISIBLE);
        tarjetasOpciones[indice].setAlpha(1.0f);

        zonaDropVenn.setBackgroundResource(R.drawable.bg_drop_zone_snapped);
        tvEstadoDropVenn.setText(etiquetasOpciones[indice] + " colocada (Toca para cambiar)");
        tvEstadoDropVenn.setTextColor(0xFFFFFFFF);

        vennRespuestaEnganchada.setDiagramType(tipoDiagrama);
        vennRespuestaEnganchada.setVisibility(View.VISIBLE);

        btnComprobarLogicaAlg.setAlpha(1.0f);
    }

    private void restaurarTarjetaEnMenu(int indice) {
        if (indice >= 0 && indice < tarjetasOpciones.length) {
            View tarjeta = tarjetasOpciones[indice];
            tarjeta.setVisibility(View.VISIBLE);
            tarjeta.setAlpha(1.0f);
        }
    }

    private void limpiarZonaDrop() {
        indiceBloqueEnganchado = -1;
        zonaDropVenn.setBackgroundResource(R.drawable.bg_drop_zone_empty);
        tvEstadoDropVenn.setText("⬇ Arrastra aquí la opción correcta (a, b o c) ⬇");
        tvEstadoDropVenn.setTextColor(0xFF415A77);
        vennRespuestaEnganchada.setVisibility(View.GONE);
        btnComprobarLogicaAlg.setAlpha(0.75f);
    }

    private void mostrarSiguientePregunta() {
        limpiarZonaDrop();

        if (bancoVenn.isEmpty()) {
            inicializarBancoVenn();
        }

        int idx = preguntasRespondidas % bancoVenn.size();
        preguntaActual = bancoVenn.get(idx);

        tvProgresoLogicaAlg.setText(categoria + " • " + (preguntasRespondidas + 1) + " / " + totalPreguntas);
        tvSubtituloVenn.setText(preguntaActual.subtitulo);
        tvEnunciadoVenn.setText(preguntaActual.enunciado);

        tiposMezclados.clear();
        tiposMezclados.add(preguntaActual.tipoCorrecto);
        tiposMezclados.add(preguntaActual.distractor1);
        tiposMezclados.add(preguntaActual.distractor2);
        Collections.shuffle(tiposMezclados);

        for (int i = 0; i < 3; i++) {
            tarjetasOpciones[i].setVisibility(View.VISIBLE);
            tarjetasOpciones[i].setAlpha(1.0f);
            diagramasOpciones[i].setDiagramType(tiposMezclados.get(i));
        }
    }

    private void validarRespuesta() {
        if (indiceBloqueEnganchado == -1) {
            Toast.makeText(this, "Arrastra una de las opciones a), b) o c) al área de respuesta", Toast.LENGTH_SHORT).show();
            return;
        }

        int tipoSeleccionado = tiposMezclados.get(indiceBloqueEnganchado);
        boolean esCorrecto = (tipoSeleccionado == preguntaActual.tipoCorrecto);

        EventoMinijuego eventoPregunta = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        double puntosPregunta = eventoPregunta.obtenerPuntos();
        int segsRestantes = Math.max(0, (int) ((frameTimer * 16L) / 1000L));
        sumaSegundosRestantes += segsRestantes;

        if (esCorrecto) {
            puntosTotales += puntosPregunta;
            String ptsFormateados = String.format(java.util.Locale.US, "%.1f", puntosPregunta);
            Toast.makeText(this, "¡Respuesta correcta! +" + ptsFormateados + " pts", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Respuesta incorrecta. Puntos: 0", Toast.LENGTH_SHORT).show();
        }

        avanzarSiguiente(esCorrecto);
    }

    @Override
    protected void tiempoAgotado() {
        avanzarSiguiente(false);
    }

    private void avanzarSiguiente(boolean fueCorrecta) {
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

    private void inicializarBancoVenn() {
        bancoVenn.clear();
        bancoVenn.addAll(obtenerBancoPorDificultad(dificultad));
    }

    public static List<PreguntaVenn> obtenerBancoPorDificultad(int dificultadNivel) {
        List<PreguntaVenn> lista = new ArrayList<>();

        if (dificultadNivel == 1) {
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • INTERSECCIÓN (A ∩ B)",
                    "Elementos que pertenecen al conjunto A y al conjunto B simultáneamente (A ∧ B):",
                    VennDiagramView.A_INTERSECT_B,
                    VennDiagramView.A_UNION_B,
                    VennDiagramView.ONLY_A
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • UNIÓN (A ∪ B)",
                    "Elementos que pertenecen al conjunto A, al conjunto B, o a ambos (A ∨ B):",
                    VennDiagramView.A_UNION_B,
                    VennDiagramView.A_INTERSECT_B,
                    VennDiagramView.COMPLEMENT_UNION_AB
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • DIFERENCIA (A - B)",
                    "Estudiantes que cursan la materia A pero NO cursan la materia B (A ∧ ¬B):",
                    VennDiagramView.ONLY_A,
                    VennDiagramView.ONLY_B,
                    VennDiagramView.A_INTERSECT_B
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • DIFERENCIA (B - A)",
                    "Elementos que pertenecen exclusivamente al conjunto B y no a A (B ∧ ¬A):",
                    VennDiagramView.ONLY_B,
                    VennDiagramView.ONLY_A,
                    VennDiagramView.A_UNION_B
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • IDENTIDAD (A)",
                    "Todos los elementos que pertenecen al conjunto A (sin importar si están en B):",
                    VennDiagramView.SET_A,
                    VennDiagramView.ONLY_A,
                    VennDiagramView.A_INTERSECT_B
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • CONJUNCIÓN LÓGICA",
                    "Programadores que dominan tanto Java (A) como Python (B) al mismo tiempo:",
                    VennDiagramView.A_INTERSECT_B,
                    VennDiagramView.SYM_DIFF_AB,
                    VennDiagramView.ONLY_B
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • DISYUNCIÓN INCLUSIVA",
                    "Proyectos que usan base de datos relacional (A) o documental (B), o ambas:",
                    VennDiagramView.A_UNION_B,
                    VennDiagramView.ONLY_A,
                    VennDiagramView.A_INTERSECT_B
            ));
            lista.add(new PreguntaVenn(
                    "CONJUNTOS • EXCLUSIÓN DE B",
                    "Dispositivos conectados a la red Wi-Fi (A) que NO tienen Bluetooth activo (B):",
                    VennDiagramView.ONLY_A,
                    VennDiagramView.A_UNION_B,
                    VennDiagramView.ONLY_B
            ));
        } else if (dificultadNivel == 2) {
            lista.add(new PreguntaVenn(
                    "DIFERENCIA SIMÉTRICA (A Δ B)",
                    "Elementos que pertenecen a A o a B, pero NO a ambos a la vez (XOR lógico):",
                    VennDiagramView.SYM_DIFF_AB,
                    VennDiagramView.A_INTERSECT_B,
                    VennDiagramView.A_UNION_B
            ));
            lista.add(new PreguntaVenn(
                    "LEY DE DE MORGAN • ¬(A ∪ B)",
                    "Elementos del universo que NO pertenecen ni al conjunto A ni al conjunto B:",
                    VennDiagramView.COMPLEMENT_UNION_AB,
                    VennDiagramView.A_UNION_B,
                    VennDiagramView.SYM_DIFF_AB
            ));
            lista.add(new PreguntaVenn(
                    "COMPLEMENTO LÓGICO (¬A)",
                    "Todos los elementos del universo U que NO pertenecen al conjunto A:",
                    VennDiagramView.COMPLEMENT_A,
                    VennDiagramView.ONLY_B,
                    VennDiagramView.SET_A
            ));
            lista.add(new PreguntaVenn(
                    "PROPOSICIÓN COMPUESTA • A ∧ ¬B",
                    "Selecciona el diagrama que cumple que la proposición A es verdadera y B es falsa:",
                    VennDiagramView.ONLY_A,
                    VennDiagramView.SYM_DIFF_AB,
                    VennDiagramView.COMPLEMENT_A
            ));
            lista.add(new PreguntaVenn(
                    "DISYUNCIÓN EXCLUSIVA",
                    "Usuarios que usan Android o iOS, pero no ambos dispositivos al mismo tiempo:",
                    VennDiagramView.SYM_DIFF_AB,
                    VennDiagramView.COMPLEMENT_UNION_AB,
                    VennDiagramView.A_INTERSECT_B
            ));
            lista.add(new PreguntaVenn(
                    "PROPOSICIÓN COMPUESTA • ¬A ∧ B",
                    "Elementos donde la condición A es falsa pero la condición B es verdadera:",
                    VennDiagramView.ONLY_B,
                    VennDiagramView.ONLY_A,
                    VennDiagramView.COMPLEMENT_UNION_AB
            ));
            lista.add(new PreguntaVenn(
                    "UNIÓN DE DIFERENCIAS • (A - B) ∪ (B - A)",
                    "Diagrama que representa la unión de (A - B) con (B - A):",
                    VennDiagramView.SYM_DIFF_AB,
                    VennDiagramView.A_UNION_B,
                    VennDiagramView.A_INTERSECT_B
            ));
            lista.add(new PreguntaVenn(
                    "EXTERIOR DE CONJUNTOS",
                    "Estudiantes del plantel (U) que no están inscritos ni en el club A ni en el club B:",
                    VennDiagramView.COMPLEMENT_UNION_AB,
                    VennDiagramView.COMPLEMENT_A,
                    VennDiagramView.ONLY_A
            ));
        } else {
            lista.add(new PreguntaVenn(
                    "TRES CONJUNTOS • (A ∩ B ∩ C)",
                    "Elementos que cumplen simultáneamente las condiciones de los conjuntos A, B y C:",
                    VennDiagramView.A_INTERSECT_B_INTERSECT_C,
                    VennDiagramView.A_INTERSECT_B_MINUS_C,
                    VennDiagramView.A_UNION_B_UNION_C
            ));
            lista.add(new PreguntaVenn(
                    "TRES CONJUNTOS • (A ∩ B) - C",
                    "Elementos que pertenecen tanto a A como a B, pero quedan excluidos de C:",
                    VennDiagramView.A_INTERSECT_B_MINUS_C,
                    VennDiagramView.A_INTERSECT_B_INTERSECT_C,
                    VennDiagramView.ONLY_C_3SET
            ));
            lista.add(new PreguntaVenn(
                    "TRES CONJUNTOS • C - (A ∪ B)",
                    "Elementos que pertenecen únicamente al conjunto C y no pertenecen ni a A ni a B:",
                    VennDiagramView.ONLY_C_3SET,
                    VennDiagramView.A_INTERSECT_B_INTERSECT_C,
                    VennDiagramView.A_UNION_B_UNION_C
            ));
            lista.add(new PreguntaVenn(
                    "TRES CONJUNTOS • (A ∪ B ∪ C)",
                    "Elementos que pertenecen a por lo menos uno de los tres conjuntos A, B o C:",
                    VennDiagramView.A_UNION_B_UNION_C,
                    VennDiagramView.A_INTERSECT_B_INTERSECT_C,
                    VennDiagramView.ONLY_C_3SET
            ));
            lista.add(new PreguntaVenn(
                    "EQUIVALENCIA DE DE MORGAN • ¬A ∧ ¬B",
                    "Diagrama equivalente a la expresión lógica (¬A ∧ ¬B) dentro del universo U:",
                    VennDiagramView.COMPLEMENT_UNION_AB,
                    VennDiagramView.SYM_DIFF_AB,
                    VennDiagramView.COMPLEMENT_A
            ));
            lista.add(new PreguntaVenn(
                    "PROPOSICIÓN TRIPLE • A ∧ B ∧ ¬C",
                    "Elementos donde A y B son verdaderas, pero la condición C es falsa:",
                    VennDiagramView.A_INTERSECT_B_MINUS_C,
                    VennDiagramView.ONLY_C_3SET,
                    VennDiagramView.A_INTERSECT_B_INTERSECT_C
            ));
            lista.add(new PreguntaVenn(
                    "PROPOSICIÓN TRIPLE • ¬A ∧ ¬B ∧ C",
                    "Elementos que cumplen únicamente la condición C y ninguna de las otras dos:",
                    VennDiagramView.ONLY_C_3SET,
                    VennDiagramView.A_INTERSECT_B_MINUS_C,
                    VennDiagramView.A_UNION_B_UNION_C
            ));
            lista.add(new PreguntaVenn(
                    "INTERSECCIÓN TOTAL • A ∧ B ∧ C",
                    "Sistemas que cumplen seguridad (A), rendimiento (B) y usabilidad (C) a la vez:",
                    VennDiagramView.A_INTERSECT_B_INTERSECT_C,
                    VennDiagramView.A_UNION_B_UNION_C,
                    VennDiagramView.ONLY_C_3SET
            ));
        }

        Collections.shuffle(lista);
        return lista;
    }

    @Override
    public void dibujarMinijuego() {
    }
}
