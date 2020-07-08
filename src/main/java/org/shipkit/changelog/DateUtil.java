package org.shipkit.changelog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date and Time utilities
 */
class DateUtil {

    /**
     * Formats date to most reasonable format to show on the release notes
     */
    static String formatDate(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(date);
    }

    /**
     * Parse Date in epoch seconds (Unix time).
     */
    static Date parseDateInEpochSeconds(String date) {
        return new Date(Long.parseLong(date) * 1000);
    }

    /**
     * Formats date to local timezone to shows in debug logs
     */
    static String formatDateToLocalTime(Date date, TimeZone tz) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm a z");
        f.setTimeZone(tz);
        return f.format(date);
    }
}
