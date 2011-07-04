package net.sacredlabyrinth.phaed.simpleteams;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author phaed
 */

public class Dates
{
    public static double differenceInMonths(Timestamp date1, Timestamp date2)
    {
	return differenceInMonths(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    public static double differenceInYears(Timestamp date1, Timestamp date2)
    {
	return differenceInYears(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    public static double differenceInDays(Timestamp date1, Timestamp date2)
    {
	return differenceInDays(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    public static double differenceInHours(Timestamp date1, Timestamp date2)
    {
	return differenceInHours(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    public static double differenceInMinutes(Timestamp date1, Timestamp date2)
    {
	return differenceInMinutes(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    public static double differenceInSeconds(Timestamp date1, Timestamp date2)
    {
	return differenceInSeconds(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    private static double differenceInMilliseconds(Timestamp date1, Timestamp date2)
    {
	return differenceInMilliseconds(new Date(date1.getTime()), new Date(date2.getTime()));
    }

    public static double differenceInMonths(Date date1, Date date2)
    {
	return differenceInYears(date1, date2) * 12;
    }

    public static double differenceInYears(Date date1, Date date2)
    {
	double days = differenceInDays(date1, date2);
	return  days / 365.2425;
    }

    public static double differenceInDays(Date date1, Date date2)
    {
	return differenceInHours(date1, date2) / 24.0;
    }

    public static double differenceInHours(Date date1, Date date2)
    {
	return differenceInMinutes(date1, date2) / 60.0;
    }

    public static double differenceInMinutes(Date date1, Date date2)
    {
	return differenceInSeconds(date1, date2) / 60.0;
    }

    public static double differenceInSeconds(Date date1, Date date2)
    {
	return differenceInMilliseconds(date1, date2) / 1000.0;
    }

    private static double differenceInMilliseconds(Date date1, Date date2)
    {
	return Math.abs(getTimeInMilliseconds(date1) - getTimeInMilliseconds(date2));
    }

    private static long getTimeInMilliseconds(Date date)
    {
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);
	return cal.getTimeInMillis() + cal.getTimeZone().getOffset(cal.getTimeInMillis());
    }
}