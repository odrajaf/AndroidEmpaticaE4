package com.example.fajardo.empaticatest;

public class DataSetPaciente {

    float eda;
    int pulsaciones;
    float temperatura;
    String idPaciente;
    String idSesion;

    public DataSetPaciente(float eda, int pulsaciones, float temperatura, String idPaciente,
                           String idSesion){
        this.eda = eda;
        this.pulsaciones = pulsaciones;
        this.temperatura = temperatura;
        this.idPaciente = idPaciente;
        this.idSesion = idSesion;
    }

    public String toString(){
        String salida = String.valueOf(eda) + ";"+String.valueOf(pulsaciones) +";";
        salida = salida + String.valueOf(temperatura) +";"+ String.valueOf(idPaciente) +";";
        salida = salida +  String.valueOf(idSesion);
        return salida;
    }
}
