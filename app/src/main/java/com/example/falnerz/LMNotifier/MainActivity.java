package com.example.falnerz.LMNotifier;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "debugme";

    SwitchCompat ntSwitch;
    private SeekBar ntSeekbar;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final int RC_SIGN_IN = 123;

    MediaPlayer mp;


    private CheckBox.OnClickListener cbClickListener = new CheckBox.OnClickListener(){

        @Override
        public void onClick(View v) {
            boolean checked = ((CheckBox) v).isChecked();
            for(NLService.NotifData notifData: NLService.offlineNotif){
                if(notifData.RId == v.getId()){
                    editor.putBoolean(notifData.notifMessage,checked).commit();
                    return;
                }
            }
            for(NLService.NotifData notifData: NLService.onlineNotif) {
                if(notifData.RId == v.getId()){
                    editor.putBoolean(notifData.notifMessage,checked).commit();
                    return;
                }
            }
            }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mp = MediaPlayer.create(this,R.raw.danger);
        mp.setLooping(true);
        final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        mp.start();

        ntSwitch = findViewById(R.id.ntSwitch);
        ntSeekbar = findViewById(R.id.ntSeekBar);

        ntSwitch.setChecked(sharedPreferences.getBoolean("enabled",false));
        ntSwitch.setText((ntSwitch.isChecked()?"enabled":"disabled"));
        ntSeekbar.setProgress((int) (ntSeekbar.getMax() * sharedPreferences.getFloat("volScale", (float) 0.5)));

        ntSwitch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("enabled",isChecked).apply();
                if(isChecked){
                    ntSwitch.setText("enabled");
                    NLService.isRunning = true;
                    editor.putBoolean("enabled",true);
                }
                else {
                    ntSwitch.setText("disabled");
                    NLService.isRunning = false;
                    editor.putBoolean("enabled",false);
                }

            }
        });



        ntSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        (int) (((float) progress / 1000.0) *
                                ((audioManager != null) ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 0)),
                        0
                );
//                Log.d(TAG, "onProgressChanged: "+audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mp.start();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.pause();
                editor.putFloat("volScale",((float)seekBar.getProgress())/ntSeekbar.getMax()).apply();
                Log.d(TAG, "onStopTrackingTouch: "+seekBar.getProgress());
            }
        });

        initCheckBox();


        Intent intent = new Intent(this,NLService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NotificationManagerCompat.getEnabledListenerPackages (getApplicationContext()).contains(getApplicationContext().getPackageName())) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Notification Read Permission");
            alertDialogBuilder
                    .setMessage("Please enabled notification read permission / tolong izinkan apk membaca notifikasi anda.")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getApplicationContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                }
                            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        requestUsageStatsPermission();

    }

    public void initCheckBox() {
        CheckBox checkBox;
        for(NLService.NotifData notifData: NLService.offlineNotif){
            checkBox = findViewById(notifData.RId);
            checkBox.setOnClickListener(cbClickListener);
            checkBox.setChecked(
                sharedPreferences.getBoolean(notifData.notifMessage,false)
            );
        }
        for(NLService.NotifData notifData: NLService.onlineNotif){
            checkBox = findViewById(notifData.RId);
            checkBox.setOnClickListener(cbClickListener);
            if(sharedPreferences.getBoolean(notifData.notifMessage,false)){
                checkBox.setChecked(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    void requestUsageStatsPermission() {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
