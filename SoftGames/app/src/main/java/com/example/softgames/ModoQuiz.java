package com.example.softgames;

public class ModoQuiz extends ModoJuego{
    public ModoQuiz(IEnrutador enrutador) {
        super(enrutador);
    }

    @Override
    public void crearAdministrador() {
        administrador = new AdministradorMinijuego(enrutador);
        administrador.setMinijuegoFinalizado((esCorrecto, puntos) -> {
            if(esCorrecto){
                administrador.aumentarPuntuacion(puntos);
            }
            administrador.siguienteMinijuego();
        });
    }
}
