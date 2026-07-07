package net.mixoftix.tallybox;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static List<String> followup_keys_list(Context context)
    {
        // BGN: handling file for path saving in client side
        SharedPreferences prefs = context.getSharedPreferences("followups", Context.MODE_PRIVATE);

        Map<String, ?> all = prefs.getAll();
        List<String> keys = new ArrayList<>();

        for (String key : all.keySet())
        {
            if (key.startsWith("followup_"))
            {
                keys.add(key);
            }
        }

        return keys;
        // END: handling file for path saving in client side
    }
    public static String followup_keys_read(Context context, String followup_key)
    {
        // BGN: handling file for path saving in client side
        SharedPreferences prefs = context.getSharedPreferences("followups", Context.MODE_PRIVATE);
        return prefs.getString(followup_key, "");

        // END: handling file for path saving in client side
    }
    public static String followup_keys_write(Context context, String followup_timestamp, String followup_text)
    {
        // BGN: handling file for path saving in client side
        SharedPreferences prefs = context.getSharedPreferences("followups", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String key = "followup_" + followup_timestamp;
        editor.putString(key, followup_text);
        editor.apply();

        return key;
        // END: handling file for path saving in client side
    }
    public static String followup_keys_remove(Context context, String followup_key)
    {
        SharedPreferences prefs = context.getSharedPreferences("followups", Context.MODE_PRIVATE);

        if (prefs.contains(followup_key)) {
            prefs.edit().remove(followup_key).apply();
            return "ignored";
        }

        return "failed";
    }

}
