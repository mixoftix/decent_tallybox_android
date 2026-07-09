package net.mixoftix.tallybox;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import net.mixoftix.tallybox.databinding.ActivityMainFollowupBinding;

import java.net.URLEncoder;

public class MainActivity_Followup extends BaseActivity {

    private static ActivityMainFollowupBinding binding;
    private TextView textview_followup,textview_broadcast_report;
    private TextView textview_offline_Send, textview_offline_url;
    private Button button_offline_Sign,button_broadcast_Sign;
    private static ImageView ImageView_offline_qr;
    private LinearLayout layout_send_offline;


    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;
    private String submit_txt = "";
    // END: browse http

    private boolean is_sign_broadcatable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_home_back);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_followup);
        setContentView(R.layout.activity_main_followup);

        binding = ActivityMainFollowupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHomeBack);

        layout_send_offline = findViewById(R.id.layout_send_offline);
        textview_followup = findViewById(R.id.textview_followup);
        button_offline_Sign = findViewById(R.id.button_offline_Sign);
        button_broadcast_Sign = findViewById(R.id.button_broadcast_Sign);

        textview_broadcast_report = findViewById(R.id.textview_broadcast_report);
        ImageView_offline_qr = findViewById(R.id.ImageView_offline_SendQR);
        textview_offline_Send = findViewById(R.id.textview_offline_send);
        textview_offline_url = findViewById(R.id.textview_offline_url);

        String str_graph_domain_from = MainActivity.graph_domain_in;
        String followup_key = getIntent().getStringExtra("followup_key");

        String currentLang = LocaleHelper.getCurrentLanguage(this);
        String refresh_utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());

        String followup_raw_tx = Access_file.followup_keys_read(getApplicationContext(), followup_key);
        Access_log.log_it("i","shahin","followup_raw_tx: " + followup_raw_tx);

        /*
        "tallybox~parcel_of_transaction~" +
        "graph_from~gpp_mars.mixoftix.net~" +
        "graph_to~gpp_mars.mixoftix.net~" +
        "wallet_from~boxB3d60b7f32966MuJqnEpqFwKD5gk7CGbfuKG9q3t7GFxiBvXnmg3B9LH~" +
        "wallet_to~boxB3d60b7f32966MuJqnEpqFwKD5gk7CGbfuKG9q3t7GFxiBvXnmg3B9LH~" +
        "order_currency~2ZR~" +
        "order_amount~450000.00000000~" +
        "order_id~~" +
        "order_utc_unix~1783410580~" +
        "the_sign~MEUCIQCKaLnYLne423FkhiQs+iNEh/nZm8w4Fq96+03/auvEnAIgKgyD8PzbXi7zRzjrNp1acsdYthz4wlyLoFviRiSKHaM=~" +
        "publicKey_xy_compressed~FGnWVEefuuB3iN4MBGgSWiAzCuCJMdAttp2qZcSqhaJQ*1"
        */

        String[] split_followup_tx;
        split_followup_tx = followup_raw_tx.split("~");

        String payment_graph_from = split_followup_tx[3];
        String payment_graph_to = split_followup_tx[5];
        String payment_wallet_from = split_followup_tx[7];
        String payment_wallet_to = split_followup_tx[9];
        String payment_currency = split_followup_tx[11];
        String payment_amount = split_followup_tx[13];
        String payment_order_id = split_followup_tx[15];
        String payment_order_utx = split_followup_tx[17];
        String payment_sign = split_followup_tx[19];
        String payment_pubkey = split_followup_tx[21];

        String payment_moment = Access_time.getTimeDifference(currentLang,refresh_utc_unix_now,followup_key.replace("followup_",""));
        textview_followup.setText(getString(R.string.follow_graph) + ": " + payment_graph_from + "\n" +
                                  getString(R.string.follow_moment) + ": " + payment_moment + "\n" +
                                  getString(R.string.follow_amount) + ": " + payment_amount + " (" + payment_currency + ")\n\n" +
                                  getString(R.string.follow_note)
                                 );

        // Set a click listener for the broadcast button
        button_offline_Sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.progressBar2.setVisibility(View.GONE);
                textview_broadcast_report.setVisibility(View.GONE);

                Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(followup_raw_tx,512,512);
                if (qrCodeBitmap != null) {
                    is_sign_broadcatable = true;

                    layout_send_offline.setVisibility(View.VISIBLE);
                    ImageView_offline_qr.setVisibility(View.VISIBLE);
                    textview_offline_Send.setVisibility(View.VISIBLE);
                    textview_offline_url.setVisibility(View.VISIBLE);

                    ImageView_offline_qr.setImageBitmap(qrCodeBitmap);
                    textview_offline_Send.setText(getString(R.string.parcel_graph_from) + ": " + payment_graph_from + "\n" +
                                                  getString(R.string.parcel_graph_to) + ": " + payment_graph_to + "\n" +
                                                  getString(R.string.parcel_wallet_from) + ": " + payment_wallet_from + "\n" +
                                                  getString(R.string.parcel_wallet_to) + ": " + payment_wallet_to + "\n" +
                                                  getString(R.string.parcel_currency) + ": " + payment_currency + "\n" +
                                                  getString(R.string.parcel_amount) + ": " + payment_amount + "\n" +
                                                  getString(R.string.parcel_order_id) + ": " + payment_order_id + "\n" +
                                                  getString(R.string.parcel_order_utc) + ": " + payment_order_utx + "\n" +
                                                  getString(R.string.parcel_order_sign) + ": " + payment_sign + "\n" +
                                                  getString(R.string.parcel_order_pubkey) + ": " + payment_pubkey
                    );

                    //String the_offline_url = MainActivity.spinner_options_address_ods[getGraphIndex(str_graph_domain_from)];
                    String the_offline_url = "";

                    if (MainActivity.graph_address_in_ods.startsWith("http"))
                    {
                        the_offline_url = MainActivity.graph_address_in_ods;
                    }
                    else
                    {
                        the_offline_url = MainActivity.setting_network_protocol +
                                          "://" +
                                          MainActivity.graph_address_in_ods
                                          ;
                    }

                    String offline_url_guide = getString(R.string.offline_url_guide);
                    textview_offline_url.setText(HtmlCompat.fromHtml(
                            offline_url_guide + ": <br>" +
                                    "<a href='" +
                                    the_offline_url + "' target=_blank>" +
                                    the_offline_url +
                                    "</a>",
                            HtmlCompat.FROM_HTML_MODE_LEGACY));
                    textview_offline_url.setMovementMethod(LinkMovementMethod.getInstance());

                }

            }
        });
        // Set a click listener for the raw-transaction
        textview_offline_Send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                copy_to_clipboard(followup_raw_tx.replace("\n","").replace("\r","").replace(" ",""));
                Toast.makeText(MainActivity_Followup.this, "copied..", Toast.LENGTH_SHORT).show();
            }
        });
        // Set a click listener for the broadcast button
        button_broadcast_Sign.setOnClickListener(view -> {

            layout_send_offline.setVisibility(View.GONE);
            ImageView_offline_qr.setVisibility(View.GONE);
            textview_offline_Send.setVisibility(View.GONE);
            textview_offline_url.setVisibility(View.GONE);

            if (!button_broadcast_Sign.isEnabled()) {
                return;
            }

            button_broadcast_Sign.setEnabled(false);
            binding.progressBar2.setVisibility(View.VISIBLE);
            textview_broadcast_report.setVisibility(View.VISIBLE);

            String followup_broadcast_msg = getString(R.string.followup_broadcast_msg);
            textview_broadcast_report.setText(HtmlCompat.fromHtml(
                    "<font color='cyan'>" + followup_broadcast_msg + "</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            progressbar_stat = true;
            doStartProgressBar2();

            // === Background Thread ===
            new Thread(() -> {
                String result = "error~0~Unknown error";   // default value

                try {
                    String server_url_query =
                            "app_name=" + URLEncoder.encode(MainActivity.app_name) +
                                    "&app_version=" + URLEncoder.encode(MainActivity.app_version) +
                                    "&order_csv=" + URLEncoder.encode(
                                    followup_raw_tx.replace("\n", "")
                                            .replace("\r", "")
                                            .replace(" ", "")
                            );

                    result = MainActivity.browse_url_POST(
                            MainActivity.server_url_ods + "order_accept",
                            server_url_query);

                    Access_log.log_it("i", "shahin", "order_accept - result: " + result);

                } catch (Exception e) {
                    result = "error~0~" + e.getMessage();
                    e.printStackTrace();
                }

                // Final variable for lambda
                final String finalResult = result;

                // Update UI on main thread
                runOnUiThread(() -> {
                    progressbar_stat = false;
                    button_broadcast_Sign.setEnabled(true);

                    if (finalResult.startsWith("pending~200~")) {
                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#32CD32'>" + finalResult + "</font><br>" +
                                        "<font color='cyan'>" + getString(R.string.follow_ods_pending) + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));

                    } else if (finalResult.equals("error~207~double spending error~the_sign_md5")) {
                        Access_file.followup_keys_remove(getApplicationContext(), followup_key);
                        Access_file.followup_keys_remove(getApplicationContext(), followup_key.replace("followup_","archive_"));

                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#FF4500'>" + finalResult + "</font><br>" +
                                        "<font color='cyan'>" + getString(R.string.follow_ledger_exists) + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));

                    } else {
                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#FF4500'>" + finalResult + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                });
            }).start();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_back, menu);
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
    private void copy_to_clipboard(String text)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    //region function_of_graph_in

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