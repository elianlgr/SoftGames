package com.example.softgames;

import static com.example.softgames.Utils.numeroAleatorio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MinijuegoLogica1 extends Minijuego{
    String[] formulas = {
            "? & V",
            "V & ? | F",
            "V & -?",
            "-(F | ?)",
            "V | ? | V",
            "V | -? | V",
            "-(-V | ?)",
            "(? & V) & ?",
            "(V & F) | (? & V)",
            "(? & F) | (-? & V)",
            "(? | -?) & (V & -?)",
            "((? & V) | -?) | -(V & -(-? & V))"
    };
    boolean[] respuestas ={
            true,
            true,
            false,
            false,
            true,
            false,
            true,
            true,
            false,
            false,
            false,
            true
    };
    int indiceFormula;
    boolean respuestaB;
    public MinijuegoLogica1(EnrutadorMinijuego enrutador, int dificultad){
        super(1000, enrutador);
        this.dificultad = dificultad;
        generarFormula();
    }

    public MinijuegoLogica1(){
        super(1000,null);
        dificultad = 1;
        generarFormula();
    }
    private int preguntasRespondidas = 0;
    private int totalPreguntas = 5;
    private int respuestasCorrectas = 0;
    private double puntosTotales = 0.0;
    private int sumaSegundosRestantes = 0;
    private String categoria = "Lógica";
    private String subtema = "Lógica 2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Leer configuración del Intent (viene de las pantallas de selección)
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
        // Generar la primera fórmula con la dificultad elegida
        generarFormula();

        setContentView(R.layout.logic_minigame1_layout);
        Button trueBtn = findViewById(R.id.trueButton);
        Button falseBtn = findViewById(R.id.falseButton);
        trueBtn.setOnClickListener(v -> enviarRespuesta(true));
        falseBtn.setOnClickListener(v -> enviarRespuesta(false));
        TextView formulaTexto = findViewById(R.id.textoFormula);
        String texto = formulas[indiceFormula] + " = " + respuestaB;
        formulaTexto.setText(texto);

        // Iniciar el temporizador base
        init();
    }

    public void enviarRespuesta(boolean respuestaFinal){
        boolean esCorrecto = (respuestaFinal == respuestas[indiceFormula]) == respuestaB;
        EventoMinijuego eventoPregunta = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        double puntosPregunta = eventoPregunta.obtenerPuntos();
        int segsRestantes = Math.max(0, (int) ((frameTimer * 16L) / 1000L));
        sumaSegundosRestantes += segsRestantes;

        if (esCorrecto) {
            puntosTotales += puntosPregunta;
            String ptsStr = String.format(java.util.Locale.US, "%.1f", puntosPregunta);
            android.widget.Toast.makeText(this, "¡Respuesta correcta! +" + ptsStr + " pts", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            android.widget.Toast.makeText(this, "Respuesta incorrecta. Puntos: 0", android.widget.Toast.LENGTH_SHORT).show();
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
            // Generar siguiente pregunta y actualizar UI
            generarFormula();
            TextView formulaTexto = findViewById(R.id.textoFormula);
            String texto = formulas[indiceFormula] + " = " + respuestaB;
            formulaTexto.setText(texto);
            
            // Reiniciar el reloj
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
    protected void generarFormula(){
        int fix = numeroAleatorio(0,2);
        respuestaB = fix % 2 != 0;
        int rango;
        if(dificultad == 1){
            rango = numeroAleatorio(0,5);
        }
        else if(dificultad == 2){
            rango = numeroAleatorio(5,9);
        }
        else{
            rango = numeroAleatorio(9, formulas.length);
        }
        indiceFormula = rango;
    }
    @Override
    public void dibujarMinijuego() {

    }
}
