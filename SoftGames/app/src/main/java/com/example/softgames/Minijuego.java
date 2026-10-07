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
    public Minijuego(int timeFrames, EnrutadorMinijuego enrutador) {
        maxTime = timeFrames;
        frameTimer = timeFrames;
        this.enrutador = enrutador;
    }
    public void init(){
        pausa = false;
        inicializado = true;

    }
    public void inicializarMenu(){
        ArrayList<View.OnClickListener> acciones = new ArrayList<>();
        acciones.add(v -> pausa = false);
        acciones.add(v -> {
            crearEvento(null);
        });
        menuPausa = new MenuPausa(acciones);
    }
    public void update(){
        if(inicializado && !pausa) {
            if (frameTimer > 0) {
                frameTimer--;
            } else {
                crearEvento(null);
            }
        }
    }
    public void enviarRespuesta(int pregunta){
        crearEvento(respuesta == pregunta);
    }

    public void crearEvento(Boolean esCorrecto){
        EventoMinijuego evento = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        evento.setDificultad(dificultad);
        evento.setNombre(nombre);
        enrutador.manejar(evento);
    }
    public void dibujar(){
        if(pausa){
            dibujarMenuPausa();
        }
        else{
            dibujarMinijuego();
        }
    }
    @Override
    protected abstract void onCreate(Bundle savedInstanceState);
    public abstract void dibujarMinijuego();
    public void dibujarMenuPausa(){

    }
}
