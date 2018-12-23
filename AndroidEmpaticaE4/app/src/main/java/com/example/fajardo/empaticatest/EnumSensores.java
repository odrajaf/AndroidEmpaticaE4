package com.example.fajardo.empaticatest;


    public enum EnumSensores  {
        EDA(1) , IBI(2),TEMP(3),ACELEROM(4), BVP(5), PULSAC(6),ACELERX(7),ACELERY(8),ACELERZ(9);

        private float value;

        private EnumSensores(int value) {
            this.value = value;
        }


        float value() {
            return value;
        }
    }

