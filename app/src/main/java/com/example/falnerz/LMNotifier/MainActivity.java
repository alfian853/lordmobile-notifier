package com.example.falnerz.LMNotifier;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity  {



    private static final String TAG = "debugme";

    SwitchCompat ntSwitch;
    SeekBar ntSeekbar;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

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

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: ");
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build()
                    );
                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };


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


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                // ...
            } else {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
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
        switch (item.getItemId()){

        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * random String 10 karakter
    * 5 dikalikan
    * 5 karakter depan modulusnya
    * */

}