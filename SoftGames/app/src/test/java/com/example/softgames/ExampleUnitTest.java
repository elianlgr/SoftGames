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
}