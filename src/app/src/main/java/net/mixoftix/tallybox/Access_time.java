package net.mixoftix.tallybox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
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


    public static String back_from_utc_persian(String my_utc)
    {
        long utcSeconds = (long) Double.parseDouble(my_utc);
        long utcMillis = utcSeconds * 1000;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(utcMillis);

        int gy = cal.get(Calendar.YEAR);
        int gm = cal.get(Calendar.MONTH) + 1;
        int gd = cal.get(Calendar.DAY_OF_MONTH);

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        String jalaliDate = gregorianToJalali(gy, gm, gd);

        String result = jalaliDate + " ساعت: " + hour + ":" + minute + ":" + second;

        return StringHelper.normalizeDigits(result); // optional ASCII-only
    }


    private static String gregorianToJalali(int gy, int gm, int gd) {

        // Persian month names
        String[] persianMonths = {
                "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
                "مهر","آبان","آذر","دی","بهمن","اسفند"
        };

        int[] g_d_m = {0,31, (gy%4==0 && gy%100!=0) || (gy%400==0) ? 29 : 28,
                31,30,31,30,31,31,30,31,30,31};

        int jy = (gy <= 1600) ? 0 : 979;
        gy -= (gy <= 1600) ? 621 : 1600;

        int gy2 = (gm > 2) ? (gy + 1) : gy;

        long days = (365 * gy) + ((gy2 + 3) / 4)
                - ((gy2 + 99) / 100) + ((gy2 + 399) / 400);

        for (int i = 1; i < gm; ++i)
            days += g_d_m[i];

        days += gd - 1;

        long j_days = days - 79;
        long j_np = j_days / 12053;
        j_days %= 12053;

        jy += 33 * j_np + 4 * (j_days / 1461);
        j_days %= 1461;

        if (j_days >= 366) {
            jy += (j_days - 366) / 365;
            j_days = (j_days - 366) % 365;
        }

        int jm, jd;
        if (j_days < 186) {
            jm = 1 + (int)(j_days / 31);
            jd = 1 + (int)(j_days % 31);
        } else {
            jm = 7 + (int)((j_days - 186) / 30);
            jd = 1 + (int)((j_days - 186) % 30);
        }

        return jy + " " + persianMonths[jm - 1] + " " + jd;
    }

}
