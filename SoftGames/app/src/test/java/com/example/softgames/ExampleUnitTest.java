package com.example.softgames;

import org.junit.Test;

import java.util.List;
import java.util.Random;

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

    @Test
    public void verificarGeneracionYPuntuacionRondaAleatoria_CU03() {
        List<RondaAleatoriaActivity.RetoAleatorio> ronda =
                RondaAleatoriaActivity.generarPartidaAleatoria(10, new Random(42L));

        assertNotNull(ronda);
        assertEquals(10, ronda.size());

        for (RondaAleatoriaActivity.RetoAleatorio reto : ronda) {
            assertNotNull(reto.categoria);
            assertTrue(reto.dificultad >= 1 && reto.dificultad <= 3);
        }

        // Flujo normal paso 5: Respuesta correcta otorga entre 1.0 y 10.0 según velocidad
        EventoMinijuego aciertoRapido = new EventoMinijuego(true, 1200, 960);
        assertTrue(aciertoRapido.obtenerPuntos() >= 1.0 && aciertoRapido.obtenerPuntos() <= 10.0);

        // Flujo alternativo 4.1: Respuesta incorrecta o tiempo agotado asigna 0.0 puntos
        EventoMinijuego fallo = new EventoMinijuego(false, 1200, 600);
        assertEquals(0.0, fallo.obtenerPuntos(), 0.001);
    }

    @Test
    public void verificarCopiarYBorrarArchivoGuardado_CU04() {
        String usuarioPrueba = "tester_cu04";
        Estadisticas.limpiarMemoriaSesion(usuarioPrueba, 0);
        Estadisticas.limpiarMemoriaSesion(usuarioPrueba, 1);
        Estadisticas.limpiarMemoriaSesion(usuarioPrueba, 2);

        AdministradorArchivos admin = new AdministradorArchivos(null, usuarioPrueba);
        assertTrue(admin.crearArchivo("PartidaOriginal", 0));

        ArchivoGuardado origen = admin.getArchivo(0);
        assertNotNull(origen);
        Estadisticas statsOrigen = new Estadisticas(origen);
        statsOrigen.registrarPuntuacion("Lógica - Lógica 1", 9.2);
        statsOrigen.registrarPuntuacion("Programación - Variables 1", 8.4);

        // Flujo alternativo 3.1.2: Rechazar copia si el nombre ya existe
        assertFalse(admin.copiarArchivo(0, 1, "PartidaOriginal"));

        // Flujo alternativo 3.1: Copiar partida en una ranura libre con nombre nuevo
        assertTrue(admin.copiarArchivo(0, 1, "PartidaCopia"));
        ArchivoGuardado copia = admin.getArchivo(1);
        assertNotNull(copia);
        assertEquals("PartidaCopia", copia.nombre);
        assertEquals(9.2, new Estadisticas(copia).obtenerPuntuacion("Lógica - Lógica 1"), 0.001);
        assertEquals(8.4, new Estadisticas(copia).obtenerPuntuacion("Programación - Variables 1"), 0.001);

        // Flujo normal pasos 3 a 6: Borrar partida y verificar que la ranura queda libre
        assertTrue(admin.borrarArchivo(0));
        assertNull(admin.getArchivo(0));
        assertTrue(admin.obtenerRanurasLibres(3).contains(0));
    }
}
