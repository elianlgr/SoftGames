package com.example.softgames;

import java.util.List;

public abstract class AdministradorMinijuego {
    double puntosAcumulados;
    // Agregue la conexion con categoria
    protected List<Categoria> categorias;
    public AdministradorMinijuego(){
    }
    public void recibirDatos(Boolean esCorrecto, double puntos){
        if(esCorrecto == true){
            // Se otorgan puntos por respuestas correctas
            puntosAcumulados += puntos;
        }
        else{
            siguienteMinijuego();
        }
    }

    public void procesarEvento(EventoMinijuego evento){
        if(evento.esCorrecto() != null & evento.esCorrecto()){
            // Se obtienen los puntos calculados en el propio evento
            puntosAcumulados += evento.obtenerPuntos();
        }else{
            siguienteMinijuego();
        }
    }

    public void siguienteMinijuego(){
    }

    // Getter and Setter para encapsular
    public List<Categoria> getCategoria(){
        return categorias;
    }

    public void setCategoria(List<Categoria> categorias){
        this.categorias = categorias;
    }
}
