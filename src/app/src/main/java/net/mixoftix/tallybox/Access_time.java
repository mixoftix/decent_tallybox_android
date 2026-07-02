package net.mixoftix.tallybox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Access_time {

    public static String getTimeDifference(String str_lang, String str_epoch1, String str_epoch2)
    {
        if (str_lang.equals("fa"))
        {
            if (str_epoch2.equals("-1"))
            {
                return "هرگز";
            }
        }
        else
        {
            if (str_epoch2.equals("-1"))
            {
                return "never";
            }
        }

        long epoch1 = (long)Double.parseDouble(str_epoch1) * 1000L;
        long epoch2 = (long)Double.parseDouble(str_epoch2) * 1000L;
        long diffInMillis = Math.abs(epoch1 - epoch2);

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        long diffInWeeks = diffInDays / 7;
        long diffInMonths = diffInDays / 30;

        //Log.v("shahin", "epoch1: " + epoch1);
        //Log.v("shahin", "epoch2: " + epoch2);
        //Log.v("shahin", "diffInMillis: " + diffInMillis);
        //Log.v("shahin", "diffInMinutes: " + diffInMinutes);

        if (str_lang.equals("fa"))
        {
            if (diffInMinutes < 1) {
                return "حالا";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " دقیقه قبل ";
            } else if (diffInHours < 24) {
                return diffInHours + " ساعت قبل ";
            } else if (diffInDays < 7) {
                return diffInDays + " روز قبل ";
            } else if (diffInWeeks < 4) {
                return diffInWeeks + " هفته قبل ";
            } else {
                return diffInMonths + " ماه قبل ";
            }
        }
        else
        {
            if (diffInMinutes < 1) {
                return "now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " minute(s) ago";
            } else if (diffInHours < 24) {
                return diffInHours + " hour(s) ago";
            } else if (diffInDays < 7) {
                return diffInDays + " day(s) ago";
            } else if (diffInWeeks < 4) {
                return diffInWeeks + " week(s) ago";
            } else {
                return diffInMonths + " month(s) ago";
            }
        }
    }

    public static String back_from_utc(String my_utc)
    {
        // Parse the string to a double
        double my_utc_number = Double.parseDouble(my_utc);
        // Cast the double to a long
        long my_utc_long = (long) my_utc_number;
        //Log.d("shahin", "my_utc_long: " + my_utc_long);

        long time = my_utc_long;
        time = time * 1000;
        Date date = new Date(time);
        //SimpleDateFormat format = new SimpleDateFormat("EEE, yyyy MMM dd, HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd, HH:mm:ss");

        //format.setTimeZone(TimeZone.getTimeZone("GMT"));
        //Log.d("shahin", format.format(date));

        return format.format(date).toString();
    }

    public static long getUnixTimestampSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

}
