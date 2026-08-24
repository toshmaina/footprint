package com.skyworld.util.formatting;


import com.skyworld.config.Constants;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DateTime {

    private static final ZoneId NAIROBI_ZONE = ZoneId.of("Africa/Nairobi");

    /**
     *
     * @return current UNIX timestamp
     * of type Long
     */
    public static long getCurrentUnixTimestamp() {
        return System.currentTimeMillis();
    }

    public static Timestamp getCurrentSqlTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp getSqlTimestamp(long unixTimestamp) {
        return new Timestamp(unixTimestamp);
    }

    /**
     *
     * @param date java.util.Date object
     * @return equivalent UNIX timestamp
     * of type Long
     */
    public static long getTimestamp(Date date) {
        try {
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     *
     * @param date of type String
     * @return equivalent UNIX timestamp
     * of type Long
     */
    public static long getTimestamp(String date) {
        try {
            Date d = convertDateStringToDate(date);
            if (d != null) {
                return d.getTime();
            }
            return 0L;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     *
     * @param format Desired date or date & time format
     *               Type String
     * @return current date
     * Type String
     */
    public static String getCurrentDate(String format) {
        try {
            return new SimpleDateFormat(format)
                    .format(new java.sql.Date(
                            getCurrentUnixTimestamp()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @return Type java.util.Date
     * Current DateTime
     */
    public static Date getCurrentJavaUtilDateTime() {
        return new Date();
    }

    /**
     *
     * @return Type java.util.Date
     * Current Date
     */
    public static Date getCurrentJavaUtilDate() {
        return new Date();
    }

    /**
     *
     * @return current date with system default format
     * Type String
     */
    public static String getCurrentDate() {
        return new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT)
                .format(new java.sql.Date(getCurrentUnixTimestamp()));
    }

    /**
     *
     * @return current date and time with system default format
     * Type String
     */
    public static String getCurrentDateTime() {
        return new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT)
                .format(new java.sql.Date(getCurrentUnixTimestamp()));
    }

    /**
     *
     * @param timestamp type String
     * @return equivalent date & time
     * Type String
     */
    public static String getDateFromTimestamp(String timestamp) {
        try {
            return new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT)
                    .format(new java.sql.Date(Long.parseLong(timestamp)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param timestamp type long
     * @return equivalent date & time
     * Type String
     */
    public static String getDateFromTimestamp(long timestamp) {
        try {
            return new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT)
                    .format(new java.sql.Date(timestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param date String value of Date to be converted
     *             Format = yyyy-M-dd HH:mm:ss (2017-10-25 18:02:25)
     * @return null, if exception occurs and java.util.Date Object if not
     */
    public static Date convertDateStringToDate(String date) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date addDaysToNow(int days) {
        Date now = convertDateStringToDate(getCurrentDateTime());
        Calendar cal = Calendar.getInstance();
        assert now != null;
        cal.setTime(now);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static Date addToNow(int time, int timeUnit) {
        Date now = convertDateStringToDate(getCurrentDateTime());
        Calendar cal = Calendar.getInstance();
        assert now != null;
        cal.setTime(now);
        cal.add(timeUnit, time);
        return cal.getTime();
    }

    public static Date addToDate(Date date, int time, int timeUnit) {
        Calendar cal = Calendar.getInstance();
        assert date != null;
        cal.setTime(date);
        cal.add(timeUnit, time);
        return cal.getTime();
    }

    public static Date subtractDaysFromNowDate(int days) {
        Date now = convertDateStringToDate(getCurrentDateTime());
        Calendar cal = Calendar.getInstance();
        assert now != null;
        cal.setTime(now);
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }

    public static String subtractDaysFromNowDateString(int days) {
        return DateTime.convertDateToDateString(subtractDaysFromNowDate(days));
    }

    public static Date convertDateStringToDate(String date, String format) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(format);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.sql.Date convertDateStringToSqlDate(String date, String format) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(format);
        try {
            return new java.sql.Date(simpleDateFormat.parse(date).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.sql.Time convertTimeStringToSqlTime(String time, String format) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(format);
        try {
            return new java.sql.Time(simpleDateFormat.parse(time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param date java.util.Date Object to convert to String
     * @return String value of Date
     * Format = yyyy-M-dd HH:mm:ss (2017-10-25 18:02:25)
     */
    public static String convertDateToDateString(Date date) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT);
        try {
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertDateToDateString(Date date, String format) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(format);
        try {
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String format(String date, String currentFormat, String newFormat) {
        return convertDateToDateString(convertDateStringToDate(date, currentFormat), newFormat);
    }

    public static String format(String date, String format) {
        return convertDateToDateString(convertDateStringToDate(date), format);
    }

    /**
     *
     * @param startDate Starting Date
     * @param endDate   Ending Date
     * @return Pretty and intelligent time difference
     * Resolution = seconds for short time diffs
     */
    public static String getPrettyDateTimeDifference(Date startDate, Date endDate) {

        String seconds = "second";
        String minutes = "minute";
        String hours = "hour";
        String days = "day";

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedSeconds > 1) seconds = seconds + "s";
        if (elapsedMinutes > 1) minutes = minutes + "s";
        if (elapsedHours > 1) hours = hours + "s";
        if (elapsedDays > 1) days = days + "s";

        if (elapsedDays <= 0) {
            if (elapsedHours <= 0) {
                if (elapsedMinutes <= 0) {
                    if (elapsedSeconds <= 0) {
                        return Constants.JUST_NOW;
                    } else {
                        return String.format("%d " + seconds + "%n", elapsedSeconds);
                    }
                } else {
                    if (elapsedSeconds > 0) {
                        return String.format("%d " + minutes + ", %d " + seconds + "%n",
                                elapsedMinutes, elapsedSeconds);
                    } else {
                        return String.format("%d " + minutes + "%n", elapsedMinutes);
                    }
                }
            } else {
                if (elapsedMinutes > 0) {
                    return String.format("%d " + hours + ", %d " + minutes + "%n",
                            elapsedHours, elapsedMinutes);
                } else {
                    return String.format("%d " + hours + "%n", elapsedHours);
                }
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d " + days + ", %d " + hours + "%n",
                        elapsedDays, elapsedHours);
            } else {
                return String.format("%d " + days + "%n", elapsedDays);
            }
        }
    }

    public static boolean isValid(String strDate, String format) {
        DateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(strDate);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static Object convertLocalDateToTimestamp(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    public static Object convertLocalDateToTimestamp(LocalDateTime localDate) {
        if (localDate == null) {
            return null;
        }
        return Timestamp.valueOf(localDate);
    }

    public static String formatDateString(String dateString) {

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime parsedDate = parseDate(dateString);
        return parsedDate.format(outputFormatter);
    }

    public static LocalDateTime parseDate(String dateString) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next format
            }
        }

        throw new IllegalArgumentException("Invalid date format: " + dateString);
    }

    public static Timestamp stringToTimestamp(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            System.err.println("Invalid date string: null or empty");
            return null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date parsedDate = dateFormat.parse(dateString);

            return new Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            System.err.println("Failed to parse date string: " + dateString);
            e.printStackTrace();
        }

        return null;
    }

    public static LocalDateTime parseFlexibleDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new DateTimeParseException("Input is null or empty", input, 0);
        }

        // Normalize: replace space with T if needed
        if (input.contains(" ") && !input.contains("T")) {
            input = input.replace(" ", "T");
        }

        // Try OffsetDateTime (e.g. 2025-08-13T10:30:00+03:00)
        try {
            OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME);
            return odt.atZoneSameInstant(NAIROBI_ZONE).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        // Try ZonedDateTime (e.g. 2025-08-13T10:30:00+03:00[Africa/Nairobi])
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(input, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            return zdt.withZoneSameInstant(NAIROBI_ZONE).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        // Try LocalDateTime (e.g. 2025-08-13T10:30:00) - assume Nairobi time
        try {
            LocalDateTime ldt = LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.atZone(NAIROBI_ZONE).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        // Try LocalDate (e.g. 2025-08-13) - assume start of day in Nairobi
        try {
            LocalDate date = LocalDate.parse(input, DateTimeFormatter.ISO_DATE);
            return date.atStartOfDay(NAIROBI_ZONE).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        throw new DateTimeParseException("Unrecognized date/time format", input, 0);
    }

    public String toCron(Timestamp timestamp) {

        if (timestamp == null) {
            return null;
        }

        ZonedDateTime zdt = timestamp.toInstant()
                .atZone(ZoneId.systemDefault());

        return String.format(
                "%d %d %d %d %d ? %d",
                zdt.getSecond(),
                zdt.getMinute(),
                zdt.getHour(),
                zdt.getDayOfMonth(),
                zdt.getMonthValue(),
                zdt.getYear()
        );
    }

    public static class DateTimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public DateTimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public boolean isValid() {
            return start != null && end != null && start.isBefore(end);
        }
    }


}


