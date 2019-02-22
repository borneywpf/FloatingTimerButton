package com.th.flytimer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ftb.library.FloatingTimerButton;

public class MainActivity extends AppCompatActivity {

    private FloatingTimerButton mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.button);
    }

    public void onClick(View view) {
        Log.d("wpf", "onClick");
    }

    public void startClick(View view) {
        mButton.startTimer();
    }

    public void pauseClick(View view) {
        mButton.pauseTimer();
    }

    public void endClick(View view) {
        mButton.endTimer();
    }

    public void resetClick(View view) {
        mButton.setProgress(0);
    }

    public void setTimeClick(View view) {
        mButton.setTimeSecond(20);
    }
}
