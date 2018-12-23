package com.example.fajardo.empaticatest;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.Serializable;

public class GraficasPersistencia implements Serializable {
    private LineChart mChartPulsaciones;
    private LineChart mChartEDA;
    private boolean graficasPreparadas = false;


    public GraficasPersistencia(LineChart mChartPulsaciones, LineChart mChartEDA) {
        this.mChartPulsaciones = mChartPulsaciones;
        this.mChartEDA = mChartEDA;
    }

    public GraficasPersistencia() {

    }


    public boolean isGraficasPreparadas() {
        return graficasPreparadas;
    }

    public void setGraficasPreparadas(boolean graficasPreparadas) {
        this.graficasPreparadas = graficasPreparadas;
    }


    public LineChart getmChartEDA() {
        return mChartEDA;
    }

    public void setmChartEDA(LineChart mChartEDA) {
        this.mChartEDA = mChartEDA;
    }

    public LineChart getmChartPulsaciones() {
        return mChartPulsaciones;
    }

    public void setmChartPulsaciones(LineChart mChartPulsaciones) {
        this.mChartPulsaciones = mChartPulsaciones;
    }

    public LineDataSet createSetEda() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    public LineDataSet createSetPulsa() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

}
