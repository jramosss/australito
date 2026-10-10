package com.australito;

import com.australito.service.ConversionService;
import com.australito.service.DolarApiService;
import com.australito.service.GastoService;
import com.australito.view.MenuConsola;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando prototipo operacional Australito...");

        // Inicialización de servicios y componentes
        ConversionService conversionService = new DolarApiService();
        GastoService gastoService = new GastoService(conversionService);

        // Lanzamiento de la interfaz de consola
        MenuConsola menu = new MenuConsola(gastoService, conversionService);
        menu.iniciar();
    }
}
