package com.autenticar.tuid.utils;

import android.util.Log;

import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.services.utils.Helper;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Marcelo on 28/10/2016.
 */

public class DNIArgentinaPDF417ParserHelper {

    private static final String TAG = "Tuid";
    private static final String[] Meses = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};

    public static void Parse(String text, Persona persona) {
/*

Documento NUEVO 2
0 TRAMITE@
1 APELLIDO
2 NOMBRE
3 GENERO
4 Documento
5 OTRO
6 FECHA NAC
7 FECHA EMI
8 OTRO

Documento NUEVO
0 TRAMITE@
1 APELLIDO
2 NOMBRE
3 GENERO
4 Documento
5 OTRO
6 FECHA NAC
7 FECHA EMI

Documento ANTERIOR
1 Numero de Documento
2 OTRO
3 OTRO
4 APELLIDO
5 NOMBRE
6 NACIONALIDAD
7 FECHA NAC
8 SEXO
9 FECHA EMITE
10 OTRO
11 OTRO
12 FECHA VENC
RESTO NO IMPORTA
 */int value=0;

        try {
            String[] result = text.split("@");
            /*for (int i=0; i<result.length; i++){
                System.out.println(i+"-"+result[i]);
            }*/
            if (result.length == 9) { //DOCUMENTO DIGITAL
                persona.TramiteQR = result[0].trim();
                persona.DocumentoQR = result[4].trim();
                persona.DocumentoOCRFront = formatNumber(Long.parseLong(persona.DocumentoQR));
                persona.ApellidoQR = Helper.changeStringCase(result[1].trim());
                persona.NombreQR = Helper.changeStringCase(result[2].trim());
                persona.EsMasculinoQR = (result[3].equals("M"));
                persona.FechaNacQR = parseDateDDMMYYYY(result[6]);
                persona.FechaNacimientoOCRFront = GetNTText(persona.FechaNacQR);
                persona.SetBarcodeReaded();
            } else if (result.length == 8) { // Documento LIBRETA
                persona.TramiteQR = result[0].trim();
                persona.DocumentoQR = result[4].trim();
                persona.DocumentoOCRFront = formatNumber(Long.parseLong(persona.DocumentoQR));
                persona.ApellidoQR = Helper.changeStringCase(result[1].trim());
                persona.NombreQR = Helper.changeStringCase(result[2].trim());
                persona.EsMasculinoQR = (result[3].equals("M"));
                persona.FechaNacQR = parseDateDDMMYYYY(result[6]);
                persona.FechaNacimientoOCRFront = GetNTText(persona.FechaNacQR);
                persona.SetBarcodeReaded();
            } else if(result.length==17){
                //persona.TramiteQR = result[0].trim();
                persona.DocumentoQR = result[1].trim();
                persona.DocumentoOCRFront = formatNumber(Long.parseLong(persona.DocumentoQR));
                persona.ApellidoQR = chararterSpece(Helper.changeStringCase(result[4]));
                persona.NombreQR = chararterSpece(Helper.changeStringCase(result[5]));
                persona.EsMasculinoQR = result[8].equals("M");
                persona.FechaNacQR = parseDateDDMMYYYY(result[7]);
                persona.FechaNacimientoOCRFront = GetNTText(persona.FechaNacQR);
                persona.NacionalidadOCR = Helper.changeStringCase(result[6]);
                persona.SetBarcodeReaded();

            }else if (result.length > 6) {
                persona.DocumentoQR = result[1].trim();
                persona.ApellidoQR = Helper.changeStringCase(result[4].trim());
                persona.NombreQR = Helper.changeStringCase(result[5].trim());
                persona.EsMasculinoQR = (result[8].equals("M"));
                persona.FechaNacQR = parseDateDDMMYYYY(result[7]);
                persona.FechaNacimientoOCRFront = GetNTText(persona.FechaNacQR);
                persona.SetBarcodeReaded();
                //sexo?
            }
        } catch (Exception ex) {
            Log.e("ParsePersona", ex.getMessage());
        }
    }

    private static String formatNumber(long number) {
        Locale spanish = new Locale("es", "ES");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(spanish);
        return numberFormat.format(number);
    }


    private static Date parseDateDDMMYYYY(String date) {
        Calendar mCalendarFechaNac = new GregorianCalendar();
        String[] dates = date.split("/");
        mCalendarFechaNac.set(Calendar.YEAR, Integer.parseInt(dates[2]));
        mCalendarFechaNac.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
        mCalendarFechaNac.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[0]));

        return mCalendarFechaNac.getTime();
    }

    private static String GetNTText(Date date) {
        Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());

        localCalendar.setTime(date);
        int currentDay = localCalendar.get(Calendar.DATE);
        int currentMonth = localCalendar.get(Calendar.MONTH) + 1;
        int currentYear = localCalendar.get(Calendar.YEAR);

        return String.format("%s %s/ %s %s", currentDay, Meses[currentMonth - 1], Meses[currentMonth - 1], currentYear);
    }
    /**
     * Función que elimina acentos y caracteres especiales de
     * una cadena de texto.
     * @param input
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    public static String chararterSpece(String input) {
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i=0; i<original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }

}
