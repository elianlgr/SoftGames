package com.example.softgames;

public class EventoMinijuego implements IEvento{
    public String nombreMinijuego;
    public int dificultad;
    public Boolean respuestaCorrecta;
    public int tiempoMaximo;
    public int tiempoRestante;
    public EventoMinijuego(Boolean esCorrecto, int tiempoMaximo, int tiempoRestante){
        respuestaCorrecta = esCorrecto;
        this.tiempoMaximo = tiempoMaximo;
        this.tiempoRestante = tiempoRestante;
    }
    public void setNombre(String nombre){
        nombreMinijuego = nombre;
    }
    public void setDificultad(int dificultad){
        this.dificultad = dificultad;
    }
    public double obtenerPuntos(){
        if(Boolean.TRUE.equals(respuestaCorrecta)){
            return 1.0 + convertirTiempo();
        }
        else{
            return 0.0;
        }
    }
    public Boolean esCorrecto(){
        return respuestaCorrecta;
    }
    protected double convertirTiempo(){
        if (tiempoMaximo <= 0 || tiempoRestante <= 0) {
            return 0.0;
        }
        // Si responde correctamente dentro del tiempo, asegura una base de 9.0 pts (1.0 + 8.0)
        // más hasta 1.0 pt adicional por velocidad (10.0 pts al responder con >= 35% del tiempo restante).
        double proporcionRestante = Math.min(1.0, (double) tiempoRestante / (tiempoMaximo * 0.35));
        return 8.0 + proporcionRestante;
    }
}
