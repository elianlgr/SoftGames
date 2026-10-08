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
    protected android.os.CountDownTimer temporizadorReal;
    protected android.widget.TextView tvTemporizador;

    public void init(){
        pausa = false;
        inicializado = true;
        iniciarTemporizadorReal();
    }

    public void reiniciarMinijuego() {
        if (temporizadorReal != null) {
            temporizadorReal.cancel();
        }
        frameTimer = maxTime; // Reiniciar el tiempo al máximo
        iniciarTemporizadorReal();
    }

    protected void iniciarTemporizadorReal() {
        tvTemporizador = findViewById(R.id.tvTemporizador);
        if (tvTemporizador == null) return; // Por si el layout no lo tiene

        // maxTime estaba en 'frames' (600 = 10 segundos asumiendo 60fps). Usaremos millis.
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

    protected void tiempoAgotado() {
        // Por defecto, envía respuesta incorrecta y termina, 
        // pero los minijuegos (como Logica1) pueden sobreescribirlo para pasar a la siguiente pregunta.
        crearEvento(false);
    }

    public void inicializarMenu(){
        ArrayList<View.OnClickListener> acciones = new ArrayList<>();
        acciones.add(v -> {
            pausa = false;
            iniciarTemporizadorReal(); // Reanudar
        });
        acciones.add(v -> {
            if (temporizadorReal != null) temporizadorReal.cancel();
            crearEvento(null);
        });
        acciones.add(v -> {
            android.content.Intent intent = new android.content.Intent(this, AjustesActivity.class);
            startActivity(intent);
        });
        menuPausa = new MenuPausa(this, acciones);
    }

    @Override
    public void onBackPressed() {
        if (!pausa) {
            pausa = true;
            if (temporizadorReal != null) temporizadorReal.cancel(); // Pausar
            if (menuPausa == null) {
                inicializarMenu();
            }
            menuPausa.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_minijuego, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_pausa) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void update(){
        // Ya no es necesario el update manual si usamos CountDownTimer, 
        // pero lo dejamos vacío para no romper nada
    }
    public void enviarRespuesta(int pregunta){
        crearEvento(respuesta == pregunta);
    }
    public void crearEvento(Boolean esCorrecto){
        EventoMinijuego evento = new EventoMinijuego(esCorrecto, maxTime, frameTimer);
        evento.setDificultad(dificultad);
        evento.setNombre(nombre);
        if (enrutador != null) {
            enrutador.manejar(evento);
        } else {
            // Solo para pruebas directas: cerrar la actividad cuando se responda
            finish();
        }
    }
    public void dibujar(){
        if(pausa){
            dibujarMenuPausa();
        }
        else{
            dibujarMinijuego();
        }
    }
    public abstract void dibujarMinijuego();
    public void dibujarMenuPausa(){

    }
}
