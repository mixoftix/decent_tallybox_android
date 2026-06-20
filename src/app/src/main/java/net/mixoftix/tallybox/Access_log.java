package net.mixoftix.tallybox;

import android.util.Log;

public class Access_log {

    public static void log_it(String log_type, String log_tag, String log_msg){

        if (!MainActivity.log_is_enable) { return; }

        switch (log_type)
        {
            case "d":
                Log.d(log_tag,log_msg);
                break;
            case "e":
                Log.e(log_tag,log_msg);
                break;
            case "i":
                Log.i(log_tag,log_msg);
                break;
            case "w":
                Log.w(log_tag,log_msg);
                break;
            case "wtf":
                Log.wtf(log_tag,log_msg);
                break;
            default:
                Log.v(log_tag,log_msg);
                break;
        }

    }
}
