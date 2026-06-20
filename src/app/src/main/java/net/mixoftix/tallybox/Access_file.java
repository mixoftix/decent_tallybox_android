package net.mixoftix.tallybox;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Access_file {
    // BGN: handling file for path saving in client side
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    // END: handling file for path saving in client side

    public static String access_file_func_read(Context context, String file_key) {

        // BGN: handling file for path saving in client side
        sharedPreferences = context.getSharedPreferences(MainActivity.file_name_path, Activity.MODE_PRIVATE);
        String temp_read_file = sharedPreferences.getString(file_key, "-");

        if (temp_read_file.length()>0) {
            // do nothing
        } else {
            temp_read_file = "-";
        }

        return temp_read_file;
        // END: handling file for path saving in client side
    }

    public static void access_file_func_write(Context context, String file_key, String value_to_write, String write_mode_append_or_write) {

        // BGN: handling file for path saving in client side
        sharedPreferences = context.getSharedPreferences(MainActivity.file_name_path, Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String resident_value_to_write = access_file_func_read(context, file_key);

        if (write_mode_append_or_write.equals("append")) {
            if(resident_value_to_write.equals("-")){
                resident_value_to_write = "";
            }
            resident_value_to_write = resident_value_to_write + value_to_write;

        } else {

            resident_value_to_write = value_to_write;
        }

        editor.putString(file_key, resident_value_to_write);
        editor.apply();
        // END: handling file for path saving in client side
    }
}
