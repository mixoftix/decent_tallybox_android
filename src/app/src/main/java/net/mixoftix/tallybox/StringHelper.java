package net.mixoftix.tallybox;

import android.content.Context;
import android.text.TextUtils;
import androidx.core.text.HtmlCompat;

public class StringHelper {

    private final Context context;

    public StringHelper(Context context) {
        this.context = context;
    }

    // ==================== Always ASCII Digits ====================
    public static String normalizeDigits(String input) {
        if (input == null) return null;

        StringBuilder out = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // Persian digits
            if (ch >= '۰' && ch <= '۹') {
                ch = (char) (ch - '۰' + '0');
            }
            // Arabic digits
            else if (ch >= '٠' && ch <= '٩') {
                ch = (char) (ch - '٠' + '0');
            }

            out.append(ch);
        }

        return out.toString();
    }

    // ==================== Last Update ====================
    public CharSequence getLastUpdateText(String timeDifference) {
        String label = context.getString(R.string.last_update_label);
        String time = (timeDifference != null ? timeDifference : "");

        String html = label + " <b>" + time + "</b>";
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    // ==================== Network Status ====================
    public CharSequence getNetworkAccessText(String network_status) {
        if (network_status == null || network_status.isEmpty()) {
            return "";
        }

        String label = context.getString(
                "OK".equals(network_status) ?
                        R.string.network_status_ok :
                        R.string.network_status_error
        );

        String color = "OK".equals(network_status) ? "#00FFFF" : "#FF4444";

        String html = label + " <font color='" + color + "'>" + network_status + "</font> / ";
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    // ==================== Combined (Recommended) ====================
    public CharSequence getBalanceHeaderText(String networkStatus, String epoch1, String epoch2) {
        String lang = LocaleHelper.getCurrentLanguage(context);
        String timeDiff = Access_time.getTimeDifference(lang, epoch1, epoch2);

        CharSequence netPart = getNetworkAccessText(networkStatus);
        CharSequence updatePart = getLastUpdateText(timeDiff);

        return TextUtils.concat(netPart, updatePart);
    }
}