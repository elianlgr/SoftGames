package com.example.softgames;

import android.content.ClipData;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private TextView tvProgresoBloques;
    private TextView tvEnunciadoBloques;
    private TextView tvSubtituloArea;
    private TextView tvCodigoAntes;
    private TextView tvCodigoDespues;
    private TextView zonaRespuestaBloque;
    private final TextView[] vistasBloques = new TextView[4];
    private Button btnComprobarBloque;

    public MinijuegoBloques() {
        super(1200, null); // ~19 segundos por pregunta
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

        // Inicializar temporizador y menú de pausa de la clase padre Minijuego
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

        // Permitir tocar la zona de respuesta para desenganchar el bloque y devolverlo al menú
        zonaRespuestaBloque.setOnClickListener(v -> {
            if (indiceBloqueEnganchado != -1) {
                devolverBloqueAlMenu(indiceBloqueEnganchado);
                limpiarZonaRespuesta();
            }
        });

        // Listener de Drag & Drop en el área de respuesta (Flujo normal y Flujo alternativo 3.1)
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

                    // Si ya había un bloque enganchado antes, devolverlo al menú de opciones
                    if (indiceBloqueEnganchado != -1 && indiceBloqueEnganchado != nuevoIndice) {
                        devolverBloqueAlMenu(indiceBloqueEnganchado);
                    }

                    // Paso 4 del Flujo Normal: Enganchar gráficamente el bloque dentro del área de respuesta
                    engancharBloqueEnArea(nuevoIndice);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    zonaRespuestaBloque.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    // Flujo Alternativo 3.1: Soltar el bloque fuera del área permitida
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
        if (indiceBloqueEnganchado == -1) {
            Toast.makeText(this, "Arrastra un bloque al área de respuesta primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String seleccion = opcionesMezcladas.get(indiceBloqueEnganchado);
        boolean esCorrecto = seleccion.equals(preguntaActual.respuestaCorrecta);

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

        avanzarTurno(esCorrecto);
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

        // Categoría: Programación (Variables, Condicionales, Bucles y Funciones por Bloques)
        if (dificultad == 1) {
            bancoActual.add(new PreguntaBloque(
                    "Identificación de Código",
                    "¿A qué opción pertenece este fragmento de código?",
                    "int edad = 19;\ndouble promedio = 9.5;\nString curso = \"SoftGames\";",
                    "",
                    "Declaración de variables",
                    "Ciclo repetitivo (Bucle)",
                    "Estructura condicional",
                    "Función recursiva"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Completar Algoritmo Básico",
                    "Coloca el bloque correcto para completar el fragmento de código:",
                    "int num1 = 8;\nint num2 = 12;",
                    "System.out.println(resultado);",
                    "int resultado = num1 + num2;",
                    "num1 + num2 = resultado;",
                    "String resultado = \"num1+num2\";",
                    "return num1 == num2;"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Cálculo de Área",
                    "Coloca el bloque correcto para calcular el área del triángulo:",
                    "double base = 10.0;\ndouble altura = 5.0;",
                    "System.out.println(\"Área: \" + area);",
                    "double area = (base * altura) / 2;",
                    "boolean area = (base == altura);",
                    "int area = base + altura;",
                    "String area = \"base * altura\";"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Salida de Datos en Consola",
                    "Arrastra el bloque que muestra el saludo en pantalla:",
                    "String nombre = \"Estudiante\";",
                    "// Fin del programa",
                    "System.out.println(\"Hola \" + nombre);",
                    "Scanner.leer(nombre);",
                    "int nombre = 0;",
                    "while (nombre != null)"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Tipos de Datos",
                    "Coloca el bloque que declara correctamente un valor Verdadero/Falso:",
                    "// Variable que indica si el juego terminó:",
                    "if (!juegoTerminado) {\n    continuarPartida();\n}",
                    "boolean juegoTerminado = false;",
                    "int juegoTerminado = \"false\";",
                    "char juegoTerminado = 0;",
                    "double juegoTerminado = true;"
            ));
        } else if (dificultad == 2) {
            bancoActual.add(new PreguntaBloque(
                    "Estructura Condicional",
                    "Arrastra el bloque que completa la estructura condicional:",
                    "if (calificacion >= 70) {\n    mostrar(\"Aprobado\");",
                    "    mostrar(\"Reprobado\");\n}",
                    "} else {",
                    "} while {",
                    "} for (int i = 0) {",
                    "} case 70:"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Verificación de Número Par",
                    "Coloca el bloque con la condición correcta para saber si 'numero' es par:",
                    "int numero = 14;",
                    "    System.out.println(\"Es Par\");\n}",
                    "if (numero % 2 == 0) {",
                    "if (numero / 2 == 0) {",
                    "while (numero > 2) {",
                    "int par = numero % 2;"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Identificación de Estructura",
                    "¿A qué opción pertenece este fragmento de código?",
                    "switch (opcionMenu) {\n  case 1: iniciarPartida(); break;\n  case 2: abrirAjustes(); break;\n  default: salir();\n}",
                    "",
                    "Condicional múltiple (switch)",
                    "Bucle infinito (while)",
                    "Arreglo unidimensional",
                    "Declaración de constantes"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Completar Función",
                    "Coloca el bloque que finaliza la función devolviendo el número mayor:",
                    "int obtenerMayor(int a, int b) {\n    if (a > b) {\n        return a;\n    }",
                    "}",
                    "return b;",
                    "return 0;",
                    "int b = a;",
                    "break;"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Acumulador en Bucle",
                    "Arrastra el bloque que incrementa el contador en 1 en cada repetición:",
                    "int i = 1;\nwhile (i <= 10) {\n    imprimir(i);",
                    "}",
                    "i = i + 1;",
                    "i = 1;",
                    "if (i == 10)",
                    "return i;"
            ));
        } else {
            bancoActual.add(new PreguntaBloque(
                    "Recorrido de Arreglos",
                    "Coloca el encabezado correcto para recorrer todos los elementos del arreglo:",
                    "int suma = 0;",
                    "    suma += puntos[i];\n}",
                    "for (int i = 0; i < puntos.length; i++) {",
                    "for (int i = 1; i > puntos.length; i--) {",
                    "while (puntos.length == 0) {",
                    "if (i < puntos.length) {"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Acumulador con Ciclo For",
                    "Coloca el bloque que acumula la suma de los números del 1 al N:",
                    "int suma = 0;\nfor (int i = 1; i <= n; i++) {",
                    "}\nreturn suma;",
                    "suma = suma + i;",
                    "i = 0; suma = 0;",
                    "if (suma == 0) break;",
                    "return i;"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Función Recursiva",
                    "Arrastra el bloque que define el caso base para evitar recursión infinita:",
                    "int factorial(int n) {",
                    "    return n * factorial(n - 1);\n}",
                    "if (n <= 1) return 1;",
                    "while (n > 0) return n;",
                    "factorial(n + 1);",
                    "int n = 1;"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Identificación de Algoritmo",
                    "¿A qué opción pertenece este fragmento de código?",
                    "for (int i = 0; i < lista.length; i++) {\n    if (lista[i] == objetivo) {\n        return i;\n    }\n}",
                    "",
                    "Búsqueda secuencial (lineal)",
                    "Ordenamiento burbuja",
                    "Árbol binario de búsqueda",
                    "Pila LIFO"
            ));
            bancoActual.add(new PreguntaBloque(
                    "Intercambio de Variables (Swap)",
                    "Coloca el bloque que completa el intercambio de valores entre 'a' y 'b':",
                    "int temp = a;\na = b;",
                    "// Ahora 'a' y 'b' intercambiaron sus valores",
                    "b = temp;",
                    "temp = b;",
                    "a = temp;",
                    "b = a;"
            ));
        }
    }

    @Override
    public void dibujarMinijuego() {
    }
}
