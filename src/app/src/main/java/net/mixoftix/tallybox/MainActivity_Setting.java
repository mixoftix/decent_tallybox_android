package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import net.mixoftix.tallybox.databinding.ActivityMainSettingBinding;

public class MainActivity_Setting extends AppCompatActivity {

    private static ActivityMainSettingBinding binding;
    private RadioButton radioConnection, radioPQC;
    private RadioGroup RadioGroupConnection, RadioGroupPQC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_setting);

        binding = ActivityMainSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

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


}