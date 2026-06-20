package net.mixoftix.tallybox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Access_internet_IMAGE extends AsyncTask<String, Void, Bitmap>
{
    @Override
    protected Bitmap doInBackground(String... imageurls) {

        URL url;
        HttpURLConnection httpURLConnection;

        try {
            url = new URL(imageurls[0]);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            InputStream in =httpURLConnection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(in);
            return myBitmap;

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
