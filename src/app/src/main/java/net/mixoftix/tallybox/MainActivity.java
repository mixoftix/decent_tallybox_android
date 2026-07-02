package net.mixoftix.tallybox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import net.mixoftix.tallybox.databinding.ActivityMainBinding;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/*
Decent TallyBox (Android Wallet)
Version: 2.935 (MVP)
Updated: 2026-06-20

This is Tallybox wallet (https://wallet.mixoftix.net) for secure management of
tokens on a DAG network. It both generates and recovers a private-public key pair of ecc secp256r1,
derives a wallet address, AES-256-CBC encryption for private keys, RFC 6979-compliant ECDSA signatures,
and offline transaction signing. Features include RFC 6979-compliant ECDSA signatures.

Licensed under the GNU General Public License v3 (GPL-3), this software is open-source, ensuring
freedom to use, modify, and distribute. Derivative works must also be open-source under GPL-3,
and source code must be provided with distributions.

MixofTix Was Here!
by shahiN Noursalehi
*/

public class MainActivity extends BaseActivity {

    //region define_variables

    //region constants

    public static final boolean log_is_enable = true;
    public static final String app_name = "tallybox";
    public static final String app_version = "2.95";
    public static final String file_name_path = "net_mixoftix_tallybox";
    public static final String[] spinner_options = {
            "gpp_mars.mixoftix.net",
            "gpp_venus.mixoftix.net",
            "gpp_pluto.mixoftix.net"
    };
    // Decent GPP - Live Demo
    public static final String[] spinner_options_value = {
            "gpp_mars_ws.mixoftix.net",
            "gpp_venus_ws.mixoftix.net",
            "gpp_pluto_ws.mixoftix.net"
    };
    public static final String[] spinner_options_tokens = {
            "2ZR",
            "",
            ""
    };
    public static final String[] spinner_options_zones = {
            "Mars",      // index 0 - gpp_mars
            "Venus",     // index 1 - gpp_venus
            "Pluto"      // index 2 - gpp_pluto
    };

    // Decent GPP - Research Lab
    /*
    public static final String[] spinner_options_value = {
            "192.168.88.111:701",
            "192.168.88.111:711",
            "192.168.88.111:721"
    };
    public static final String[] spinner_options_tokens = {
            "USD,TLH,IRR",
            "2ZR",
            "USD,2ZR"
    };
    */

    //endregion

    //region variables_of_spinners

    public static String[] spinner_options_crypto_list = new String[3];
    public static String[] spinner_options_pqc_serial = new String[3];
    public static String[] spinner_options_pqc_pk = new String[3];

    //endregion

    //region variables_of_runtime

    // variables - PQC
    public static String app_pqc_serial ="";
    public static String app_pqc_pk = "";
    public static String app_pqc_psk ="";
    public static String app_pqc_psk_cipher ="";

    // variables - settings
    public static String setting_network_protocol = "";
    public static String setting_safeguard_pqc = "";
    public static String setting_graph_domain_in = "";

    // variables - graphs
    public static String graph_domain_in = ""; // ""gpp_mars.mixoftix.net";
    public static String graph_address_in = ""; //  "192.168.88.111:701";

    // variables - connection
    public static String server_url = "";

    //endregion

    //region variables_of_cryptography

    public static String wallet_address;
    public static String my_local_key_in_KeyStore = null;
    public static String aes_of_privateKey_d_b58 = null;
    public static String publicKey_x_HEX = "";
    public static String publicKey_y_HEX = "";

    //endregion

    //region variables_of_layouts

    private StringHelper stringHelper;
    private Button buttonSend, buttonReceive;
    private TextView textview_main_advertise, textview_main_whatsup,
                     textview_balance_wallet;
    private LinearLayout layout_main_whatsup, layout_main_advertise, layout_main_test;
    private Spinner dropdownSpinner;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<ItemData> dataList;
    private SwipeRefreshLayout swipeContainer;
    private static ActivityMainBinding binding;

    //endregion

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
        //String message_sign_order = sign_order(message, message_prvkey);
        String message_sign_order = "MEYCIQDyEdof5lKy7cwu4+IOGLB74UMOTHdjSCsn6RRpf96b9QIhAIy/x7CTN+KaXSQzZNyACG+hQl/+/mq0FDSH0QMkdZLE";

        Log.w("shahin","message_sign_order: " + message_sign_order);
        boolean message_sign_verify = net.mixoftix.tallybox.MainActivity.sign_verify(
                message,
                message_sign_order,
                MainActivity.publicKey_x_HEX,
                MainActivity.publicKey_y_HEX
        );

        Log.i("shahin","message_sign_verify: " + message_sign_verify);
        */

        //region activity_initialization

        //region define_views

        // Force light mode programmatically
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Initialize UI elements
        layout_main_whatsup = findViewById(R.id.layout_main_whatsup);
        layout_main_advertise = findViewById(R.id.layout_main_advertise);
        //layout_main_test = findViewById(R.id.layout_main_test);

        buttonReceive = findViewById(R.id.buttonReceive);
        buttonSend = findViewById(R.id.buttonSend);

        textview_main_whatsup = findViewById(R.id.textview_main_whatsup);
        textview_main_advertise = findViewById(R.id.textview_main_advertise);

        textview_balance_wallet  = findViewById(R.id.textview_balance_wallet);
        //textview_main_test  = findViewById(R.id.textview_main_test);

        // BGN: spinner of graph_in

        dropdownSpinner = findViewById(R.id.dropdownSpinner);
        // String[] spinner_options = {"tehran.dag.tejaratbank.ir", "tabriz.dag.tejaratbank.ir", "shiraz.dag.tejaratbank.ir"};

        GraphSpinnerAdapter adapter = new GraphSpinnerAdapter(this, spinner_options);
        dropdownSpinner.setAdapter(adapter);

        // END: spinner of graph_in

        //endregion

        stringHelper = new StringHelper(this);

        //region keypair_variables
        PrivateKey privateKey = null;
        PublicKey publicKey = null;

        BigInteger publicKey_x_bigInt = BigInteger.ZERO;
        BigInteger publicKey_y_bigInt = BigInteger.ZERO;

        String publicKey_xy = "";
        String publicKey_x_b58 = "";
        String publicKey_y_b58 = "";
        //endregion

        //region local_privacy_sha256

        String local_privacy_sha256 = Access_file.access_file_func_read(getApplicationContext(), "local_privacy_sha256");
        Access_log.log_it("i","shahin","111 - local_privacy_sha256: " + local_privacy_sha256);

        if (local_privacy_sha256.equals("-"))
        {
            Intent i = new Intent(getApplicationContext(),MainActivity_Wallet_config.class);
            finishAffinity();
            startActivity(i);
            return;
        }

        //endregion

        //region access_history_privatekey

        String access_history_privatekey = Access_file.access_file_func_read(getApplicationContext(), "access_history_privatekey");
        Access_log.log_it("i","shahin","111 - access_history_privatekey: " + access_history_privatekey);

        if (access_history_privatekey.equals("-"))
        {
            Intent i = new Intent(getApplicationContext(),MainActivity_PrivateKey.class);
            //finishAffinity();
            startActivity(i);
            //return;
        }

        //endregion

        //region keypair_generate

        // creates the keys in the first run
        aes_of_privateKey_d_b58 = Access_file.access_file_func_read(getApplicationContext(), "aes_of_privateKey_d_b58");
        Access_log.log_it("i","shahin","111 - aes_of_privateKey_d_b58: " + aes_of_privateKey_d_b58);

        boolean result_of_wallet_config = false;

        if (aes_of_privateKey_d_b58.equals("-"))
        {
            try {
                String privatekey_restore = getIntent().getStringExtra("privatekey_restore");
                Access_log.log_it("i","shahin","privatekey_restore: " + privatekey_restore);
                result_of_wallet_config = wallet_config(privatekey_restore);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //endregion

        //region publickey_load

        publicKey_x_b58 = Access_file.access_file_func_read(getApplicationContext(), "publicKey_x_b58");
        publicKey_y_b58 = Access_file.access_file_func_read(getApplicationContext(), "publicKey_y_b58");

        Access_log.log_it("w","shahin","publicKey_x_b58 - retrieved: " + publicKey_x_b58);
        Access_log.log_it("w","shahin","publicKey_y_b58 - retrieved: " + publicKey_y_b58);

        // convert b58 to bigint
        publicKey_x_bigInt = Base58.decode(publicKey_x_b58);
        publicKey_y_bigInt = Base58.decode(publicKey_y_b58);

        // convert bigint to hex
        publicKey_x_HEX = publicKey_x_bigInt.toString(16);
        publicKey_y_HEX = publicKey_y_bigInt.toString(16);
        publicKey_xy = publicKey_x_HEX + "~" + publicKey_y_HEX;

        Access_log.log_it("i","shahin","xy (reloaded): " + publicKey_xy);

        //endregion

        //region wallet_load

        wallet_address = wallet_address(publicKey_x_HEX, publicKey_y_HEX);
        Access_log.log_it("i","shahin","wallet_address: " + wallet_address);

        my_local_key_in_KeyStore = Access_file.access_file_func_read(getApplicationContext(), "my_local_key_in_KeyStore");
        Access_log.log_it("w","shahin","my_local_key_in_KeyStore - retrieved: " + my_local_key_in_KeyStore);
        aes_of_privateKey_d_b58 = Access_file.access_file_func_read(getApplicationContext(), "aes_of_privateKey_d_b58");
        Access_log.log_it("w","shahin","aes_of_privateKey_d_b58 - retrieved: " + aes_of_privateKey_d_b58);

        //endregion

        //endregion

        //region settings

        // set connection protocol in the first run
        setting_network_protocol = Access_file.access_file_func_read(getApplicationContext(), "setting_network_protocol");
        Access_log.log_it("i","shahin","333 - setting_network_protocol: " + setting_network_protocol);

        if (setting_network_protocol.equals("-"))
        {
            Access_file.access_file_func_write(getApplicationContext(), "setting_network_protocol", "http", "write");
            setting_network_protocol = Access_file.access_file_func_read(getApplicationContext(), "setting_network_protocol");
        }

        // set PQC protocol in the first run
        setting_safeguard_pqc = Access_file.access_file_func_read(getApplicationContext(), "setting_safeguard_pqc");
        Access_log.log_it("i","shahin","333 - setting_safeguard_pqc: " + setting_safeguard_pqc);

        if (setting_safeguard_pqc.equals("-"))
        {
            Access_file.access_file_func_write(getApplicationContext(), "setting_safeguard_pqc", "disable", "write");
            setting_safeguard_pqc = Access_file.access_file_func_read(getApplicationContext(), "setting_safeguard_pqc");
        }

        // set the graph_domain_in
        setting_graph_domain_in = Access_file.access_file_func_read(getApplicationContext(), "setting_graph_domain_in");
        Access_log.log_it("i","shahin","111 - setting_graph_domain_in: " + setting_graph_domain_in);

        if (setting_graph_domain_in.equals("-"))
        {
            Access_file.access_file_func_write(getApplicationContext(), "setting_graph_domain_in", spinner_options[0], "write");
            setting_graph_domain_in = Access_file.access_file_func_read(getApplicationContext(), "setting_graph_domain_in");
            graph_domain_in = setting_graph_domain_in;

            Access_log.log_it("w","shahin","Graph_in_domain Setup: " + graph_domain_in);
        }
        else
        {
            graph_domain_in = setting_graph_domain_in;
            Access_log.log_it("w","shahin","Graph_in_domain Reloaded: " + graph_domain_in);
        }

        // set all PQC serial and pk including graph_address_in
        for (int i = 0; i < spinner_options.length; i++)
        {
            spinner_options_pqc_serial[i] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_serial_" + i);
            spinner_options_pqc_pk[i] = Access_file.access_file_func_read(getApplicationContext(), "app_pqc_pk_" + i);

            Access_log.log_it("i","shahin","333 - spinner_options_pqc_serial[" + i + "]: " + spinner_options_pqc_serial[i]);
            Access_log.log_it("i","shahin","333 - spinner_options_pqc_pk["+ i + "]: " + spinner_options_pqc_pk[i]);

            if (spinner_options[i].equals(graph_domain_in))
            {
                // reconfig all network settings
                setting_connection_values(i);
                setting_connection(setting_network_protocol);
            }
        }

        //endregion

        //region define_event_listener

        // Set a click listener for buttonReceive
        buttonReceive.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),MainActivity_Receive.class);
                //finishAffinity();
                startActivity(i);
            }
        });
        // Set a click listener for buttonSend
        buttonSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),MainActivity_Send.class);
                //finishAffinity();
                startActivity(i);
            }
        });
        // Set a selected listener for graph_in_spinner
        final Boolean[] spinner_isFirstSelection = {true};
        dropdownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = parent.getItemAtPosition(position).toString();

                // BGN: graph settings
                setting_graph_domain_in = Access_file.access_file_func_read(getApplicationContext(), "setting_graph_domain_in");
                Access_log.log_it("i","shahin","111 - spinner setting_graph_domain_in: " + setting_graph_domain_in);

                if (spinner_isFirstSelection[0])
                {
                    spinner_isFirstSelection[0] = false;

                    if (setting_graph_domain_in.equals("-"))
                    {
                        Access_file.access_file_func_write(getApplicationContext(), "setting_graph_domain_in", selectedItem, "write");
                        setting_graph_domain_in = Access_file.access_file_func_read(getApplicationContext(), "setting_graph_domain_in");
                        graph_domain_in = setting_graph_domain_in;

                        Access_log.log_it("w","shahin","111 - spinner set graph_domain_in: " + graph_domain_in);
                    }
                    else
                    {
                        graph_domain_in = setting_graph_domain_in;
                        Access_log.log_it("w","shahin","111 - spinner reload graph_domain_in: " + graph_domain_in);
                    }
                }
                else
                {
                    if (selectedItem.equals(setting_graph_domain_in))
                    {
                        Access_log.log_it("w","shahin","111 - spinner select graph_domain_in: " + graph_domain_in);
                    }
                    else
                    {
                        Access_file.access_file_func_write(getApplicationContext(), "setting_graph_domain_in", selectedItem, "write");
                        setting_graph_domain_in = Access_file.access_file_func_read(getApplicationContext(), "setting_graph_domain_in");
                        graph_domain_in = setting_graph_domain_in;

                        Access_log.log_it("w","shahin","111 - spinner re-wrote graph_domain_in: " + graph_domain_in);
                    }

                }

                // END: graph settings

                // Find the position of selection
                int matchedIndex = -1;
                for (int i = 0; i < spinner_options.length; i++) {
                    if (spinner_options[i].equals(graph_domain_in)) {
                        matchedIndex = i;
                        break;  // Stop at first match
                    }
                }

                // Find the position of selection parallel
                if (matchedIndex != -1)
                {
                    // reconfig all network settings
                    setting_connection_values(matchedIndex);
                    setting_connection(setting_network_protocol);

                    // redraw_views
                    crypto_list_label(graph_domain_in, spinner_options_crypto_list[matchedIndex]);
                    refresh_label(graph_domain_in, "");

                    // Set the selection
                    dropdownSpinner.setSelection(matchedIndex);
                }
                else
                {
                    graph_domain_in = "unknown!!";
                    graph_address_in = "127.0.0.1";
                    Access_log.log_it("w","shahin","Fatal Error graph_in_address: " + graph_domain_in);
                }

                Access_log.log_it("w","shahin","new spinner graph_in_domain: " + graph_domain_in);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //endregion

        //region SwipeRefresh

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize data
        dataList = new ArrayList<>();

        // Set up SwipeRefreshLayout
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Access_log.log_it("i", "shahin", "Swipe-to-refresh triggered!");
                refresh_general();
                //swipeContainer.setRefreshing(false);

            }
        });

        //endregion

        //region crypto_lists

        // set all crypto_lists
        for (int i = 0; i < spinner_options.length; i++)
        {
            spinner_options_crypto_list[i] = Access_file.access_file_func_read(getApplicationContext(), "crypto_list_last_" + i);
            Access_log.log_it("i","shahin","444 - spinner_options_crypto_list[" + i + "]: " + spinner_options_crypto_list[i]);

            if (spinner_options[i].equals(graph_domain_in))
            {
                // redraw_views
                crypto_list_label(graph_domain_in, spinner_options_crypto_list[i]);
                refresh_label(graph_domain_in, "");
            }
        }

        //endregion

    }

    //region functions_of_cryptography

    private boolean wallet_config(String privateKey_d_exists) throws Exception {

        //region define_variables
        String privateKey_d = "";
        String publicKey_xy = "";
        String publicKey_x = "";
        String publicKey_y = "";

        BigInteger privateKey_d_bigInt = BigInteger.ZERO;
        BigInteger publicKey_x_bigInt = BigInteger.ZERO;
        BigInteger publicKey_y_bigInt = BigInteger.ZERO;

        String privateKey_d_b58 = "";
        String publicKey_x_b58 = "";
        String publicKey_y_b58 = "";

        String my_local_key = null;
        String my_local_key_in_KeyStore = null;
        String aes_of_privateKey_d_b58 = null;
        //endregion

        if (privateKey_d_exists.isEmpty())
        {
            // key_pair generation
            KeyPair eccKeyPair = crypto_asym_pure_secp256r1.createKeyPair();
            Access_log.log_it("w","shahin","eccKeyPair: " + eccKeyPair);

            publicKey_xy = crypto_asym_pure_secp256r1.extract_xy_param(eccKeyPair);
            Access_log.log_it("i","shahin","xy (public key parameter): " + publicKey_xy);

            assert eccKeyPair != null;
            privateKey_d = crypto_asym_pure_secp256r1.extract_d_param(eccKeyPair);
            Access_log.log_it("i","shahin","d (private key parameter): " + privateKey_d);
        }
        else
        {
            // key_pair recovery
            privateKey_d = privateKey_d_exists;
            Access_log.log_it("w","shahin","wallet recovery by (d): " + privateKey_d);

            //PublicKey publicKey_recover = crypto_asym_keys_recovery.recover("secp256r1",privateKey_d);
            //ECPublicKey ecPublicKey = (ECPublicKey) publicKey_recover;
            //ECPoint ecPoint = ecPublicKey.getW();
            ECPoint ecPoint = crypto_asym_keys_recovery.recover("secp256r1",privateKey_d);
            BigInteger x = ecPoint.getAffineX();
            BigInteger y = ecPoint.getAffineY();

            publicKey_x = x.toString(16);
            publicKey_y = y.toString(16);
            publicKey_xy = publicKey_x + "~" + publicKey_y;
        }

        // keypair-qc goes here
        boolean return_of_wallet_config = false;
        if (qc_keypairs(privateKey_d,publicKey_xy))
        {
            return_of_wallet_config = true;
        }

        if (return_of_wallet_config)
        {
            //region save publicKey(xy)

            // split back the publicKey_xy
            String[] split_publicKey_xy;
            split_publicKey_xy = publicKey_xy.split("~");
            publicKey_x = split_publicKey_xy[0];
            publicKey_y = split_publicKey_xy[1];

            // convert into base58
            publicKey_x_bigInt = new BigInteger(publicKey_x, 16);
            publicKey_y_bigInt = new BigInteger(publicKey_y, 16);
            publicKey_x_b58 = Base58.encode(publicKey_x_bigInt);
            publicKey_y_b58 = Base58.encode(publicKey_y_bigInt);

            Access_log.log_it("i","shahin","x_b58 (public key parameter): " + publicKey_x_b58);
            Access_log.log_it("i","shahin","y_b58 (public key parameter): " + publicKey_y_b58);

            // save the keypair - b58
            Access_file.access_file_func_write(getApplicationContext(), "publicKey_x_b58", publicKey_x_b58, "write");
            Access_file.access_file_func_write(getApplicationContext(), "publicKey_y_b58", publicKey_y_b58, "write");

            //endregion

            //region save privateKey(d) by keystore

            privateKey_d_bigInt = new BigInteger(privateKey_d, 16);
            privateKey_d_b58 = Base58.encode(privateKey_d_bigInt);
            Access_log.log_it("i","shahin","d_b58 (private key parameter): " + privateKey_d_b58);

            // make my_local_key
            try {
                my_local_key = hash_functions.Hash_SHA_256(privateKey_d + "~" + publicKey_xy);
                Access_log.log_it("i","shahin","my_local_key - generated: " + my_local_key);

            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            // aes privatekey(d) by my_local_key
            try {
                aes_of_privateKey_d_b58 = crypto_symm_aes.aes_encrypt_b64(privateKey_d_b58, my_local_key);
                Access_file.access_file_func_write(getApplicationContext(), "aes_of_privateKey_d_b58", aes_of_privateKey_d_b58, "write");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // save my_local_key in KeyStore
            KeyPair rsaKyePair_store = crypto_asym_keys_store.createKeyPair();
            Access_log.log_it("i","shahin","rsaKyePair_store: " + rsaKyePair_store);

            my_local_key_in_KeyStore = crypto_asym_keys_store.encriptData(my_local_key);
            Access_log.log_it("i","shahin","my_local_key_in_KeyStore: " + my_local_key_in_KeyStore);

            Access_file.access_file_func_write(getApplicationContext(), "my_local_key_in_KeyStore", my_local_key_in_KeyStore, "write");

            //endregion

        }

        return return_of_wallet_config;
    }
    private String wallet_address(String publicKey_x,String publicKey_y) {
        // algorithm in C#
        // 1- public_key_b58 = pubKeyString_x(b58) + "~" + pubKeyString_y(even / odd);
        // 2- my_anonymous_id = HASH_CLASS.HASH_SHA256(public_key_b58);
        // 3- str_wallet_raw = "0" + my_anonymous_id
        // 4- str_value_base58 = Base58.Encode_in_hex(str_wallet_raw);
        // 5- wallet_md5 = HASH_CLASS.HASH_MD5(str_value_base58);
        // 6- wallet_checksum = "azm" + "B" + wallet_md5.Substring(0, 11);
        // 7- wallet_address = wallet_checksum + str_value_base58;

        // 1 <
        String public_key_b58 = crypto_asym_keys_compress.PublicKeyCompression(publicKey_x,publicKey_y,"B58");
        // 2 <
        String my_anonymous_id = "";
        try {
            my_anonymous_id = hash_functions.Hash_SHA_256(public_key_b58);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // 3 <
        //String str_wallet_raw = "0" + my_anonymous_id; // old C# compatibility to reach UNSIGNED bigintegers
        String str_wallet_raw = my_anonymous_id;
        // 4 <
        BigInteger str_value_base58_bigInt = new BigInteger(str_wallet_raw, 16);
        String str_value_base58 = Base58.encode(str_value_base58_bigInt);
        // 5 <
        String wallet_md5 = hash_functions.Hash_MD5(str_value_base58);
        // 6 <
        assert wallet_md5 != null;
        String wallet_checksum = "box" + "B" + wallet_md5.substring(0, 11);
        // 7 <
        // wallet_address = wallet_checksum + str_value_base58;

        return wallet_checksum + str_value_base58;
    }
    public static String sign_order(String my_order, String privateKey_d) {

        String my_sign = null;
        PrivateKey privateKey = rebuild_key_private(privateKey_d);

        try {
            my_sign = crypto_asym_pure_secp256r1.signMessage(my_order, privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Access_log.log_it("i","shahin","sign: " + my_sign);

        return my_sign;
    }
    static boolean sign_verify(String my_order, String my_sign, String publicKey_x, String publicKey_y) {

        PublicKey publicKey = rebuild_key_public(publicKey_x, publicKey_y);

        // Verify the signature
        boolean isValid = false;
        try {
            isValid = crypto_asym_pure_secp256r1.verifySignature(my_order, my_sign, publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Print verification result
        Access_log.log_it("i","shahin","sign is valid: " + isValid);

        return isValid;
    }
    public static String retrieve_private_key()  {

        String my_local_key = null;
        //String my_local_key_in_KeyStore = null;
        //String aes_of_privateKey_d_b58 = null;
        String privateKey_d_b58 = "";
        BigInteger privateKey_d_bigInt = BigInteger.ZERO;
        String privateKey_d = "";

        //region load privateKey(d) from keystore
        my_local_key = crypto_asym_keys_store.decriptData(my_local_key_in_KeyStore);
        Access_log.log_it("i","shahin","my_local_key - retrieved: " + my_local_key);

        // decrypt by aes
        try {
            privateKey_d_b58 = crypto_symm_aes.aes_decrypt_b64(aes_of_privateKey_d_b58, my_local_key);
            Access_log.log_it("w","shahin","privateKey_d_b58 - retrieved: " + privateKey_d_b58);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        privateKey_d_bigInt = Base58.decode(privateKey_d_b58);
        privateKey_d = privateKey_d_bigInt.toString(16);
        Access_log.log_it("i","shahin","d (reloaded): " + privateKey_d);

        //endregion

        return privateKey_d;
    }
    private static PrivateKey rebuild_key_private(String privateKey_d)  {

        PrivateKey privateKey = null;

        // reload keypair from hex
        privateKey = crypto_asym_pure_secp256r1.rebuild_by_d_param(privateKey_d);
        //Access_log.log_it("i","shahin","Private Key - reloaded: " + privateKey.toString());

        return privateKey;
    }
    private static PublicKey rebuild_key_public(String publicKey_x, String publicKey_y)  {

        PublicKey publicKey = null;

        // reload keypair from hex
        publicKey = crypto_asym_pure_secp256r1.rebuild_by_xy_param(publicKey_x + "~" + publicKey_y);
        //Access_log.log_it("i","shahin","Public Key - reloaded: " + publicKey.toString());

        return publicKey;
    }

    public static boolean qc_keypairs (String privateKey_d, String publicKey_xy) throws Exception {

        boolean final_qc = true;

        // qc.0: read back the private key
        String qc_privateKey_d = privateKey_d;
        //Access_log.log_it("i","shahin","QC.0. qc_privateKey_d - " + qc_privateKey_d);

        // qc.0: split back the publicKey_xy
        String[] qc_split_publicKey_xy;
        qc_split_publicKey_xy = publicKey_xy.split("~");
        String qc_publicKey_x = qc_split_publicKey_xy[0];
        String qc_publicKey_y = qc_split_publicKey_xy[1];

        //Access_log.log_it("i","shahin","QC.0. qc_publicKey_x - " + qc_publicKey_x);
        //Access_log.log_it("i","shahin","QC.0. qc_publicKey_y - " + qc_publicKey_y);

        Access_log.log_it("i","shahin","QC.0. --> " + final_qc);

        // qc.1: compress public key to b58
        String publicKey_xy_compressed = crypto_asym_keys_compress.PublicKeyCompression(
                qc_publicKey_x,
                qc_publicKey_y,
                "B58"
        );

        //Access_log.log_it("i","shahin","QC.1. publicKey_xy_compressed - " + publicKey_xy_compressed);

        Access_log.log_it("i","shahin","QC.1. --> " + final_qc);

        // qc.2: decompress back public key from b58
        String[]  qc_split_publicKey_xy_2 = publicKey_xy_compressed.split("\\*");
        String qc_publicKey_x_b58 = qc_split_publicKey_xy_2[0];
        String qc_publicKey_y_b58 = qc_split_publicKey_xy_2[1];

        //Access_log.log_it("i","shahin","QC.2. qc_publicKey_x_b58 - " + qc_publicKey_x_b58);
        //Access_log.log_it("i","shahin","QC.2. qc_publicKey_y_b58 - " + qc_publicKey_y_b58);

        BigInteger qc_publicKey_x_bigInt = Base58.decode(qc_publicKey_x_b58);
        String qc_publicKey_x_new = qc_publicKey_x_bigInt.toString(16);

        //Access_log.log_it("i","shahin","QC.2. qc_publicKey_x_new - " + qc_publicKey_x_new);

        String publicKey_xy_decompressed_str = crypto_asym_keys_decompress.decompressSecp256r1(
                qc_publicKey_x_new + "*" + qc_publicKey_y_b58
        );

        Access_log.log_it("i","shahin","QC.2. publicKey_xy_decompressed_str - " + publicKey_xy_decompressed_str);

        String[]  qc_split_publicKey_xy_3 = publicKey_xy_decompressed_str.split("\\*");
        String qc_publicKey_x_decompressed = qc_split_publicKey_xy_3[0];
        String qc_publicKey_y_decompressed = qc_split_publicKey_xy_3[1];

        Access_log.log_it("i","shahin","QC.2. --> " + final_qc);

        // qc.3: evaluate compress-decompress process
        if (qc_publicKey_x.equals(qc_publicKey_x_decompressed))
        {
            //Access_log.log_it("i","shahin","QC.3. Passed: qc_publicKey_x_decompressed - " + qc_publicKey_x_decompressed);
        }
        else
        {
            final_qc = false;
        }
        if (qc_publicKey_y.equals(qc_publicKey_y_decompressed))
        {
            //Access_log.log_it("i","shahin","QC.3. Passed: qc_publicKey_y_decompressed - " + qc_publicKey_y_decompressed);
        }
        else
        {
            final_qc = false;
        }

        Access_log.log_it("i","shahin","QC.3. --> " + final_qc);

        // qc.4: sign and validate
        String qc_sign_value = sign_order("Hello World!",qc_privateKey_d);
        boolean qc_sign_verify = sign_verify("Hello World!",qc_sign_value,qc_publicKey_x,qc_publicKey_y);

        if (qc_sign_verify)
        {
            //Access_log.log_it("i","shahin","QC.4. Passed: sign verified - " + qc_sign_verify);
        }
        else
        {
            final_qc = false;
        }

        Access_log.log_it("i","shahin","QC.4. --> " + final_qc);

        // qc.5: wallet recovery by private key
        ECPoint qc_ecPoint = crypto_asym_keys_recovery.recover("secp256r1",qc_privateKey_d);
        BigInteger qc_recovery_x = qc_ecPoint.getAffineX();
        BigInteger qc_recovery_y = qc_ecPoint.getAffineY();

        String qc_recovery_publicKey_x = qc_recovery_x.toString(16);
        String qc_recovery_publicKey_y = qc_recovery_y.toString(16);

        if (qc_recovery_publicKey_x.equals(qc_publicKey_x))
        {
            //Access_log.log_it("i","shahin","QC.5. Passed: qc_recovery_publicKey_x - " + qc_recovery_publicKey_x);
        }
        else
        {
            final_qc = false;
        }
        if (qc_recovery_publicKey_y.equals(qc_publicKey_y))
        {
            //Access_log.log_it("i","shahin","QC.5. Passed: qc_recovery_publicKey_y - " + qc_recovery_publicKey_y);
        }
        else
        {
            final_qc = false;
        }

        Access_log.log_it("i","shahin","QC.5. --> " + final_qc);

        return final_qc;

    }

    //endregion

    //region functions_of_menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        if (id == R.id.action_refresh) {

            // Signal SwipeRefreshLayout to start the progress indicator.
            swipeContainer.setRefreshing(true);
            Access_log.log_it("i","shahin","swipeContainer.setRefreshing(true)");

            refresh_general();

            return true;
        }

        if (id == R.id.action_private_key) {

            Intent i = new Intent(getApplicationContext(),MainActivity_PrivateKey.class);
            //finishAffinity();
            startActivity(i);

            return true;
        }

        if (id == R.id.action_language) {

            showLanguageSelector();

            return true;
        }

        if (id == R.id.action_kyc) {

            Intent i = new Intent(getApplicationContext(),MainActivity_KYC.class);
            //finishAffinity();
            startActivity(i);

            return true;
        }

        if (id == R.id.action_about) {

            Intent i = new Intent(getApplicationContext(),MainActivity_About.class);
            //finishAffinity();
            startActivity(i);

            return true;
        }

        if (id == R.id.action_terms) {

            Intent i = new Intent(getApplicationContext(),MainActivity_Terms.class);
            //finishAffinity();
            startActivity(i);

            return true;
        }

        if (id == R.id.action_setting) {

            Intent i = new Intent(getApplicationContext(),MainActivity_Setting.class);
            //finishAffinity();
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Inside your Settings Activity
    private void showLanguageSelector() {
        String[] languageNames = {"English", "فارسی (Persian)"};
        String[] languageCodes = {"en", "fa"};

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)   // Better to use string resource
                .setSingleChoiceItems(languageNames, -1, (dialog, which) -> {
                    String selectedCode = languageCodes[which];

                    dialog.dismiss();

                    // Use the new method
                    changeLanguage(selectedCode);
                })
                .show();
    }

    // New Method - Recommended
    private void changeLanguage(String newLang) {
        // Save and apply new language
        LocaleHelper.setLocale(this, newLang);

        // Restart the app cleanly (best for full direction refresh)
        restartApplication();
    }

    private void restartApplication() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();        // Close all old activities
    }

    //endregion

    //region functions_of_Internet

    @SuppressLint("SetTextI18n")
    public static String browse_url(String the_url)  {

        // browse url
        Access_internet task_of_browsing = new Access_internet();
        Access_log.log_it("i","shahin","Download_Task - object created");

        String result = "";
        try
        {
            //Inside the emulator, 127.0.0.1 refers to the emulator itself - not your local machine.
            // You need to use ip 10.0.2.2, which is bridged to your local machine.

            //result = task_of_browsing.execute("http://127.0.0.1/").get();
            result = task_of_browsing.execute(the_url).get();
            Access_log.log_it("i","shahin","Download_Task - executed");

        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            //            this prints all the information about the error to the logs.
            Access_log.log_it("i","shahin","Error1");

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Access_log.log_it("i","shahin","Error2");
        }
        Access_log.log_it("i","shahin","result: " + result);

        return result;
    }
    @SuppressLint("SetTextI18n")
    public static String browse_url_POST(String the_url, String post_data)  {

        // set progressbar
        //progressbar_stat = true;
        //binding.textviewWhatsUp.setText("Working..");
        //doStartProgressBar2();

        // browse url
        Access_internet_POST task_of_browsing = new Access_internet_POST();
        Access_log.log_it("i","shahin","Download_Task - object created");

        String result = "";
        try
        {
            //Inside the emulator, 127.0.0.1 refers to the emulator itself - not your local machine.
            // You need to use ip 10.0.2.2, which is bridged to your local machine.

            //result = task_of_browsing.execute("http://127.0.0.1/").get();
            result = task_of_browsing.execute(the_url,post_data).get();
            Access_log.log_it("i","shahin","Download_Task - executed");
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            //            this prints all the information about the error to the logs.
            Access_log.log_it("i","shahin","Error1");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Access_log.log_it("i","shahin","Error2");
        }

        Access_log.log_it("i","shahin","browse with post - result: " + result);

        // reset progressbar
        //progressbar_stat = false;

        return result;
    }
    public static void setting_connection_values(int the_matched_Index)
    {
        app_pqc_serial = spinner_options_pqc_serial[the_matched_Index];
        app_pqc_pk = spinner_options_pqc_pk[the_matched_Index];
        graph_address_in = spinner_options_value[the_matched_Index];

        Access_log.log_it("i","shahin","555 - app_pqc_serial: " + app_pqc_serial);
        Access_log.log_it("i","shahin","555 - app_pqc_pk: " + app_pqc_pk);
        Access_log.log_it("i","shahin","555 - graph_address_in: " + graph_address_in);
    }
    public static void setting_connection(String my_protocol)
    {
        setting_network_protocol = my_protocol; // "https"; //  "http"; //
        server_url = setting_network_protocol + "://" + graph_address_in + "/"; // "://192.168.88.111:701/";
    }

    //endregion

    //region functions_of_dynamic_views

    // Show spinners
    private void refresh_general()
    {
        // config internet connection
        String server_url_query =
                "?app_name=" + URLEncoder.encode(app_name)
                        + "&app_version=" + URLEncoder.encode(app_version)
                        + "&in_graph=" + URLEncoder.encode(graph_domain_in)
                        + "&wallet_address=" + URLEncoder.encode(wallet_address);

        String result = browse_url(server_url + "dmz.asmx/ledger_history" + server_url_query);
        //String result = browse_url_POST(server_url_order_history + server_file_order_history, server_url_query);
        Access_log.log_it("i","shahin","dmz.asmx/ledger_history" + " - result: " + result);

        String network_msg = "Er";

        // update refresh datetime
        if (!result.equals("Failed") && !result.equals("no_record"))
        {
            refresh_update(graph_domain_in);
            //network_msg = "Net: <font color=cyan>OK</font> / ";
            network_msg = "OK";
        }

        // show refresh datetime
        crypto_list_label(graph_domain_in, result);
        refresh_label(graph_domain_in, network_msg);

        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
        Access_log.log_it("i","shahin","swipeContainer.setRefreshing(false)");
    }
    private void refresh_label(String my_graph_in, String my_msg)
    {
        // Find the position of selection
        int spinner_matchedIndex = -1;
        for (int i = 0; i < spinner_options.length; i++) {
            if (spinner_options[i].equals(my_graph_in)) {
                spinner_matchedIndex = i;
                break;  // Stop at first match
            }
        }

        String refresh_utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());
        String refresh_utc_unix_last = Access_file.access_file_func_read(getApplicationContext(), "refresh_utc_unix_last_" + spinner_matchedIndex);

        Access_log.log_it("i","shahin","refresh_utc_unix_now: " + refresh_utc_unix_now);
        Access_log.log_it("i","shahin","refresh_utc_unix_last: " + refresh_utc_unix_last);

        if (refresh_utc_unix_last.equals("-")) {
            //refresh_utc_unix_last = refresh_utc_unix_now;
            refresh_utc_unix_last = "-1";
        }

        /*
        textview_balance_wallet.setText(
                HtmlCompat.fromHtml(my_msg + "Last update: <b>" +
                                Access_time.getTimeDifference(refresh_utc_unix_now,refresh_utc_unix_last) +
                                "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY)
        );

        // Get current language
        String currentLang = LocaleHelper.getCurrentLanguage(this);

        CharSequence result = TextUtils.concat(
                stringHelper.getNetworkAccessText(my_msg),
                stringHelper.getLastUpdateText(
                             Access_time.getTimeDifference(
                                                          currentLang,
                                                          refresh_utc_unix_now,
                                                          refresh_utc_unix_last
                                                          )
                                               )
        );

        textview_balance_wallet.setText(result);
        */

        CharSequence result = stringHelper.getBalanceHeaderText(
                my_msg,
                refresh_utc_unix_now,
                refresh_utc_unix_last
        );

        textview_balance_wallet.setText(result);
    }
    private void refresh_update(String my_graph_in)
    {
        // Find the position of selection
        int spinner_matchedIndex = -1;
        for (int i = 0; i < spinner_options.length; i++) {
            if (spinner_options[i].equals(my_graph_in)) {
                spinner_matchedIndex = i;
                break;  // Stop at first match
            }
        }

        String refresh_utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());
        Access_file.access_file_func_write(getApplicationContext(), "refresh_utc_unix_last_" + spinner_matchedIndex, refresh_utc_unix_now, "write");
    }
    private void crypto_list_label(String my_graph_in, String my_crypto_list)
    {
        Access_log.log_it("i","shahin","crypto_list: inside");

        // Remove all items from the list
        dataList.clear();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter(dataList, this); // You'll need to create MyAdapter
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged(); // Notify the adapter of the change

        String crypto_intersection = getCommonTokens(my_graph_in,my_graph_in);
        Access_log.log_it("i","shahin","crypto_intersection: " + crypto_intersection);

        // split result
        String[] split_crypto_intersection;
        split_crypto_intersection = crypto_intersection.split(",");

        Access_log.log_it("i","shahin","crypto_list: begin");

        // Find the position of selection
        int spinner_matchedIndex = -1;
        for (int i = 0; i < spinner_options.length; i++) {
            if (spinner_options[i].equals(my_graph_in)) {
                spinner_matchedIndex = i;
                break;  // Stop at first match
            }
        }

        String crypto_list = "";

        if (my_crypto_list.contains("~")) {
            crypto_list = my_crypto_list;

            /*
            // fill the gap
            if (!crypto_list.contains("USD"))
            {
                crypto_list += "~USD~0.00000000";
                Access_log.log_it("i","shahin","crypto_list (USD): " + crypto_list);
            }
            if (!crypto_list.contains("2ZR"))
            {
                crypto_list += "~2ZR~0.00000000";
                Access_log.log_it("i","shahin","crypto_list (2ZR): " + crypto_list);
            }
            if (!crypto_list.contains("TLH"))
            {
                crypto_list += "~TLH~0.00000000";
                Access_log.log_it("i","shahin","crypto_list (TLH): " + crypto_list);
            }
            */

            for (int kk=0; kk < split_crypto_intersection.length; kk++)
            {
                if (!crypto_list.contains(split_crypto_intersection[kk]))
                {
                    crypto_list += "~" + split_crypto_intersection[kk] + "~0.00000000";
                    Access_log.log_it("i","shahin","crypto_list added (" + split_crypto_intersection[kk] + "): " + crypto_list);
                }
            }

            Access_file.access_file_func_write(getApplicationContext(), "crypto_list_last_" + spinner_matchedIndex, crypto_list, "write");
            Access_log.log_it("i","shahin","crypto_list: write");
        }
        else
        {
            crypto_list = Access_file.access_file_func_read(getApplicationContext(), "crypto_list_last_" + spinner_matchedIndex);
            if (crypto_list.equals("-"))
            {
                crypto_list = my_graph_in + "~~msg~null~adv~null";
            }

            for (int kk=0; kk < split_crypto_intersection.length; kk++)
            {
                if (!crypto_list.contains(split_crypto_intersection[kk]))
                {
                    crypto_list += "~" + split_crypto_intersection[kk] + "~0.00000000";
                    Access_log.log_it("i","shahin","crypto_list added (" + split_crypto_intersection[kk] + "): " + crypto_list);
                }
            }

            Access_log.log_it("i","shahin","crypto_list: read - " + crypto_list);
        }

        Access_log.log_it("i","shahin","crypto_list: " + crypto_list);
        Access_log.log_it("i","shahin","crypto_list: end");

        // split result
        String[] split_result;
        split_result = crypto_list.split("~");

        // sample result:
        // in_graph + "~" + wallet_addres + "~msg~null~adv~null~null"
        // in_graphs + "~" + wallet_addres + "~msg~null~adv~null~" + currency_name_2pn + "~" + left_amount_2pn + "~" + currency_name_2zr + "~" + left_amount_2zr

        // split_result[0] : in_graph
        // split_result[1] : wallet_address

        Access_log.log_it("i","shahin","split_result.length: " + split_result.length);
        for (int kk=2; kk < split_result.length; kk=kk+2)
        //for (int kk=2; kk < split_result.length; kk++)   // changed to k++ for empty wallets
        {
            if (split_result[kk].equals("msg"))
            {
                if (split_result[kk+1].equals("null"))
                {
                    layout_main_whatsup.setVisibility(View.GONE);
                }
                else
                {
                    layout_main_whatsup.setVisibility(View.VISIBLE);
                    textview_main_whatsup.setText(
                            HtmlCompat.fromHtml(
                                    split_result[kk+1],
                                    HtmlCompat.FROM_HTML_MODE_LEGACY));
                    textview_main_whatsup.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            if (split_result[kk].equals("adv"))
            {
                if (split_result[kk+1].equals("null"))
                {
                    layout_main_advertise.setVisibility(View.GONE);
                }
                else
                {
                    layout_main_advertise.setVisibility(View.VISIBLE);
                    textview_main_advertise.setText(
                            HtmlCompat.fromHtml(
                                    split_result[kk+1],
                                    HtmlCompat.FROM_HTML_MODE_LEGACY));
                    textview_main_advertise.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            if (crypto_intersection.contains(split_result[kk]))
            {
                dataList.add(new ItemData(split_result[kk],split_result[kk+1], getIconForToken(split_result[kk])));
                Access_log.log_it("i","shahin",kk + ": ItemData - if  : " + split_result[kk] + " - " + split_result[kk+1]);
            }
            else
            {
                Access_log.log_it("i","shahin",kk + ": ItemData - else: " + split_result[kk]);
            }

            /*
            // we need a list of all possible valid tokens among all graphs here
            if (split_result[kk].equals("USD"))
            {
                dataList.add(new ItemData("USD",split_result[kk+1], R.mipmap.coin_2pn));
                Access_log.log_it("i","shahin","ItemData: " + split_result[kk] + " - " + split_result[kk+1]);
            }
            if (split_result[kk].equals("2ZR"))
            {
                dataList.add(new ItemData("2ZR", split_result[kk+1], R.mipmap.coin_2zr));
                Access_log.log_it("i","shahin","ItemData: " + split_result[kk] + " - " + split_result[kk+1]);
            }
            if (split_result[kk].equals("TLH"))
            {
                dataList.add(new ItemData("TLH", split_result[kk+1], R.mipmap.coin_tlh));
                Access_log.log_it("i","shahin","ItemData: " + split_result[kk] + " - " + split_result[kk+1]);
            }
            */
        }

    }

    //endregion

    //region functions_of_GPP

    /**
     * Returns the common tokens (intersection) between two servers.
     *
     * @param server1 First server from spinner_options
     * @param server2 Second server from spinner_options
     * @return Comma-separated string of common tokens, or empty string if none
     */
    public static String getCommonTokens(String server1, String server2) {
        if (server1 == null || server2 == null)
        {
            return "";
        }

        int index1 = findServerIndex(server1);
        int index2 = findServerIndex(server2);

        if (index1 == -1 || index2 == -1) {
            return ""; // Server not found
        }

        String tokens1 = MainActivity.spinner_options_tokens[index1];
        String tokens2 = MainActivity.spinner_options_tokens[index2];

        Access_log.log_it("w","shahin","tokens1: " + tokens1);
        Access_log.log_it("w","shahin","tokens2: " + tokens2);

        return getIntersection(tokens1, tokens2);
    }

    /**
     * Helper: Find index of a server in spinner_options
     */
    private static int findServerIndex(String server) {
        for (int i = 0; i < MainActivity.spinner_options.length; i++) {
            if (MainActivity.spinner_options[i].equals(server)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper: Returns intersection of two comma-separated token strings
     */
    private static String getIntersection(String tokens1, String tokens2) {
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return "";
        }

        Set<String> set1 = Arrays.stream(tokens1.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> set2 = Arrays.stream(tokens2.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // Keep only common tokens
        set1.retainAll(set2);

        if (set1.isEmpty()) {
            return "";
        }

        return String.join(",", set1);
    }

    // Put this in your Activity or a utility class
    public static int getIconForToken(String token) {
        switch (token.toUpperCase().trim()) {
            case "2ZR":
                return R.mipmap.coin_2zr;
            case "TLH":
                return R.mipmap.coin_tlh;
            case "USD":
                return R.mipmap.coin_usd;
            case "IRR":
                return R.mipmap.coin_irr;
            default:
                return R.drawable.baseline_fingerprint_24; // fallback
        }
    }

    //endregion

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    // Inner class for the RecyclerView Adapter
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<ItemData> items;
        private Context context;

        public MyAdapter(List<ItemData> items, Context context) {
            this.items = items;
            this.context = context;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_activity_main, parent, false);
            return new MyViewHolder(view);
        }
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ItemData item = items.get(position);

            holder.titleView.setText(HtmlCompat.fromHtml(
                            "<font color='#7851A9'>" +
                            item.getTitle() +
                            "</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            holder.textView.setText(item.getText());
            holder.imageView.setImageResource(item.getImageResourceId());

            //final int currentPosition = position; // Create a final local variable
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle click directly here (less organized)
                    //String clickedItem = dataList.get(currentPosition);
                    Intent i = new Intent(getApplicationContext(),MainActivity_History.class);
                    i.putExtra("detail_of_currency_name", item.getTitle());
                    startActivity(i);
                }
            });

            //If you were using urls, you would load the image using an image loading library like Glide or Picasso.
            //Glide.with(context).load(item.getImageUrl()).into(holder.imageView);
        }
        @Override
        public int getItemCount() {
            return items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView titleView;
            public TextView textView;
            public ImageView imageView;

            public MyViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.item_title);
                textView = itemView.findViewById(R.id.item_text);
                imageView = itemView.findViewById(R.id.item_image);
            }
        }
    }
    private class ItemData {

        private String title;
        private String text;
        private int imageResourceId; // Or String imageUrl

        public ItemData(String title, String text, int imageResourceId) { // Or String imageUrl
            this.title = title;
            this.text = text;
            this.imageResourceId = imageResourceId;
        }

        public String getTitle() {
            return title;
        }
        public String getText() {
            return text;
        }
        public int getImageResourceId() {
            return imageResourceId;
        }
        // Or String getImageUrl
    }


    // Custom Adapter to show Graph + Zones
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

            String domain = spinner_options[position];
            String zones = spinner_options_zones[position];

            String zonesText = (zones.length() > 0)
                    ? " [" + String.join(", ", zones) + "]"
                    : " [no zone]";

            textView.setText(domain + zonesText);

            return view;
        }
    }
}

