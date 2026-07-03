package net.mixoftix.tallybox;

import static net.mixoftix.tallybox.MainActivity.spinner_options;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.mixoftix.tallybox.databinding.ActivityMainSendBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity_Send extends BaseActivity {

    //region variables

    private boolean is_sign_broadcatable, is_parcel_processing;
    private static ActivityMainSendBinding binding;
    private static ImageView ImageView_offline_qr;
    private TextView textview_graph_in, textview_offline_Send, textview_offline_url, textview_broadcast_report;
    private EditText editTextGraphDomainTo, editTextWalletTo, editTextAmount, editTextOrder;
    private CheckBox checkbox_extra_info,checkbox_offline_sign;
    private Button buttonSign, buttonBroadcast;

    //private RadioButton radioCurrency;
    //private RadioGroup RadioGroupCurrency;

    private Spinner dropdownSpinner_GraphDomainTo;
    private ImageView dropdownIcon;
    private TextView dropdownText;
    private ListPopupWindow popupWindow;
    private List<DropdownItem> currentDropdownItems = new ArrayList<>();

    private LinearLayout layout_of_extra,layout_send_offline;

    // BGN: browse http
    private static Handler handler = new Handler();
    private static boolean progressbar_stat = false;
    private String submit_txt = "";
    // END: browse http

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //region activity_definition

        super.onCreate(savedInstanceState);
        setTitle(R.string.title_send);
        setContentView(R.layout.activity_main_send);

        binding = ActivityMainSendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHomeqr);

        textview_graph_in = findViewById(R.id.textview_graph_in);
        //editTextGraphDomainTo = findViewById(R.id.editTextGraphDomainTo);

        // BGN: spinner of graph_to
        dropdownSpinner_GraphDomainTo = findViewById(R.id.dropdownSpinner_GraphDomainTo);

        GraphSpinnerAdapter adapter = new GraphSpinnerAdapter(
                this,
                MainActivity.spinner_options
        );

        dropdownSpinner_GraphDomainTo.setAdapter(adapter);
        // END: spinner of graph_to

        editTextWalletTo = findViewById(R.id.editTextWalletTo);
        //RadioGroupCurrency = findViewById(R.id.RadioGroupCurrency);
        editTextOrder = findViewById(R.id.editTextOrder);
        editTextAmount = findViewById(R.id.editTextAmount);

        checkbox_extra_info = findViewById(R.id.checkbox_extra_info);
        checkbox_offline_sign = findViewById(R.id.checkbox_offline_sign);
        buttonSign = findViewById(R.id.buttonSign);
        buttonBroadcast = findViewById(R.id.buttonBroadcast);

        textview_broadcast_report = findViewById(R.id.textview_broadcast_report);
        ImageView_offline_qr = findViewById(R.id.ImageView_offline_SendQR);
        textview_offline_Send = findViewById(R.id.textview_offline_send);
        textview_offline_url = findViewById(R.id.textview_offline_url);

        layout_of_extra = findViewById(R.id.layout_of_extra);
        layout_send_offline = findViewById(R.id.layout_send_offline);

        //endregion

        //region drop_menu

        /*
        List<DropdownItem> items = new ArrayList<>();
        items.add(new DropdownItem(R.drawable.baseline_fingerprint_24, "null"));
        items.add(new DropdownItem(R.mipmap.coin_2pn, "2PN"));
        items.add(new DropdownItem(R.mipmap.coin_2zr, "2ZR"));
        items.add(new DropdownItem(R.mipmap.coin_tlh, "TLH"));
        */

        // Get the two selected servers
        String selectedServer1 = MainActivity.graph_domain_in;
        String selectedServer2 = MainActivity.graph_domain_in;
        updateCommonTokensDropdown(selectedServer1, selectedServer2);

        //endregion

        //region setting_of_checkboxes

        // set extra info in the first run
        String str_is_extra_info = Access_file.access_file_func_read(getApplicationContext(), "setting_is_extra_info");
        Access_log.log_it("i","shahin","str_is_extra_info: " + str_is_extra_info);

        if (str_is_extra_info.toLowerCase().equals("true"))
        {
            checkbox_extra_info.setChecked(true);
            layout_of_extra.setVisibility(View.VISIBLE);
        }
        else
        {
            checkbox_extra_info.setChecked(false);
            layout_of_extra.setVisibility(View.GONE);
        }

        // Set a click listener for the extra info
        checkbox_extra_info.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String str_is_extra_info = String.valueOf(checkbox_extra_info.isChecked());
                Access_log.log_it("i","shahin","setting str_is_extra_info: " + str_is_extra_info);
                Access_file.access_file_func_write(getApplicationContext(), "setting_is_extra_info", str_is_extra_info, "write");

                if (str_is_extra_info.toLowerCase().equals("true"))
                {
                    layout_of_extra.setVisibility(View.VISIBLE);
                }
                else
                {
                    layout_of_extra.setVisibility(View.GONE);
                }

            }
        });

        // set offline sign in the first run
        String str_is_offline_sign = Access_file.access_file_func_read(getApplicationContext(), "setting_is_offline_sign");
        Access_log.log_it("i","shahin","str_is_offline_sign: " + str_is_offline_sign);

        if (str_is_offline_sign.toLowerCase().equals("true"))
        {
            checkbox_offline_sign.setChecked(true);
            layout_send_offline.setVisibility(View.VISIBLE);
        }
        else
        {
            checkbox_offline_sign.setChecked(false);
            layout_send_offline.setVisibility(View.GONE);
        }

        // Set a click listener for the extra info
        checkbox_offline_sign.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String str_is_offline_sign = String.valueOf(checkbox_offline_sign.isChecked());
                Access_log.log_it("i","shahin","setting str_is_offline_sign: " + str_is_offline_sign);
                Access_file.access_file_func_write(getApplicationContext(), "setting_is_offline_sign", str_is_offline_sign, "write");

                if (str_is_offline_sign.toLowerCase().equals("true"))
                {
                    layout_send_offline.setVisibility(View.VISIBLE);
                    buttonBroadcast.setEnabled(false);
                }
                else
                {
                    layout_send_offline.setVisibility(View.GONE);
                    if (is_sign_broadcatable)
                    {
                        buttonBroadcast.setEnabled(true);
                    }
                }

            }
        });

        //endregion

        //region view_listeners

        // Set a click listener for the sign button
        buttonSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Retrieve order data
                String str_graph_domain_from = MainActivity.graph_domain_in;
                String str_graph_domain_to = dropdownSpinner_GraphDomainTo.getSelectedItem().toString();

                int fromIdx = getGraphIndex(str_graph_domain_from);
                int toIdx   = getGraphIndex(str_graph_domain_to);

                if (!haveSameZone(fromIdx, toIdx)) {
                    Toast.makeText(MainActivity_Send.this, "Transfer not allowed: Different zones", Toast.LENGTH_LONG).show();
                    return;
                }

                String str_wallet_from = net.mixoftix.tallybox.MainActivity.wallet_address;
                String str_wallet_to = editTextWalletTo.getText().toString();
                //String str_amount = editTextAmount.getText().toString();
                String str_amount = StringHelper.normalizeDigits(editTextAmount.getText().toString());
                String str_order_id = StringHelper.normalizeDigits(editTextOrder.getText().toString());
                String str_order_utc_unix = String.valueOf(getUnixTimestampSeconds());

                //int selectedId = RadioGroupCurrency.getCheckedRadioButtonId();
                //radioCurrency = (RadioButton) findViewById(selectedId);
                String str_currency = dropdownText.getText().toString(); // (String) radioCurrency.getText();
                Access_log.log_it("i","shahin","str_currency: " + str_currency);
                boolean is_valid_currency = isValidSelectedCurrency(str_currency);
                Access_log.log_it("i","shahin","is_valid_currency: " + is_valid_currency);

                //layout_send_offline.setVisibility(View.GONE);
                //ImageView_wallet_qr.setVisibility(View.GONE);
                //textviewSend.setVisibility(View.GONE);

                textview_broadcast_report.setVisibility(View.GONE);
                buttonBroadcast.setEnabled(false);
                is_sign_broadcatable = false;

                if (str_graph_domain_to == null || str_graph_domain_to.isEmpty())
                {
                    dropdownSpinner_GraphDomainTo.requestFocus();
                    Toast.makeText(MainActivity_Send.this, "invalid graph domain..", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (str_wallet_to == null || str_wallet_to.isEmpty())
                {
                    editTextWalletTo.requestFocus();
                    Toast.makeText(MainActivity_Send.this, "invalid wallet address..", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!isValidSelectedCurrency(str_currency))
                {
                    popupWindow.show();  // Open popup programmatically
                    Toast.makeText(MainActivity_Send.this, "invalid token..", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (str_amount == null || str_amount.isEmpty())
                {
                    editTextAmount.requestFocus();
                    Toast.makeText(MainActivity_Send.this, "invalid transaction amount..", Toast.LENGTH_SHORT).show();
                    return;
                }

                //region proceed_to_sign

                // old single language
                /*
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(8);
                df.setMinimumFractionDigits(8);
                df.setGroupingUsed(false); // Disable grouping (commas)
                double amount = Double.parseDouble(str_amount); // Parsing string to double
                str_amount = df.format(amount); // Formatting the double and storing back as string
                */

                // new multi-language
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                DecimalFormat df = new DecimalFormat("0.00000000", symbols);
                df.setGroupingUsed(false);

                double amount = Double.parseDouble(str_amount);
                str_amount = df.format(amount);


                //editTextGraphDomainTo.setText("");
                int drawableResId = R.drawable.baseline_fingerprint_24;
                dropdownText.setText("null");
                dropdownIcon.setImageResource(drawableResId);

                editTextWalletTo.setText("");
                editTextAmount.setText("");
                editTextOrder.setText("");

                //layout_send_offline.setVisibility(View.VISIBLE);
                //ImageView_wallet_qr.setVisibility(View.VISIBLE);
                //textviewSend.setVisibility(View.VISIBLE);

                if (!checkbox_offline_sign.isChecked())
                {
                    buttonBroadcast.setEnabled(true);
                }

                // "I said a penny for your thoughts, but I got two pennies' worth"
                // get selected radio button from radioGroup

                // config order & sign
                String my_order =
                        str_graph_domain_from + "~" +
                                str_graph_domain_to + "~" +
                                str_wallet_from + "~" +
                                str_wallet_to + "~" +
                                str_currency + "~" +
                                str_amount + "~" +
                                str_order_id + "~" +
                                str_order_utc_unix;

                Access_log.log_it("i","shahin","my_order: " + my_order);

                try {
                    Access_log.log_it("i","shahin","hash(my_order): " + hash_functions.Hash_SHA_256(my_order));

                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                String my_sign = net.mixoftix.tallybox.MainActivity.sign_order(
                        my_order
                        ,net.mixoftix.tallybox.MainActivity.retrieve_private_key());

                Access_log.log_it("i","shahin","sign: " + my_sign);

                boolean my_sign_verify = net.mixoftix.tallybox.MainActivity.sign_verify(
                        my_order,
                        my_sign,
                        MainActivity.publicKey_x_HEX,
                        MainActivity.publicKey_y_HEX
                );

                Access_log.log_it("i","shahin","my_sign_verify: " + my_sign_verify);

                String publicKey_xy_compressed = crypto_asym_keys_compress.PublicKeyCompression(
                        MainActivity.publicKey_x_HEX,
                        MainActivity.publicKey_y_HEX,
                        "B58"
                );

                Access_log.log_it("i","shahin","publicKey_xy_compressed: " + publicKey_xy_compressed);

                String result = "tallybox~parcel_of_transaction" +
                        "~graph_from~" + str_graph_domain_from +
                        "~graph_to~" + str_graph_domain_to +
                        "~wallet_from~" + str_wallet_from +
                        "~wallet_to~" + str_wallet_to +
                        "~order_currency~" + str_currency +
                        "~order_amount~" + str_amount +
                        "~order_id~" + str_order_id +
                        "~order_utc_unix~" + str_order_utc_unix +
                        "~the_sign~" + my_sign +
                        "~publicKey_xy_compressed~" + publicKey_xy_compressed
                        ;

                Access_log.log_it("i","shahin","broadcast_result: " + result);

                //encodeToQrCode(wallet_address,300,300);
                Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(result,512,512);
                if (qrCodeBitmap != null) {
                    is_sign_broadcatable = true;

                    ImageView_offline_qr.setVisibility(View.VISIBLE);
                    textview_offline_Send.setVisibility(View.VISIBLE);
                    textview_offline_url.setVisibility(View.VISIBLE);

                    ImageView_offline_qr.setImageBitmap(qrCodeBitmap);
                    textview_offline_Send.setText(result);
                    String the_offline_url = MainActivity.spinner_options_value[getGraphIndex(str_graph_domain_from)];

                    String offline_url_guide = getString(R.string.offline_url_guide);
                    textview_offline_url.setText(HtmlCompat.fromHtml(
                            offline_url_guide + ": <br>" +
                                    "<a href='" +
                                    MainActivity.setting_network_protocol +
                                    "://" +
                                    the_offline_url + "' target=_blank>" +
                                    the_offline_url +
                                    "</a>",
                                    HtmlCompat.FROM_HTML_MODE_LEGACY));
                    textview_offline_url.setMovementMethod(LinkMovementMethod.getInstance());

                }

                //endregion

            }
        });
        // Set a click listener for the broadcast button
        buttonBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // tallybox~
                // graph_from~tallybox.mixoftix.net~
                // graph_to~tallybox.mixoftix.net~
                // wallet_from~boxB2bbc15c8c135W8PzPEZf98cEu2h2muhkeJQS3MwTYUaTHVkFTgihcS7~
                // wallet_to~boxB2bbc15c8c135W8PzPEZf98cEu2h2muhkeJQS3MwTYUaTHVkFTgihcS7~
                // order_currency~2ZR~
                // order_amount~3500.00000000~
                // order_id~778844~
                // order_utc_unix~1741675583~
                // the_sign~MEYCIQCxzNKhOUXijLr+z2mI9npu/+KZijiEv3//W7Ya3VpvzgIhAI1m7wJLJ9ldP2m5jmYfUreuvoKTjoZmFQmt5e6foakp~
                // publicKey_xy_compressed~2C7SVvEj45VMWwbd8UQNoYYWMeCMeyKm6qfDNQXhHkKK*1

                if (buttonBroadcast.isEnabled())
                {
                    buttonBroadcast.setEnabled(false);

                    // set progressbar
                    progressbar_stat = true;
                    doStartProgressBar2();

                    // config internet connection
                    String server_url_query =
                            "app_name=" + URLEncoder.encode(MainActivity.app_name)
                                    + "&app_version=" + URLEncoder.encode(MainActivity.app_version)
                                    + "&order_csv=" + URLEncoder.encode(textview_offline_Send.getText().toString().replace("\n","").replace("\r",""))
                            ;

                    String result = MainActivity.browse_url_POST(
                                                          MainActivity.server_url +
                                                                 "dmz.asmx/order_accept", server_url_query);

                    Access_log.log_it("i","shahin","dmz.asmx/order_accept" + " - result: " + result);

                    textview_broadcast_report.setVisibility(View.VISIBLE);

                    if (result.startsWith("pending~200~"))
                    {
                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#32CD32'>" + result + "</font>"
                                ,HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                    else
                    {
                        textview_broadcast_report.setText(HtmlCompat.fromHtml(
                                "<font color='#FF4500'>" + result + "</font>"
                                ,HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }

                    // reset buttom
                    is_sign_broadcatable = false;
                    // reset progressbar
                    progressbar_stat = false;
                }
                else
                {
                    Toast.makeText(MainActivity_Send.this, "broadcasting is in progress..", Toast.LENGTH_SHORT).show();
                }

            }
        });
        // Set a click listener for the raw-transaction
        textview_offline_Send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                copy_to_clipboard(textview_offline_Send.getText().toString());
                Toast.makeText(MainActivity_Send.this, "copied..", Toast.LENGTH_SHORT).show();
            }
        });
        // Set a selected listener for graph_in_spinner
        dropdownSpinner_GraphDomainTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                onGraphDomainChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //endregion

        //region activity_intent

        String Scanner_Result = getIntent().getStringExtra("Scanner_Result");
        Access_log.log_it("i","shahin","intent --> Scanner_Result: " + Scanner_Result);

        if (Scanner_Result != null)
        {
            parcel_processor(Scanner_Result);
        }

        //endregion

        //textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + ")");
        updateGraphFromDisplay();

        //region activity_initialization

        //editTextGraphDomainTo.setText(MainActivity.graph_domain_in);
        // Find the index of the target domain
        int targetIndex = -1;
        for (int i = 0; i < spinner_options.length; i++) {
            if (spinner_options[i].equals(MainActivity.graph_domain_in)) {
                targetIndex = i;
                break;
            }
        }
        // Set the selection if found
        if (targetIndex != -1) {
            dropdownSpinner_GraphDomainTo.setSelection(targetIndex);
        }

        //editTextWalletTo.requestFocus();

        //endregion

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

    //region functions_of_menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send, menu);
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

        if (id == R.id.action_scanner) {

            Intent i = new Intent(getApplicationContext(), MainActivity_Send_Scanner.class);
            //finishAffinity();
            startActivity(i);

            return true;
        }

        if (id == R.id.action_paste) {

            parcel_processor(paste_from_clipboard());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region functions_of_parcel_processor

    private void parcel_processor(String tally_parcel)
    {
        is_parcel_processing = true;

        // reset buttom
        is_sign_broadcatable = false;
        buttonBroadcast.setEnabled(false);

        textview_broadcast_report.setVisibility(View.GONE);
        ImageView_offline_qr.setVisibility(View.GONE);
        textview_offline_Send.setVisibility(View.GONE);
        textview_offline_url.setVisibility(View.GONE);

        if (tally_parcel.startsWith("box")) {
            editTextWalletTo.setText(tally_parcel);

            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    //popupWindow.show();
                    //Access_log.log_it("i", "shahin", "parcel_processor: popupWindow shown successfully");
                    //editTextAmount.requestFocus();
                    popupWindow.show();

                    Access_log.log_it("i", "shahin", "parcel_processor: amount focused successfully");
                } catch (Exception e) {
                    Access_log.log_it("e", "shahin", "parcel_processor: Error showing popupWindow: " + e.getMessage());
                }
            });
        }
        else if (tally_parcel.startsWith("tallybox"))
        {
            // tallybox~wallet_to~boxBdaf153aec2cCec2n7sBggXVuVBgVKv83oC5HxHk4EFq7cgNyDraT17b
            // ~order_id~9744111~order_amount~32000~order_currency~2ZR

            // split publicKey_xy
            String[] split_tally_parcel;
            split_tally_parcel = tally_parcel.split("~");
            String graph_to = "";
            String wallet_to = "";
            String order_id = "";
            String order_amount = "";
            String order_currency = "";

            for (int i = 0; i < split_tally_parcel.length; i++) {

                if (split_tally_parcel[i].equals("graph_to"))
                {
                    graph_to = split_tally_parcel[i + 1];
                }
                if (split_tally_parcel[i].equals("wallet_to"))
                {
                    wallet_to = split_tally_parcel[i + 1];
                }
                if (split_tally_parcel[i].equals("order_currency"))
                {
                    order_currency = split_tally_parcel[i + 1];
                }
                if (split_tally_parcel[i].equals("order_amount"))
                {
                    order_amount = split_tally_parcel[i + 1];
                }
                if (split_tally_parcel[i].equals("order_id"))
                {
                    order_id = split_tally_parcel[i + 1];
                }
            }

            //editTextGraphDomainTo.setText(graph_domain_to);
            // Find the index of the target domain

            Access_log.log_it("i", "shahin", "is_parcel_processing(before): " + is_parcel_processing);
            int targetIndex = -1;
            for (int i = 0; i < spinner_options.length; i++) {
                if (spinner_options[i].equals(graph_to)) {
                    targetIndex = i;
                    break;
                }
            }
            // Set the selection if found
            if (targetIndex != -1) {
                dropdownSpinner_GraphDomainTo.setSelection(targetIndex);
            }
            Access_log.log_it("i", "shahin", "is_parcel_processing(after): " + is_parcel_processing);

            editTextWalletTo.setText(wallet_to);
            editTextOrder.setText(order_id);
            editTextAmount.setText(order_amount);

            // get selected radio button from radioGroup
            Access_log.log_it("i","shahin","order_currency: " + order_currency);

            //int selectedId = getResources().getIdentifier("currency_" + order_currency.toLowerCase(), "id", this.getPackageName());
            //radioCurrency = (RadioButton) findViewById(selectedId);
            //radioCurrency.setChecked(true);

            int drawableResId = MainActivity.getIconForToken(order_currency);
            dropdownText.setText(order_currency);
            dropdownIcon.setImageResource(drawableResId);
            //popupWindow.dismiss();

            buttonSign.requestFocus();
        }
        else
        {
            //editTextGraphDomainTo.setText(MainActivity.graph_domain_in);
            // Find the index of the target domain
            int targetIndex = -1;
            for (int i = 0; i < spinner_options.length; i++) {
                if (spinner_options[i].equals(MainActivity.graph_domain_in)) {
                    targetIndex = i;
                    break;
                }
            }
            // Set the selection if found
            if (targetIndex != -1) {
                dropdownSpinner_GraphDomainTo.setSelection(targetIndex);
            }

            editTextWalletTo.setText("");
            editTextAmount.setText("");
            editTextOrder.setText("");

            is_parcel_processing = false;        // Manually trigger
            onGraphDomainChanged(targetIndex);   // Manually trigger

            Toast.makeText(MainActivity_Send.this, "invalid tally-parcel..", Toast.LENGTH_SHORT).show();
        }

        //is_parcel_processing = false;
    }
    private void copy_to_clipboard(String text)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
    private String paste_from_clipboard()
    {
        final ClipboardManager clipBoard= (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        String clipboard_str = "";

        if (clipBoard.hasPrimaryClip())
        {
            //clipboard_str = clipBoard.getText().toString();
            clipboard_str = clipBoard.getPrimaryClip().getItemAt(0).getText().toString();
        }

        return clipboard_str;
    }
    private long getUnixTimestampSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
    }

    //endregion

    //region function_of_graph_in

    // Show Graph From + Zones
    private void updateGraphFromDisplay() {

        String history_in_graph = getString(R.string.history_in_graph);
        int index = getGraphIndex(MainActivity.graph_domain_in);

        String zones = (index != -1) ? getZoneForGraph(index) : " [no zone]";

        String zonesText = (zones.length() > 0)
                ? " [" + String.join(", ", zones) + "]"
                : " [no zone]";

        //textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + zonesText + ")");
        textview_graph_in.setText(HtmlCompat.fromHtml(
                "(" + history_in_graph + ": <b>" + MainActivity.graph_domain_in + "</b>" +
                        "<font color='cyan'>" + zonesText + "</font>)",
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

    //region functions_of_graph_zone

    // Check if two graphs have the SAME zone
    private boolean haveSameZone(int fromIndex, int toIndex) {
        String zoneFrom = getZoneForGraph(fromIndex);
        String zoneTo   = getZoneForGraph(toIndex);

        if (zoneFrom.isEmpty() || zoneTo.isEmpty()) {
            return fromIndex == toIndex;   // fallback: only same graph
        }

        return zoneFrom.equals(zoneTo);
    }
    // Custom Adapter to show domain + zones using your existing layout
    private class GraphSpinnerAdapter extends ArrayAdapter<String> {

        public GraphSpinnerAdapter(Context context, String[] domains) {
            super(context, 0, domains);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        private View createCustomView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.items_activity_spinner, parent, false);
            }

            TextView textView = view.findViewById(R.id.spinnerText);

            String domain = MainActivity.spinner_options[position];
            String zones = getZoneForGraph(position);

            String zonesText = (zones.length() > 0)
                    ? " [" + String.join(", ", zones) + "]"
                    : " [no zone]";

            textView.setText(domain + zonesText);

            return view;
        }
    }

    //endregion

    //region functions_of_graph_token

    private void onGraphDomainChanged(int position) {
        if (position < 0 || position >= MainActivity.spinner_options.length) return;

        String selectedServer1 = MainActivity.graph_domain_in;   // Graph From
        int fromIndex = getGraphIndex(selectedServer1);
        int toIndex   = position;

        // === Zone Validation (Same Zone Only) ===
        if (!haveSameZone(fromIndex, toIndex)) {
            Toast.makeText(this,
                    "Transfer not allowed: Different zones",
                    Toast.LENGTH_LONG).show();

        }

        updateCommonTokensDropdown(selectedServer1, MainActivity.spinner_options[position]);

        if (!is_parcel_processing) {
            // popupWindow.show();
            // do nothing
        } else {
            is_parcel_processing = false;
        }
    }
    private boolean isValidSelectedCurrency(String selectedCurrency) {
        if (selectedCurrency == null || selectedCurrency.equals("No common tokens") || selectedCurrency.trim().isEmpty()) {
            return false;
        }

        String cleanCurrency = selectedCurrency.trim().toUpperCase();

        // Check if it exists in our current dropdown items
        for (DropdownItem item : currentDropdownItems) {
            if (item.getText().equalsIgnoreCase(cleanCurrency)) {
                return true;
            }
        }
        return false;
    }
    private void updateCommonTokensDropdown(String selectedServer1, String selectedServer2) {
        // Get the two selected servers
        // String selectedServer1 = MainActivity.graph_domain_in;
        // String selectedServer2 = MainActivity.graph_domain_in;   // Change this later when you have two different servers

        // Get dynamic items based on common tokens
        List<DropdownItem> items = getCommonTokensDropdownItems(selectedServer1, selectedServer2);

        // Store reference for later use (e.g. in onItemClickListener)
        this.currentDropdownItems = items;

        // If popupWindow is not initialized yet, set it up
        if (popupWindow == null) {
            setupPopupWindow(items);
        } else {
            // Just update the adapter with new items
            popupWindow.setAdapter(new DropdownAdapter(this, items));
        }

        // Optional: Reset to first item if available
        if (!items.isEmpty()) {
            DropdownItem firstItem = items.get(0);
            dropdownText.setText(firstItem.getText());
            dropdownIcon.setImageResource(firstItem.getImageResId());
        } else {
            // Fallback UI
            dropdownText.setText("No common tokens");
            dropdownIcon.setImageResource(R.drawable.baseline_fingerprint_24);
        }
    }
    private void setupPopupWindow(List<DropdownItem> items) {
        DropdownAdapter popupAdapter = new DropdownAdapter(this, items);

        LinearLayout dropdownContainer = findViewById(R.id.dropdown_container);
        dropdownIcon = findViewById(R.id.dropdown_icon);
        dropdownText = findViewById(R.id.dropdown_text);

        popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(popupAdapter);
        popupWindow.setAnchorView(dropdownContainer);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setModal(true);

        // Show popup when container is clicked
        dropdownContainer.setOnClickListener(v -> {
            if (popupWindow != null) {
                popupWindow.show();
            }
        });

        // Handle item selection
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            if (position < currentDropdownItems.size()) {
                DropdownItem selectedItem = currentDropdownItems.get(position);

                dropdownText.setText(selectedItem.getText());
                dropdownIcon.setImageResource(selectedItem.getImageResId());

                Access_log.log_it("i", "shahin",
                        "item_clicked: " + selectedItem.getText() + " - " + selectedItem.getImageResId());

                popupWindow.dismiss();
                //editTextWalletTo.requestFocus();
                editTextAmount.requestFocus();

            }
        });
    }
    private List<DropdownItem> getCommonTokensDropdownItems(String server1, String server2) {
        List<DropdownItem> items = new ArrayList<>();

        Access_log.log_it("w","shahin","server1: " + server1);
        Access_log.log_it("w","shahin","server2: " + server2);

        String commonTokens = MainActivity.getCommonTokens(server1, server2);
        Access_log.log_it("w","shahin","commonTokens: " + commonTokens);

        if (commonTokens.isEmpty()) {
            // Fallback when no common tokens
            items.add(new DropdownItem(R.drawable.baseline_fingerprint_24, "No common tokens"));
            return items;
        }

        String[] tokens = commonTokens.split(",");

        for (String token : tokens) {
            String cleanToken = token.trim().toUpperCase();
            Access_log.log_it("w","shahin","cleanToken: " + cleanToken);

            int iconRes = MainActivity.getIconForToken(cleanToken);
            items.add(new DropdownItem(iconRes, cleanToken));
        }

        return items;
    }
    public class DropdownAdapter extends ArrayAdapter<DropdownItem> {
        public DropdownAdapter(Context context, List<DropdownItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.items_activity_send, parent, false
                );
            }
            ImageView icon = convertView.findViewById(R.id.item_image);
            TextView text = convertView.findViewById(R.id.item_text);

            DropdownItem item = getItem(position);
            if (item != null) {
                icon.setImageResource(item.getImageResId());
                text.setText(item.getText());
            }
            return convertView;
        }
    }
    private class DropdownItem {
        private int imageResId;
        private String text;

        public DropdownItem(int imageResId, String text) {
            this.imageResId = imageResId;
            this.text = text;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String getText() {
            return text;
        }

        /*
        @Override
        public String toString() {
            return text; // Return the text field for display
        }
        */

    }

    //endregion

}


