package com.example.softgames;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void verificarConexionEnrutadorEstadisticasArchivo() {
        ArchivoGuardado archivo = new ArchivoGuardado(1, "Partida");
        Estadisticas estadisticas = new Estadisticas(archivo);
        Enrutador enrutador = new Enrutador(estadisticas);

        assertSame(estadisticas, enrutador.getEstadisticas());
        assertSame(archivo, enrutador.getEstadisticas().getArchivoGuardado());
    }

    @Test
    public void verificarGuardadoYRecuperacionDeEstadisticas() {

        // Crear el archivo de guardado
        ArchivoGuardado archivo = new ArchivoGuardado(1, "Partida");

        // Crear las estadísticas conectadas al archivo
        Estadisticas estadisticas = new Estadisticas(archivo);

        // Registrar una puntuación
        estadisticas.registrarPuntuacion("QuizJava", 8.5);

        // Verificar que la puntuación fue registrada
        assertEquals(8.5, estadisticas.obtenerPuntuacion("QuizJava"), 0.001);

        // Verificar que existe la puntuación en ArchivoGuardado
        assertTrue(estadisticas.obtenerPuntuaciones().containsKey("QuizJava"));
    }
}