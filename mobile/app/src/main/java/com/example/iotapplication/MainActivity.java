package com.example.iotapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    int id=100;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv=findViewById(R.id.textView);

        createNotificationChannel();

        RequestSender RC = new RequestSender();
        RC.execute(this);
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

    class RequestSender extends AsyncTask<MainActivity, String, Void> {
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
                    data = "error " + e.getMessage();
                }
                publishProgress(data);

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    data = "error " + e.getMessage();
                    publishProgress(data);
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... messages) {
            super.onProgressUpdate(messages[0]);

            if (messages[0].length() >= 5 && messages[0].startsWith("error")) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "101")
                        .setSmallIcon(R.drawable.warningicon)
                        .setContentTitle("Ошибка")
                        .setContentText(messages[0])
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(100, builder.build());
            }
            else if (!messages[0].equals("no")) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "101")
                        .setSmallIcon(R.drawable.signalicon)
                        .setContentTitle("ВНИМАНИЕ")
                        .setContentText("сработала сигнализация")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                id+=1;
                if (id>200) id=101;

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(id, builder.build());

                tv.setText(messages[0]);
            }
        }
    }
}
