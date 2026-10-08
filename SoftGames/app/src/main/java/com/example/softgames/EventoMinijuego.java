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
        return (9.0/tiempoMaximo)*tiempoRestante;
    }
}
