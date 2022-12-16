package com.example.iotapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    public void examplebutton(View view) {
        TextView mytextview;
        mytextview = (TextView) findViewById(R.id.textView) ;
        mytextview.setText("Click");
    }
}