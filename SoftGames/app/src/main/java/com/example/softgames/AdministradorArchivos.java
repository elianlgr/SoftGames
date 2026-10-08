package com.example.softgames;

import android.content.Context;
import com.example.softgames.ArchivoGuardado;

public class AdministradorArchivos {
    private ArchivoGuardado[] archivos;
    private ArchivoGuardado archivoActual;
    // Nuevo atributo para manejar archivos en Android
    private Context context;

    public AdministradorArchivos(Context context) {
        this.context = context;
        this.archivos = new ArchivoGuardado[5];
        
        for (int i = 0; i < 5; i++) {
            ArchivoGuardado temp = new ArchivoGuardado(i, "");
            if (temp.deserializar(context, "guardado_" + i + ".json")) {
                this.archivos[i] = temp;
            }
        }
    }

    public boolean crearArchivo(String nombreArchivo, int indice) {
        if (indice < 0 || indice >= 5) {
            return false;
        }

        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i] != null && archivos[i].nombre.equals(nombreArchivo)) {
                return false;
            }
        }

        ArchivoGuardado nuevoArchivo = new ArchivoGuardado(indice, nombreArchivo);

        if (nuevoArchivo.serializar(this.context)) {
            archivos[indice] = nuevoArchivo;
            this.archivoActual = nuevoArchivo;
            return true;
        }
        return false;
    }

    public boolean iniciarSesion(int indice) {
        if (indice >= 0 && indice < 5) {
            String nombreArchivoJson = "guardado_" + indice + ".json";
            ArchivoGuardado archivoTemp = new ArchivoGuardado(indice, "");

            if (archivoTemp.deserializar(this.context, nombreArchivoJson)) {
                this.archivos[indice] = archivoTemp;
                this.archivoActual = archivoTemp;
                return true;
            }
        }
        return false;
    }

    public ArchivoGuardado getArchivoActual() {
        return archivoActual;
    }
}