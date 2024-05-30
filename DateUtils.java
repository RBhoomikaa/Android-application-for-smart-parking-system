package com.app.smartparking.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String TIME_STAMP = "yyyy-MM-dd HH:mm:ss";

    public static final String now() {
        return new SimpleDateFormat(TIME_STAMP).format(new Date());
    }

    public static Date stringToDate(@NonNull String dateStr) {
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_STAMP, Locale.ENGLISH);
        try {
            date = dateFormat.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
}
