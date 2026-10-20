package com.example.softgames;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void verificarGuardadoYRecuperacionReal() {

        Context context =
                InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Crear un archivo de guardado
        ArchivoGuardado archivo =
                new ArchivoGuardado(1, "Partida");

        // Crear las estadísticas conectadas al archivo
        Estadisticas estadisticas =
                new Estadisticas(archivo);

        // Registrar una puntuación
        estadisticas.registrarPuntuacion("QuizJava", 8.5);

        // Guardar en el dispositivo
        assertTrue(estadisticas.guardar(context));

        // Crear otro objeto para simular una nueva sesión
        ArchivoGuardado archivoRecuperado =
                new ArchivoGuardado(1, "PartidaNueva");

        Estadisticas estadisticasRecuperadas =
                new Estadisticas(archivoRecuperado);

        // Cargar los datos guardados
        assertTrue(
                estadisticasRecuperadas.cargar(
                        context,
                        "guardado_1.json"
                )
        );

        // Comprobar que recuperamos la puntuación
        assertEquals(
                8.5,
                estadisticasRecuperadas.obtenerPuntuacion("QuizJava"),
                0.001
        );
    }
}