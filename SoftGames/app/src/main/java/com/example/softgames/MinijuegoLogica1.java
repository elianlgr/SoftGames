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
        super(600, enrutador);
        this.dificultad = dificultad;
        generarFormula();
    }

    public MinijuegoLogica1(){
        super(600,null);
        dificultad = 1;
        generarFormula();
    }
    private int preguntasRespondidas = 0;
    private int totalPreguntas = 5;
    private int respuestasCorrectas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Leer configuración del Intent (viene de las pantallas de selección)
        if (getIntent() != null) {
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
        if (esCorrecto) {
            android.widget.Toast.makeText(this, "¡Respuesta correcta!", android.widget.Toast.LENGTH_SHORT).show();
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
            android.widget.Toast.makeText(this, "¡Juego Terminado! Acertaste " + respuestasCorrectas + " de " + totalPreguntas, android.widget.Toast.LENGTH_LONG).show();
            // Como esto es un resumen de las 5, podemos enviar si ganó o perdió
            boolean pasoElNivel = respuestasCorrectas >= 3;
            crearEvento(pasoElNivel);
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
            rango = numeroAleatorio(9,13);
        }
        indiceFormula = rango;
    }
    @Override
    public void dibujarMinijuego() {

    }
}
