package com.amazonaws.sample.lex;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private Context appContext;
    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    // Cognito pool ID. Pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    String COGNITO_POOL_ID = "us-east-1:3a5a9bd2-18ef-4ca4-b077-89c0adcc7de0";
    CognitoCachingCredentialsProvider credentialsProvider;
    // Region of Amazon Polly.
    Regions MY_REGION = Regions.US_EAST_1;
    Context context;



    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");

        wakeLock.acquire();


        Log.d("receive", "receive :" + intent);

        Log.d("gogo" ,"gogo");
        PendingIntent pendingIntent;

        Toast toast = Toast.makeText(context, "알람이 울립니다. ", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();

        wakeLock.release();

        String todo = intent.getStringExtra("todo");

        Intent I = new Intent(context,MyService.class);
        I.putExtra("todo",todo);
        I.setPackage("com.amazonaws.sample.lex");
        context.startService(I);

        //notification(context);
    }



    void notification(Context context) {
        Intent intent = new Intent();



        //알림 사운드
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //큰 아이콘
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_gallery);


        //노티피케이션을 생성할때 매개변수는 PendingIntent이므로 Intent를 pendingIntent로 만들어야 함
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //노티피케이션 빌더 : 위에서 생성한 이미지나 텍스트, 사운드등을 설정
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_gallery)
                .setLargeIcon(bitmap)
                .setContentTitle("알람") //푸시의 타이틀이다.
                .setContentText("알람 딸랑딸랑 ~")// 서버에서 받은 텍스
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //노티피케이션을 생성합니다.

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}


