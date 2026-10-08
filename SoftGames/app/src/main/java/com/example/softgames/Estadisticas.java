package com.example.softgames;

import android.content.Context;

import java.util.Map;

public class Estadisticas {

    private ArchivoGuardado archivoGuardado;

    public Estadisticas(ArchivoGuardado archivoGuardado) {
        this.archivoGuardado = archivoGuardado;
    }

    public ArchivoGuardado getArchivoGuardado() {
        return archivoGuardado;
    }

    public void setArchivoGuardado(ArchivoGuardado archivoGuardado) {
        this.archivoGuardado = archivoGuardado;
    }

    // Registra o actualiza la puntuación de un minijuego
    public void registrarPuntuacion(String minijuego, double puntuacion) {
        archivoGuardado.puntuaciones.put(minijuego, puntuacion);
    }

    // Obtiene la puntuación de un minijuego
    public double obtenerPuntuacion(String minijuego) {
        Double puntuacion = archivoGuardado.puntuaciones.get(minijuego);
        return puntuacion != null ? puntuacion : 0.0;
    }

    // Guarda las estadísticas en el archivo
    public boolean guardar(Context context) {
        return archivoGuardado.serializar(context);
    }

    // Carga las estadísticas desde el archivo
    public boolean cargar(Context context, String nombreArchivo) {
        return archivoGuardado.deserializar(context, nombreArchivo);
    }

    // Obtiene todas las puntuaciones
    public Map<String, Double> obtenerPuntuaciones() {
        return archivoGuardado.puntuaciones;
    }
}