package net.mixoftix.tallybox;

import static java.lang.Boolean.parseBoolean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.mixoftix.tallybox.databinding.ActivityMainPrivateKeyBinding;
// import net.mixoftix.tallybox.databinding.ActivityMainReceiveBinding;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity_PrivateKey extends AppCompatActivity {

    private TextView textview_privatekey_info, textview_privatekey,
            textview_privatekey_attempt_info, textview_privatekey_attempt;
    private EditText editTextPassword1;
    private Button buttonPasswordCheck;

    private TextView textview_main_qc;
    private LinearLayout layout_main_qc;

    private static ActivityMainPrivateKeyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // BGN: to prevent screenshots from privatekey - available in version: 2.0
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        int flags = getWindow().getAttributes().flags;
        Access_log.log_it("d","shahin","FLAG_SECURE_CHECK - Flags: " + flags + " Is Secure: " + ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) );
        // END: to prevent screenshots from privatekey - available in version: 2.0
        setContentView(R.layout.activity_main_private_key);

        binding = ActivityMainPrivateKeyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        editTextPassword1 = findViewById(R.id.editTextPassword1);
        buttonPasswordCheck = findViewById(R.id.buttonPasswordCheck);

        textview_privatekey_info = findViewById(R.id.textview_privatekey_info);
        textview_privatekey = findViewById(R.id.textview_privatekey);
        textview_privatekey_attempt_info = findViewById(R.id.textview_privatekey_attempt_info);
        textview_privatekey_attempt = findViewById(R.id.textview_privatekey_attempt);

        layout_main_qc = findViewById(R.id.layout_main_qc);
        textview_main_qc = findViewById(R.id.textview_main_qc);


        // Set a click listener for the wallet address
        buttonPasswordCheck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String str_pass1 = editTextPassword1.getText().toString();
                String local_privacy_salt = Access_file.access_file_func_read(getApplicationContext(), "local_privacy_salt");
                String local_privacy_sha256 = Access_file.access_file_func_read(getApplicationContext(), "local_privacy_sha256");

                // recreate the local password
                String local_privacy_sha256_check = null;
                try {
                    local_privacy_sha256_check = hash_functions.Hash_SHA_256(str_pass1 + "~" + local_privacy_salt);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                if (local_privacy_sha256_check.equals(local_privacy_sha256))
                {
                    editTextPassword1.setText("");
                    editTextPassword1.setEnabled(false);
                    buttonPasswordCheck.setEnabled(false);

                    // access & attempts
                    String access_history_privatekey_new = "";

                    String access_history_privatekey = Access_file.access_file_func_read(getApplicationContext(), "access_history_privatekey");
                    if (access_history_privatekey.equals("-"))
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());

                        access_history_privatekey_new = "ACCESS~1~" + currentDateandTime;
                        Access_file.access_file_func_write(getApplicationContext(), "access_history_privatekey", access_history_privatekey_new, "write");

                        textview_privatekey_info.setText("Write down & preserve your private key:");
                    }
                    else
                    {
                        String[] split_attempts_privatekey;
                        split_attempts_privatekey = access_history_privatekey.split("~");
                        String att_number = split_attempts_privatekey[1];
                        int att_number_int = Integer.parseInt(att_number);
                        att_number_int++;

                        //Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());

                        access_history_privatekey_new = "ACCESS~" + att_number_int + "~" + currentDateandTime;
                        Access_file.access_file_func_write(getApplicationContext(), "access_history_privatekey", access_history_privatekey_new, "write");
                    }


                    String the_private_key = net.mixoftix.tallybox.MainActivity.retrieve_private_key();

                    String publicKey_xy = MainActivity.publicKey_x_HEX + "~" + MainActivity.publicKey_y_HEX;
                    Access_log.log_it("i","shahin","222 - xy (reloaded): " + publicKey_xy);

                    boolean result_of_qc = false;
                    try {
                        result_of_qc = MainActivity.qc_keypairs(the_private_key,publicKey_xy);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Access_log.log_it("i","shahin","222 - result_of_qc: " + result_of_qc);

                    String yellow_color_code = "#FFD700";
                    String green_color_code = "#32CD32";
                    String red_color_code = "#FF4500";

                    if (result_of_qc)
                    {
                        // visible views
                        textview_privatekey_info.setVisibility(View.VISIBLE);
                        textview_privatekey.setVisibility(View.VISIBLE);
                        textview_privatekey_attempt_info.setVisibility(View.VISIBLE);
                        textview_privatekey_attempt.setVisibility(View.VISIBLE);

                        // qc of keys passed
                        layout_main_qc.setVisibility(View.VISIBLE);
                        textview_main_qc.setText(
                                HtmlCompat.fromHtml(
                                        "<font color='" + green_color_code + "'>" +
                                               "QC passed.." +
                                               "</font>",
                                        HtmlCompat.FROM_HTML_MODE_LEGACY));

                        textview_privatekey.setText(splitter(the_private_key,8));
                        textview_privatekey_attempt.setText(
                                HtmlCompat.fromHtml(
                                        "Previous: " +
                                                access_history_privatekey +
                                                "<br>" +
                                                "Current: " +
                                                access_history_privatekey_new
                                        , HtmlCompat.FROM_HTML_MODE_LEGACY)
                        );
                    }
                    else
                    {
                        textview_privatekey_info.setVisibility(View.GONE);
                        textview_privatekey.setVisibility(View.GONE);
                        textview_privatekey_attempt_info.setVisibility(View.GONE);
                        textview_privatekey_attempt.setVisibility(View.GONE);

                        // clear the cache of keys and retry
                        layout_main_qc.setVisibility(View.VISIBLE);
                        textview_main_qc.setText(
                                HtmlCompat.fromHtml(
                                        "<font color='" + red_color_code + "'>" +
                                                "QC failed! " +
                                                "</font>" +
                                                "<br>" +
                                                "<font color='" + yellow_color_code + "'>" +
                                                "Uninstall the app and try again.." +
                                                "</font>" ,
                                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }

                    Access_file.access_file_func_write(getApplicationContext(), "attempts_to_erase_privatekey", "0", "write");
                    Access_log.log_it("i","shahin","222 - attempts_to_erase_privatekey: " + "0");
                }
                else
                {
                    textview_privatekey_info.setVisibility(View.GONE);
                    textview_privatekey.setVisibility(View.GONE);
                    textview_privatekey_attempt_info.setVisibility(View.GONE);
                    textview_privatekey_attempt.setVisibility(View.GONE);
                    layout_main_qc.setVisibility(View.GONE);

                    // 5-times attempt to erase
                    String attempts_to_erase_privatekey = Access_file.access_file_func_read(getApplicationContext(), "attempts_to_erase_privatekey");
                    if (attempts_to_erase_privatekey.equals("-"))
                    {
                        attempts_to_erase_privatekey = "0";
                    }

                    int att_number_int = Integer.parseInt(attempts_to_erase_privatekey);
                    att_number_int++;

                    Access_log.log_it("i","shahin","222 - att_number_int: " + att_number_int);

                    if (att_number_int <= 5)
                    {
                        Access_file.access_file_func_write(getApplicationContext(), "attempts_to_erase_privatekey", String.valueOf(att_number_int), "write");
                        Toast.makeText(MainActivity_PrivateKey.this, "Password mis-match!!\n Attempt " + att_number_int + "/5 to erase wallet..", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Access_file.access_file_func_write(getApplicationContext(), "local_privacy_sha256", "", "write");
                        Access_file.access_file_func_write(getApplicationContext(), "my_local_key_in_KeyStore", "", "write");
                        Access_file.access_file_func_write(getApplicationContext(), "aes_of_privateKey_d_b58", "", "write");
                        Access_file.access_file_func_write(getApplicationContext(), "publicKey_x_b58", "", "write");
                        Access_file.access_file_func_write(getApplicationContext(), "publicKey_y_b58", "", "write");

                        Access_file.access_file_func_write(getApplicationContext(), "access_history_privatekey", "", "write");
                        Access_file.access_file_func_write(getApplicationContext(), "attempts_to_erase_privatekey", "", "write");
                        Toast.makeText(MainActivity_PrivateKey.this, "wallet erased..", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(getApplicationContext(),MainActivity.class);
                        finishAffinity();
                        startActivity(i);
                    }

                }
            }

        });

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

    public String splitter(String input, int lengths)
    {
        String output = "";

        int pos = 0;
        for(int i=0; i<input.length(); i=i+lengths)
        {
            int pos_next = pos + lengths;
            while (pos_next > input.length())
            {
                pos_next--;
            }
            output = output + input.substring(pos, pos_next) + "\n";
            pos = pos + lengths;
        }
        return "\n" + output;
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