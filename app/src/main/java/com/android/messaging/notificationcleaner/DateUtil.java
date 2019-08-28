package com.android.messaging.notificationcleaner;

import android.app.AlarmManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    // 格式：月/日/年
    private static final String FORMAT_DATE = "MM/dd/yyyy";
    private static final String FORMAT_DATE_HOUR = "h:mm a";

    public static String convertTimeStampToString(long timeMills) {
        DateFormat format = new SimpleDateFormat(FORMAT_DATE_HOUR, Locale.getDefault());
        return format.format(timeMills);
    }

    public static String getDaysAgoString(int numberDays) {
        Date numberDaysAgoDateString = getStartDateOfDaysAgo(numberDays);
        return convertDateToString(numberDaysAgoDateString);
    }

    public static String convertDateToString(Date date) {
        if (date == null) {
            return "";
        }

        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
        return format.format(date);
    }

    // 输入mills, 输出相隔几天
    public static long daysFromNow(long checkedTimeMills) {
        Date currentDate = new Date();
        Date endTimeOfCurrentDate = getEndOfDate(currentDate);

        return (endTimeOfCurrentDate.getTime() - checkedTimeMills) / AlarmManager.INTERVAL_DAY;
    }

    public static long getStartTimeStampOfDaysAgo(int dayNumber) {
        Date date = new Date();
        Date amountDayAgo = dateFieldOperation(Calendar.DAY_OF_YEAR, -dayNumber, date);

        return getEndOfDate(amountDayAgo).getTime();
    }

    public static Date getStartDateOfDaysAgo(int amount) {
        Date date = new Date();
        Date dateAfterOperation = dateFieldOperation(Calendar.DAY_OF_YEAR, -amount, date);

        return getStartOfDate(dateAfterOperation);
    }

    public static Date dateFieldOperation(int calendarField, int fieldNumber, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendarField, fieldNumber);

        return calendar.getTime();
    }

    public static Date getStartOfDate(Date date) {
        Calendar tempCalendar = Calendar.getInstance();
        Calendar returnedCalendar = Calendar.getInstance();

        tempCalendar.setTime(date);
        returnedCalendar.set(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH),
                tempCalendar.get(Calendar.DATE), 0, 0, 0);

        return returnedCalendar.getTime();
    }

    public static Date getEndOfDate(Date date) {
        Calendar tempCalendar = Calendar.getInstance();
        Calendar returnedCalendar = Calendar.getInstance();

        tempCalendar.setTime(date);
        returnedCalendar.set(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH),
                tempCalendar.get(Calendar.DATE), 23, 59, 59);

        return returnedCalendar.getTime();
    }
}