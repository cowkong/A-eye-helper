package com.amazonaws.sample.lex.SMS;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

import com.amazonaws.sample.lex.R;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SmsActivity extends Activity {
    private static final String TAG = "SmsActivity";
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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms2);
        init();
        new GetPollyVoices().execute();
        play();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void init(){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );

        // Create a client that supports generation of presigned URLs.
        client = new AmazonPollyPresigningClient(credentialsProvider);
        /*
        appContext = getApplication();
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider(
                "us-east-1:3a5a9bd2-18ef-4ca4-b077-89c0adcc7de0",
                Regions.fromName(appContext.getResources().getString(R.string.cognito_region)));

        // Create a client that supports generation of presigned URLs.
        client = new AmazonPollyPresigningClient(credentialsProvider);
        */
    }
    private class GetPollyVoices extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (voices != null) {
                return null;
            }
            // Create describe voices request.
            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

            DescribeVoicesResult describeVoicesResult;
            try {
                // Synchronously ask the Polly Service to describe available TTS voices.
                describeVoicesResult = client.describeVoices(describeVoicesRequest);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unable to get available voices. " + e.getMessage());
                return null;
            }

            // Get list of voices from the result.
            voices = describeVoicesResult.getVoices();

            // Log a message with a list of available TTS voices.
            Log.i(TAG, "Available Polly voices: " + voices);

            return null;
        }
    }
    public void play(){
        Intent i = getIntent();
        String readSMS = i.getStringExtra("smsMessage");
        // Create speech synthesis request.
        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                new SynthesizeSpeechPresignRequest()
                        // Set the text to synthesize.
                        .withText(getName()+ "에게" + readSMS + ". 메시지 도착")
                        // Select voice for synthesis.
                        .withVoiceId("Seoyeon")
                        // Set format to MP3.
                        .withOutputFormat(OutputFormat.Mp3);

// Get the presigned URL for synthesized speech audio stream.
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
    public String getName() {
        Intent i = getIntent();
        String pn = i.getStringExtra("contactPerson");
        //String pn = "01041564451";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pn));
        String[] projection = new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME};
        String name = "";
        Cursor cursor = getBaseContext().getContentResolver().query(uri,projection,null,null,null);
        if (cursor != null){
            if(cursor.moveToFirst()){
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name;
    }
}
