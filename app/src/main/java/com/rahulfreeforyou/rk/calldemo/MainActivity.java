package com.rahulfreeforyou.rk.calldemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rahulfreeforyou.rk.calldemo.Service.CallDetectService;
import com.rahulfreeforyou.rk.calldemo.Service.CheckPermission;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static TextView name,num,time;
    EditText etPhoneNumber;
    Button btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name= findViewById(R.id.name);
        num = findViewById(R.id.num);
        time = findViewById(R.id.time);

        askPermission();
        startService(new Intent(MainActivity.this, CallDetectService.class));


    }

    private void askPermission() {
        CheckPermission checkPermission = new CheckPermission();
        checkPermission.checkPermission(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
