package com.aalto.happypolar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

}
