package com.example.falnerz.LMNotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by falnerz on 7/25/18.
 */

public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = "debugme";
    private TextView tvNotif;
    Button btStopAlert;

    MediaPlayer mp;
    int btTapCounter = 0;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        String ntMessage  = getIntent().getStringExtra("message");
        getIntent().removeExtra("message");
        tvNotif = findViewById(R.id.tvNotif);
        btStopAlert = findViewById(R.id.btStopExit);
        sharedPreferences = this.getSharedPreferences("", MODE_PRIVATE);
        tvNotif.setText(ntMessage);
        mp = MediaPlayer.create(this,R.raw.danger);
        mp.setLooping(true);
        mp.seekTo(360000);
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                (int) (sharedPreferences.getFloat("volScale", (float) 0.5) * (audioManager != null ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 0)),
                0
        );
        mp.start();

        btStopAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btTapCounter == 1){
                    finish();
                    return;
                }
                mp.stop();
                btTapCounter++;
                Toast.makeText(getApplicationContext(),"tap again to exit", LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
