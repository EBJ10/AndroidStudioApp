package com.example.faceemotiontest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class activity_negative extends AppCompatActivity {
    private Button button4;

    Button button5, button6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negative);

        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goLink("https://www.rit.edu/behindthebricks/content/10-ways-boost-your-mood");
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goLink("https://www.insider.com/funny-animal-photos-perfect-timing-2019-3#this-fox-has-perfect-aim-4");
            }
        });

        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivitymain2();
            }
        });
    }

    private void goLink(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(getIntent().ACTION_VIEW, uri));
    }

    public void openactivitymain2(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}