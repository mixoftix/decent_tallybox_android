package net.mixoftix.tallybox;

import static net.mixoftix.tallybox.MainActivity.wallet_address;

import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.net.URLEncoder;

import net.mixoftix.tallybox.databinding.ActivityMainKycBinding;

public class MainActivity_KYC extends BaseActivity {

    private TextView textview_graph_in, textview_broadcast_report;
    private EditText editText_KYC_National_ID, editText_KYC_Mobile_Number, EditText_KYC_MSG, editText_KYC_PIN;
    private Button buttonSign, buttonBroadcast;
    private LinearLayout layout_of_kyc_sign, layout_of_kyc_request;
    private static ActivityMainKycBinding binding;

    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_kyc);
        setContentView(R.layout.activity_main_kyc);

        binding = ActivityMainKycBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        layout_of_kyc_request = findViewById(R.id.layout_of_kyc_request);
        editText_KYC_National_ID = findViewById(R.id.editText_KYC_National_ID);
        editText_KYC_Mobile_Number = findViewById(R.id.editText_KYC_Mobile_Number);
        buttonBroadcast = findViewById(R.id.buttonBroadcast);
        textview_broadcast_report = findViewById(R.id.textview_broadcast_report);

        layout_of_kyc_sign = findViewById(R.id.layout_of_kyc_sign);
        EditText_KYC_MSG = findViewById(R.id.EditText_KYC_MSG);
        editText_KYC_PIN = findViewById(R.id.editText_KYC_PIN);
        buttonSign = findViewById(R.id.buttonSign);

        textview_graph_in = findViewById(R.id.textview_graph_in);
        //textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + ")");
        updateGraphFromDisplay();

        // check previous kyc attempts
        String previous_kyc_result = Access_file.access_file_func_read(getApplicationContext(), "kyc_result");
        if (!previous_kyc_result.equals("-"))
        {
            textview_broadcast_report.setVisibility(View.VISIBLE);
            textview_broadcast_report.setText(HtmlCompat.fromHtml(
                    "<font color='#32CD32'>" + previous_kyc_result + "</font>"
                    ,HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

// ==================== buttonBroadcast (KYC Generate) ====================
        buttonBroadcast.setOnClickListener(view -> {

            if (!buttonBroadcast.isEnabled()) return;

            buttonBroadcast.setEnabled(false);

            progressbar_stat = true;
            doStartProgressBar2();

            String kyc_request_msg = getString(R.string.kyc_request_msg);
            textview_broadcast_report.setVisibility(View.VISIBLE);
            textview_broadcast_report.setText(HtmlCompat.fromHtml(
                    "<font color='cyan'>" + kyc_request_msg + "</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            new Thread(() -> {
                String result = "error~0~Unknown error";

                try {
                    String server_url_query =
                            "app_name=" + URLEncoder.encode(MainActivity.app_name) +
                                    "&app_version=" + URLEncoder.encode(MainActivity.app_version) +
                                    "&national_id=" + URLEncoder.encode(
                                    editText_KYC_National_ID.getText().toString().trim()) +
                                    "&mobile_number=" + URLEncoder.encode(
                                    editText_KYC_Mobile_Number.getText().toString().trim());

                    result = MainActivity.browse_url_POST(
                            MainActivity.server_url_ods + "kyc_generate",
                            server_url_query);

                    Access_log.log_it("i", "shahin", "kyc_generate - result: " + result);

                } catch (Exception e) {
                    result = "error~0~" + e.getMessage();
                    e.printStackTrace();
                }

                final String finalResult = result;

                runOnUiThread(() -> {
                    progressbar_stat = false;
                    buttonBroadcast.setEnabled(true);

                    if (finalResult.startsWith("error~") || finalResult.startsWith("info~") || finalResult.startsWith("Failed")) {
                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#FF4500'>" + finalResult + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else {
                        // Success - show signing section
                        layout_of_kyc_request.setEnabled(false);
                        editText_KYC_National_ID.setEnabled(false);
                        editText_KYC_Mobile_Number.setEnabled(false);
                        buttonBroadcast.setEnabled(false);

                        textview_broadcast_report.setVisibility(View.GONE);
                        layout_of_kyc_sign.setVisibility(View.VISIBLE);
                        EditText_KYC_MSG.setText(finalResult);
                    }
                });
            }).start();
        });


// ==================== buttonSign (KYC Accept) ====================
        buttonSign.setOnClickListener(view -> {

            if (!buttonSign.isEnabled()) return;

            buttonSign.setEnabled(false);

            progressbar_stat = true;
            doStartProgressBar2();

            String kyc_submit_msg = getString(R.string.kyc_submit_msg);
            textview_broadcast_report.setVisibility(View.VISIBLE);
            textview_broadcast_report.setText(HtmlCompat.fromHtml(
                    "<font color='cyan'>" + kyc_submit_msg + "</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            new Thread(() -> {
                String result = "error~0~Unknown error";

                try {
                    String national_id = editText_KYC_National_ID.getText().toString().trim();
                    String wallet_2_kyc = wallet_address;
                    String kyc_pin = editText_KYC_PIN.getText().toString().trim();
                    String the_kyc_order = national_id + "~" + wallet_2_kyc + "~" + kyc_pin;

                    String my_sign = MainActivity.sign_order(
                            the_kyc_order,
                            MainActivity.retrieve_private_key());

                    String publicKey_xy_compressed = crypto_asym_keys_compress.PublicKeyCompression(
                            MainActivity.publicKey_x_HEX,
                            MainActivity.publicKey_y_HEX,
                            "B58");

                    String server_url_query =
                            "app_name=" + URLEncoder.encode(MainActivity.app_name) +
                                    "&app_version=" + URLEncoder.encode(MainActivity.app_version) +
                                    "&national_id=" + URLEncoder.encode(national_id) +
                                    "&wallet_2_kyc=" + URLEncoder.encode(wallet_2_kyc) +
                                    "&wallet_pubkey=" + URLEncoder.encode(publicKey_xy_compressed) +
                                    "&sign_4_kyc=" + URLEncoder.encode(my_sign);

                    result = MainActivity.browse_url_POST(
                            MainActivity.server_url_ods + "kyc_accept",
                            server_url_query);

                    Access_log.log_it("i", "shahin", "kyc_accept - result: " + result);

                } catch (Exception e) {
                    result = "error~0~" + e.getMessage();
                    e.printStackTrace();
                }

                final String finalResult = result;

                runOnUiThread(() -> {
                    progressbar_stat = false;
                    buttonSign.setEnabled(true);

                    if (finalResult.startsWith("error~") || finalResult.startsWith("Failed")) {
                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#FF4500'>" + finalResult + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else {
                        // Success
                        layout_of_kyc_sign.setEnabled(false);
                        EditText_KYC_MSG.setEnabled(false);
                        editText_KYC_PIN.setEnabled(false);
                        buttonSign.setEnabled(false);

                        Access_file.access_file_func_write(getApplicationContext(), "kyc_result", finalResult, "write");

                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#32CD32'>" + finalResult + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                });
            }).start();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kyc, menu);
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
            onBackPressed();

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

    //region function_of_graph_in

    // Show Graph From + Zones
    private void updateGraphFromDisplay() {

        String history_in_graph = getString(R.string.history_in_graph);
        int index = getGraphIndex(MainActivity.graph_domain_in);

        String zones = (index != -1) ? getZoneForGraph(index) : " [no zone]";

        String zonesText = (zones.length() > 0)
                ? String.join(", ", zones)
                : "no zone";

        //textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + zonesText + ")");
        textview_graph_in.setText(HtmlCompat.fromHtml(
                "(" +
                        "<font color='cyan'>" + zonesText + "</font> / " +
                        "<b>" + MainActivity.graph_domain_in + "</b>" +
                        ")",
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

}