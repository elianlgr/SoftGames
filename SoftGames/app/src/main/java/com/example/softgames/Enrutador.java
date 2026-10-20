package com.example.softgames;

public class Enrutador {

    private Estadisticas estadisticas;

    public Enrutador(Estadisticas estadisticas) {
        this.estadisticas = estadisticas;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(Estadisticas estadisticas) {
        this.estadisticas = estadisticas;
    }
}