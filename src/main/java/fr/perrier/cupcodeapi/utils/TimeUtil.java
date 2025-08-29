package fr.perrier.cupcodeapi.utils;

import org.apache.commons.lang.time.DurationFormatUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static final long PERMANENT = -1;
    static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    static final ThreadLocal<DecimalFormat> SECONDS = ThreadLocal.withInitial(() -> new DecimalFormat("0.#"));
    static final ThreadLocal<DecimalFormat> TRAILING = ThreadLocal.withInitial(() -> new DecimalFormat("0"));

    /**
     * Format a duration in milliseconds to a human-readable string.
     *
     * @param input Duration in milliseconds.
     * @return Formatted duration string.
     */
    private static String formatDuration(long input) {
        return DurationFormatUtils.formatDurationWords(input, true, true);
    }

    /**
     * Format a timestamp (in milliseconds) to a date string.
     *
     * @param value Timestamp in milliseconds.
     * @return Formatted date string.
     */
    public static String formatDate(long value) {
        return FORMAT.format(new Date(value));
    }


    /**
     * Get a human-readable duration string from a duration in milliseconds.
     *
     * @param input Duration in milliseconds.
     * @return Human-readable duration string.
     */
    public static String getDuration(long input) {
        if (input == -2) return "&cIndéterminé";
        return input == PERMANENT ? "Permanent" : formatDuration(input);
    }

    /**
     * Parse a duration string (e.g., "1d2h30m") to milliseconds.
     * If the string starts with a letter, it is considered permanent.
     *
     * @param input Duration string.
     * @return Duration in milliseconds or PERMANENT.
     */
    public static long getDuration(String input) {

        if (Character.isLetter(input.charAt(0))) {
            return PERMANENT;
        }

        long result = 0L;

        StringBuilder number = new StringBuilder();

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);

            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                String str = number.toString();

                if (Character.isLetter(c) && !str.isEmpty()) {
                    result += convert(Integer.parseInt(str), c);
                    number = new StringBuilder();
                }
            }
        }

        return result;
    }

    /**
     * Convert a time value and its unit to milliseconds.
     *
     * @param value    Time value.
     * @param charType Time unit character (y, M, w, d, h, m, s).
     * @return Time in milliseconds or -1 if the unit is invalid.
     */
    static long convert(int value, char charType) {
        return switch (charType) {
            case 'y' -> value * TimeUnit.DAYS.toMillis(365L);
            case 'M' -> value * TimeUnit.DAYS.toMillis(30L);
            case 'w' -> value * TimeUnit.DAYS.toMillis(7L);
            case 'd' -> value * TimeUnit.DAYS.toMillis(1L);
            case 'h' -> value * TimeUnit.HOURS.toMillis(1L);
            case 'm' -> value * TimeUnit.MINUTES.toMillis(1L);
            case 's' -> value * TimeUnit.SECONDS.toMillis(1L);
            default -> -1L;
        };
    }

    /**
     * Format a duration in seconds to a "HH:mm:ss" string.
     *
     * @param i Duration in seconds.
     * @return Formatted time string.
     */
    public static String niceTime(int i) {
        int r = i * 1000;
        int sec = r / 1000 % 60;
        int min = r / 60000 % 60;
        int h = r / 3600000 % 24;
        return (h > 0 ? (h < 10 ? "0" : "") + h + ":" : "") + (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
    }

    /**
     * Format a duration in milliseconds to a "HH:mm:ss" string.
     *
     * @param i Duration in milliseconds.
     * @return Formatted time string.
     */
    public static String niceTime(long i) {
        long sec = i / 1000 % 60;
        long min = i / 60000 % 60;
        long h = i / 3600000 % 24;
        return (h > 0 ? (h < 10 ? "0" : "") + h + ":" : "") + (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
    }

    /**
     * Format a duration in milliseconds to a human-readable string.
     * If the duration is less than a minute and milliseconds is true, it shows seconds with one decimal place.
     * Otherwise, it shows the duration in "HH:mm:ss" format.
     *
     * @param millis       Duration in milliseconds.
     * @param milliseconds Whether to show milliseconds for durations less than a minute.
     * @return Formatted time string.
     */
    public static String niceTime(long millis, boolean milliseconds) {
        return niceTime(millis, milliseconds, true);
    }

    /**
     * Format a duration in milliseconds to a human-readable string.
     * If the duration is less than a minute and milliseconds is true, it shows seconds with one decimal place.
     * Otherwise, it shows the duration in "HH:mm:ss" format.
     *
     * @param duration     Duration in milliseconds.
     * @param milliseconds Whether to show milliseconds for durations less than a minute.
     * @param trail        Whether to show trailing zeros in seconds.
     * @return Formatted time string.
     */
    public static String niceTime(long duration, boolean milliseconds, boolean trail) {
        return milliseconds && duration < TimeUnit.MINUTES.toMillis(1) ? (trail ? TRAILING : SECONDS).get().format((double) duration * 0.001) + 's' : DurationFormatUtils.formatDuration(duration, (duration >= TimeUnit.HOURS.toMillis(1) ? "HH:" : "") + "mm:ss");
    }

    /**
     * Format the difference between the current date and a given date in a simplified manner.
     *
     * @param date The target date in milliseconds.
     * @return A string representing the difference (e.g., "2d3h").
     */
    public static String formatSimplifiedDateDiff(long date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date);

        return formatSimplifiedDateDiff(new GregorianCalendar(), calendar);
    }

    /**
     * Format the difference between two dates in a simplified manner.
     *
     * @param fromDate The starting date.
     * @param toDate   The target date.
     * @return A string representing the difference (e.g., "2d3h").
     */
    public static String formatSimplifiedDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;

        if (toDate.equals(fromDate)) {
            return "now";
        }

        if (toDate.after(fromDate)) {
            future = true;
        }

        StringBuilder sb = new StringBuilder();
        int[] types = new int[]{1, 2, 5, 11, 12, 13};
        String[] names = new String[]{"y", "y", "m", "m", "d", "d", "h", "h", "m", "m", "s", "s"};
        int accuracy = 0;

        for (int i = 0; i < types.length && accuracy <= 2; ++i) {
            int diff = dateDiff(types[i], fromDate, toDate, future);

            if (diff <= 0) {
                continue;
            }

            ++accuracy;
            sb.append(diff).append(names[i * 2 + (diff > 1 ? 1 : 0)]);
        }

        return sb.isEmpty() ? "now" : sb.toString().trim();
    }

    /**
     * Calculate the difference between two dates for a specific calendar field.
     *
     * @param type     The calendar field (e.g., Calendar.YEAR, Calendar.MONTH).
     * @param fromDate The starting date.
     * @param toDate   The target date.
     * @param future   Whether to calculate for future dates.
     * @return The difference in the specified field.
     */
    static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();

        while (future && !fromDate.after(toDate) || !future && !fromDate.before(toDate)) {
            savedDate = fromDate.getTimeInMillis();

            fromDate.add(type, future ? 1 : -1);

            ++diff;
        }

        fromDate.setTimeInMillis(savedDate);
        return --diff;
    }

    /**
     * Convert milliseconds to a rounded time string (e.g., "2 days", "3 hours").
     *
     * @param millis Duration in milliseconds.
     * @return Rounded time string.
     */
    public static String millisToRoundedTime(long millis) {
        ++millis;
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;
        if (years > 0L) {
            return years + " year" + (years == 1L ? "" : "s");
        } else if (months > 0L) {
            return months + " month" + (months == 1L ? "" : "s");
        } else if (weeks > 0L) {
            return weeks + " week" + (weeks == 1L ? "" : "s");
        } else if (days > 0L) {
            return days + " day" + (days == 1L ? "" : "s");
        } else if (hours > 0L) {
            return hours + " hour" + (hours == 1L ? "" : "s");
        } else {
            return minutes > 0L ? minutes + " minute" + (minutes == 1L ? "" : "s") : seconds + " second" + (seconds == 1L ? "" : "s");
        }
    }

    /**
     * Convert a duration in milliseconds to a detailed time string (e.g., "2 days 3 hours 15 minutes").
     *
     * @param duration Duration in milliseconds.
     * @return Detailed time string.
     */
    public static String getReallyNiceTime(long duration) {
        if (duration == -2) return "Indéterminé";
        if (duration == -1) return "Permanent";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return (days > 0 ? (days + " jour(s) ") : "") + (hours % 24 > 0 ? (hours % 24 + " heure(s) ") : "") + (minutes % 60 > 0 ? (minutes % 60 + " minute(s) ") : "") + (seconds % 60 > 0 ? (seconds % 60 + " seconde(s)") : "");
    }

    /**
     * Convert a duration in milliseconds to a compact time string (e.g., "2j 3h 15m").
     *
     * @param duration Duration in milliseconds.
     * @return Compact time string.
     */
    public static String getSmallReallyNiceTime(long duration) {
        if (duration == -2) return "Indéterminé";
        if (duration == -1) return "Permanent";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return (days > 0 ? (days + "j ") : "") + (hours % 24 > 0 ? (hours % 24 + "h ") : "") + (minutes % 60 > 0 ? (minutes % 60 + "m ") : "") + (seconds % 60 > 0 ? (seconds % 60 + "s ") : "");
    }

    /**
     * Convert milliseconds to a small rounded time string (e.g., "2d", "3h").
     *
     * @param millis Duration in milliseconds.
     * @return Small rounded time string.
     */
    public static String millisToSmallRoundedTime(long millis) {
        ++millis;
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;
        if (years > 0L) {
            return years + "y";
        } else if (months > 0L) {
            return months + "M";
        } else if (weeks > 0L) {
            return weeks + "w";
        } else if (days > 0L) {
            return days + "d";
        } else if (hours > 0L) {
            return hours + "h";
        } else {
            return minutes > 0L ? minutes + "m" : seconds + "s";
        }
    }
}
