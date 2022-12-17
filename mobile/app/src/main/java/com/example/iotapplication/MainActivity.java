package com.example.iotapplication;

import androidx.appcompat.app.AppCompatActivity;
import java.net.*;
import java.io.*;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    public void SendOffSignal(View view) throws IOException {
        new RequestSender().execute("https://192.168.0.100:8080");
    }

}
class RequestSender extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls)
    {
        String data="";
        try {
            URL url = new URL("https://192.168.0.100:8080");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                data+=inputLine;
            }
            in.close();
            /*
            new AlertDialog.Builder(this)
                    .setTitle("Info")
                    .setMessage("Answer recieved\n"+data)
                    .show();

             */
        }
        catch (Exception e)
        {
            /*
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to send request\n"+e.toString())
                    .show();

             */
        }
        return null;
    }
}