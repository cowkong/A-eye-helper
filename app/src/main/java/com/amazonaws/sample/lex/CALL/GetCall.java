package com.amazonaws.sample.lex.CALL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.sample.lex.CONTACT.GetContact;
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

public class GetCall extends Activity {
    private static final String TAG = "makeCall";
    private Context appContext;
    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    // Cognito pool ID. Pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_call);
        init();
        new GetCall.GetPollyVoices().execute();
        play();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void init() {

        appContext = getApplicationContext();
        CognitoIdentification();
    }

    private class GetPollyVoices extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (voices != null) {
                return null;
            }

            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

            DescribeVoicesResult describeVoicesResult;
            try {
                describeVoicesResult = client.describeVoices(describeVoicesRequest);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unable to get available voices. " + e.getMessage());
                return null;
            }

            // 결과에서 여러 voice들을 받아옴.
            voices = describeVoicesResult.getVoices();
            Log.i(TAG, "Available Polly voices: " + voices);

            return null;
        }
    }

    public void play() {
        // Intent i = getIntent();
        // String readSMS = i.getStringExtra("smsMessage");
        //Intent i = getIntent();
        //String pn = i.getStringExtra("pNum");
        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                new SynthesizeSpeechPresignRequest()
                        // 읽어줄 메시지
                        //.withText(pn+ "에게 전화가 왔습니다.")
                        .withText(getName() + "에게 전화가 왔습니다.")
                        // voice 선택.
                        .withVoiceId("Seoyeon")
                        // format은 MP3로 선택.
                        .withOutputFormat(OutputFormat.Mp3);

        // 오디오 스트림을 위해 presigned URL 받아옴
        URL presignedSynthesizeSpeechUrl =
                client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

        // 오디오 스트림 재생 위해 media player 생성
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
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
        String name = "";
        Intent i = getIntent();
        String pn1 = i.getStringExtra("pNum");
        String pn = pn1.replace("-", "");

        name = GetContact.getName(pn,appContext);

        return name;
    }

    private void CognitoIdentification() {
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider(
                appContext.getResources().getString(R.string.identity_id_test),
                Regions.fromName(appContext.getResources().getString(R.string.cognito_region)));

        client = new AmazonPollyPresigningClient(credentialsProvider);

    }
}