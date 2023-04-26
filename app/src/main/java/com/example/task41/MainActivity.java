package com.example.task41;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    Button startCancelButton;
    Button playPauseButton;
    EditText restTimeInput;
    EditText workoutTimeInput;
    ProgressBar progressBar;
    TextView resultTextView;
    TextView phaseView;
    LinearLayout timerLayout;

    Boolean timerStarted = false;
    Boolean timerPaused = false;
    long timerTimeLeft = 0;

    Boolean workoutPhase = false;
    Boolean restPhase = false;
    Integer workoutTime = 0;
    Integer restTime = 0;
    CountDownTimer timer = null;
    public static boolean isNumber(String str) {
        try {
            double v = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException nfe) {
        }
        return false;
    }
    public void timerController(long timeLeft){
        timer = new CountDownTimer(timeLeft, 1000){
            public void onTick(long millisUntilFinished){
                timerTimeLeft = millisUntilFinished;
                long minutesUntilFinished = millisUntilFinished/(60000);
                long secondsUntilFinished = (millisUntilFinished/1000) % 60;
                secondsUntilFinished++;
                int progressValue = 0;
                if(workoutPhase) {
                    progressValue = (int) ((millisUntilFinished* progressBar.getMax()) / (workoutTime * 1000)) ;
                    Log.v("progressValue", String.valueOf(progressValue));
                    Log.v("workoutPhase", workoutPhase.toString());
                }
                if(restPhase){
                    progressValue = (int) ((millisUntilFinished* progressBar.getMax()) / (restTime * 1000)) ;
                    Log.v("progressValue", String.valueOf(progressValue));
                    Log.v("restPhase", restPhase.toString());
                }
                progressBar.setProgress(progressValue);
                String minutesText = "";
                String secondsText = "";
                if(minutesUntilFinished < 10) {
                    minutesText = "0"+String.valueOf(minutesUntilFinished);
                }
                else{
                    minutesText = ""+ minutesUntilFinished;
                }
                if(secondsUntilFinished < 10) {
                    secondsText = "0"+String.valueOf(secondsUntilFinished);
                }
                else{
                    secondsText = ""+ secondsUntilFinished;
                }
                resultTextView.setText(minutesText+":"+secondsText);
            }
            public  void onFinish(){
                switchTimer();
                resultTextView.setText("00:00");
            }
        }.start();
    }

    public void switchTimer(){
        if(workoutPhase){
            // Switch to rest phase
            restPhase = true;
            workoutPhase = false;
            phaseView.setText("Rest!");
            progressBar.getProgressDrawable().setColorFilter(
                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "Notification");
            builder.setContentTitle("Workout Time App");
            builder.setContentText("Rest phase start!");
            builder.setSmallIcon(R.drawable.ic_launcher_background);
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setAutoCancel(true);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
            managerCompat.notify(1,builder.build());
            timerController(restTime*1000);
        }
        else{
            // Switch to workout phase
            restPhase = false;
            workoutPhase = true;
            phaseView.setText("Workout!");
            progressBar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "Notification");
            builder.setContentTitle("Workout Time App");
            builder.setContentText("Workout phase start!");
            builder.setSmallIcon(R.drawable.ic_launcher_background);
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setAutoCancel(true);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
            managerCompat.notify(1,builder.build());
            timerController(workoutTime*1000);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager =  getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        startCancelButton = (Button) findViewById(R.id.cancelButton);
        playPauseButton = (Button) findViewById(R.id.playPauseButton);
        restTimeInput =  findViewById(R.id.restTime);
        workoutTimeInput = findViewById(R.id.workoutTime);
        progressBar = findViewById(R.id.progressBar);
        resultTextView = findViewById(R.id.resultTextView);
        phaseView = findViewById(R.id.phaseView);
        timerLayout = findViewById(R.id.timerLayout);
        progressBar.setScaleY(3f);
        // Hide timer layout
        timerLayout.setVisibility(View.GONE);

        startCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerStarted) {
                    String workoutTimeString = workoutTimeInput.getText().toString();
                    String restTimeString = restTimeInput.getText().toString();
                    if (!isNumber(workoutTimeString)) {
                        Toast.makeText(MainActivity.this, "Workout Time is invalid!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isNumber(restTimeString)) {
                        Toast.makeText(MainActivity.this, "Rest Time is invalid!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    timerLayout.setVisibility(View.VISIBLE);
                    restTimeInput.setText("");
                    workoutTimeInput.setText("");
                    workoutTime = Integer.parseInt(workoutTimeString);
                    restTime = Integer.parseInt(restTimeString);
                    Log.v("workoutTime", workoutTime.toString());
                    Log.v("restTime", restTime.toString());
                    startCancelButton.setText("Cancel");
                    timerStarted = true;
                    restPhase = true;
                    switchTimer();
                } else {
                    timerStarted = false;
                    workoutPhase = false;
                    restPhase = false;
                    timerPaused = false;
                    timer.cancel();
                    workoutTime = 0;
                    restTime = 0;
                    timer = null;
                    startCancelButton.setText("Start");
                    playPauseButton.setText("Pause");
                    timerLayout.setVisibility(View.GONE);
                }
            }
        });
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerStarted) {
                    return;
                }
                if(!timerPaused){
                    timerPaused = true;
                    timer.cancel();
                    playPauseButton.setText("Play");
                }
                else{
                    timerPaused = false;
                    timerController(timerTimeLeft);
                    playPauseButton.setText("Pause");
                }
            }

        });

    }
}