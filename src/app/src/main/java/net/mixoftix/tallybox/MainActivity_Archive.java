package net.mixoftix.tallybox;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import net.mixoftix.tallybox.databinding.ActivityMainArchiveBinding;
import net.mixoftix.tallybox.databinding.ActivityMainFollowupBinding;

import java.util.List;

public class MainActivity_Archive extends BaseActivity {

    private static ActivityMainArchiveBinding binding;
    private TextView textview_archive, textview_archive_empty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_home_refresh);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_archive);
        setContentView(R.layout.activity_main_archive);

        binding = ActivityMainArchiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHomeRefresh);

        textview_archive_empty = findViewById(R.id.textview_archive_empty);
        LinearLayout layoutArchive = findViewById(R.id.layout_archive);
        textview_archive = findViewById(R.id.textview_archive);
        textview_archive.setText(getString(R.string.archive_note));

        //region followup

        String follow_up = getString(R.string.follow_up);
        String follow_ignore = getString(R.string.follow_ignore);

        String currentLang = LocaleHelper.getCurrentLanguage(this);
        String refresh_utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());
        List<String> followup_keys_list = Access_file.followup_keys_list(getApplicationContext(),"followup");

        layoutArchive.removeAllViewsInLayout();

        if (followup_keys_list.size() <= 0)
        {
            textview_archive_empty.setVisibility(View.VISIBLE);
        }
        else
        {
            textview_archive_empty.setVisibility(View.GONE);

            for (String followup_key : followup_keys_list) {
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

                String payment_graphName = split_followup_tx[3];
                String payment_moment = followup_key.replace("followup_","");
                payment_moment = Access_time.getTimeDifference(currentLang,refresh_utc_unix_now,payment_moment);
                String payment_currency = split_followup_tx[11];
                String payment_amount = split_followup_tx[13];

                addDynamicItemLayout(
                        layoutArchive,
                        payment_graphName,
                        payment_moment,
                        payment_currency,
                        payment_amount,
                        follow_up,               // Button 1 text
                        follow_ignore,           // Button 2 text
                        v -> {
                            // ← Button 1 Clicked (e.g. Check Network / PQC)
                            //checkPqcStatus(graphName, serial);
                            Intent i = new Intent(getApplicationContext(),MainActivity_Followup.class);
                            i.putExtra("followup_key", followup_key);
                            startActivity(i);
                        },
                        v -> {
                            // ← Button 2 Clicked (e.g. Another action)
                            //doAnotherAction(graphName, serial);
                            Access_file.followup_keys_remove(getApplicationContext(), followup_key);
                            Access_file.followup_keys_remove(getApplicationContext(), followup_key.replace("followup_","archive_"));

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                );

            }
        }

        //endregion

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_refresh, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_home) {
            //onBackPressed();
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);

            return true;
        }

        if (id == R.id.action_refresh) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //region functions_of_followup

    private void addDynamicItemLayout(LinearLayout parent,
                                      String graphName,
                                      String payment_moment,
                                      String payment_currency,
                                      String payment_amount,
                                      String button1Text,
                                      String button2Text,
                                      View.OnClickListener button1Listener,
                                      View.OnClickListener button2Listener) {

        // Main container
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setBackgroundResource(R.drawable.frame_white);

        int padding = dpToPx(12);
        itemLayout.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.bottomMargin = dpToPx(12);
        itemLayout.setLayoutParams(containerParams);

        // Non-clickable TextView
        TextView textView = new TextView(this);
        textView.setText(
                getString(R.string.follow_moment_1) +
                " (" + payment_moment + ") " +
                getString(R.string.follow_moment_2) + "\n" +
                getString(R.string.follow_graph) + ": " + graphName + "\n" +
                getString(R.string.follow_amount) + ": " + payment_amount + " (" + payment_currency + ")"
        );
        textView.setTextSize(17);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(0, 0, 0, dpToPx(12));
        setVazirmatnFont(textView);

        // Buttons container (Horizontal)
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonsLayout.setWeightSum(2);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnParams.leftMargin = dpToPx(4);
        btnParams.rightMargin = dpToPx(4);

        // === Button 1 ===
        com.google.android.material.button.MaterialButton button1 =
                new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        button1.setText(button1Text);
        button1.setAllCaps(false);
        button1.setLayoutParams(btnParams);
        button1.setTextSize(16);
        setVazirmatnFont(button1);
        button1.setIcon(ContextCompat.getDrawable(this, R.drawable.baseline_checklist_rtl_24));
        button1.setIconGravity(MaterialButton.ICON_GRAVITY_END);
        button1.setIconSize(dpToPx(24));
        button1.setOnClickListener(button1Listener);

        // === Button 2 ===
        com.google.android.material.button.MaterialButton button2 =
                new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);

        button2.setText(button2Text);
        button2.setAllCaps(false);
        button2.setLayoutParams(btnParams);
        button2.setTextSize(16);
        setVazirmatnFont(button2);
        button2.setIcon(ContextCompat.getDrawable(this, R.drawable.baseline_visibility_off_24));
        button2.setIconGravity(MaterialButton.ICON_GRAVITY_END);
        button2.setIconSize(dpToPx(24));
        button2.setOnClickListener(button2Listener);

        // Add views
        itemLayout.addView(textView);
        buttonsLayout.addView(button1);
        buttonsLayout.addView(button2);
        itemLayout.addView(buttonsLayout);

        parent.addView(itemLayout);
    }

    private interface OnItemClickListener {
        void onClick(TextView textView, String graphName, String pqcSerial);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setVazirmatnFont(TextView view) {
        try {
            Typeface vazirTypeface = ResourcesCompat.getFont(this, R.font.vazirmatn);
            if (vazirTypeface != null) {
                view.setTypeface(vazirTypeface);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default font if loading fails
        }
    }

    //endregion


}