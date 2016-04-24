package com.aalto.happypolar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Gaurav on 16-Apr-16.
 */
public class DateUtility {

    private static String isoFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static Date getDateFromISOString(String isoStringDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(isoFormat);
        Date date = null;
        try {
            date = sdf.parse(isoStringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getISOStringFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(isoFormat);
        String isoString = sdf.format(date);
        return isoString;
    }


    public static Date decrementDate(Date date, int daysToDecrement) {
        int decr = 0 - daysToDecrement;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, decr);
        return calendar.getTime();
    }

    public static Date removeTimePart(Date dt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static boolean isSameDay(Date d1, Date d2) {
        Date dt1 = removeTimePart(d1);
        Date dt2 = removeTimePart(d2);

        int diff = dt1.compareTo(dt2);
        if (diff == 0) return true; else return false;
    }


    public static String getMMMd(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d");
        return sdf.format(d);
    }
}
