package net.mixoftix.tallybox;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Access_internet extends AsyncTask<String, Void, String> {

    //    a class is a collection of methods and variables that we can use in our app.
//    AsyncTask: is a way of running our code on a different thread than the main thread.
//    so far we used onCreate method thread which known as ui thread. it is advised to run
//    any code that gets a bit of time on a different thread rather than the main thread.
//    so we are creating some code that will be run in background.
//    AsyncTask usually gets 3 parameter.
//    The First one: is the type of variable that we are going to send to this class. to instruct it what to do.
//    The Second One: is the name of the method that we may or maynot use to show the progress of this async task
//    The Third One: is the type of the variable that is going to be returned by this class.
    @Override
    protected String doInBackground(String... urls) {
//            protected : this protected means that this string can be accessed from anywhere in the package, not just
//            this class and not just this java file, it can be access from anywhere in the app.
//            String... : this is know as varArgs and this is a new variable in java and we can think of it like an array.
//            3)To access the variable that we sent here we do this:

        Access_log.log_it("i","shahin",urls[0]);

//            5)we are going to write code to download from internet:
        StringBuilder result = new StringBuilder();
        URL url;
        HttpURLConnection urlConnection = null;
//            HttpUrlConnection is like a browser. so it open a browser and use it to fetch the content
        try
        {
            url = new URL(urls[0]);
//                this code could give us a error if he had a malformed url.
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);

            InputStream in = urlConnection.getInputStream();
            Access_log.log_it("i","shahin","connected");

//                we use this to do the loading from the net. this is just a stream that holds the input of data
            InputStreamReader reader = new InputStreamReader(in);
//                to read that data, we created this.
            int data = reader.read();
//                which will keep track of the location through the html content that we are currently on.

            Access_log.log_it("i","shahin",String.valueOf(data));

            while (data != -1) {
//                    the data will count through 1 2 3 ... and keeps on going while reading characters,
//                    and when it gets to the end it gets the value of -1.
                char current = (char) data;
//                    this is the current character that is being downloaded.
                result.append(current);
                data = reader.read();
            }
            return result.toString();

        }
        catch (Exception e)
        {
            e.printStackTrace();

            Access_log.log_it("i","shahin",e.toString());

            return "Failed";
        }
//            6)finally we need to ask permission to use internet connection. we do that in the manifest
//            androidManifest.xml file.
    }
}
