package com.example.softgames;

public class Utils {
    public static int numeroAleatorio(int minimo, int maximo){
        int diff = maximo-minimo;
        double random = Math.random();
        return minimo + (int)(random*diff);
    }
}
