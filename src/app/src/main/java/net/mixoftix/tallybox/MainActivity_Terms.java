package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.mixoftix.tallybox.databinding.ActivityMainTermsBinding;

public class MainActivity_Terms extends AppCompatActivity {

    private static ActivityMainTermsBinding binding;
    private TextView textview_about,textview_terms_disclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_terms);

        binding = ActivityMainTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        textview_about = findViewById(R.id.textview_terms_content);
        textview_terms_disclaimer = findViewById(R.id.textview_terms_disclaimer);

        String tallybox_terms =
                "<b>1. Acceptance of Terms</b>" +
                        "<br>" +
                "By downloading, installing, or using the TallyBox application, you agree to be bound by these Terms and Conditions (\"Terms\"). If you do not agree with these Terms, please do not use the TallyBox wallet. These Terms govern your access to and use of TallyBox, including any updates or modifications." +
                        "<br>" +
                        "<br>" +
                "<b>2. Description of Service</b>" +
                        "<br>" +
                "TallyBox is an innovative cryptocurrency wallet developed to facilitate digital transactions with simplicity and efficiency. It operates on a cutting-edge Directed Acyclic Graph (DAG) protocol, reducing transaction costs to 0.75 2PN (digital two-penny) per transaction—down from 25 cents on the lowest fees of several blockchains. " +
                        "<br>" +
                        "<br>" +
                "TallyBox features a unique dual-token model with 2ZR (digital do-zari) and 2PN, inspired by cultural expressions like “I said a penny for your thoughts, but I got two pennies’ worth.” It is designed as a closed-loop system with potential for inter-loop transactions, offering a secure, educational, cost-effective, and engaging platform for managing digital assets." +
                        "<br>" +
                        "<br>" +
                "<b>3. Description of System Coins</b>" +
                        "<br>" +
                "2ZR (Digital Do-Zari): 2ZR is a zero-stable meme coin used to support the research and development (R&D) phase of the TallyBox project. 2ZR is NOT ALLOWED for exchange within or outside the TallyBox ecosystem." +
                        "<br>" +
                        "<br>" +
                "2PN (Digital Two-Pennies): 2PN is a cent-stable meme coin used to facilitate fee processing within the TallyBox project. 2PN is NOT ALLOWED for exchange within or outside the TallyBox ecosystem." +
                        "<br>" +
                        "<br>" +
                "<b>4. User Responsibilities</b>" +
                        "<br>" +
                "You are responsible for maintaining the confidentiality of your private keys, wallet credentials, and any associated tokens.  " +
                        "<br>" +
                        "<br>" +
                "You must comply with all applicable laws, regulations, and these Terms while using TallyBox. " +
                        "<br>" +
                        "<br>" +
                "You agree not to use TallyBox for any illegal or unauthorized purpose, including but not limited to money laundering, fraud, or violation of cryptocurrency regulations." +
                        "<br>" +
                        "<br>" +
                "2ZR and 2PN are not intended to be securities or investment vehicles and are restricted to use within the TallyBox ecosystem only." +
                        "<br>" +
                        "<br>" +
                "<b>5. Fees and Transactions</b>" +
                        "<br>" +
                "TallyBox charges a transaction fee of 0.75 2PN per transaction, deducted automatically via the DAG protocol. Additional fees may apply for inter-loop transactions or other services, as updated in these Terms or within the app. " +
                        "<br>" +
                        "<br>" +
                "All transactions are final and non-reversible once confirmed on the TallyBox network." +
                        "<br>" +
                        "<br>" +
                "<b>6. Intellectual Property</b>" +
                        "<br>" +
                "TallyBox and its associated software, logos, and content are the intellectual property of \"Mixoftix Curiosity Lab\". You may not reproduce, distribute, or modify TallyBox without explicit permission." +
                        "<br>" +
                        "<br>" +
                "<b>7. Limitation of Liability</b>" +
                        "<br>" +
                "To the fullest extent permitted by law, \"Mixoftix Curiosity Lab\" is not liable for any direct, indirect, incidental, special, or consequential damages arising from your use of TallyBox, including but not limited to loss of digital assets, transaction delays, or system outages. TallyBox is provided “as is,” and we make no warranties or guarantees regarding its performance or security." +
                        "<br>" +
                        "<br>" +
                "<b>8. Termination</b>" +
                        "<br>" +
                "We reserve the right to suspend or terminate your access to TallyBox at our discretion, with or without notice, if you violate these Terms or engage in activities that harm the TallyBox ecosystem." +
                        "<br>" +
                        "<br>" +
                "<b>9. Changes to Terms</b>" +
                        "<br>" +
                "We may update these Terms periodically. Continued use of TallyBox after such updates constitutes your acceptance of the revised Terms. Please review these Terms regularly for changes." +
                        "<br>" +
                        "<br>" +
                "<b>10. Contact Us</b>" +
                        "<br>" +
                "For questions or concerns about these Terms, contact us at mixoftix@gmail.com." +
                        "<br>";

        String tallybox_disclaimer =
                "<b>Disclaimer</b>" +
                        "<br>" +
                "TallyBox is a cryptocurrency wallet and does not constitute financial, legal, or investment advice. The use of 2ZR, 2PN, or any digital assets within TallyBox carries inherent risks, including potential loss of value, market volatility, and technological failures. " +
                        "<br>" +
                        "<br>" +
                "\"Mixoftix Curiosity Lab\" does not guarantee the stability, security, or value of any digital assets managed through TallyBox. " +
                        "<br>" +
                        "<br>" +
                "Users are solely responsible for their investment decisions and the security of their private keys and wallet data. " +
                        "<br>" +
                        "<br>" +
                "TallyBox operates in compliance with applicable cryptocurrency regulations, but users should be aware that such regulations may change, impacting the use of digital assets." +
                        "<br>" +
                        "<br>" +
                "2ZR and 2PN are not intended to be securities or investment vehicles and are restricted to use within the TallyBox ecosystem only." +
                        "<br>";

        textview_about.setText(HtmlCompat.fromHtml(tallybox_terms,HtmlCompat.FROM_HTML_MODE_LEGACY));
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