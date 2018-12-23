package com.example.fajardo.empaticatest;

public class Logueado {
    private long idPaciente;
    private String email;
    private boolean temporal;
    private String idMongo;

    public  Logueado(long idPaciente, String email, boolean temporal){
        this.idPaciente = idPaciente;
        this.email = email;
        this.temporal = temporal;
    }

    public  Logueado(long idPaciente, String email, boolean temporal, String idMongo){
        this.idPaciente = idPaciente;
        this.email = email;
        this.temporal = temporal;
        this.idMongo = idMongo;
    }

    public long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isTemporal() {
        return temporal;
    }

    public void setTemporal(boolean temporal) {
        this.temporal = temporal;
    }

    public String getIdMongo() {        return idMongo;    }

    public void setIdMongo(String idMongo) {        this.idMongo = idMongo;    }
}
