package com.example.softgames;

import android.content.Context;
import android.content.SharedPreferences;

public class AdministradorArchivos {
    private ArchivoGuardado[] archivos;
    private ArchivoGuardado archivoActual;
    private Context context;
    private String usuario;

    public AdministradorArchivos(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("SesionJuego", Context.MODE_PRIVATE);
        this.usuario = prefs.getString("usuarioActual", "default");
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

    public boolean crearArchivo(String nombreArchivo, int indice) {
        if (indice < 0 || indice >= 5) {
            return false;
        }

        // Flujo alternativo 3.1: Validar si el nombre ya existe para este usuario
        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i] != null && archivos[i].nombre.equalsIgnoreCase(nombreArchivo)) {
                return false;
            }
        }

        ArchivoGuardado nuevoArchivo = new ArchivoGuardado(indice, nombreArchivo, this.usuario);

        if (nuevoArchivo.serializar(this.context)) {
            archivos[indice] = nuevoArchivo;
            this.archivoActual = nuevoArchivo;
            return true;
        }
        return false;
    }

    public boolean iniciarSesion(int indice) {
        if (indice >= 0 && indice < 5) {
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