package com.example.softgames;

public abstract class Minijuego {
    protected EnrutadorMinijuego enrutador;
    protected int dificultad;
    protected String nombre;
    protected int maxTime;
    protected int frameTimer;
    protected int respuesta;
    public Minijuego(int timeFrames, EnrutadorMinijuego enrutador){
        maxTime = timeFrames;
        frameTimer = timeFrames;
        this.enrutador = enrutador;
    }
    public void update(){
        if(frameTimer > 0){
            frameTimer--;
        }
        else{
            crearEvento(null);
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
    public abstract void draw();
}
