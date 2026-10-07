package com.example.softgames;

import java.util.List;

public class Categoria {
    // Atrivutos definidos
    public String tipoCategoria;
    public List<Minijuego> minijuegos;

    public Categoria(String tipoCategoria, List<Minijuego> minijuegos){
        this.tipoCategoria = tipoCategoria;
        this.minijuegos = minijuegos;
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
}
