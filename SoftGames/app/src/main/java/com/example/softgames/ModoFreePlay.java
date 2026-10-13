package com.example.softgames;

public class ModoFreePlay extends ModoJuego{
    public ModoFreePlay(IEnrutador enrutador) {
        super(enrutador);
    }

    @Override
    public void crearAdministrador() {
        administrador = new AdministradorMinijuego(null);
    }

}
