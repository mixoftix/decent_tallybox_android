package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.mixoftix.tallybox.databinding.ActivityMainReceiveBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_Receive extends AppCompatActivity {

    private TextView textview_graph_in,textview_wallet;
    private EditText editTextAmount,editTextOrder;
    private Button buttonGenerateQR;
    private CheckBox checkbox_is_pos;

    //private RadioButton radioCurrency;
    //private RadioGroup RadioGroupCurrency;

    private ImageView dropdownIcon;
    private TextView dropdownText;
    private ListPopupWindow popupWindow;


    private static ActivityMainReceiveBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_receive);

        binding = ActivityMainReceiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHome);

        textview_graph_in = findViewById(R.id.textview_graph_in);
        textview_wallet = findViewById(R.id.textview_wallet);
        //RadioGroupCurrency = findViewById(R.id.RadioGroupCurrency);
        editTextOrder = findViewById(R.id.editTextOrder);
        editTextAmount = findViewById(R.id.editTextAmount);
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR);

        checkbox_is_pos = findViewById(R.id.checkbox_is_pos);
        LinearLayout layout_of_pos = (LinearLayout) findViewById(R.id.layout_of_pos);

        textview_wallet.setText(net.mixoftix.tallybox.MainActivity.wallet_address);
        textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + ")");

        // set connection protocol in the first run
        String setting_is_pos = Access_file.access_file_func_read(getApplicationContext(), "setting_is_pos");
        Access_log.log_it("i","shahin","setting_is_pos: " + setting_is_pos);

        if (setting_is_pos.toLowerCase().equals("true"))
        {
            checkbox_is_pos.setChecked(true);
            layout_of_pos.setVisibility(View.VISIBLE);
        }
        else
        {
            checkbox_is_pos.setChecked(false);
            layout_of_pos.setVisibility(View.GONE);
        }


        //region drop_menu

        List<DropdownItem> items = new ArrayList<>();
        items.add(new DropdownItem(R.drawable.baseline_fingerprint_24, "null"));
        items.add(new DropdownItem(R.mipmap.coin_2pn, "2PN"));
        items.add(new DropdownItem(R.mipmap.coin_2zr, "2ZR"));
        items.add(new DropdownItem(R.mipmap.coin_tlh, "TLH"));

        LinearLayout dropdownContainer = findViewById(R.id.dropdown_container);
        dropdownIcon = findViewById(R.id.dropdown_icon);
        dropdownText = findViewById(R.id.dropdown_text);

        // Set up the ListPopupWindow
        popupWindow = new ListPopupWindow(this);
        DropdownAdapter popupAdapter = new DropdownAdapter(this, items);
        popupWindow.setAdapter(popupAdapter);
        popupWindow.setAnchorView(dropdownContainer);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT); // Adjust as needed
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setModal(true);

        // Show popup on click
        dropdownContainer.setOnClickListener(v -> popupWindow.show());

        // Handle item selection
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            DropdownItem selectedItem = items.get(position);
            dropdownText.setText(selectedItem.getText());
            dropdownIcon.setImageResource(selectedItem.getImageResId());
            Access_log.log_it("i", "shahin", "item_clicked: " + selectedItem.getText() + " - " + selectedItem.getImageResId());
            popupWindow.dismiss();
            editTextAmount.requestFocus();
        });

        //endregion


        // Set a click listener for the wallet address
        textview_wallet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                copy_to_clipboard(net.mixoftix.tallybox.MainActivity.wallet_address);
                Toast.makeText(MainActivity_Receive.this, "Copied..", Toast.LENGTH_SHORT).show();
            }
        });

        // Set a click listener for the pos
        checkbox_is_pos.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String str_is_pos = String.valueOf(checkbox_is_pos.isChecked());
                Access_log.log_it("i","shahin","setting str_is_pos: " + str_is_pos);
                Access_file.access_file_func_write(getApplicationContext(), "setting_is_pos", str_is_pos, "write");

                if (str_is_pos.toLowerCase().equals("true"))
                {
                    layout_of_pos.setVisibility(View.VISIBLE);
                }
                else
                {
                    layout_of_pos.setVisibility(View.GONE);
                }

            }
        });

        // Set a click listener for the wallet address
        buttonGenerateQR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String the_qr_result = MainActivity.wallet_address;

                // get selected radio button from radioGroup
                //int selectedId = RadioGroupCurrency.getCheckedRadioButtonId();
                //radioCurrency = (RadioButton) findViewById(selectedId);
                //String str_currency = (String) radioCurrency.getText();
                String str_currency = (String) dropdownText.getText();

                String str_order = editTextOrder.getText().toString();
                String str_amount = editTextAmount.getText().toString();
                String the_qr_xml = "tallybox~parcel_of_pos" +
                        "~graph_domain_to~" + MainActivity.graph_domain_in +
                        "~wallet_to~" + MainActivity.wallet_address +
                        "~order_id~" + str_order +
                        "~order_amount~" + str_amount +
                        "~order_currency~" + str_currency
                        ;

                if (!str_currency.equals("null"))
                {
                    the_qr_result = the_qr_xml;
                }

                Intent i = new Intent(getApplicationContext(),MainActivity_Receive_qr.class);
                i.putExtra("the_qr_result", the_qr_result);
                //finishAffinity();
                startActivity(i);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receive, menu);
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

    private void copy_to_clipboard(String text)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
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

}