package com.example.softgames;

public class ModoFreePlay extends ModoJuego{
    public ModoFreePlay(IEnrutador enrutador) {super(enrutador);}

    @Override
    public void crearAdministrador() {
        administrador = new AdministradorMinijuego(null);
        administrador.setMinijuegoFinalizado((esCorrecto, puntos) -> {
            if(esCorrecto != null) {
                if(esCorrecto){
                    administrador.aumentarPuntuacion(puntos);
                }
                Minijuego minActual = administrador.getMinijuegoActual();
                if(minActual != null){
                    minActual.reiniciarMinijuego();
                }
            }
        });
    }


}
