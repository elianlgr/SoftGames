package com.example.softgames;

public class EventoModoFinalizado implements IEvento{
    protected double puntosTotal;
    protected int dificultad;
    protected int numPreguntas;
    public EventoModoFinalizado(double puntosTotal, int dificultad, int numPreguntas){
        this.puntosTotal = puntosTotal;
        this.dificultad = dificultad;
        this.numPreguntas = numPreguntas;
    }
}
