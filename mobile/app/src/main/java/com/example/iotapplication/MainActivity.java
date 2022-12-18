package com.example.iotapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.net.*;
import java.io.*;

import java.util.concurrent.TimeUnit;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv=findViewById(R.id.textView);

        createNotificationChannel();
        //RequestSender RC = new RequestSender();
        //RC.execute(this);
    }
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "myCname";
            String description = "myCD";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("101", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void TestNot(View view)
    {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "101")
                .setSmallIcon(R.drawable.warningicon)
                .setContentTitle("ВНИМАНИЕ")
                .setContentText("сработала сигнализация ")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);



        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(101, builder.build());
         /*
        Toast toast = Toast.makeText(getApplicationContext(),
                "Пора покормить кота!", Toast.LENGTH_SHORT);
        toast.show();

          */

    }
}
/*
class RequestFactory extends AsyncTask<Void, Void, Void>
{


    @Override
    protected Void doInBackground(Void... voids)
    {
        while (true)
        {
            RequestSender RS = new RequestSender();
            RS.execute();
            String data= "";
            try {
                data = RS.get();
            }
            catch (Exception e) {
                data="error\n"+e.toString();
            }

            if (data.length()>=5 && data.substring(0,5)=="error")
            {
                new AlertDialog.Builder(window)
                        .setTitle("Error")
                        .setMessage(data)
                        .show();
            }
            else if (data!="no")
            {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(window, "channelID")
                        .setContentTitle("ВНИМАНИЕ")
                        .setContentText("сработала сигнализация "+data)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            }

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                new AlertDialog.Builder(window)
                        .setTitle("Time Error")
                        .setMessage(e.toString())
                        .show();
            }
        }
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        mInfoTextView.setText("Этаж: " + values[0]);
        mHorizontalProgressBar.setProgress(values[0]);
    }
}
*/
class Box
{
    String answer;
    MainActivity window;

    public Box(MainActivity win, String data) {
        window=win;
        answer=data;
    }
}
class RequestSender extends AsyncTask<MainActivity, Box, Void> {
    @Override
    protected Void doInBackground(MainActivity... wins)
    {
        while (true) {
            String data = "";
            try {
                URL url = new URL("http://192.168.0.100:8080");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "text/html");
                con.setConnectTimeout(1500);
                con.setReadTimeout(1500);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    data += inputLine;
                }
                in.close();
            } catch (Exception e) {
                data = "error\n" + e.toString();
            }
            publishProgress(new Box(wins[0],data));

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                /*
                new AlertDialog.Builder(window)
                        .setTitle("Time Error")
                        .setMessage(e.toString())
                        .show();

                 */
            }
        }

    }
    @Override
    protected void onProgressUpdate(Box... boxes) {
        super.onProgressUpdate();
        if (boxes[0].answer!="no")
        {

        }

        if (boxes[0].answer.length() >= 5 && boxes[0].answer.substring(0, 5) == "error") {
            new AlertDialog.Builder(boxes[0].window)
                    .setTitle("Error")
                    .setMessage(boxes[0].answer)
                    .show();
        } else if (boxes[0].answer != "no") {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(boxes[0].window, "channelID")
                    .setContentTitle("ВНИМАНИЕ")
                    .setContentText("сработала сигнализация " + boxes[0].answer)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
    }
}