package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.mixoftix.tallybox.databinding.ActivityMainAboutBinding;

import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MainActivity_About extends AppCompatActivity {

    private static ActivityMainAboutBinding binding;
    private TextView textview_about_update,textview_about;

    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;
    private String submit_txt = "";
    // END: browse http


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_about);

        binding = ActivityMainAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        textview_about_update = findViewById(R.id.textview_about_update);
        textview_about = findViewById(R.id.textview_about_content);

        String tallybox_update = "version: " + MainActivity.app_version + " / check for update";

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

        textview_about_update.setText(HtmlCompat.fromHtml(tallybox_update,HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_about_update.setMovementMethod(LinkMovementMethod.getInstance());
        textview_about.setText(HtmlCompat.fromHtml(tallybox_about,HtmlCompat.FROM_HTML_MODE_LEGACY));

        // Set a click listener for textview_about_update
        textview_about_update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                //String tallybox_update = "version: " + MainActivity.app_version + " / connecting..";
                //textview_about_update.setText(HtmlCompat.fromHtml(tallybox_update,HtmlCompat.FROM_HTML_MODE_LEGACY));

                // set progressbar
                progressbar_stat = true;
                doStartProgressBar2();

                // config internet connection
                String server_url_wallet =  MainActivity.setting_network_protocol +
                                            "://wallet.mixoftix.net/" +
                                            "VersionString.txt";

                String result = MainActivity.browse_url(server_url_wallet);
                Access_log.log_it("i","shahin",server_url_wallet + " - result: " + result);
                //result = result.replace ("http://","https://");
                //result = result.replace ("http://",MainActivity.setting_network_protocol + "://");
                //Access_log.log_it("i","shahin",MainActivity.server_url + " - result: " + result);

                if (isNewerVersion(result, MainActivity.app_version)) {
                    // New version is available
                    result = result.replace (".","_");
                    String server_url_wallet_dl =  MainActivity.setting_network_protocol +
                                                "://wallet.mixoftix.net" +
                                                "/dlx" +
                                                "/android" +
                                                "/" + MainActivity.app_name + "_" + result + ".apk"
                                                ;

                    textview_about_update.setText(HtmlCompat.fromHtml("<a href=" + server_url_wallet_dl + ">New version: " + result + "</a>",HtmlCompat.FROM_HTML_MODE_LEGACY));
                }
                else
                {
                    textview_about_update.setText(HtmlCompat.fromHtml("version: " + MainActivity.app_version + " / up to date!",HtmlCompat.FROM_HTML_MODE_LEGACY));
                }

                // reset progressbar
                progressbar_stat = false;
            }
        });

    }

    private boolean isNewerVersion(String serverVer, String localVer) {
        try {
            double s = Double.parseDouble(serverVer.trim());
            double l = Double.parseDouble(localVer.trim());
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