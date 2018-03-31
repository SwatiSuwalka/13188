package com.example.android.attendanceassure;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView t;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       t = (TextView) findViewById(R.id.text1) ;
        Typeface myfont = Typeface.createFromAsset(getAssets(),"fonts/Sofia-Regular.otf");
        t.setTypeface(myfont);

      /* t = (TextView) findViewById(R.id.heading) ;
       Typeface myfont1 = Typeface.createFromAsset(getAssets(),"fonts/Pacifico.ttf");
       t.setTypeface(myfont1);*/



        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {

                Intent i=new Intent(MainActivity.this,welcomewindow.class);
                startActivity(i);
            }
        }, 2000);

    }
}
