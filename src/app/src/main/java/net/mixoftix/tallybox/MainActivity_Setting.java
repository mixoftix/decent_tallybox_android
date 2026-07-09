package net.mixoftix.tallybox;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import net.mixoftix.tallybox.databinding.ActivityMainSettingBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

public class MainActivity_Setting extends BaseActivity {

    private static ActivityMainSettingBinding binding;
    private RadioButton radioConnection, radioPQC;
    private RadioGroup RadioGroupConnection, RadioGroupPQC;

    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;
    // END: browse http


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_settings);
        setContentView(R.layout.activity_main_setting);

        binding = ActivityMainSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        // In your Activity
        LinearLayout layoutSettings = findViewById(R.id.layout_settings);
        RadioGroupConnection = findViewById(R.id.RadioGroupConnection);
        RadioGroupPQC = findViewById(R.id.RadioGroupPQC);

        // load last settings
        Access_log.log_it("i","shahin","connection_" + MainActivity.setting_network_protocol.toLowerCase());
        int selectedId = getResources().getIdentifier("connection_" + MainActivity.setting_network_protocol.toLowerCase(), "id", this.getPackageName());
        Access_log.log_it("i","shahin","selectedId: " + selectedId);
        radioConnection = (RadioButton) findViewById(selectedId);
        radioConnection.setChecked(true);

        Access_log.log_it("i","shahin","pqc_" + MainActivity.setting_safeguard_pqc.toLowerCase());
        int selectedId2 = getResources().getIdentifier("pqc_" + MainActivity.setting_safeguard_pqc.toLowerCase(), "id", this.getPackageName());
        Access_log.log_it("i","shahin","selectedId2: " + selectedId2);
        radioPQC = (RadioButton) findViewById(selectedId2);
        radioPQC.setChecked(true);

        // Set a click listener for the RadioGroupConnection button
        RadioGroupConnection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                radioConnection = (RadioButton) findViewById(checkedId);
                String str_connection_protocol = (String) radioConnection.getText();

                Access_file.access_file_func_write(getApplicationContext(), "setting_network_protocol", str_connection_protocol, "write");
                MainActivity.setting_connection(str_connection_protocol);

                Toast.makeText(MainActivity_Setting.this, "Network Protocol: " + str_connection_protocol, Toast.LENGTH_SHORT).show();
                Access_log.log_it("i","shahin","Network Protocol: " + str_connection_protocol);
            }
        });

        // Set a click listener for the RadioGroupPQC button
        RadioGroupPQC.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                radioPQC = (RadioButton) findViewById(checkedId);
                String str_radioPQC = (String) radioPQC.getText();

                Access_file.access_file_func_write(getApplicationContext(), "setting_safeguard_pqc", str_radioPQC, "write");

                Toast.makeText(MainActivity_Setting.this, "Safeguard by PQC: " + str_radioPQC + "d", Toast.LENGTH_SHORT).show();
                Access_log.log_it("i","shahin","Safeguard by PQC: " + str_radioPQC);
            }
        });

        String settings_serial = getString(R.string.settings_serial);
        String settings_network = getString(R.string.settings_network);
        String settings_uptodate = getString(R.string.settings_uptodate);
        String settings_updated = getString(R.string.settings_updated);
        String settings_checksum = getString(R.string.settings_checksum);

        // Dynamic TextViews:
        for (int i = 0; i < MainActivity.spinner_options.length; i++) {

            String graphName = MainActivity.spinner_options[i];
            String serial = MainActivity.spinner_options_pqc_serial[i];

            addDynamicTextView(layoutSettings,
                                graphName,
                                serial,
                                (textView, graph, pqcSerial) -> // Correct lambda
                                {
                                    // config internet connection
                                    String server_url_query =
                                            "app_name=" + URLEncoder.encode(MainActivity.app_name)
                                                    + "&app_version=" + URLEncoder.encode(MainActivity.app_version);

                                    String my_server_url = "";

                                    for (int j = 0; j < MainActivity.spinner_options.length; j++)
                                    {
                                        if (MainActivity.spinner_options[j].equals(graph))
                                        {
                                            if (MainActivity.spinner_options_address_dw[j].startsWith("http"))
                                            {
                                                my_server_url = MainActivity.spinner_options_address_dw[j] +
                                                                "/dmz_dw.asmx/"; // "://192.168.88.111:701/";
                                            }
                                            else
                                            {
                                                my_server_url = MainActivity.setting_network_protocol + "://" +
                                                                MainActivity.spinner_options_address_dw[j] +
                                                                "/dmz_dw.asmx/"; // "://192.168.88.111:701/";
                                            }
                                        }
                                    }

                                    String result = net.mixoftix.tallybox.MainActivity.browse_url(my_server_url + "app_pqc_pk?" + server_url_query);
                                    Access_log.log_it("i","shahin",MainActivity.server_url_dw + " - result: " + result);

                                    String network_msg = " / " + settings_network + ": <font color=red>Er</font>";

                                    if (result.equals("Failed"))
                                    {
                                        // set the text
                                        textView.setText(HtmlCompat.fromHtml(graph + "<br>" + settings_serial + ": " + pqcSerial + network_msg, HtmlCompat.FROM_HTML_MODE_LEGACY));
                                    }
                                    else if (result.equals("no_record"))
                                    {
                                        for (int j = 0; j < MainActivity.spinner_options.length; j++)
                                        {
                                            if (MainActivity.spinner_options[j].equals(graph))
                                            {
                                                Access_file.access_file_func_write(getApplicationContext(), "app_pqc_serial_" + j, "", "write");
                                                Access_file.access_file_func_write(getApplicationContext(), "app_pqc_pk_" + j, "", "write");

                                                MainActivity.spinner_options_pqc_serial[j] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_serial_" + j);
                                                MainActivity.spinner_options_pqc_pk[j] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_pk_" + j);

                                                Access_log.log_it("i","shahin","333 - spinner_options_pqc_serial[" + j + "]: " + MainActivity.spinner_options_pqc_serial[j]);
                                                Access_log.log_it("i","shahin","333 - spinner_options_pqc_pk["+ j + "]: " + MainActivity.spinner_options_pqc_pk[j]);
                                            }
                                        }

                                        // set the text
                                        network_msg = " / " + settings_network + ": <font color=cyan>OK</font>";
                                        textView.setText(HtmlCompat.fromHtml(graph + "<br>" + settings_serial + ": " + result + network_msg, HtmlCompat.FROM_HTML_MODE_LEGACY));

                                    }
                                    else
                                    {
                                        // interpret server's CSV data
                                        String[] split_output;
                                        split_output = result.split("\\^");

                                        String pqc_serial = split_output[0];
                                        String pqc_sha256 = split_output[1];
                                        String pqc_pk = split_output[2];

                                        // make the local privacy
                                        String local_pqc_sha256 = null;
                                        try {
                                            local_pqc_sha256 = hash_functions.Hash_SHA_256(pqc_pk);
                                        } catch (NoSuchAlgorithmException e) {
                                            throw new RuntimeException(e);
                                        } catch (UnsupportedEncodingException e) {
                                            throw new RuntimeException(e);
                                        }

                                        Access_log.log_it("i","shahin","333 - pqc_sha256: " + pqc_sha256);
                                        Access_log.log_it("i","shahin","333 - local_pqc_sha256: " + local_pqc_sha256);

                                        if (local_pqc_sha256.equals(pqc_sha256))
                                        {
                                            for (int j = 0; j < MainActivity.spinner_options.length; j++)
                                            {
                                                if (MainActivity.spinner_options[j].equals(graph))
                                                {
                                                    Access_file.access_file_func_write(getApplicationContext(), "app_pqc_serial_" + j, pqc_serial, "write");
                                                    Access_file.access_file_func_write(getApplicationContext(), "app_pqc_pk_" + j, pqc_pk, "write");

                                                    MainActivity.spinner_options_pqc_serial[j] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_serial_" + j);
                                                    MainActivity.spinner_options_pqc_pk[j] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_pk_" + j);

                                                    Access_log.log_it("i","shahin","333 - spinner_options_pqc_serial[" + j + "]: " + MainActivity.spinner_options_pqc_serial[j]);
                                                    Access_log.log_it("i","shahin","333 - spinner_options_pqc_pk["+ j + "]: " + MainActivity.spinner_options_pqc_pk[j]);
                                                }
                                            }

                                            if (pqc_serial.equals(pqcSerial))
                                            {
                                                // set the text
                                                network_msg = " / <font color=green>" + settings_uptodate + "</font> / " + settings_network + ": <font color=cyan>OK</font>";
                                                textView.setText(HtmlCompat.fromHtml(graph + "<br>" + settings_serial + ": " + pqc_serial + network_msg, HtmlCompat.FROM_HTML_MODE_LEGACY));
                                            }
                                            else
                                            {
                                                // set the text
                                                network_msg = " / <font color=cyan>" + settings_updated + "</font> / " + settings_network + ": <font color=cyan>OK</font>";
                                                textView.setText(HtmlCompat.fromHtml(graph + "<br>" + settings_serial + ": " + pqc_serial + network_msg, HtmlCompat.FROM_HTML_MODE_LEGACY));
                                            }
                                        }
                                        else
                                        {
                                            for (int j = 0; j < MainActivity.spinner_options.length; j++)
                                            {
                                                if (MainActivity.spinner_options[j].equals(graph))
                                                {
                                                    Access_file.access_file_func_write(getApplicationContext(), "app_pqc_serial_" + j, "", "write");
                                                    Access_file.access_file_func_write(getApplicationContext(), "app_pqc_pk_" + j, "", "write");

                                                    MainActivity.spinner_options_pqc_serial[j] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_serial_" + j);
                                                    MainActivity.spinner_options_pqc_pk[j] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_pk_" + j);

                                                    Access_log.log_it("i","shahin","333 - spinner_options_pqc_serial[" + j + "]: " + MainActivity.spinner_options_pqc_serial[j]);
                                                    Access_log.log_it("i","shahin","333 - spinner_options_pqc_pk["+ j + "]: " + MainActivity.spinner_options_pqc_pk[j]);
                                                }
                                            }

                                            // set the text
                                            network_msg = " / <font color=magenta>" + settings_checksum + "</font> / " + settings_network + ": <font color=cyan>OK</font>";
                                            textView.setText(HtmlCompat.fromHtml(graph + "<br>" + settings_serial + ": -" +  network_msg, HtmlCompat.FROM_HTML_MODE_LEGACY));
                                        }
                                    }
                                }
                                );
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
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
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            finishAffinity();
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void addDynamicTextView(LinearLayout parent,
                                    String graph_name,
                                    String pqc_serial,
                                    OnItemClickListener listener) {

        TextView textView = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(10);
        textView.setLayoutParams(params);

        String settings_serial = getString(R.string.settings_serial);
        textView.setText(graph_name + "\n" + settings_serial + ": " + pqc_serial);
        textView.setTextSize(17);
        textView.setGravity(Gravity.CENTER_VERTICAL);

        textView.setBackgroundResource(R.drawable.frame_white);

        int padding = dpToPx(10);
        textView.setPadding(padding, padding, padding, padding);

        Drawable refreshIcon = ContextCompat.getDrawable(this, R.drawable.baseline_refresh_24);
        if (refreshIcon != null) {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, refreshIcon, null);
        }
        textView.setCompoundDrawablePadding(dpToPx(4));

        // Click Listener with Progress Bar
        textView.setOnClickListener(v -> {
            if (listener != null) {
                // Show progress
                progressbar_stat = true;
                doStartProgressBar2();

                // Disable the TextView while loading
                textView.setEnabled(false);

                new Thread(() -> {
                    // This is the original logic you had, now running in background
                    String server_url_query =
                            "app_name=" + URLEncoder.encode(MainActivity.app_name) +
                                    "&app_version=" + URLEncoder.encode(MainActivity.app_version);

                    String my_server_url = "";

                    for (int j = 0; j < MainActivity.spinner_options.length; j++) {
                        if (MainActivity.spinner_options[j].equals(graph_name)) {
                            if (MainActivity.spinner_options_address_dw[j].startsWith("http")) {
                                my_server_url = MainActivity.spinner_options_address_dw[j] + "/dmz_dw.asmx/";
                            } else {
                                my_server_url = MainActivity.setting_network_protocol + "://" +
                                        MainActivity.spinner_options_address_dw[j] + "/dmz_dw.asmx/";
                            }
                        }
                    }

                    String result = MainActivity.browse_url(my_server_url + "app_pqc_pk?" + server_url_query);
                    Access_log.log_it("i", "shahin", "app_pqc_pk - result: " + result);

                    final String finalResult = result;

                    runOnUiThread(() -> {
                        progressbar_stat = false;

                        String settings_network = getString(R.string.settings_network);
                        String settings_uptodate = getString(R.string.settings_uptodate);
                        String settings_updated = getString(R.string.settings_updated);
                        String settings_checksum = getString(R.string.settings_checksum);

                        String network_msg = " / " + settings_network + ": <font color=red>Er</font>";

                        if (finalResult.equals("Failed")) {
                            textView.setText(HtmlCompat.fromHtml(
                                    graph_name + "<br>" + settings_serial + ": " + pqc_serial + network_msg,
                                    HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }
                        else if (finalResult.equals("no_record")) {
                            updatePQCInStorage(graph_name, "", "");
                            network_msg = " / " + settings_network + ": <font color=cyan>OK</font>";
                            textView.setText(HtmlCompat.fromHtml(
                                    graph_name + "<br>" + settings_serial + ": " + finalResult + network_msg,
                                    HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }
                        else {
                            processPQCResponse(textView, graph_name, pqc_serial, finalResult);
                        }

                        textView.setEnabled(true);
                    });
                }).start();
            }
        });

        parent.addView(textView);
    }

    private void updatePQCInStorage(String graph, String serial, String pk) {
        for (int j = 0; j < MainActivity.spinner_options.length; j++) {
            if (MainActivity.spinner_options[j].equals(graph)) {
                Access_file.access_file_func_write(getApplicationContext(), "app_pqc_serial_" + j, serial, "write");
                Access_file.access_file_func_write(getApplicationContext(), "app_pqc_pk_" + j, pk, "write");

                MainActivity.spinner_options_pqc_serial[j] = serial;
                MainActivity.spinner_options_pqc_pk[j] = pk;
            }
        }
    }

    private void processPQCResponse(TextView textView, String graph, String oldSerial, String result) {
        String[] split_output = result.split("\\^");
        String pqc_serial = split_output[0];
        String pqc_sha256 = split_output[1];
        String pqc_pk = split_output[2];

        String local_pqc_sha256 = "";
        try {
            local_pqc_sha256 = hash_functions.Hash_SHA_256(pqc_pk);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String settings_serial = getString(R.string.settings_serial);
        String settings_network = getString(R.string.settings_network);
        String settings_uptodate = getString(R.string.settings_uptodate);
        String settings_updated = getString(R.string.settings_updated);
        String settings_checksum = getString(R.string.settings_checksum);

        String network_msg = settings_network + ": <font color=cyan>OK</font>";

        if (local_pqc_sha256.equals(pqc_sha256)) {
            updatePQCInStorage(graph, pqc_serial, pqc_pk);

            if (pqc_serial.equals(oldSerial)) {
                network_msg = " / <font color=green>" + settings_uptodate + "</font> / " + network_msg;
            } else {
                network_msg = " / <font color=cyan>" + settings_updated + "</font> / " + network_msg;
            }

            textView.setText(HtmlCompat.fromHtml(
                    graph + "<br>" + settings_serial + ": " + pqc_serial + network_msg,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            updatePQCInStorage(graph, "", "");
            network_msg = " / <font color=magenta>" + settings_checksum + "</font> / " + network_msg;
            textView.setText(HtmlCompat.fromHtml(
                    graph + "<br>" + settings_serial + ": - " + network_msg,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    private interface OnItemClickListener {
        void onClick(TextView textView, String graphName, String pqcSerial);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

}