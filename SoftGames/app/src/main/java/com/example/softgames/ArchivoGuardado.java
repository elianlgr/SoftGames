package com.example.softgames;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArchivoGuardado {
    public int numero;
    public String nombre;
    public Map<String, Double> puntuaciones;

    // Constructor
    public ArchivoGuardado(int numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
        this.puntuaciones = new HashMap<>();
    }

    public boolean serializar(Context context) {
        try {
            JSONObject json = new JSONObject();
            json.put("numero", this.numero);
            json.put("nombre", this.nombre);

            // Convertimos el Map de puntuaciones a JSON
            JSONObject scoresJson = new JSONObject();
            for (Map.Entry<String, Double> entry : this.puntuaciones.entrySet()) {
                scoresJson.put(entry.getKey(), entry.getValue());
            }
            json.put("puntuaciones", scoresJson);

            // Definimos el nombre del archivo y la ruta interna de Android
            String filename = "guardado_" + this.numero + ".json";
            File file = new File(context.getFilesDir(), filename);

            // Escribimos el archivo en la memoria del teléfono
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();

            return true;
        } catch (Exception e) {
            // Logcat mostrara este error si algo falla
            Log.e("ArchivoGuardado", "Error al serializar los datos", e);
            return false;
        }
    }

    // Lee el archivo desde la memoria del telefono y restaura los datos
    public boolean deserializar(Context context, String nombreArchivo) {
        try {
            File file = new File(context.getFilesDir(), nombreArchivo);

            // Si el archivo no existe, retornamos falso
            if (!file.exists()) return false;

            // Leemos el contenido del archivo
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            // Reconstruimos el JSON a partir del texto
            String jsonString = new String(data, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonString);

            // Asignamos los valores recuperados a nuestros atributos
            this.numero = json.getInt("numero");
            this.nombre = json.getString("nombre");

            JSONObject scoresJson = json.getJSONObject("puntuaciones");
            this.puntuaciones.clear();
            Iterator<String> keys = scoresJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                this.puntuaciones.put(key, scoresJson.getDouble(key));
            }

            return true;
        } catch (Exception e) {
            Log.e("ArchivoGuardado", "Error al deserializar los datos", e);
            return false;
        }
    }
}