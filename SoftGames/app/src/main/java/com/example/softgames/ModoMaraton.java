package com.example.softgames;

public class ModoMaraton extends ModoJuego{
    public ModoMaraton(IEnrutador enrutador) {
        super(enrutador);
    }

    @Override
    public void crearAdministrador() {
        administrador = new AdministradorMinijuego(enrutador);
        administrador.setMinijuegoFinalizado((esCorrecto, puntos) -> {
            if(esCorrecto){
                administrador.aumentarPuntuacion(puntos);
                administrador.siguienteMinijuego();
            }
            else{
                administrador.detener();
            }
        });
    }
}
