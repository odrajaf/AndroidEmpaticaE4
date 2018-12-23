package com.example.fajardo.empaticatest;


import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by philipp on 02/06/16.
 */
public class DayAxisValueFormatter implements IAxisValueFormatter
{

    public DayAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String valueStr = String.valueOf(value);
        String horaFormateada = "";

        int horas =(int) value/3600;
        int restohora = (int) value%3600;
        int minutos = (int) restohora/60;
        int restominuto = (int) restohora%60;
        int segundos = (int) restominuto;


        int milisegundos = Integer.parseInt(valueStr.substring(valueStr.indexOf('.')+1));

         horaFormateada = String.format("%02d",horas) +":"
                +String.format("%02d",minutos) +":" + String.format("%02d",segundos);


        return horaFormateada;
    }


}