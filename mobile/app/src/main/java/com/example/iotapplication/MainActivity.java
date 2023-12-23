package com.example.iotapplication;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    int id=100;
    TextView tv;
    TextView tv2;
    TextView tv_token;
    Button btn_on;
    Button btn_off;
    Button btn_qr;
    String CommandPath ="Command.txt";
    String TokenPath="Token.txt";
    String AddressPath="ServerAddress.txt";
    Logger logger;
    String ServerAddress;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv=findViewById(R.id.textView);
        tv2=findViewById(R.id.textView2);
        tv_token=findViewById(R.id.textView3);
        btn_on = findViewById(R.id.button2);
        btn_off = findViewById(R.id.button);
        btn_qr = findViewById(R.id.button3);

        logger = Logger.getLogger(MainActivity.class.getName());
        logger.log(Level.INFO,"application start");

        //LOGGER CONFIG [BEGIN]
        try {
            String logFileName = Environment.getExternalStorageDirectory() + File.separator + "Iot_App_Log_%g.log";
            FileHandler logHandler = null;
            logHandler = new FileHandler(logFileName, 100 * 1024, 5, true);
            logHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(logHandler);
            logger.log(Level.INFO, "logger is ready");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "logger is not ready");
        }
        //LOGGER CONFIG [END]

        btn_on.setOnClickListener(v -> SetStringToFile("on", CommandPath));
        btn_off.setOnClickListener(v -> SetStringToFile("off", CommandPath));
        btn_qr.setOnClickListener(this);

        createNotificationChannel();

        token=GetStringFromFile(TokenPath);
        ServerAddress=GetStringFromFile(AddressPath);
        tv_token.setText(token);

        ShowConnectionOff();

        RequestSender RC = new RequestSender();
        RC.execute();
    }
    @Override
    public void onClick(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                logger.log(Level.WARNING, "QR reading - canceled");
            } else {
                String[] ConnectionData=intentResult.getContents().split("-");
                ServerAddress=ConnectionData[0];
                token=ConnectionData[1];
                SetStringToFile(ServerAddress,AddressPath);
                SetStringToFile(token,TokenPath);
                tv_token.setText(token);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String GetStringFromFile(String path)
    {
        try {
            FileInputStream fin = openFileInput(path);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            fin.close();
            return new String (bytes);
        }
        catch(FileNotFoundException ex) {
            SetStringToFile("none",path);
        }
        catch(IOException ex) {
            logger.log(Level.SEVERE,"read file "+path);
        }
        return "none";
    }
    private void SetStringToFile(String data,String path)
    {
        try {
            FileOutputStream fos = openFileOutput(path,MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        }
        catch(IOException ex) {
            logger.log(Level.SEVERE,"write file "+path);
        }

    }
    private void ShowConnectionOff()
    {
        tv2.setText("Подключение отсутствует");
        tv2.setTextColor(Color.RED);
    }
    private void ShowConnectionOn()
    {
        tv2.setText("Подключено");
        tv2.setTextColor(Color.GREEN);
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

    @SuppressLint("StaticFieldLeak")
    class RequestSender extends AsyncTask<Void, String, Void> {
        SSLSocketFactory GetSSL() {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(getResources().openRawResource(R.raw.app));
                Certificate ca = cf.generateCertificate(caInput);
                caInput.close();
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
                return context.getSocketFactory();
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE,"SSL error",e);
                return null;
            }
        }
        @SuppressLint("AllowAllHostnameVerifier")
        @Override
        protected Void doInBackground(Void... voids)
        {
            while (true) {
                String command=GetStringFromFile(CommandPath);
                if (!command.equals("none"))
                {
                    boolean endOperation=false;
                    while (!endOperation) {
                        try {
                            command=GetStringFromFile(CommandPath);
                            logger.log(Level.INFO,"try to send command "+command);

                            URL obj = new URL(ServerAddress);
                            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                            con.setHostnameVerifier(new AllowAllHostnameVerifier()); //only for testing app
                            con.setSSLSocketFactory(GetSSL());
                            con.setRequestMethod("POST");
                            con.setConnectTimeout(2000);
                            con.setRequestProperty("Content-Type", "text/html");
                            con.setRequestProperty("User-Agent", "mobile");
                            con.setRequestProperty("Token", token);
                            con.setDoOutput(true);

                            OutputStream os = con.getOutputStream();
                            byte[] input = command.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);

                            int r=con.getResponseCode();

                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuilder data=new StringBuilder();
                            while ((inputLine = in.readLine()) != null)
                            {
                                data.append(inputLine);
                            }
                            in.close();


                            publishProgress("connection on");
                            endOperation=true;
                            SetStringToFile("none", CommandPath);
                            logger.log(Level.INFO,"command "+command+" sent, code: "+ r +" data: "+data);

                        } catch (Exception e) {
                            logger.log(Level.SEVERE,"connection failure",e);
                            publishProgress("connection off");
                            try {TimeUnit.SECONDS.sleep(2);} catch (InterruptedException ignored) {}
                        }
                    }
                }

                StringBuilder data = new StringBuilder();
                try {
                    URL url = new URL(ServerAddress);
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    con.setHostnameVerifier(new AllowAllHostnameVerifier()); //only for testing app
                    con.setSSLSocketFactory(GetSSL());
                    con.setConnectTimeout(2000);
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Content-Type", "text/html");
                    con.setRequestProperty("User-Agent", "mobile");
                    con.setRequestProperty("Token", token);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        data.append(inputLine);
                    }
                    in.close();
                    publishProgress("connection on");
                } catch (Exception e) {
                    logger.log(Level.SEVERE,"connection failure",e);
                    publishProgress("connection off");
                }
                publishProgress(data.toString());

                try {TimeUnit.SECONDS.sleep(3);} catch (InterruptedException ignored) {}
            }
        }

        @Override
        protected void onProgressUpdate(String... messages) {
            super.onProgressUpdate(messages[0]);

            switch (messages[0]) {
                case "w":
                    logger.log(Level.INFO, "alarm signal");
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "101")
                            .setSmallIcon(R.drawable.signalicon)
                            .setContentTitle("ВНИМАНИЕ")
                            .setContentText("сработала сигнализация")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    id += 1;
                    if (id > 200) id = 101;

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify(id, builder.build());

                    Date date = new Date();
                    tv.setText(date.toString());
                    break;
                case "connection on":
                    ShowConnectionOn();
                    break;
                case "connection off":
                    ShowConnectionOff();
                    break;
            }
        }
    }
}
