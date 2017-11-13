package com.siddapps.android.simpleweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    public static String formatTime(long millis) {
        Date date = new Date(millis * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE hh:mm a");
        return sdf.format(date);
    }
}
