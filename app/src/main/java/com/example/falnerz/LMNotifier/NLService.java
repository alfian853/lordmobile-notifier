package com.example.falnerz.LMNotifier;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.rvalerio.fgchecker.AppChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import static android.os.Environment.*;

/**
 * Created by falnerz on 7/23/18.
 */

@SuppressLint("NewApi")
public class NLService extends NotificationListenerService {

    private String TAG = "debugme";
    //android.example.com.squawker
    //com.igg.android.lordsmobile
    static SharedPreferences sharedPreferences;

    static boolean isRunning = true;

    public static String packageName = "com.igg.android.lordsmobile";
    public static class NotifData {
        int RId,notifId;
        String notifMessage;
        NotifData(int Rid, int notifId, String notifMessage){
            this.RId = Rid;
            this.notifId = notifId;
            this.notifMessage = notifMessage;
        }
    };


    public static NotifData[] onlineNotif = {
            new NotifData(R.id.cbWatcher,-1,"(wa{1,}tche{1,}r{1,}|wahcer{1,}|watcer{1,})"),
            new NotifData(R.id.cbAttack,-1,"kebakaran|serangan|(atta{1,}ck|se{1,}r{1,}a{1,}n{1,}g|invade|wa{1,}r{1,})"),
            new NotifData(R.id.cbScout,-1,"(intai|scout)"),
            new NotifData(R.id.cbDarkness,-1,
                    "(((5|10) minutes|1 hours) rally against a lv([3-5]) darknest)|" +
                                "(((5|10) menit|1 jam) melawan darknest lv([3-5]))"
            ),
            new NotifData(R.id.cbRally,-1,
                    "((5|10) minutes rally against (?!a lv\\d darknest))|"+
                                "((5|10) menit melawan (?!darknest lv\\d))"),
    };

    public static NotifData[] offlineNotif = {
            new NotifData(R.id.cbShield,8,"shield"),
            new NotifData(R.id.cbEnergy,7,"energy"),
            new NotifData(R.id.cbShelter,10,"shelter"),
            new NotifData(R.id.cbMerging,23,"merging"),
            new NotifData(R.id.cbFamiliarGym,15,"gym"),
            new NotifData(R.id.cbCargoReset,13,"cargo"),
            new NotifData(R.id.cbTroopsTraining,2,"training"),
            new NotifData(R.id.cbResearch,1,"research"),
            new NotifData(R.id.cbBuilding,0,"building"),
            new NotifData(R.id.cbMysteryBox,5,"mysterybox"),
            new NotifData(R.id.cbForging,4,"forging"),
            new NotifData(R.id.cbTrapBuilding,3,"trapBuild")
    };

    NotifData getRid(int notifId, String message){

        if(notifId < 10000){//ofline notif
            for(NotifData notifData: offlineNotif) {
                if(notifData.notifId == notifId){
                    if(sharedPreferences.getBoolean(notifData.notifMessage, false)){
                        return notifData;
                    }
                    return null;
                }
            }
        }
        else {//online notif
            message = message.toLowerCase();
            for(NotifData notifData: onlineNotif){
                if(Pattern.compile(notifData.notifMessage).matcher(message).find()){
                    if(sharedPreferences.getBoolean(notifData.notifMessage,false)){
                        return notifData;
                    }
                    return null;
                }
            }
        }

        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "create services");
        sharedPreferences = this.getSharedPreferences("", MODE_PRIVATE);
        isRunning = sharedPreferences.getBoolean("enabled",false);
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: hehe");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved: ");
    }

    public boolean isForeground(String PackageName){
        AppChecker appChecker = new AppChecker();
        String packageName = appChecker.getForegroundApp(this);
        return packageName.equals(this.packageName);
    }

    public void writeToFile(String data)
    {
        // Get the directory for the user's public pictures directory.
        final File path =
                getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                "/notif_logger/"
                        );

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "my_log.txt");

        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            String curTime = new SimpleDateFormat("HH:mm:ss , dd/MM/yyyy").format(Calendar.getInstance().getTime());
            myOutWriter.append("["+curTime+"]\n"+data+"\n");

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG,"**********  onNotificationPosted");
        Notification mNotification=sbn.getNotification();

        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName()+
        "\n"+mNotification.extras.getCharSequence(Notification.EXTRA_TEXT));

        if(sbn.getPackageName().equals(packageName)){
            //hanya debug mode
//
            NotifData notifData = getRid(
                    sbn.getId(),
                    mNotification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                    );

            int Rid = (notifData!=null)?notifData.RId:-1;

            writeToFile(
                    "ID : "+sbn.getId()+
                            "\nTitle : "+sbn.getNotification().tickerText+
                            "\nRid : "+((notifData == null)?"null":notifData.notifMessage)+
                            "\ntext : \n"+
                            mNotification.extras.getCharSequence(Notification.EXTRA_TEXT)
            );

            if(isRunning && Rid != -1 && !isForeground(packageName) ){
                Intent intent = new Intent(this,AlarmActivity.class);
                intent.putExtra("message",sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }

}