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
import java.security.cert.CertificateException;
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
    Integer notificationId;

    TextView textLastAlarm;
    TextView textConnectionStatus;
    TextView textToken;
    Button buttonOn;
    Button buttonOff;
    Button buttonQR;

    final String LAST_COMMAND_FILE_PATH = "Command.txt";
    final String TOKEN_PATH = "Token.txt";
    final String SERVER_ADDRESS_PATH = "ServerAddress.txt";

    final Integer LOG_FILE_COUNT = 5;
    final Integer LOG_SIZE = 102400;
    final Boolean LOG_APPEND = true;
    final String LOG_FILE_NAME_PATTERN = "Iot_App_Log_%g.log";

    final Integer CONNECTION_TIMEOUT = 2000;
    final Integer REQUESTS_TIMEOUT = 2;

    final Integer FIRST_NOTIFICATION_ID = 100;
    final Integer MAX_NOTIFICATION_ID = 200;


    final Integer CONNECTION_ON_CODE = 0;
    final Integer CONNECTION_OFF_CODE = 1;
    final Integer ALARM_CODE = 2;

    Logger logger;
    String serverAddress;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLastAlarm = findViewById(R.id.textView);
        textConnectionStatus = findViewById(R.id.textView2);
        textToken = findViewById(R.id.textView3);
        buttonOn = findViewById(R.id.button2);
        buttonOff = findViewById(R.id.button);
        buttonQR = findViewById(R.id.button3);

        notificationId = FIRST_NOTIFICATION_ID;

        logger = Logger.getLogger(MainActivity.class.getName());
        logger.log(Level.INFO, "application start");

        try {
            String logFileName = Environment.getExternalStorageDirectory() +
                    File.separator +
                    LOG_FILE_NAME_PATTERN;
            FileHandler logHandler = new FileHandler(logFileName, LOG_SIZE,
                    LOG_FILE_COUNT, LOG_APPEND);
            logHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(logHandler);

            logger.log(Level.INFO, "logger is ready");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "logger is not ready",e);
        }

        buttonOn.setOnClickListener(v -> setStringToFile("on",
                LAST_COMMAND_FILE_PATH));
        buttonOff.setOnClickListener(v -> setStringToFile("off",
                LAST_COMMAND_FILE_PATH));
        buttonQR.setOnClickListener(this);

        createNotificationChannel();

        token = getStringFromFile(TOKEN_PATH);
        serverAddress = getStringFromFile(SERVER_ADDRESS_PATH);

        textToken.setText(token);

        showConnectionOff();

        RequestSender requestSender = new RequestSender();
        requestSender.execute();
    }

    @Override
    public void onClick(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,
                resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                logger.log(Level.WARNING, "QR reading - canceled");
            } else {
<<<<<<< Updated upstream
                String[] ConnectionData=intentResult.getContents().split("-");
                ServerAddress=ConnectionData[0];
                token=ConnectionData[1];
                SetStringToFile(ServerAddress,AddressPath);
                SetStringToFile(token,TokenPath);
                tv_token.setText(ServerAddress+' '+token);
=======
                String[] ConnectionData = intentResult.getContents().split("-");
                serverAddress = ConnectionData[0];
                token = ConnectionData[1];

                setStringToFile(serverAddress, SERVER_ADDRESS_PATH);
                setStringToFile(token, TOKEN_PATH);

                textToken.setText(token);
>>>>>>> Stashed changes
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getStringFromFile(String path) {
        try {
            FileInputStream fin = openFileInput(path);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            fin.close();
            return new String(bytes);
        } catch (FileNotFoundException ex) {
            setStringToFile("none", path);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("read file %s", path));
        }
        return "none";
    }

    private void setStringToFile(String data, String path) {
        try {
            FileOutputStream fos = openFileOutput(path, MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("write file %s", path));
        }

    }

    private void showConnectionOff() {
        textConnectionStatus.setText("Подключение отсутствует");
        textConnectionStatus.setTextColor(Color.RED);
    }

    private void showConnectionOn() {
        textConnectionStatus.setText("Подключено");
        textConnectionStatus.setTextColor(Color.GREEN);
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
    class RequestSender extends AsyncTask<Void, Integer, Void> {
        SSLSocketFactory getSSL() {
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
            } catch (CertificateException e) {
                logger.log(Level.SEVERE, "SSL error", e);
                return null;
            }
        }

        @SuppressLint("AllowAllHostnameVerifier")
        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                String command = getStringFromFile(LAST_COMMAND_FILE_PATH);
                if (!command.equals("none")) {
                    boolean endOperation = false;
                    while (!endOperation) {
                        try {
<<<<<<< Updated upstream
                            command=GetStringFromFile(CommandPath);
                            logger.log(Level.INFO,"try to send command "+command);
                            //ServerAddress="https://192.168.43.42:8080";//DEL
                            //token="Q66U";//DEL
                            URL obj = new URL(ServerAddress);
=======
                            command = getStringFromFile(LAST_COMMAND_FILE_PATH);
                            logger.log(Level.INFO, String.format("try to send command %s", command));

                            URL obj = new URL(serverAddress);
>>>>>>> Stashed changes
                            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                            con.setHostnameVerifier(new AllowAllHostnameVerifier()); //only for testing app
                            con.setSSLSocketFactory(getSSL());
                            con.setRequestMethod("POST");
                            con.setConnectTimeout(CONNECTION_TIMEOUT);
                            con.setRequestProperty("Content-Type", "text/html");
                            con.setRequestProperty("User-Agent", "mobile");
                            con.setRequestProperty("token", token);
                            con.setDoOutput(true);

                            OutputStream os = con.getOutputStream();
                            byte[] input = command.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);

<<<<<<< Updated upstream
                            int code=con.getResponseCode();
=======
                            int code = con.getResponseCode();
>>>>>>> Stashed changes

                            InputStreamReader isr = new InputStreamReader(con.getInputStream());
                            BufferedReader br = new BufferedReader(isr);
                            String inputLine;
                            StringBuilder data = new StringBuilder();
                            while ((inputLine = br.readLine()) != null) {
                                data.append(inputLine);
                            }
                            br.close();

<<<<<<< Updated upstream
                            if (code==200)
                            {
                                publishProgress("connection on");
                                endOperation=true;
                                SetStringToFile("none", CommandPath);
                                logger.log(Level.INFO,"command "+command+" sent, code: "+ code +" data: "+data);
                            }
                            else
                            {
                                publishProgress("connection off");
                                logger.log(Level.SEVERE,"connection error: code "+code);
                            }
=======

                            publishProgress(CONNECTION_ON_CODE);
                            endOperation = true;
                            setStringToFile("none", LAST_COMMAND_FILE_PATH);
                            logger.log(Level.INFO, String.format("command %s sent, code: %d", command, code));

>>>>>>> Stashed changes
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "connection failure", e);
                            publishProgress(CONNECTION_OFF_CODE);
                            try {
                                TimeUnit.SECONDS.sleep(REQUESTS_TIMEOUT);
                            } catch (InterruptedException ignored) {

                            }
                        }
                    }
                }

                StringBuilder data = new StringBuilder();
                try {
<<<<<<< Updated upstream
                    //ServerAddress="https://192.168.43.42:8080";//DEL
                    //token="HeUb";//DEL
                    URL url = new URL(ServerAddress);
=======
                    URL url = new URL(serverAddress);
>>>>>>> Stashed changes
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                    con.setHostnameVerifier(new AllowAllHostnameVerifier()); //only for testing app
                    con.setSSLSocketFactory(getSSL());
                    con.setConnectTimeout(CONNECTION_TIMEOUT);
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Content-Type", "text/html");
                    con.setRequestProperty("User-Agent", "mobile");
<<<<<<< Updated upstream
                    con.setRequestProperty("token", token);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
=======
                    con.setRequestProperty("Token", token);

                    InputStreamReader isr = new InputStreamReader(con.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
>>>>>>> Stashed changes
                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        data.append(inputLine);
                    }
<<<<<<< Updated upstream
                    in.close();
                    int code=con.getResponseCode();
                    if (code==200)
                    {
                        publishProgress("connection on");
                    }
                    else
                    {
                        publishProgress("connection off");
                        logger.log(Level.SEVERE,"connection error: code "+code);
                    }
=======
                    br.close();
                    publishProgress(CONNECTION_ON_CODE);
>>>>>>> Stashed changes
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "connection failure", e);
                    publishProgress(CONNECTION_OFF_CODE);
                }
                if (data.toString().equals("w")) {
                    publishProgress(ALARM_CODE);

                    try {
                        TimeUnit.SECONDS.sleep(REQUESTS_TIMEOUT);
                    } catch (InterruptedException ignored) {

                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... messages) {
            super.onProgressUpdate(messages[0]);

            switch (messages[0]) {
                case 0:
                    showConnectionOn();
                    break;
                case 1:
                    showConnectionOff();
                    break;
                case 2:
                    logger.log(Level.INFO, "alarm signal");
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "101")
                            .setSmallIcon(R.drawable.signalicon)
                            .setContentTitle("ВНИМАНИЕ")
                            .setContentText("сработала сигнализация")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationId += 1;
                    if (notificationId > MAX_NOTIFICATION_ID)
                        notificationId = FIRST_NOTIFICATION_ID;

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify(notificationId, builder.build());

                    Date date = new Date();
                    textLastAlarm.setText(date.toString());
                    break;
            }
        }
    }
}
