package com.example.softgames;

import java.util.ArrayList;
import java.util.List;

public class Categoria {
    // Atrivutos definidos
    public String tipoCategoria;
    public List<Minijuego> minijuegos;

    public Categoria(String tipoCategoria){
        this.tipoCategoria = tipoCategoria;
        minijuegos = new ArrayList<>();
    }
    public int obtenerCantidadMinijuegos(){
        return minijuegos.size();
    }
    public Minijuego seleccionarMinijuego(int indice){
        if(indice >= 0 && indice < minijuegos.size()){
            return minijuegos.get(indice);
        }
        else{
            return null;
        }
    }

    public String getTipoCategoria(){
        return tipoCategoria;
    }

    public void setTipoCategoria(String tipoCategoria){
        this.tipoCategoria = tipoCategoria;
    }

    public List<Minijuego> getMinijuegos(){
        return minijuegos;
    }

    public void setMinijuegos(List<Minijuego> minijuegos){
        this.minijuegos = minijuegos;
    }
    public void setDificultadMinijuegos(int dificultad){
        for (int i = 0; i < minijuegos.size(); i++) {
            minijuegos.get(i).setDificultad(dificultad);
        }
    }
    public void agregarMinijuego(Minijuego nuevo){
        minijuegos.add(nuevo);
    }
    public int getDificultad(){
        return minijuegos.get(0).getDificultad();
    }
}
