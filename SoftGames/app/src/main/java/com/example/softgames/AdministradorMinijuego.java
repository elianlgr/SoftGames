package com.example.softgames;

import static com.example.softgames.Utils.numeroAleatorio;

import java.util.ArrayList;
import java.util.List;

public class AdministradorMinijuego {
    double puntosAcumulados;
    protected int maxNumPreguntas;
    protected int numPreguntas;
    protected List<Categoria> categorias;
    protected Categoria categoriaActual;
    protected Minijuego minijuegoActual;
    protected MinijuegoFinalizado mf;
    protected IEnrutador enrutador;
    public AdministradorMinijuego(IEnrutador enrutador){
        this.enrutador = enrutador;
        inicializarCategorias(enrutador,1);
        maxNumPreguntas = 10;
        numPreguntas = 0;
        mf = (esCorrecto, puntos) -> {
            if(esCorrecto == true) {
                puntosAcumulados += puntos;
                numPreguntas++;
            }
            siguienteMinijuego();
        };
    }
    public void setDificultad(int dificultad){
        dificultad = dificultad%4;
        minijuegoActual.setDificultad(dificultad);
        categoriaActual.setDificultadMinijuegos(dificultad);
        for (int i = 0; i < categorias.size(); i++) {
            categorias.get(i).setDificultadMinijuegos(dificultad);
        }
    }
    public void setMinijuegoFinalizado(MinijuegoFinalizado mf){
        this.mf = mf;
    }
    public void recibirDatos(Boolean esCorrecto, double puntos){
        mf.recibirEvento(esCorrecto, puntos);
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
            if(categoriaActual != null){
                int random = numeroAleatorio(0,categorias.size());
                minijuegoActual = categoriaActual.seleccionarMinijuego(random);
            }
            else{
                int random = numeroAleatorio(0,10);
            }
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
    public void reiniciarMinijuego(){
        if(minijuegoActual != null){
            minijuegoActual.reiniciarMinijuego();
        }
    }
    public void update(){
        if(minijuegoActual != null){
            minijuegoActual.update();
        }
    }
    protected void inicializarCategorias(IEnrutador enrutador, int dificultad){
        categorias = new ArrayList<>();
        Categoria logica = new Categoria("Lógica");
        logica.agregarMinijuego(new MinijuegoLogica1(enrutador,dificultad));
        categorias.add(logica);
    }
    public void detener(){
        minijuegoActual = null;
        enrutador.manejar(new EventoModoFinalizado(
                puntosAcumulados,
                categorias.get(0).getDificultad(),
                numPreguntas)
        );

    }
}
