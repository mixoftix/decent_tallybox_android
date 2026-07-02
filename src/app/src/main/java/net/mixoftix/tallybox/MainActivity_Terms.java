package net.mixoftix.tallybox;

import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.mixoftix.tallybox.databinding.ActivityMainTermsBinding;

public class MainActivity_Terms extends BaseActivity {

    private static ActivityMainTermsBinding binding;
    private TextView textview_terms_content,textview_terms_disclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_terms);

        binding = ActivityMainTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        textview_terms_content = findViewById(R.id.textview_terms_content);
        textview_terms_disclaimer = findViewById(R.id.textview_terms_disclaimer);

        String tallybox_terms = getString(R.string.tallybox_terms);
        String tallybox_disclaimer = getString(R.string.tallybox_disclaimer);

        textview_terms_content.setText(HtmlCompat.fromHtml(tallybox_terms,HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_terms_disclaimer.setText(HtmlCompat.fromHtml(tallybox_disclaimer,HtmlCompat.FROM_HTML_MODE_LEGACY));
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