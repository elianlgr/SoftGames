package com.example.softgames;

public abstract class ModoJuego {
    AdministradorMinijuego administrador;
    public ModoJuego() {
        crearAdministrador();
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
}
