/*
 * Copyright 2016-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.sample.lex;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.lex.interactionkit.Response;
import com.amazonaws.mobileconnectors.lex.interactionkit.config.InteractionConfig;
import com.amazonaws.mobileconnectors.lex.interactionkit.ui.InteractiveVoiceView;
import com.amazonaws.regions.Regions;
import com.amazonaws.sample.lex.CONTACT.GetContact;
import com.amazonaws.sample.lex.REKOGNITION.cameraActivity;
import com.amazonaws.sample.lex.SMS.SendMessage;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.util.StringUtils;


import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.lang.String;

public class InteractiveVoiceActivity extends Activity
        implements InteractiveVoiceView.InteractiveVoiceListener {
    public String temp;
    private static final String TAG = "VoiceActivity";
    private static final int REKOGTYPE_OBJECT = 0;
    private static final int REKOGTYPE_MONEY = 1;
    int count = 0;

    private Context appContext;
    private InteractiveVoiceView voiceView;
    private TextView transcriptTextView;
    private TextView responseTextView;
    private String responseTodo;
    private String responseYear;
    private String responseMonth;
    private String responseDay;
    private String responseTime;

    //polly 이용
    private AmazonPollyPresigningClient client;
    private Uri notificationVoIce;

    AlarmManager alarmManager;
    Alarm al;

    GetContact gC;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("hello coutn is ", count + "");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_alarm);
        setContentView(R.layout.activity_interactive_voice);
        transcriptTextView = (TextView) findViewById(R.id.transcriptTextView);
        responseTextView = (TextView) findViewById(R.id.responseTextView);
        init();
        StringUtils.isBlank("notempty");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public void onBackPressed() {
        exit();
    }


    private void init() {
        al = new Alarm();

        appContext = getApplicationContext();

        voiceView = (InteractiveVoiceView) findViewById(R.id.voiceInterface);
        voiceView.setInteractiveVoiceListener(this);
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider(
                appContext.getResources().getString(R.string.identity_id_test),
                Regions.fromName(appContext.getResources().getString(R.string.cognito_region)));
        voiceView.getViewAdapter().setCredentialProvider(credentialsProvider);
        voiceView.getViewAdapter().setInteractionConfig(
                new InteractionConfig(appContext.getString(R.string.bot_name),
                        appContext.getString(R.string.bot_alias)));
        voiceView.getViewAdapter().setAwsRegion(appContext.getString(R.string.lex_region));

        //polly clinet 가져오기
        //client = new AmazonPollyPresigningClient(credentialsProvider);
    }




    private void exit() {
        finish();
    }

    @Override
    public void dialogReadyForFulfillment(final Map<String, String> slots, final String intent) {
        Log.d(TAG, String.format(
                Locale.US,
                "Dialog ready for fulfillment:\n\tIntent: %s\n\tSlots: %s",
                intent,
                slots.toString()));
    }

    @Override
    public void onResponse(Response response) {

        Log.d(TAG, "Bot response: " + response.getTextResponse());
        Log.d(TAG, "Transcript: " + response.getInputTranscript());

        responseTextView.setText(response.getTextResponse());
        transcriptTextView.setText(response.getInputTranscript());


        if(response.getTextResponse().contains("Send") && count ==0){
            count++;
            gC = new GetContact();

            String Value = response.getTextResponse();

            String[] array = Value.split(" ");
            //출력
            for (int i = 3; i < array.length; i++) {
                System.out.println(array[i]);
            }
            gC = new GetContact();

            String contactName = gC.getPhoneNumber(array[3], appContext);
            String SendMessageType = "";

            for(int i = 4; i<array.length; i++){
                SendMessageType += array[i] +" ";
            }

            SendMessage.SendMessage(appContext, contactName, SendMessageType);

        }  else if(response.getTextResponse().contains("Ok. I will turn Camera On for Object")  && count ==0) {
            count++;
            //showToast("Good");

            int permissionCheck = ContextCompat.checkSelfPermission(InteractiveVoiceActivity.this, Manifest.permission.CAMERA);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                //권한없음
                ActivityCompat.requestPermissions(InteractiveVoiceActivity.this,new String[]{Manifest.permission.CAMERA},0);
                //Toast.makeText(getApplicationContext(),"권한 없음",Toast.LENGTH_SHORT).show();
            }else{
                //권한 있음
                Intent voiceIntent = new Intent(appContext, cameraActivity.class);
                voiceIntent.putExtra("type",REKOGTYPE_OBJECT);
                startActivity(voiceIntent);
            }
            exit();

        }
        else if(response.getTextResponse().contains("Ok. I will turn Camera On for Money") && count ==0 ) {
            count++;
            //showToast("Good");

            int permissionCheck = ContextCompat.checkSelfPermission(InteractiveVoiceActivity.this, Manifest.permission.CAMERA);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                //권한없음
                ActivityCompat.requestPermissions(InteractiveVoiceActivity.this,new String[]{Manifest.permission.CAMERA},0);
                //Toast.makeText(getApplicationContext(),"권한 없음",Toast.LENGTH_SHORT).show();
            }else{
                //권한 있음
                Intent voiceIntent = new Intent(appContext, cameraActivity.class);
                voiceIntent.putExtra("type",REKOGTYPE_MONEY);
                startActivity(voiceIntent);
            }
            exit();

        }
        else if(response.getTextResponse().contains("correct??") && count ==0 ){
            count++;

            String Value = response.getTextResponse();

            Log.d("Response","Response Todo : " +Value );
            String[] array = Value.split("-");
            responseTodo = array[0];
            responseYear = array[1];
            responseMonth = array[2];
            responseDay = array[3];
            responseTime = array[4];

            Calendar ct = Calendar.getInstance();

            String[] array3 = responseTime.split(" ");
            String[] timeHolder = array3[0].split(":");

            ct.set(Calendar.YEAR,Integer.parseInt(responseYear));
            ct.set(Calendar.MONTH,Integer.parseInt(responseMonth)-1);
            ct.set(Calendar.DATE,Integer.parseInt(responseDay));
            ct.set(Calendar.HOUR_OF_DAY,Integer.parseInt(timeHolder[0]));
            ct.set(Calendar.MINUTE,Integer.parseInt(timeHolder[1]));
            ct.set(Calendar.SECOND,0);


            al.setTime(ct);
            al.setAlarmManager(alarmManager);

        }

        if(response.getTextResponse().contains("Success")){
            al.setAlarm(appContext,responseTodo);
            exit();
        }
    }

    @Override
    public void onError(final String responseText, final Exception e) {
        Log.e(TAG, "Error: " + responseText, e);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 0){
            if(grantResults[0] == 0){
                Toast.makeText(this,"카메라 권한이 승인됨", Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(this,"카메라 권한이 거절됨", Toast.LENGTH_SHORT).show();

        }
    }
}