package net.mixoftix.tallybox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import net.mixoftix.tallybox.databinding.ActivityMainSendScannerBinding;
import java.util.List;
import android.Manifest;

public class MainActivity_Send_Scanner extends BaseActivity {

    private DecoratedBarcodeView barcodeView;
    private static ActivityMainSendScannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_send);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_send_scanner);
        setContentView(R.layout.activity_main_send_scanner);

        binding = ActivityMainSendScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSend);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        barcodeView = findViewById(R.id.barcode_scanner_view);
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Get the decoded text from the QR code
                String Scanner_Result = result.getText();
                Access_log.log_it("d","shahin","Scanned: " + Scanner_Result);

                // Handle the scanned result here
                //Toast.makeText(MainActivity_Send_Scanner.this, "Scanned: " + Scanner_Result, Toast.LENGTH_LONG).show();

                DoBackScanner(Scanner_Result);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Optional: Handle possible result points
                Access_log.log_it("d","shahin","Possible points: " + resultPoints.size());
            }
        });

        //barcodeView = findViewById(R.id.barcode_scanner_view);
        /*
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    // Get the decoded text from the QR code
                    String Scanner_Result = result.getText();
                    // Do something with the decoded text

                    Access_log.log_it("i","shahin","Decoded text: " + Scanner_Result);

                    Intent i = new Intent(getApplicationContext(),MainActivity_Send.class);
                    i.putExtra("Scanner_Result", Scanner_Result);
                    finishAffinity();
                    startActivity(i);
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Handle possible result points if needed
            }
        });
        */
    }

    //region functions_of_scanner

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume(); // Start the camera preview
        //Log.d("shahin", "scanner resumed");
        Access_log.log_it("d","shahin","scanner resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause(); // Stop the camera preview
        Access_log.log_it("d","shahin","scanner paused");
    }

    //endregion

    //region functions_of_menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_scanner, menu);
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

        if (id == R.id.action_back) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity_Send.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
    }

    public void DoBackScanner(String Scanner_Result) {
        Intent intent = new Intent(this, MainActivity_Send.class);
        intent.putExtra("Scanner_Result", Scanner_Result);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
    }

}