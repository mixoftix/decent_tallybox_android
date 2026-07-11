package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import net.mixoftix.tallybox.databinding.ActivityMainAboutBinding;

import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MainActivity_About extends BaseActivity {

    private static ActivityMainAboutBinding binding;
    private static ImageView ImageView_wallet_url;
    private TextView textview_about_update,textview_about,textview_wallet_url,textview_wallet_invitation;

    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;
    // END: browse http


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_about);
        setContentView(R.layout.activity_main_about);

        binding = ActivityMainAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        textview_about_update = findViewById(R.id.textview_about_update);
        textview_about = findViewById(R.id.textview_about_content);
        ImageView_wallet_url = findViewById(R.id.ImageView_wallet_url);
        textview_wallet_url = findViewById(R.id.textview_wallet_url);
        textview_wallet_invitation = findViewById(R.id.textview_wallet_invitation);

        String tallybox_about_version = getString(R.string.about_version);
        String tallybox_about_version_new = getString(R.string.about_version_new);
        String tallybox_about_update = getString(R.string.about_update_check);
        String tallybox_about_result = getString(R.string.about_update_result);
        String tallybox_update = tallybox_about_version + ": " + MainActivity.app_version + " / " + tallybox_about_update;

        /*
        String tallybox_about =
                "<b>Description of Service</b>" +
                        "<br>" +
                        "<br>" +
                "TallyBox is an innovative cryptocurrency wallet developed to redefine digital transactions with simplicity and efficiency. " +
                        "<br>" +
                        "<br>" +
                "Designed as a closed-loop system with potential for inter-loop transactions, TallyBox is poised to empower users in the evolving world of cryptocurrency, offering a secure, educational, cost-effective, and engaging platform for managing digital assets." +
                        "<br>" +
                        "<br>" +
                "Developed with passion in our lab, featuring a unique dual-token model with both Fiat money and Digital Token, TallyBox draws inspiration from digital transformation adding a playful yet meaningful layer to the crypto experience." +
                        "<br>";
        */

        textview_about_update.setText(HtmlCompat.fromHtml(tallybox_update,HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_about_update.setMovementMethod(LinkMovementMethod.getInstance());

        String tallybox_about = getString(R.string.tallybox_about);
        //textview_about.setText(Html.fromHtml(tallybox_about, Html.FROM_HTML_MODE_LEGACY));
        textview_about.setText(HtmlCompat.fromHtml(tallybox_about,HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_about.setMovementMethod(LinkMovementMethod.getInstance());

        //encodeToQrCode(wallet_address,300,300);
        String wallet_url = "https://wallet.mixoftix.net";
        Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(wallet_url,300,300);
        if (qrCodeBitmap != null) {
            String tallybox_invite = getString(R.string.tallybox_invite);
            textview_wallet_invitation.setText(HtmlCompat.fromHtml(tallybox_invite,HtmlCompat.FROM_HTML_MODE_LEGACY));
            ImageView_wallet_url.setImageBitmap(qrCodeBitmap);
            textview_wallet_url.setText(HtmlCompat.fromHtml(
                                "<a href=\"" + wallet_url + "\">" + wallet_url + "</a>"
                                ,HtmlCompat.FROM_HTML_MODE_LEGACY));
            textview_wallet_url.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // Set a click listener for textview_about_update
        textview_about_update.setOnClickListener(view -> {

            // Show progress immediately
            progressbar_stat = true;
            doStartProgressBar2();

            // Disable interaction while loading (optional but recommended)
            textview_about_update.setEnabled(false);

            new Thread(() -> {
                String result = "error";

                try {
                    String server_url_wallet = "https://wallet.mixoftix.net/VersionString.txt";
                    result = MainActivity.browse_url(server_url_wallet);

                    Access_log.log_it("i", "shahin", server_url_wallet + " - result: " + result);

                } catch (Exception e) {
                    result = "error~" + e.getMessage();
                    e.printStackTrace();
                }

                final String finalResult = result;   // Needed for lambda

                // Update UI on main thread
                runOnUiThread(() -> {

                    progressbar_stat = false;           // Stop progress bar
                    textview_about_update.setEnabled(true);

                    if (isNewerVersion(finalResult, MainActivity.app_version)) {

                        Access_log.log_it("i","shahin","New version available..");
                        // New version available
                        String versionClean = finalResult.replace(".", "_");
                        String server_url_wallet_dl = "https://wallet.mixoftix.net" +
                                "/dlx/android/" +
                                MainActivity.app_name + "_" + versionClean + ".apk";

                        textview_about_update.setText(HtmlCompat.fromHtml(
                                "<a href=\"" + server_url_wallet_dl + "\">" +
                                        getString(R.string.about_version_new) + ": " + finalResult +
                                        "</a>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY));

                    }
                    else
                    {
                        Access_log.log_it("i","shahin","No update..");
                        // No update
                        textview_about_update.setText(HtmlCompat.fromHtml(
                                tallybox_about_version + ": " + MainActivity.app_version +
                                        " / " + tallybox_about_result,
                                HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                });
            }).start();
        });

    }

    private boolean isNewerVersion(String serverVer, String localVer) {
        try {
            double s = Double.parseDouble(serverVer.trim());
            double l = Double.parseDouble(localVer.trim());
            Access_log.log_it("i","shahin","serverVer: " + s);
            Access_log.log_it("i","shahin","localVer: " + l);

            return s > l;
        } catch (Exception e) {
            Access_log.log_it("e", "shahin", "Version compare error: " + e.getMessage());
            return false; // safe fallback
        }
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
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
    }

}