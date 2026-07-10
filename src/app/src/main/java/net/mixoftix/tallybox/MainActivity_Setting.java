package net.mixoftix.tallybox;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import net.mixoftix.tallybox.databinding.ActivityMainSettingBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MainActivity_Setting extends BaseActivity {

    private static ActivityMainSettingBinding binding;
    private RadioButton radioConnection, radioPQC;
    private RadioGroup RadioGroupConnection, RadioGroupPQC;
    private Spinner dropdownSpinner_Zone;
    private LinearLayout layoutDynamicPQC;

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

        // Initialize views
        RadioGroupConnection = findViewById(R.id.RadioGroupConnection);
        RadioGroupPQC = findViewById(R.id.RadioGroupPQC);
        dropdownSpinner_Zone = findViewById(R.id.dropdownSpinner_Zone);

        // IMPORTANT: Use the dedicated container
        layoutDynamicPQC = findViewById(R.id.layout_dynamic_pqc);

        if (layoutDynamicPQC == null) {
            Access_log.log_it("e", "shahin", "layout_dynamic_pqc not found in XML!");
        }

        // Spinner setup
        List<String> validZones = MainActivity.getValidUniqueZones();
        GraphSpinnerAdapter adapter = new GraphSpinnerAdapter(this, validZones);
        dropdownSpinner_Zone.setAdapter(adapter);

        loadLastSelectedZone();
        loadLastNetworkAndPQCSettings();

        // Zone change listener
        dropdownSpinner_Zone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedZone = (String) parent.getItemAtPosition(position);
                Access_log.log_it("i", "shahin", "Selected Zone: " + selectedZone);

                Access_file.access_file_func_write(getApplicationContext(),
                        "setting_zone_filter", selectedZone.toLowerCase(), "write");

                redrawDynamicPQCList(selectedZone);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Initial draw
        String initialZone = Access_file.access_file_func_read(getApplicationContext(), "setting_zone_filter");
        redrawDynamicPQCList(initialZone);
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

    //region dynamic_pqc_settings
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

    //endregion

    //region Graph Zone Spinner Adapter
    private class GraphSpinnerAdapter extends ArrayAdapter<String> {

        public GraphSpinnerAdapter(Context context, List<String> zones) {
            super(context, 0, zones);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        private View createCustomView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.items_activity_spinner, parent, false);
            }

            TextView textView = view.findViewById(R.id.spinnerText);
            String zone = getItem(position);

            textView.setText(zone);
            return view;
        }
    }
    //endregion

    private void loadLastSelectedZone() {
        // Read saved setting
        String savedZone = Access_file.access_file_func_read(getApplicationContext(), "setting_zone_filter");

        Access_log.log_it("i", "shahin", "Loading saved zone filter: " + savedZone);

        if (savedZone == null || savedZone.equals("-") || savedZone.isEmpty()) {
            // Select first item as default if nothing is saved
            if (dropdownSpinner_Zone.getCount() > 0) {
                dropdownSpinner_Zone.setSelection(0);
            }
            return;
        }

        // Find matching zone (case insensitive)
        for (int i = 0; i < dropdownSpinner_Zone.getCount(); i++) {
            String displayedZone = (String) dropdownSpinner_Zone.getItemAtPosition(i);

            if (displayedZone.equalsIgnoreCase(savedZone)) {
                dropdownSpinner_Zone.setSelection(i);
                Access_log.log_it("i", "shahin", "Zone spinner set to position " + i + " (" + displayedZone + ")");
                return;
            }
        }

        // If no exact match found, try partial match (e.g. "mars" should match "Mars")
        for (int i = 0; i < dropdownSpinner_Zone.getCount(); i++) {
            String displayedZone = (String) dropdownSpinner_Zone.getItemAtPosition(i);
            if (displayedZone.toLowerCase().contains(savedZone.toLowerCase()) ||
                    savedZone.toLowerCase().contains(displayedZone.toLowerCase())) {
                dropdownSpinner_Zone.setSelection(i);
                Access_log.log_it("i", "shahin", "Zone spinner set using partial match: " + displayedZone);
                return;
            }
        }

        // Fallback: select first item
        if (dropdownSpinner_Zone.getCount() > 0) {
            dropdownSpinner_Zone.setSelection(0);
        }
    }

    // Redraw PQC TextViews based on selected zone
    private void redrawDynamicPQCList(String selectedZone) {
        if (layoutDynamicPQC == null) return;

        layoutDynamicPQC.removeAllViews();

        List<String> filteredDomains = MainActivity.getFilteredDomainsForZone(selectedZone);

        for (String domain : filteredDomains) {
            String serial = getPqcSerialForDomain(domain);
            addDynamicTextView(layoutDynamicPQC, domain, serial,
                    (textView, graph, pqcSerial) -> refreshPQCForGraph(textView, graph, pqcSerial));
        }
    }

    private void refreshPQCForGraph(TextView textView, String graph, String oldSerial) {
        // Your existing full logic for fetching PQC can go here
        // (I kept it similar to what you had)

        String server_url_query = "app_name=" + URLEncoder.encode(MainActivity.app_name) +
                "&app_version=" + URLEncoder.encode(MainActivity.app_version);

        String my_server_url = getServerUrlForGraph(graph);

        String result = MainActivity.browse_url(my_server_url + "app_pqc_pk?" + server_url_query);

        // ... rest of your result processing (you can call processPQCResponse if you want)
        // For now, just call the existing one you already have:
        processPQCResponse(textView, graph, oldSerial, result);
    }

    private String getServerUrlForGraph(String graph) {
        for (int j = 0; j < MainActivity.spinner_options.length; j++) {
            if (MainActivity.spinner_options[j].equals(graph)) {
                if (MainActivity.spinner_options_address_dw[j].startsWith("http")) {
                    return MainActivity.spinner_options_address_dw[j] + "/dmz_dw.asmx/";
                } else {
                    return MainActivity.setting_network_protocol + "://" +
                            MainActivity.spinner_options_address_dw[j] + "/dmz_dw.asmx/";
                }
            }
        }
        return "";
    }

    // Helper to get current PQC serial for a domain
    private String getPqcSerialForDomain(String domain) {
        for (int i = 0; i < MainActivity.spinner_options.length; i++) {
            if (MainActivity.spinner_options[i].equals(domain)) {
                return MainActivity.spinner_options_pqc_serial[i] != null ?
                        MainActivity.spinner_options_pqc_serial[i] : "";
            }
        }
        return "";
    }

    // Load network & PQC radio buttons
    private void loadLastNetworkAndPQCSettings() {
        // load last network protocol
        Access_log.log_it("i","shahin","connection_" + MainActivity.setting_network_protocol.toLowerCase());
        int selectedId = getResources().getIdentifier("connection_" + MainActivity.setting_network_protocol.toLowerCase(), "id", this.getPackageName());
        radioConnection = (RadioButton) findViewById(selectedId);
        if (radioConnection != null) radioConnection.setChecked(true);

        // load last pqc
        Access_log.log_it("i","shahin","pqc_" + MainActivity.setting_safeguard_pqc.toLowerCase());
        int selectedId2 = getResources().getIdentifier("pqc_" + MainActivity.setting_safeguard_pqc.toLowerCase(), "id", this.getPackageName());
        radioPQC = (RadioButton) findViewById(selectedId2);
        if (radioPQC != null) radioPQC.setChecked(true);
    }

}