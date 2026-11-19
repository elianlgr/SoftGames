package com.example.softgames;

import android.content.Context;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Estadisticas {

    private ArchivoGuardado archivoGuardado;

    // Flujo alternativo 3.1: Respaldo en memoria durante la sesión activa si falla la escritura .json
    private static final Map<String, Map<String, Double>> respaldoMemoriaSesion = new HashMap<>();

    public Estadisticas(ArchivoGuardado archivoGuardado) {
        this.archivoGuardado = archivoGuardado;
        sincronizarDesdeMemoriaSesion();
    }

    public ArchivoGuardado getArchivoGuardado() {
        return archivoGuardado;
    }

    public void setArchivoGuardado(ArchivoGuardado archivoGuardado) {
        this.archivoGuardado = archivoGuardado;
        sincronizarDesdeMemoriaSesion();
    }

    private String obtenerClaveSesion() {
        if (archivoGuardado == null) return "default_0";
        String user = archivoGuardado.usuario != null ? archivoGuardado.usuario : "default";
        return user + "_" + archivoGuardado.numero;
    }

    public static void limpiarMemoriaSesion(String usuario, int numero) {
        String user = (usuario != null && !usuario.trim().isEmpty()) ? usuario.trim() : "default";
        respaldoMemoriaSesion.remove(user + "_" + numero);
    }

    public static void copiarMemoriaSesion(String usuario, int origen, int destino) {
        String user = (usuario != null && !usuario.trim().isEmpty()) ? usuario.trim() : "default";
        Map<String, Double> memOrigen = respaldoMemoriaSesion.get(user + "_" + origen);
        if (memOrigen != null) {
            respaldoMemoriaSesion.put(user + "_" + destino, new HashMap<>(memOrigen));
        } else {
            respaldoMemoriaSesion.remove(user + "_" + destino);
        }
    }

    private void sincronizarDesdeMemoriaSesion() {
        if (archivoGuardado == null) return;
        Map<String, Double> mem = respaldoMemoriaSesion.get(obtenerClaveSesion());
        if (mem != null) {
            for (Map.Entry<String, Double> entry : mem.entrySet()) {
                Double actual = archivoGuardado.puntuaciones.get(entry.getKey());
                if (actual == null || entry.getValue() > actual) {
                    archivoGuardado.puntuaciones.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    // Registra o actualiza la puntuación de un minijuego (y la respalda en memoria de sesión)
    public void registrarPuntuacion(String minijuego, double puntuacion) {
        archivoGuardado.puntuaciones.put(minijuego, puntuacion);
        String claveSesion = obtenerClaveSesion();
        Map<String, Double> mem = respaldoMemoriaSesion.get(claveSesion);
        if (mem == null) {
            mem = new HashMap<>();
            respaldoMemoriaSesion.put(claveSesion, mem);
        }
        mem.put(minijuego, puntuacion);
    }

    // Obtiene la puntuación previa de un minijuego
    public double obtenerPuntuacion(String minijuego) {
        sincronizarDesdeMemoriaSesion();
        Double puntuacion = archivoGuardado.puntuaciones.get(minijuego);
        return puntuacion != null ? puntuacion : 0.0;
    }

    // Guarda las estadísticas serializando el archivo .json en el almacenamiento interno
    public boolean guardar(Context context) {
        return archivoGuardado.serializar(context);
    }

    // Carga las estadísticas deserializando el archivo .json activo
    public boolean cargar(Context context, String nombreArchivo) {
        boolean exito = archivoGuardado.deserializar(context, nombreArchivo);
        sincronizarDesdeMemoriaSesion();
        return exito;
    }

    // Obtiene todas las puntuaciones registradas
    public Map<String, Double> obtenerPuntuaciones() {
        sincronizarDesdeMemoriaSesion();
        return archivoGuardado.puntuaciones;
    }

    /**
     * Obtiene los minijuegos jugados que pertenecen a una categoría dada.
     * Las claves se almacenan con formato "Categoría - Minijuego" (ej. "Lógica - Lógica 1").
     */
    public Map<String, Double> obtenerMinijuegosPorCategoria(String categoria) {
        sincronizarDesdeMemoriaSesion();
        Map<String, Double> resultado = new LinkedHashMap<>();
        if (archivoGuardado == null || archivoGuardado.puntuaciones == null) {
            return resultado;
        }
        String prefijo = categoria + " - ";
        for (Map.Entry<String, Double> entry : archivoGuardado.puntuaciones.entrySet()) {
            String clave = entry.getKey();
            Double valor = entry.getValue();
            if (clave != null && valor != null && valor > 0.0) {
                if (clave.regionMatches(true, 0, prefijo, 0, prefijo.length())) {
                    String nombreMinijuego = clave.substring(prefijo.length()).trim();
                    resultado.put(nombreMinijuego, valor);
                }
            }
        }
        return resultado;
    }
}
