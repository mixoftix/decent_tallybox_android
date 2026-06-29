package net.mixoftix.tallybox;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.mixoftix.tallybox.databinding.ActivityMainHistoryDetailBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

public class MainActivity_History_Detail extends AppCompatActivity {

    private String detail_of_currency_name = "";
    private String tnx_tally_hash = "";
    private String browse_by_tally_hash = "";

    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;
    private String submit_txt = "";
    // END: browse http

    private TextView textview_network, textview_graph_in, textview_history_hash_tnxid,
            textview_history_hash_orderid, textview_history_hash_channel,
            textview_history_log_monitor, textview_history_log;
    private LinearLayout path_interactive_views;
    private static ActivityMainHistoryDetailBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_history_detail);

        binding = ActivityMainHistoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        detail_of_currency_name = getIntent().getStringExtra("detail_of_currency_name");
        tnx_tally_hash = getIntent().getStringExtra("tnx_tally_hash");
        //tnx_tally_hash = "81b6e0167092fdb04018cd9faa61fa00ac2e20f67142a0a077ff856de9f26862";

        textview_network = findViewById(R.id.textview_network);
        textview_graph_in = findViewById(R.id.textview_graph_in);
        textview_history_hash_tnxid = findViewById(R.id.textview_history_hash_tnxid);
        textview_history_hash_orderid = findViewById(R.id.textview_history_hash_orderid);
        textview_history_hash_channel = findViewById(R.id.textview_history_hash_channel);
        textview_history_log_monitor = findViewById(R.id.textview_history_log_monitor);
        textview_history_log = findViewById(R.id.textview_history_log);

        // define dynamic views
        path_interactive_views = (LinearLayout) findViewById(R.id.interactive_views);

        // connection
        textview_network.setText(HtmlCompat.fromHtml(
                "<font color='#FFD700'>Network: Connecting..</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_graph_in.setText("( please wait )");

        // Trigger code after 1 second (1000 milliseconds)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // read server data and generate views
                try {
                    // set progressbar
                    progressbar_stat = true;
                    doStartProgressBar2();

                    order_history_browser(tnx_tally_hash);

                    // reset progressbar
                    progressbar_stat = false;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500); // 1000 milliseconds = 1 second

    }

    private static void doStartProgressBar2()  {
        binding.progressBar2.setIndeterminate(true);

        Thread thread = new Thread(new Runnable()  {
            @Override
            public void run() {

                // Update interface
                handler.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    public void run() {
                        //binding.textviewWhatsUp.setText("Working...");
                        //buttonStart2.setEnabled(false);
                    }
                });

                while (progressbar_stat)
                {
                    // Do something ... (Update database,..)
                    SystemClock.sleep(500); // Sleep 1 seconds.
                }

                binding.progressBar2.setIndeterminate(false);
                binding.progressBar2.setMax(1);
                binding.progressBar2.setProgress(1);

                // Update interface
                handler.post(new Runnable() {
                    public void run() {
                        //textViewInfo2.setText("Completed!");
                        //buttonStart2.setEnabled(true);
                    }
                });
            }
        });
        thread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history_detail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String server_url_query = "";
        String result = "";

        if (id == R.id.action_home) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            finishAffinity();
            startActivity(i);

            return true;
        }

        if (id == R.id.action_back) {

            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //region function_of_graph_in

    // Show Graph From + Zones
    private void updateGraphFromDisplay() {
        int index = getGraphIndex(MainActivity.graph_domain_in);
        String zones = (index != -1) ? getZoneForGraph(index) : " [no zone]";

        String zonesText = (zones.length() > 0)
                ? " [" + String.join(", ", zones) + "]"
                : " [no zone]";

        //textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + zonesText + ")");
        textview_graph_in.setText(HtmlCompat.fromHtml(
                "(in graph: <b>" + MainActivity.graph_domain_in + "</b>" +
                        "<font color='cyan'>" + zonesText + "</font>)",
                HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
    // Get zones for a graph by index
    private int getGraphIndex(String domain) {
        for (int i = 0; i < MainActivity.spinner_options.length; i++) {
            if (MainActivity.spinner_options[i].equals(domain)) {
                return i;
            }
        }
        return -1;
    }
    // Get zone for a graph by index
    private String getZoneForGraph(int graphIndex) {
        if (graphIndex < 0 || graphIndex >= MainActivity.spinner_options_zones.length) {
            return "";
        }
        return MainActivity.spinner_options_zones[graphIndex];
    }

    //endregion

    private void order_history_browser(String detail_of_tnx_tally_hash) throws Exception {

        String the_log_str = "";
        String pqc_cipher_serial = "";
        String the_pqc_cipher = "";
        String the_pqc_psk = "";

        String is_pqc = MainActivity.setting_safeguard_pqc;

        Access_log.log_it("i","shahin","is_pqc: " + is_pqc);
        the_log_str = "PQC - Status: " + is_pqc + "d" + "\n";

        Access_log.log_it("i","shahin","333 - app_pqc_serial: " + MainActivity.app_pqc_serial);
        Access_log.log_it("i","shahin","333 - app_pqc_pk: " + MainActivity.app_pqc_pk);

        if (is_pqc.equals("enable") && MainActivity.app_pqc_serial.equals("-"))
        {
            Access_log.log_it("i","shahin","app_pqc_serial: not found! procedure bypassed.. (" + MainActivity.app_pqc_serial + ")");
            the_log_str += "PQC - Serial: not found! procedure bypassed..\n";
        }
        if (is_pqc.equals("enable") && !MainActivity.app_pqc_serial.equals("-"))
        {
            Access_log.log_it("i","shahin","app_pqc_serial: " + MainActivity.app_pqc_serial);
            the_log_str += "PQC - Serial: " + MainActivity.app_pqc_serial + "\n";

            // make the local privacy
            String local_pqc_sha256 = null;
            try {
                local_pqc_sha256 = hash_functions.Hash_SHA_256(MainActivity.app_pqc_pk);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            the_log_str += "PQC - Checksum: \n" + local_pqc_sha256 + "\n\n";

            // initial pqc connection
            pqc_mlkem.pqc_psk_pk();

            pqc_cipher_serial = MainActivity.app_pqc_serial;
            the_pqc_cipher = MainActivity.app_pqc_psk_cipher;
            the_pqc_psk = MainActivity.app_pqc_psk;

            Access_log.log_it("i","shahin","the_pqc_cipher: " + the_pqc_cipher);
            Access_log.log_it("i","shahin","the_pqc_psk: " + the_pqc_psk);

            the_log_str += "PQC - Cipher: " + "\n" + the_pqc_cipher + "\n\n";
            the_log_str += "PQC - PSK: " + "\n" + the_pqc_psk + "\n\n";
        }

        Access_log.log_it("i","shahin","in_graph: " + MainActivity.graph_domain_in);
        Access_log.log_it("i","shahin","wallet_address: " + MainActivity.wallet_address);
        Access_log.log_it("i","shahin","my_tally_hash: " + detail_of_tnx_tally_hash);
        Access_log.log_it("i","shahin","pqc_cipher_base64: " + the_pqc_cipher);

        // config internet connection
        String server_url_query =
                "?app_name=" + MainActivity.app_name
                        + "&app_version=" + MainActivity.app_version
                        + "&in_graph=" + URLEncoder.encode(MainActivity.graph_domain_in)
                        + "&wallet_address=" + MainActivity.wallet_address
                        + "&my_tally_hash=" + URLEncoder.encode(detail_of_tnx_tally_hash)
                        + "&pqc_cipher_serial=" + URLEncoder.encode(pqc_cipher_serial)
                        + "&pqc_cipher_base64=" + URLEncoder.encode(the_pqc_cipher)
                        ;

        the_log_str += "Request - Wallet: " + "\n" + MainActivity.wallet_address + "\n\n";
        the_log_str += "Request - TallyHash: " + "\n" + detail_of_tnx_tally_hash + "\n\n";
        the_log_str += "Url: " + "\n" + MainActivity.server_url + "\n\n";

        String result_history_by_tally_hash = MainActivity.browse_url(
                MainActivity.server_url +
                        "dmz.asmx/ledger_history_tally_hash" +
                        server_url_query);

        Access_log.log_it("i","shahin","result_history_by_tally_hash: " + result_history_by_tally_hash);

        if (result_history_by_tally_hash.equals("Failed"))
        {
            onBackPressed();
        }
        else
        {
            textview_network.setVisibility(View.GONE);
            textview_history_hash_tnxid.setVisibility(View.VISIBLE);
            textview_history_hash_orderid.setVisibility(View.VISIBLE);
            textview_history_hash_channel.setVisibility(View.VISIBLE);
            path_interactive_views.setVisibility(View.VISIBLE);
            textview_history_log_monitor.setVisibility(View.VISIBLE);
            textview_history_log.setVisibility(View.VISIBLE);
        }

        if (is_pqc.equals("enable") && !MainActivity.app_pqc_serial.equals("-"))
        {
            the_log_str += "Response - Encrypted: " + "\n" + result_history_by_tally_hash + "\n\n";

            // decrypt the server response..
            result_history_by_tally_hash = crypto_symm_aes.AES_Decrypt_by_secret_with_custom_padding(result_history_by_tally_hash,the_pqc_psk);

            the_log_str += "Response - Decrypted: " + "\n" + result_history_by_tally_hash + "\n";
        }
        else
        {
            the_log_str += "Response: " + "\n" + result_history_by_tally_hash + "\n";
        }

        Access_log.log_it("i","shahin","result_history_by_tally_hash: " + result_history_by_tally_hash);

        // simulate server response
        /*
        result_history_by_tally_hash = "my_graph^" +
                "my_wallet^" +
                "my_order^" +
                "tnx_id^" +
                "0^" +
                "tallybox.mixoftix.net^" +
                "boxB2e7517135fa2awY4VXxrhjVMEvyz8HZV8fTotJHf4aX5nHzpEiPJuPb^" +
                "1741769698.1000000^" +
                "2PN^" +
                "0.75000000^" +
                "99897.00000000^" +
                "8330850afce66a7859c3e9a0f19a2d2f876b1a4cf13af713168ac33931ed88ca^" +
                "1^" +
                "tallybox.mixoftix.net^" +
                "boxB2e7517135fa2awY4VXxrhjVMEvyz8HZV8fTotJHf4aX5nHzpEiPJuPb^" +
                "1741769618.1000000^" +
                "2PN^" +
                "100.00000000^" +
                "99797.00000000^" +
                "a578912e0498bc8ddbe739e62117f3c25c1c60a8ba0b80c69612d2ec509750cb^" +
                "2^" +
                "tallybox.mixoftix.net^" +
                "boxB2e7517135fa2awY4VXxrhjVMEvyz8HZV8fTotJHf4aX5nHzpEiPJuPb^" +
                "1741769744.1000000^" +
                "2PN^" +
                "100.00000000^" +
                "2400.00000000^" +
                "81b6e0167092fdb04018cd9faa61fa00ac2e20f67142a0a077ff856de9f26862"
        ;
        */

        // cache the result
        browse_by_tally_hash = result_history_by_tally_hash;

        // write the log
        textview_history_log.setText(the_log_str);

        // generate views of history
        redraw_history_views();
    }
    private void redraw_history_views()
    {
        // generate views of history
        generate_history_views(tnx_tally_hash, browse_by_tally_hash);
    }
    @SuppressLint("SetTextI18n")
    private void generate_history_views(String the_tnx_tally_hash, String result) {

        path_interactive_views.removeAllViewsInLayout();

        // interpret server's CSV data8
        String[] split_output;
        split_output = result.split("\\^");

        // index[0] : in_graph
        // index[1] : wallet_address
        // index[2] : order_id
        // index[3] : tnx_id

        String tnx_id = split_output[3];
        String order_id = split_output[2];

        if (order_id.isEmpty())
        {
            order_id = "..";
        }

        // "tnx_type^" +
        // "graph_id^" +
        // "wallet_id^" +
        // "tnx_id_dag^" +
        // "currency_id^" +
        // "currency_amount^" +
        // "left_amount^" +
        // "tally_hash" +

        String cmd_tnx_type = "";
        String cmd_graph_id = "";
        String cmd_wallet_id = "";
        String cmd_tnx_id_dag = "";
        String cmd_currency_id = "";
        String cmd_currency_amount = "";
        String cmd_left_amount = "";
        String cmd_tally_hash = "";

        textview_graph_in.setText("(in graph: " + split_output[0] + ")");
        textview_history_hash_tnxid.setText(HtmlCompat.fromHtml(
                "Transaction: " +
                        "<font color='#7851A9'>" +
                        tnx_id +
                        "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_history_hash_orderid.setText(HtmlCompat.fromHtml(
                "Order ID: " +
                        "<font color='#7851A9'>" +
                        order_id +
                        "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));


        // channel info
        String concrete_color_code = "#32CD32";
        String safe_color_code  = "#7851A9";
        String warning_color_code = "#FFD700";
        String danger_color_code = "#FF4500";

        String channel_protocol = MainActivity.setting_network_protocol;
        String channel_pqc = MainActivity.setting_safeguard_pqc;

        String channel_info = channel_protocol;
        String channel_color_code = "";

        if (channel_protocol.equals("https") && channel_pqc.equals("enable"))
        {
            channel_color_code = concrete_color_code;
            channel_info = channel_info + " + PQC" + " (Concrete)";
        }
        if (channel_protocol.equals("https") && channel_pqc.equals("disable"))
        {
            channel_color_code = safe_color_code;
            channel_info = channel_info + " (Safe)";
        }
        if (channel_protocol.equals("http") && channel_pqc.equals("enable"))
        {
            channel_color_code = warning_color_code;
            channel_info = channel_info + " + PQC" + " (Warning)";
        }
        if (channel_protocol.equals("http") && channel_pqc.equals("disable"))
        {
            channel_color_code = danger_color_code;
            channel_info = channel_info + " (Dangerous)";
        }

        textview_history_log_monitor.setText(HtmlCompat.fromHtml(
                "<font color='" + channel_color_code + "'>" +
                        "<i>Channel monitor:</i>" +
                        "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        textview_history_hash_channel.setText(HtmlCompat.fromHtml(
                "Channel: " +
                        "<font color='" + channel_color_code + "'>" +
                        channel_info +
                        "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (split_output[2].equals("no_record"))
        {
            return;
        }
        if (split_output[3].equals("no_record"))
        {
            return;
        }

        for (int kk=4; kk < split_output.length; kk=kk+8) {

            cmd_tnx_type = split_output[kk];
            cmd_graph_id = split_output[kk+1];
            cmd_wallet_id = split_output[kk+2];
            cmd_tnx_id_dag = split_output[kk+3];
            cmd_currency_id = split_output[kk+4];
            cmd_currency_amount = split_output[kk+5];
            cmd_left_amount = split_output[kk+6];
            cmd_tally_hash = split_output[kk+7];

            String log_str = kk + "-" +
                    cmd_tnx_type +
                    " [" + cmd_tnx_type + "]" +
                    " [" + cmd_currency_amount + "]" +
                    " [" + cmd_left_amount + "]" +
                    " [" + cmd_tally_hash + "]";

            Access_log.log_it("i","shahin",log_str);

            String utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());
            String cmd_tnx_id_moment = Access_time.back_from_utc(cmd_tnx_id_dag);
            cmd_tnx_id_moment = Access_time.getTimeDifference(utc_unix_now,cmd_tnx_id_dag) + ",<br>" + cmd_tnx_id_moment;
            Access_log.log_it("i","shahin",kk + "- cmd_tnx_id_moment:" + cmd_tnx_id_moment);

            // generate textviews
            // main textview

            // BGN: insert linear_block
            LinearLayout parent = new LinearLayout(MainActivity_History_Detail.this);
            parent.setId(kk);
            final int id_parent = parent.getId();
            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.bottomMargin = 15;
            //addContentView(parent,layoutParams);
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            parent.setPadding(10,10,10,10);
            path_interactive_views.addView(parent,layoutParams);
            // END: insert linear_block

            TextView msg = new TextView(MainActivity_History_Detail.this, null, android.R.attr.textViewStyle);
            msg.setId(kk+1);
            final int id_msg = msg.getId();

            /*
            String cmd_tnx_type = split_output[kk];
            String cmd_graph_id = split_output[kk+1];
            String cmd_wallet_id = split_output[kk+2];
            String cmd_tnx_id_dag = split_output[kk+3];
            String cmd_currency_id = split_output[kk+4];
            String cmd_currency_amount = split_output[kk+5];
            String cmd_left_amount = split_output[kk+6];
            String cmd_tally_hash = split_output[kk+7];
            */

            // check the dark mode
            String silver_color_code = "#696969";
            String balance_color_code = "#4169E1";
            String fee_color_code = "#FFD700";
            String sender_color_code = "#FF4500";
            String receiver_color_code = "#32CD32";

            // config color codes
            String the_color_code = "";
            if (cmd_tnx_type.equals("0")) {
                the_color_code = fee_color_code;
            }
            else if (cmd_tnx_type.equals("1")) {
                the_color_code = sender_color_code;
            }
            else {
                the_color_code = receiver_color_code;
            }

            // config labels
            String cmd_tnx_type_label = "";
            if (cmd_tnx_type.equals("0"))
            {
                cmd_tnx_type_label = "Fee";
            }
            else if (cmd_tnx_type.equals("1"))
            {
                cmd_tnx_type_label = "Sender";
            }
            else
            {
                cmd_tnx_type_label = "Receiver";
            }

            String my_wallet_address = "";
            if (cmd_wallet_id.equals(MainActivity.wallet_address))
            {
                my_wallet_address = " <i>--Me!</i>";
            }

            if (cmd_tally_hash.equals(the_tnx_tally_hash))
            {
                msg.setText(HtmlCompat.fromHtml(
                        "<font color='" + the_color_code + "'>" +
                                "<b>" +
                                cmd_tnx_type_label +
                                "</b>" +
                                "</font>" +
                                my_wallet_address +
                                "<br>" +
                                sliceString(cmd_wallet_id).replace(",","<br>")
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            else
            {
                msg.setText(HtmlCompat.fromHtml(
                        "<font color='" + silver_color_code + "'>" +
                                "<b>" +
                                cmd_tnx_type_label +
                                "</b>" +
                                my_wallet_address +
                                "<br>" +
                                sliceString(cmd_wallet_id).replace(",","<br>") +
                                "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            msg.setTextSize(17);
            msg.setEllipsize(TextUtils.TruncateAt.END);
            //msg.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            msg.setPadding(10,10,10,10);

            if (cmd_tally_hash.equals(the_tnx_tally_hash))
            {
                msg.setCompoundDrawablesWithIntrinsicBounds(null,null,ContextCompat.getDrawable(this, R.drawable.baseline_content_copy_24),null);
            }

            // Align the text inside the TextView to the right
            msg.setGravity(Gravity.START | Gravity.LEFT);
            /*
            FrameLayout.LayoutParams msg_params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            msg_params.gravity = Gravity.END | Gravity.RIGHT;
            msg.setLayoutParams(msg_params);
            */

            //path_interactive_views.addView(msg);
            parent.addView(msg);

            // sub textview
            TextView msg1 = new TextView(MainActivity_History_Detail.this, null, android.R.attr.textViewStyle);
            msg1.setId(kk+2);
            final int id_msg1 = msg1.getId();

            if (cmd_tally_hash.equals(the_tnx_tally_hash))
            {
                msg1.setText(HtmlCompat.fromHtml(
                        "<font color='" + the_color_code + "'>" +
                                cmd_currency_amount +
                                " " +
                                cmd_currency_id +
                                "</font>" +
                                "<br>" +
                                "<font color='" + balance_color_code + "'>" +
                                cmd_left_amount +
                                " " +
                                cmd_currency_id +
                                "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            else
            {
                msg1.setText(HtmlCompat.fromHtml(
                         "<font color='" + silver_color_code + "'>" +
                                cmd_currency_amount +
                                 " " +
                                 cmd_currency_id +
                                "</font>" +
                                "<br>" +
                                "<font color='" + silver_color_code + "'>" +
                                cmd_left_amount +
                                " " +
                                cmd_currency_id +
                                "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            msg1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            msg1.setTextSize(17);
            msg1.setEllipsize(TextUtils.TruncateAt.END);
            //msg1.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            msg1.setPadding(10,10,10,10);

            // Align the text inside the TextView to the right
            msg1.setGravity(Gravity.END | Gravity.RIGHT);

            //path_interactive_views.addView(msg1);
            parent.addView(msg1);

            // sub textview
            TextView msg2 = new TextView(MainActivity_History_Detail.this, null, android.R.attr.textViewStyle);
            msg2.setId(kk+3);
            final int id_msg2 = msg2.getId();

            if (cmd_tally_hash.equals(the_tnx_tally_hash))
            {
                msg2.setText(HtmlCompat.fromHtml(
                        "<i>" +
                                sliceString(cmd_tally_hash).replace(",","<br>") +
                               "</i>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            else
            {
                msg2.setText(HtmlCompat.fromHtml(
                        "<font color='" + silver_color_code + "'>" +
                               "<i>" +
                                sliceString(cmd_tally_hash).replace(",","<br>") +
                               "</i>" +
                               "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            msg2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            msg2.setTextSize(13);
            msg2.setEllipsize(TextUtils.TruncateAt.END);
            //msg2.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            msg2.setPadding(10,10,10,10);

            if (cmd_tally_hash.equals(the_tnx_tally_hash))
            {
                msg2.setCompoundDrawablesWithIntrinsicBounds(null,null,ContextCompat.getDrawable(this, R.drawable.baseline_content_copy_24),null);
            }
            else
            {
                //msg2.setCompoundDrawablesWithIntrinsicBounds(null,null,ContextCompat.getDrawable(this, R.drawable.baseline_fingerprint_24),null);
            }
            // Align the text inside the TextView to the right
            msg2.setGravity(Gravity.START | Gravity.LEFT);

            //path_interactive_views.addView(msg2);
            parent.addView(msg2);

            if (cmd_tally_hash.equals(the_tnx_tally_hash))
            {
                msg = ((TextView) findViewById(id_msg));
                String finalCmd_wallet_id = cmd_wallet_id;
                msg.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(View view) {

                        copy_to_clipboard(finalCmd_wallet_id);
                        Toast.makeText(view.getContext(),
                                        "Wallet Address Copied:\n\r" + finalCmd_wallet_id, Toast.LENGTH_SHORT)
                                .show();

                        // Run Application
                        /*
                        Intent i = new Intent(getApplicationContext(),MainActivity_History_Detail.class);
                        i.putExtra("detail_of_currency_name", detail_of_currency_name);
                        i.putExtra("tnx_tally_hash", cmd_tnx_tally_hash);
                        finishAffinity();
                        startActivity(i);
                        */

                    }
                });

                msg2 = ((TextView) findViewById(id_msg2));
                String finalCmd_tally_hash1 = cmd_tally_hash;
                msg2.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(View view) {

                        copy_to_clipboard(finalCmd_tally_hash1);
                        Toast.makeText(view.getContext(),
                                        "Tally-Hash Copied:\n\r" + finalCmd_tally_hash1, Toast.LENGTH_SHORT)
                                .show();

                        // Run Application
                        /*
                        Intent i = new Intent(getApplicationContext(),MainActivity_History_Detail.class);
                        i.putExtra("detail_of_currency_name", detail_of_currency_name);
                        i.putExtra("tnx_tally_hash", cmd_tnx_tally_hash);
                        finishAffinity();
                        startActivity(i);
                        */

                    }
                });
            }
            else
            {
                parent = ((LinearLayout) findViewById(id_parent));
                String finalCmd_tally_hash = cmd_tally_hash;
                parent.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(View view) {

                        // move on new tally hash
                        tnx_tally_hash = finalCmd_tally_hash;
                        redraw_history_views();

                        /*
                        copy_to_clipboard(cmd_tally_hash);
                        Toast.makeText(view.getContext(),
                                        "New Tally-Hash:\n\r" + cmd_tally_hash, Toast.LENGTH_SHORT)
                                .show();
                        */

                        // Run Application
                        /*
                        Intent i = new Intent(getApplicationContext(),MainActivity_History_Detail.class);
                        i.putExtra("detail_of_currency_name", detail_of_currency_name);
                        i.putExtra("tnx_tally_hash", cmd_tnx_tally_hash);
                        finishAffinity();
                        startActivity(i);
                        */


                    }
                });
            }

        }

    }


    private void copy_to_clipboard(String text)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
    public String sliceString(String input) {
        int length = input.length();
        int mid = (length + 1) / 2; // Adding 1 ensures the first half gets the extra character if the length is odd

        String firstHalf = input.substring(0, mid);
        String secondHalf = input.substring(mid);

        return firstHalf + "," + secondHalf;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity_History.class);
        intent.putExtra("detail_of_currency_name", detail_of_currency_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
    }

}