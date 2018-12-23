package com.example.fajardo.empaticatest;

import com.github.mikephil.charting.data.Entry;
import java.util.List;

public class DataSetAcelerometro {
    private List<Entry> valoresEjeX;
    private List<Entry> valoresEjeY;
    private List<Entry> valoresEjeZ;


    public DataSetAcelerometro(List<Entry> valoresEjeX, List<Entry> valoresEjeY, List<Entry> valoresEjeZ) {
        this.valoresEjeX = valoresEjeX;
        this.valoresEjeY = valoresEjeY;
        this.valoresEjeZ = valoresEjeZ;
    }

    public List<Entry> getValoresEjeX() {
        return valoresEjeX;
    }

    public void setValoresEjeX(List<Entry> valoresEjeX) {
        this.valoresEjeX = valoresEjeX;
    }

    public List<Entry> getValoresEjeY() {
        return valoresEjeY;
    }

    public void setValoresEjeY(List<Entry> valoresEjeY) {
        this.valoresEjeY = valoresEjeY;
    }

    public List<Entry> getValoresEjeZ() {
        return valoresEjeZ;
    }

    public void setValoresEjeZ(List<Entry> valoresEjeZ) {
        this.valoresEjeZ = valoresEjeZ;
    }
}
