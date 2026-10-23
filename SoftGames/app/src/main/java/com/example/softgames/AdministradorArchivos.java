package com.example.softgames;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class AdministradorArchivos {
    private ArchivoGuardado[] archivos;
    private ArchivoGuardado archivoActual;
    private Context context;
    private String usuario;

    public AdministradorArchivos(Context context) {
        this.context = context;
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("SesionJuego", Context.MODE_PRIVATE);
            this.usuario = prefs.getString("usuarioActual", "default");
        } else {
            this.usuario = "default";
        }
        this.archivos = new ArchivoGuardado[5];
        cargarArchivos();
    }

    public AdministradorArchivos(Context context, String usuario) {
        this.context = context;
        this.usuario = (usuario != null && !usuario.trim().isEmpty()) ? usuario.trim() : "default";
        this.archivos = new ArchivoGuardado[5];
        cargarArchivos();
    }

    public void cargarArchivos() {
        if (this.context == null) {
            return;
        }
        for (int i = 0; i < 5; i++) {
            ArchivoGuardado temp = new ArchivoGuardado(i, "", this.usuario);
            String nombreArchivo = ArchivoGuardado.obtenerNombreArchivo(this.usuario, i);
            if (temp.deserializar(this.context, nombreArchivo)) {
                this.archivos[i] = temp;
            } else {
                this.archivos[i] = null;
            }
        }
    }

    public boolean existeNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return false;
        }
        String buscado = nombreArchivo.trim();
        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i] != null && archivos[i].nombre != null
                    && archivos[i].nombre.trim().equalsIgnoreCase(buscado)) {
                return true;
            }
        }
        return false;
    }

    public boolean crearArchivo(String nombreArchivo, int indice) {
        if (indice < 0 || indice >= 5) {
            return false;
        }
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return false;
        }
        String nombreLimpio = nombreArchivo.trim();

        // Flujo alternativo 3.1: Validar si el nombre ya existe para este usuario
        if (existeNombreArchivo(nombreLimpio)) {
            return false;
        }

        ArchivoGuardado nuevoArchivo = new ArchivoGuardado(indice, nombreLimpio, this.usuario);

        if (this.context == null || nuevoArchivo.serializar(this.context)) {
            archivos[indice] = nuevoArchivo;
            this.archivoActual = nuevoArchivo;
            return true;
        }
        return false;
    }

    /**
     * Caso de Uso CU-04 (Flujo alternativo 3.1):
     * Clona el progreso y estadísticas del archivo origen en la ranura destino con el nuevo nombre.
     */
    public boolean copiarArchivo(int origen, int destino, String nuevoNombre) {
        if (origen < 0 || origen >= archivos.length || destino < 0 || destino >= archivos.length) {
            return false;
        }
        if (origen == destino) {
            return false;
        }
        if (archivos[origen] == null || archivos[destino] != null) {
            return false;
        }
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            return false;
        }
        String nombreLimpio = nuevoNombre.trim();

        // Flujo alternativo 3.1.2: El nombre ingresado para la copia ya existe
        if (existeNombreArchivo(nombreLimpio)) {
            return false;
        }

        // Sincronizar puntuaciones en memoria antes de clonar
        Estadisticas statsOrigen = new Estadisticas(archivos[origen]);
        statsOrigen.obtenerPuntuaciones();

        ArchivoGuardado copia = archivos[origen].clonarParaRanura(destino, nombreLimpio);
        Estadisticas.copiarMemoriaSesion(this.usuario, origen, destino);

        if (this.context == null || copia.serializar(this.context)) {
            archivos[destino] = copia;
            return true;
        }
        return false;
    }

    /**
     * Caso de Uso CU-04 (Flujo normal pasos 3 a 6):
     * Borra el archivo .json del almacenamiento interno, limpia la ranura en memoria y restablece su estado.
     */
    public boolean borrarArchivo(int ranura) {
        if (ranura < 0 || ranura >= archivos.length) {
            return false;
        }
        ArchivoGuardado objetivo = archivos[ranura];
        if (objetivo == null) {
            return false;
        }

        boolean eliminadoFisico = objetivo.eliminarArchivoFisico(this.context);
        Estadisticas.limpiarMemoriaSesion(this.usuario, ranura);

        if (this.archivoActual == objetivo) {
            this.archivoActual = null;
        }
        this.archivos[ranura] = null;
        return eliminadoFisico;
    }

    public List<Integer> obtenerRanurasLibres(int limiteRanuras) {
        List<Integer> libres = new ArrayList<>();
        int max = Math.min(limiteRanuras, archivos.length);
        for (int i = 0; i < max; i++) {
            if (archivos[i] == null) {
                libres.add(i);
            }
        }
        return libres;
    }

    public List<Integer> obtenerRanurasOcupadas(int limiteRanuras) {
        List<Integer> ocupadas = new ArrayList<>();
        int max = Math.min(limiteRanuras, archivos.length);
        for (int i = 0; i < max; i++) {
            if (archivos[i] != null) {
                ocupadas.add(i);
            }
        }
        return ocupadas;
    }

    public boolean iniciarSesion(int indice) {
        if (indice >= 0 && indice < 5) {
            if (this.context == null) {
                if (this.archivos[indice] != null) {
                    this.archivoActual = this.archivos[indice];
                    return true;
                }
                return false;
            }
            String nombreArchivoJson = ArchivoGuardado.obtenerNombreArchivo(this.usuario, indice);
            ArchivoGuardado archivoTemp = new ArchivoGuardado(indice, "", this.usuario);

            if (archivoTemp.deserializar(this.context, nombreArchivoJson)) {
                this.archivos[indice] = archivoTemp;
                this.archivoActual = archivoTemp;
                return true;
            }
        }
        return false;
    }

    public ArchivoGuardado getArchivo(int indice) {
        if (indice >= 0 && indice < archivos.length) {
            return archivos[indice];
        }
        return null;
    }

    public ArchivoGuardado getArchivoActual() {
        return archivoActual;
    }

    public String getUsuario() {
        return usuario;
    }
}
