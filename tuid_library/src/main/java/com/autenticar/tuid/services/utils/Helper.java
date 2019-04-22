package com.autenticar.tuid.services.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Helper {

    public static String changeStringCase(String s) {

        final String DELIMITERS = " '-/";

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (DELIMITERS.indexOf((int) c) >= 0);
        }
        return sb.toString();
    }


    private static String GetFromatedFromDate(Date date) {
        Calendar mCalendarFechaNac = new GregorianCalendar();
        mCalendarFechaNac.setTime(date);
        int currentDay = mCalendarFechaNac.get(Calendar.DATE);
        int currentMonth = mCalendarFechaNac.get(Calendar.MONTH) + 1;
        int currentYear = mCalendarFechaNac.get(Calendar.YEAR);

        return String.format("%s-%s-%s", currentYear, currentMonth, currentDay);

    }
}
