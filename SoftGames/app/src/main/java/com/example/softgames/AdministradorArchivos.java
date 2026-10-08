package com.example.softgames;

import android.content.Context;
import com.example.softgames.ArchivoGuardado;

public class AdministradorArchivos {
    // Restriccion: Solo existe un maximo de 5 espacios de guardado
    private ArchivoGuardado[] archivos;
    private ArchivoGuardado archivoActual;
    // Nuevo atributo para manejar archivos en Android
    private Context context;

    public AdministradorArchivos(Context context) {
        this.context = context;
        this.archivos = new ArchivoGuardado[5];
        
        // Cargar todos los archivos existentes en el arreglo para validar nombres duplicados
        for (int i = 0; i < 5; i++) {
            ArchivoGuardado temp = new ArchivoGuardado(i, "");
            if (temp.deserializar(context, "guardado_" + i + ".json")) {
                this.archivos[i] = temp;
            }
        }
    }

    // Caso de Uso: Crear archivo de guardado
    public boolean crearArchivo(String nombreArchivo, int indice) {
        if (indice < 0 || indice >= 5) {
            return false;
        }

        // Flujo alternativo 3.1: Validar si el nombre ya existe
        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i] != null && archivos[i].nombre.equals(nombreArchivo)) {
                return false;
            }
        }

        ArchivoGuardado nuevoArchivo = new ArchivoGuardado(indice, nombreArchivo);

        // Se le pasa el context al metodo serializar de tu clase
        if (nuevoArchivo.serializar(this.context)) {
            archivos[indice] = nuevoArchivo;
            this.archivoActual = nuevoArchivo;
            return true;
        }
        return false;
    }

    // Caso de uso: Seleccionar archivo (Iniciar sesion)
    public boolean iniciarSesion(int indice) {
        if (indice >= 0 && indice < 5) {
            String nombreArchivoJson = "guardado_" + indice + ".json";
            ArchivoGuardado archivoTemp = new ArchivoGuardado(indice, "");

            // Se le pasa el context al metodo deserializar de tu clase
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