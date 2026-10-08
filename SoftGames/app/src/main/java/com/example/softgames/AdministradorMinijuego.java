package com.example.softgames;

import java.util.ArrayList;
import java.util.List;

public class AdministradorMinijuego {
    double puntosAcumulados;
    protected int maxNumPreguntas;
    protected int numPreguntas;
    // Agregue la conexion con categoria
    protected List<Categoria> categorias;
    protected Categoria categoriaActual;
    protected Minijuego minijuegoActual;
    public AdministradorMinijuego(){
        inicializarCategorias();
        maxNumPreguntas = 10;
        numPreguntas = 0;
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
    public void setMaxNumPreguntas(int numPreguntas){
        if(numPreguntas >= 10 && numPreguntas <= 20){
            this.numPreguntas = numPreguntas;
        }
    }
    public void setCategoriaActual(int index){
        if(index >= 0 && index < categorias.size()){
            categoriaActual = categorias.get(index);
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
    public void seleccionarMinijuego(int index){
        if(categoriaActual == null){
            Categoria tempCategoria = categorias.get(0);
            minijuegoActual = tempCategoria.seleccionarMinijuego(index);
        }
        else if(index >= 0 && index < categoriaActual.obtenerCantidadMinijuegos()){
            minijuegoActual = categoriaActual.seleccionarMinijuego(index);
        }
    }

    public void siguienteMinijuego(){
        if(numPreguntas <= maxNumPreguntas){
            minijuegoActual = categoriaActual.seleccionarMinijuego(1);
            numPreguntas++;
        }
        else{
            detener();
        }
    }

    // Getter and Setter para encapsular
    public List<Categoria> getCategoria(){
        return categorias;
    }

    public void setCategoria(List<Categoria> categorias){
        this.categorias = categorias;
    }
    public void update(){
        if(minijuegoActual != null){
            minijuegoActual.update();
        }
    }
    protected void inicializarCategorias(){
        categorias = new ArrayList<>();

    }
    public void detener(){
        minijuegoActual = null;
    }
}
