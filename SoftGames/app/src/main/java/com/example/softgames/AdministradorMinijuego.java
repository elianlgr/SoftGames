package com.example.softgames;

public abstract class AdministradorMinijuego {
    double puntosAcumulados;
    public AdministradorMinijuego(){

    }
    public void recibirDatos(Boolean esCorrecto, double puntos){
        if(esCorrecto == true){
            puntosAcumulados += puntos;
        }
        else{
            siguienteMinijuego();
        }
    }
    public void siguienteMinijuego(){

    }
}
