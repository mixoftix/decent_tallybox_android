package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.zip.Inflater;

import net.mixoftix.tallybox.databinding.ActivityMainReceiveQrBinding;


public class MainActivity_Receive_qr extends AppCompatActivity {

    private static ImageView ImageView_wallet_qr;
    private TextView textview_result_receive;

    private static ActivityMainReceiveQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_receive_qr);

        binding = ActivityMainReceiveQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHomeqr);

        ImageView_wallet_qr = findViewById(R.id.ImageView_private_key);
        textview_result_receive = findViewById(R.id.textview_result_receive);

        String the_qr_result = getIntent().getStringExtra("the_qr_result");

        //encodeToQrCode(wallet_address,300,300);
        Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(the_qr_result,512,512);
        if (qrCodeBitmap != null) {
            ImageView_wallet_qr.setImageBitmap(qrCodeBitmap);
            textview_result_receive.setText(the_qr_result);
        }

        // Set a click listener for the wallet address
        textview_result_receive.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                copy_to_clipboard(the_qr_result);
                Toast.makeText(MainActivity_Receive_qr.this, "Copied..", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //region functions_of_menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receive_qr, menu);
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

        if (id == R.id.action_back) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    private void copy_to_clipboard(String text)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity_Receive.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
    }

}