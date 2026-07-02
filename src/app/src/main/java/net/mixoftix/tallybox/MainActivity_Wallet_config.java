package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import net.mixoftix.tallybox.databinding.ActivityMainWalletConfigBinding;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class MainActivity_Wallet_config extends BaseActivity {

    private EditText editTextPassword1,editTextPassword2,editTextRestore_Privatekey;
    private RadioButton radioWalletType;
    private RadioGroup RadioGroupWalletType;
    private Button buttonPasswordSave;
    private static ActivityMainWalletConfigBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wallet_config);

        binding = ActivityMainWalletConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        editTextPassword1 = findViewById(R.id.editTextPassword1);
        editTextPassword2 = findViewById(R.id.editTextPassword2);
        RadioGroupWalletType = findViewById(R.id.RadioGroupWalletAction);
        editTextRestore_Privatekey = findViewById(R.id.editTextRestore_Privatekey);
        buttonPasswordSave = findViewById(R.id.buttonPasswordSave);

        // Set a click listener for the wallet address
        RadioGroupWalletType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                // get selected radio button from radioGroup
                int selectedId = RadioGroupWalletType.getCheckedRadioButtonId();
                radioWalletType = (RadioButton) findViewById(selectedId);
                String str_wallet_type = (String) radioWalletType.getText();

                if (str_wallet_type.contains("Restore")) {
                    editTextRestore_Privatekey.setVisibility(View.VISIBLE);
                    editTextRestore_Privatekey.setText("");
                    editTextRestore_Privatekey.setEnabled(true);
                    editTextRestore_Privatekey.requestFocus();
                }
                if (str_wallet_type.contains("New")) {
                    editTextRestore_Privatekey.setText("");
                    editTextRestore_Privatekey.setEnabled(false);
                    editTextRestore_Privatekey.setVisibility(View.GONE);
                }

                //Toast.makeText(MainActivity_Wallet_config.this, String.valueOf(editTextRestore_Privatekey.isEnabled()), Toast.LENGTH_SHORT).show();
            }
        });

        // Set a click listener for the wallet address
        buttonPasswordSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String str_pass1 = editTextPassword1.getText().toString();
                String str_pass2 = editTextPassword2.getText().toString();

                if (str_pass1.equals(str_pass2) && str_pass1.length()>0)
                {
                    // generate salt
                    String local_privacy_salt = String.valueOf(crypto_random.simple_rnd(1000000,9999999));
                    local_privacy_salt += String.valueOf(crypto_random.simple_rnd(1000000,9999999));
                    local_privacy_salt += String.valueOf(crypto_random.simple_rnd(1000000,9999999));
                    local_privacy_salt += String.valueOf(crypto_random.simple_rnd(1000000,9999999));
                    local_privacy_salt += String.valueOf(crypto_random.simple_rnd(1000000,9999999));
                    local_privacy_salt += String.valueOf(crypto_random.simple_rnd(1000000,9999999));
                    local_privacy_salt += String.valueOf(crypto_random.simple_rnd(1000000,9999999));

                    // make the local privacy
                    String local_privacy_sha256 = null;
                    try {
                        local_privacy_sha256 = hash_functions.Hash_SHA_256(str_pass1 + "~" + local_privacy_salt);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }

                    Access_log.log_it("i","shahin","str_pass1: " + str_pass1);
                    Access_log.log_it("i","shahin","local_privacy_salt: " + local_privacy_salt);
                    Access_log.log_it("i","shahin","local_privacy_sha256: " + local_privacy_sha256);

                    // save the local privacy
                    Access_file.access_file_func_write(getApplicationContext(), "local_privacy_sha256", local_privacy_sha256, "write");
                    Access_file.access_file_func_write(getApplicationContext(), "local_privacy_salt", local_privacy_salt, "write");

                    String privatekey_restore = editTextRestore_Privatekey.getText().toString();
                    Access_log.log_it("i","shahin","privatekey_restore: " + privatekey_restore);

                    // UX: trim private_key
                    privatekey_restore = privatekey_restore.replace(" ","");
                    privatekey_restore = privatekey_restore.replace("\n","");
                    privatekey_restore = privatekey_restore.replace("\r","");

                    // Run Application
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    i.putExtra("privatekey_restore", privatekey_restore);
                    finishAffinity();
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(MainActivity_Wallet_config.this, "Password mis-match!!", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

}