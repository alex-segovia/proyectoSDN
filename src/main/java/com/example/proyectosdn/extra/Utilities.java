package com.example.proyectosdn.extra;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utilities {
    public static String obtenerFechaHoraActual(){
        return ZonedDateTime.now(ZoneId.of("GMT-5")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
