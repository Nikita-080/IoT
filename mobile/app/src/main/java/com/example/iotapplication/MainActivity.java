package com.example.iotapplication;

import androidx.appcompat.app.AppCompatActivity;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutionException;

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


    public void SendOffSignal(View view) throws IOException, ExecutionException, InterruptedException {
        RequestSender RS = new RequestSender();//.execute("");
        RS.execute();
        String data=RS.get();
        new AlertDialog.Builder(this)
                .setTitle("Info")
                .setMessage(data)
                .show();
    }

}
class RequestSender extends AsyncTask<Void, Void, String> {
    @Override
    protected String doInBackground(Void... voids)
    {

        String data="";
        try {
            URL url = new URL("http://192.168.0.100:8080");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "text/html");
            con.setConnectTimeout(1500);
            con.setReadTimeout(3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                data+=inputLine;
            }
            in.close();
        }
        catch (Exception e)
        {
            return "error\n"+e.toString();
        }
        return data;
    }

}