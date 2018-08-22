package com.amazonaws.sample.lex;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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

public class MyService extends Service {

    private static final String TAG = "service";
    private Context appContext;
    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    // Cognito pool ID. Pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    String COGNITO_POOL_ID = "us-east-1:3a5a9bd2-18ef-4ca4-b077-89c0adcc7de0";
    CognitoCachingCredentialsProvider credentialsProvider;
    // Region of Amazon Polly.
    Regions MY_REGION = Regions.US_EAST_1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public MyService() {
    }


    public void init() {

        appContext = getApplicationContext();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                appContext.getResources().getString(R.string.identity_id_test),
                Regions.fromName(appContext.getResources().getString(R.string.cognito_region))
        );

        // Create a client that supports generation of presigned URLs.
        client = new AmazonPollyPresigningClient(credentialsProvider);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "서비스가 실행됩니다 1.", Toast.LENGTH_SHORT).show();

        init();
        final String todo = intent.getStringExtra("todo");

        new Thread() {
            public void run(){

            play(todo);
            }
        }.start();
        return START_STICKY;

    }

    public void play(String todo){
        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                new SynthesizeSpeechPresignRequest()
                        // Set the text to synthesize.
                        .withText(todo + "할 시간이 되었습니다.")
                        // Select voice for synthesis.
                        .withVoiceId("Seoyeon")
                        // Set format to MP3.
                        .withOutputFormat(OutputFormat.Mp3);

        URL presignedSynthesizeSpeechUrl =
                client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);
        // Use MediaPlayer: https://developer.android.com/guide/topics/media/mediaplayer.html

        // Create a media player to play the synthesized audio stream.
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set media player's data source to previously obtained URL.
            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
        } catch (IOException e) {
            Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
        }

// Prepare the MediaPlayer asynchronously (since the data source is a network stream).
        mediaPlayer.prepareAsync();

// Set the callback to start the MediaPlayer when it's prepared.
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

// Set the callback to release the MediaPlayer after playback is completed.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}
