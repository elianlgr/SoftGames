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
    /*public MinijuegoLogica1(){
        super(600,null);
        dificultad = 1;
        generarFormula();
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logic_minigame1_layout);
        Button trueBtn = findViewById(R.id.trueButton);
        Button falseBtn = findViewById(R.id.falseButton);
        trueBtn.setOnClickListener(v -> enviarRespuesta(true));
        falseBtn.setOnClickListener(v -> enviarRespuesta(false));
        TextView formulaTexto = findViewById(R.id.textoFormula);
        String texto = formulas[indiceFormula] + " = " + respuestaB;
        formulaTexto.setText(texto);
    }
    public void enviarRespuesta(boolean respuestaFinal){
        crearEvento((respuestaFinal == respuestas[indiceFormula]) == respuestaB);
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
