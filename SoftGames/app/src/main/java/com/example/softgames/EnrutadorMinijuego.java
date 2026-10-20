package com.example.softgames;

public class EnrutadorMinijuego implements IEnrutador<EventoMinijuego>{
    protected AdministradorMinijuego am;
    public EnrutadorMinijuego(AdministradorMinijuego am){
        this.am = am;
    }
    @Override
    public void manejar(EventoMinijuego evento) {
        am.recibirDatos(evento.esCorrecto(), evento.obtenerPuntos());
    }
    public void salir(){
        am.detener();
    }
}
