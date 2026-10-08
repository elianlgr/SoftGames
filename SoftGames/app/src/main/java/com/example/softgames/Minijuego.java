package com.example.softgames;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public abstract class Minijuego extends AppCompatActivity {
    protected EnrutadorMinijuego enrutador;
    protected boolean inicializado = false;
    protected boolean pausa;
    protected int dificultad;
    protected String nombre;
    protected int maxTime;
    protected int frameTimer;
    protected int respuesta;
    protected MenuPausa menuPausa;
    protected android.os.CountDownTimer temporizadorReal;
    protected android.widget.TextView tvTemporizador;
    public Minijuego(int timeFrames, EnrutadorMinijuego enrutador) {
        maxTime = timeFrames;
        frameTimer = timeFrames;
        this.enrutador = enrutador;
    }
    public void init(){
        pausa = false;
        inicializado = true;
        iniciarTemporizadorReal();
    }
    protected void iniciarTemporizadorReal() {
        tvTemporizador = findViewById(R.id.tvTemporizador);
        // Por si el layout no lo tiene
        if (tvTemporizador == null) return;

        long tiempoRestanteMillis = frameTimer > 0 ? frameTimer * 16L : maxTime * 16L;

        temporizadorReal = new android.os.CountDownTimer(tiempoRestanteMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                frameTimer = (int) (millisUntilFinished / 16);
                int segundos = (int) (millisUntilFinished / 1000);
                if (tvTemporizador != null) {
                    tvTemporizador.setText(segundos + "s");
                }
            }

            @Override
            public void onFinish() {
                frameTimer = 0;
                if (tvTemporizador != null) {
                    tvTemporizador.setText("0s");
                }
                android.widget.Toast.makeText(Minijuego.this, "¡Se acabó el tiempo!", android.widget.Toast.LENGTH_SHORT).show();
                tiempoAgotado();
            }
        };
        temporizadorReal.start();
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    public void setDificultad(int dificultad){
        this.dificultad = dificultad;
    }
    public void inicializarMenu(){
        ArrayList<View.OnClickListener> acciones = new ArrayList<>();
        acciones.add(v -> pausa = false);
        acciones.add(v -> {
            crearEvento(null);
        });
        menuPausa = new MenuPausa(acciones);
    }
    public void update(){}
    public void enviarRespuesta(int pregunta){
        crearEvento(respuesta == pregunta);
    }
    public void crearEvento(Boolean esCorrecto){
        EventoMinijuego evento = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        evento.setDificultad(dificultad);
        evento.setNombre(nombre);
        enrutador.manejar(evento);
    }
    protected void onCreate(Bundle savedInstanceState) {
        if(inicializado){
            super.onCreate(savedInstanceState);
            dibujarMinijuego();
        }
    }
    protected void tiempoAgotado() {
        crearEvento(null);
    }
    public void dibujar(){
        if(pausa){
            dibujarMenuPausa();
        }
    }

    public abstract void dibujarMinijuego();
    public void dibujarMenuPausa(){

    }
}
