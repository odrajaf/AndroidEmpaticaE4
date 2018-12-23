package com.example.fajardo.empaticatest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Sesion {
    private long idSesion;
    private Date fechaInicio;
    private Date fechaFin;
    private long idPaciente;


    public Sesion(long idSesion, Date fechaInicio, Date fechaFin, long idPaciente) {
        this.idSesion = idSesion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.idPaciente = idPaciente;
    }

    public long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(long idSesion) {
        this.idSesion = idSesion;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String duracionSesion(){
        long diferencia = fechaFin.getTime() - fechaInicio.getTime();

        int hora =(int) diferencia/3600000;
        int restohora = (int) diferencia%3600000;
        int minutos = (int) restohora/60000;
        int restominuto = (int) restohora%60000;
        int segundos = (int) restominuto/1000;
        String duracionStr = String.format("%02d",hora) +":"
                +String.format("%02d",minutos) +":" + String.format("%02d",segundos);
        return duracionStr;
    }
}
