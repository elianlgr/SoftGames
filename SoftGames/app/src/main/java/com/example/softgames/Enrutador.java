package com.example.softgames;

public class Enrutador{

    private Estadisticas estadisticas;
    private AdministradorMinijuego am;
    private ModoJuego modoJuego;



    public Enrutador() {
    }
    public Enrutador(Estadisticas estadisticas){
        this.estadisticas = estadisticas;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(Estadisticas estadisticas) {
        this.estadisticas = estadisticas;
    }
    public ModoJuego getModoJuego(){
        return modoJuego;
    }
    public void setModoJuego(ModoJuego mj){
        modoJuego = mj;
    }
    public void manejar(IEvento evento){
        if(evento instanceof EventoMinijuego){
            EventoMinijuego em = (EventoMinijuego)evento;
            am.recibirDatos(em.esCorrecto(), em.obtenerPuntos());
        }
        else if(evento instanceof EventoModoFinalizado){

        }
    }

}