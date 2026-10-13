package com.example.softgames;

public abstract class ModoJuego{
    IEnrutador enrutador;
    AdministradorMinijuego administrador;
    public ModoJuego(IEnrutador enrutador) {

        crearAdministrador();
        this.enrutador = enrutador;
    }
    public abstract void crearAdministrador();
    public void elegirCategoria(int opcion){
        if(opcion >= 0){
            administrador.setCategoriaActual(opcion);
        }
    }
    public void update(){
        administrador.update();
    }
    public void finalizar(){

    }
}
