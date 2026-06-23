package net.mixoftix.tallybox;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mixoftix.tallybox.databinding.ActivityMainHistoryBinding;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity_History extends AppCompatActivity {

    private String detail_of_currency_name = "";
    private ImageView image_view_coin;
    private TextView textview_graph_in,textview_history_currency,textview_history_balance,textview_history_last_update;
    private LinearLayout path_interactive_views;

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<ItemData> dataList;
    private SwipeRefreshLayout swipeContainer;

    private static ActivityMainHistoryBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_history);

        binding = ActivityMainHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarHomeRefresh);

        textview_graph_in = findViewById(R.id.textview_graph_in);
        image_view_coin = findViewById(R.id.image_view_coin);
        textview_history_currency = findViewById(R.id.textview_history_hash_orderid);
        textview_history_balance = findViewById(R.id.textview_history_hash_tnxid);
        textview_history_last_update = findViewById(R.id.textview_history_last_update);

        // initial history content
        // read intent
        detail_of_currency_name = getIntent().getStringExtra("detail_of_currency_name");
        // read graph
        textview_graph_in.setText("(in graph: " + MainActivity.graph_domain_in + ")");
        // set currency image
        int selectedId = getResources().getIdentifier("coin_" + detail_of_currency_name.toLowerCase(), "mipmap", this.getPackageName());
        image_view_coin.setImageResource(selectedId);
        image_view_coin.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        // set currency abbreviation
        textview_history_currency.setText(HtmlCompat.fromHtml(
                "<b>" + detail_of_currency_name + "</b>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        // connection
        textview_history_balance.setText(HtmlCompat.fromHtml(
                "<font color='#FFD700'>Network: Connecting..</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        textview_history_last_update.setText(HtmlCompat.fromHtml(
                "( please wait )",
                HtmlCompat.FROM_HTML_MODE_LEGACY));


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
                order_history_refresh(detail_of_currency_name);
                //swipeContainer.setRefreshing(false);

            }
        });

        //endregion

        // define dynamic views - old
        // path_interactive_views = (LinearLayout) findViewById(R.id.interactive_views);
        // read server data and generate views

        // read server data and generate views


        // Trigger code after 1 second (1000 milliseconds)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // read server data and generate views
                try {

                    order_history_refresh(detail_of_currency_name);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500); // 1000 milliseconds = 1 second

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
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

        if (id == R.id.action_back) {
            onBackPressed();

            return true;
        }

        if (id == R.id.action_refresh) {

            // read server data and generate views
            order_history_refresh(detail_of_currency_name);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private String getdatetime_now()
    {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd, HH:mm:ss");
        //format.setTimeZone(TimeZone.getTimeZone("GMT"));
        //Log.d("shahin", format.format(date));

        return format.format(date).toString();
    }
    private String getTimeDifference(String str_epoch1, String str_epoch2) {
        long epoch1 = (long)Double.parseDouble(str_epoch1) * 1000L;
        long epoch2 = (long)Double.parseDouble(str_epoch2) * 1000L;
        long diffInMillis = Math.abs(epoch1 - epoch2);

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        long diffInWeeks = diffInDays / 7;
        long diffInMonths = diffInDays / 30;

        //Log.v("shahin", "epoch1: " + epoch1);
        //Log.v("shahin", "epoch2: " + epoch2);
        //Log.v("shahin", "diffInMillis: " + diffInMillis);
        //Log.v("shahin", "diffInMinutes: " + diffInMinutes);

        if (diffInMinutes < 1) {
            return "now";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " minute(s) ago";
        } else if (diffInHours < 24) {
            return diffInHours + " hour(s) ago";
        } else if (diffInDays < 7) {
            return diffInDays + " day(s) ago";
        } else if (diffInWeeks < 4) {
            return diffInWeeks + " week(s) ago";
        } else {
            return diffInMonths + " month(s) ago";
        }
    }
    private void order_history_refresh(String detail_of_currency_name){

        // Now we call setRefreshing(true) to signal refresh has begun
        swipeContainer.setRefreshing(true);
        Access_log.log_it("i","shahin","swipeContainer.setRefreshing(true)");

        // config internet connection
        String server_url_query =
                "?app_name=" + MainActivity.app_name
                        + "&app_version=" + MainActivity.app_version
                        + "&in_graph=" + URLEncoder.encode(MainActivity.graph_domain_in)
                        + "&wallet_address=" + MainActivity.wallet_address
                        + "&currency_name=" + detail_of_currency_name;

        String result_history_wallet_currency = MainActivity.browse_url(
                MainActivity.server_url +
                        "dmz.asmx/ledger_history_detail" +
                        server_url_query);

        // update datetime
        textview_history_last_update.setText(HtmlCompat.fromHtml(
                "( " + getdatetime_now() + " )",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        // generate views of history
        generate_history_views(result_history_wallet_currency);

        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
        Access_log.log_it("i","shahin","swipeContainer.setRefreshing(false)");
    }
    @SuppressLint("SetTextI18n")
    private void generate_history_views_old(String result) {

        path_interactive_views.removeAllViewsInLayout();

        // interpret server's CSV data
        String[] split_output;
        split_output = result.split("\\^");

        // index[0] : in_graph
        // index[1] : wallet_address
        // index[2] : left_amount

        textview_graph_in.setText("(in graph: " + split_output[0] + ")");

        textview_history_balance.setText(HtmlCompat.fromHtml(
                "<font color='#4169E1'>" + split_output[2] + "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (split_output[3].equals("no_record"))
        {
            return;
        }

        for (int kk=3; kk < split_output.length; kk=kk+5) {
            String cmd_tnx_type = split_output[kk];
            String cmd_tnx_id = split_output[kk+1];
            String cmd_tnx_curr_amount = split_output[kk+2];
            String cmd_tnx_left_amount = split_output[kk+3];
            String cmd_tnx_tally_hash = split_output[kk+4];

            String log_str = kk + "-" +
                    cmd_tnx_type +
                    " [" + cmd_tnx_id + "]" +
                    " [" + cmd_tnx_curr_amount + "]" +
                    " [" + cmd_tnx_left_amount + "]" +
                    " [" + cmd_tnx_tally_hash + "]";

            Access_log.log_it("i","shahin",log_str);

            String utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());
            String cmd_tnx_id_moment = Access_time.back_from_utc(cmd_tnx_id);
            cmd_tnx_id_moment = Access_time.getTimeDifference(utc_unix_now,cmd_tnx_id) + ",<br>" + cmd_tnx_id_moment;

            Access_log.log_it("i","shahin",kk + "- cmd_tnx_id_moment:" + cmd_tnx_id_moment);

            // generate textviews
            // main textview

            // BGN: insert linear_block
            LinearLayout parent = new LinearLayout(MainActivity_History.this);
            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.bottomMargin = 15;
            //addContentView(parent,layoutParams);
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            parent.setPadding(10,10,10,10);
            path_interactive_views.addView(parent,layoutParams);
            // END: insert linear_block

            TextView msg = new TextView(MainActivity_History.this, null, android.R.attr.textViewStyle);
            msg.setId(kk);
            final int id_msg = msg.getId();

            // check the dark mode
            /*
            String fee_color_code = "";
            boolean isDarkMode = (getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkMode) {
                // App is in Dark Mode
                //System.out.println("Dark Mode is enabled!");
                fee_color_code = "#FFFACD";
            } else {
                // App is in Light Mode
                //System.out.println("Light Mode is enabled!");
                fee_color_code = "#C0C0C0";
            }
            fee_color_code = "#696969";
            */

            if (cmd_tnx_type.equals("0")) {
                msg.setText(HtmlCompat.fromHtml(
                        "<font color='#4169E1'>" +
                                cmd_tnx_left_amount +
                                "</font>" +
                                "<br><font color='#FFD700'>-" +
                                cmd_tnx_curr_amount +
                                "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            else if (cmd_tnx_type.equals("1")) {
                msg.setText(HtmlCompat.fromHtml(
                        "<font color='#4169E1'>" +
                                cmd_tnx_left_amount +
                                "</font>" +
                                "<br><font color='#FF4500'>-" +
                                cmd_tnx_curr_amount +
                                "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            else {
                msg.setText(HtmlCompat.fromHtml(
                        "<font color='#4169E1'>" +
                                cmd_tnx_left_amount +
                                "</font>" +
                                "<br><font color='#32CD32'>+" +
                                cmd_tnx_curr_amount +
                                "</font>"
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            msg.setTextSize(19);
            msg.setEllipsize(TextUtils.TruncateAt.END);
            //msg.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            //msg.setPadding(10,10,10,10);

            // Align the text inside the TextView to the right
            msg.setGravity(Gravity.END | Gravity.RIGHT);
            /*
            FrameLayout.LayoutParams msg_params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            msg_params.gravity = Gravity.END | Gravity.RIGHT;
            msg.setLayoutParams(msg_params);
            */

            //path_interactive_views.addView(msg);
            parent.addView(msg);

            // sub textview
            TextView msg2 = new TextView(MainActivity_History.this, null, android.R.attr.textViewStyle);
            msg2.setId(kk+1);
            final int id_msg2 = msg2.getId();

            msg2.setText(HtmlCompat.fromHtml(
                    "<i>" +
                    cmd_tnx_id_moment +
                            " (" +
                            (long)Double.parseDouble(cmd_tnx_id) +
                            ")" +
                            "<i>"
                    , HtmlCompat.FROM_HTML_MODE_LEGACY));
            msg2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            msg2.setTextSize(13);
            msg2.setEllipsize(TextUtils.TruncateAt.END);
            //msg2.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_white));
            msg2.setCompoundDrawablesWithIntrinsicBounds(null,null,ContextCompat.getDrawable(this, R.drawable.baseline_fingerprint_24),null);
            msg2.setPadding(10,10,10,10);

            // Align the text inside the TextView to the right
            msg2.setGravity(Gravity.START | Gravity.LEFT);

            //path_interactive_views.addView(msg2);
            parent.addView(msg2);

            msg2 = ((TextView) findViewById(id_msg2));
            msg2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void onClick(View view) {

                    //copy_to_clipboard(cmd_tnx_tally_hash);
                    //Toast.makeText(view.getContext(),
                    //                "tally_hash copied:\n\r" + cmd_tnx_tally_hash, Toast.LENGTH_SHORT)
                    //        .show();

                    // Run Application
                    Intent i = new Intent(getApplicationContext(),MainActivity_History_Detail.class);
                    i.putExtra("detail_of_currency_name", detail_of_currency_name);
                    i.putExtra("tnx_tally_hash", cmd_tnx_tally_hash);
                    //finishAffinity();
                    startActivity(i);

                }
            });
        }
    }
    @SuppressLint("SetTextI18n")
    private void generate_history_views(String result) {

        String about_graph = "";
        String left_amount = "";

        if (!result.contains("^"))
        {
            textview_history_balance.setText(HtmlCompat.fromHtml(
                    "<font color='#FFD700'>Network: " + result + "</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            return;
        }

        // interpret server's CSV data
        String[] split_output;
        split_output = result.split("#");

        about_graph = split_output[0];
        Access_log.log_it("i","shahin","about_graph: " + about_graph);

        // interpret output_block's CSV data
        String[] split_output_block;
        split_output_block = about_graph.split("\\^");
        Access_log.log_it("i","shahin","split_output_block[0]: " + split_output_block[0]);

        // index[0] : in_graph
        // index[1] : wallet_address
        // index[2] : left_amount

        if (!split_output_block[0].equals(MainActivity.graph_domain_in))
        {
            textview_graph_in.setText(HtmlCompat.fromHtml(
                    "<font color='#FF4500'>" + split_output_block[0] + "</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            textview_history_balance.setText(HtmlCompat.fromHtml(
                    "<font color='#FF4500'>Graph: Failed!</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            return;
        }

        left_amount = split_output_block[2];
        textview_history_balance.setText(HtmlCompat.fromHtml(
                "<font color='#4169E1'>" + left_amount + "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (split_output[1].equals("no_record"))
        {
            return;
        }

        // Remove all items from the list
        dataList.clear();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter(dataList, this); // You'll need to create MyAdapter
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged(); // Notify the adapter of the change

        for (int kk=1; kk < split_output.length; kk++) {
            split_output_block = split_output[kk].split("\\^");

            String cmd_tnx_type = split_output_block[0];
            String cmd_tnx_id = split_output_block[1];
            String cmd_tnx_curr_amount = split_output_block[2];
            String cmd_tnx_left_amount = split_output_block[3];
            String cmd_tnx_tally_hash = split_output_block[4];

            String log_str = kk + "-" +
                    cmd_tnx_type +
                    " [" + cmd_tnx_id + "]" +
                    " [" + cmd_tnx_curr_amount + "]" +
                    " [" + cmd_tnx_left_amount + "]" +
                    " [" + cmd_tnx_tally_hash + "]";

            Access_log.log_it("i","shahin",log_str);

            String utc_unix_now = String.valueOf(Access_time.getUnixTimestampSeconds());
            String cmd_tnx_id_moment = Access_time.back_from_utc(cmd_tnx_id);
            cmd_tnx_id_moment = "<i>" + Access_time.getTimeDifference(utc_unix_now,cmd_tnx_id) + ",<br>" + cmd_tnx_id_moment + " (" + (long)Double.parseDouble(cmd_tnx_id) + ")" + "</i>";

            Access_log.log_it("i","shahin",kk + "- cmd_tnx_id_moment:" + cmd_tnx_id_moment);

            // generate textviews
            // main textview

            String cmd_balance = "";

            if (cmd_tnx_type.equals("0")) {
                cmd_balance =
                                "<font color='#4169E1'>" +
                                cmd_tnx_left_amount +
                                "</font>" +
                                "<br><font color='#FFD700'>-" +
                                cmd_tnx_curr_amount +
                                "</font>"
                                ;
            }
            else if (cmd_tnx_type.equals("1")) {
                cmd_balance =
                                "<font color='#4169E1'>" +
                                cmd_tnx_left_amount +
                                "</font>" +
                                "<br><font color='#FF4500'>-" +
                                cmd_tnx_curr_amount +
                                "</font>"
                                ;
            }
            else {
                cmd_balance =
                                "<font color='#4169E1'>" +
                                cmd_tnx_left_amount +
                                "</font>" +
                                "<br><font color='#32CD32'>+" +
                                cmd_tnx_curr_amount +
                                "</font>"
                                ;
            }

            dataList.add(new ItemData(cmd_tnx_tally_hash, cmd_tnx_id_moment, cmd_balance));

        }

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional
        super.onBackPressed();
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
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_activity_history, parent, false);
            return new MyAdapter.MyViewHolder(view);
        }
        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            ItemData item = items.get(position);

            holder.textView.setText(HtmlCompat.fromHtml(
                    item.getText(),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            holder.titleView.setText(HtmlCompat.fromHtml(
                    item.getTitle(),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            //final int currentPosition = position; // Create a final local variable
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle click directly here (less organized)
                    //String clickedItem = dataList.get(currentPosition);
                    Intent i = new Intent(getApplicationContext(),MainActivity_History_Detail.class);
                    i.putExtra("detail_of_currency_name", detail_of_currency_name);
                    i.putExtra("tnx_tally_hash", item.getHash());
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

            public MyViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.item_title);
                textView = itemView.findViewById(R.id.item_text);
            }
        }
    }
    private class ItemData {

        private String hash;
        private String title;
        private String text;

        public ItemData(String hash, String title, String text) { // Or String imageUrl
            this.hash = hash;
            this.title = title;
            this.text = text;
        }

        public String getHash() {
            return hash;
        }
        public String getTitle() {
            return title;
        }
        public String getText() {
            return text;
        }

        // Or String getImageUrl
    }

}