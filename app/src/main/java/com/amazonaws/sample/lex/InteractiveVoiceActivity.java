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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.lex.interactionkit.Response;
import com.amazonaws.mobileconnectors.lex.interactionkit.config.InteractionConfig;
import com.amazonaws.mobileconnectors.lex.interactionkit.ui.InteractiveVoiceView;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;


import java.util.Locale;
import java.util.Map;
import java.lang.String;

public class InteractiveVoiceActivity extends Activity
        implements InteractiveVoiceView.InteractiveVoiceListener {
    public String temp;
    private static final String TAG = "VoiceActivity";
    private Context appContext;
    private InteractiveVoiceView voiceView;
    private TextView transcriptTextView;
    private TextView responseTextView;
    getContact gC;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_alarm);
        setContentView(R.layout.activity_interactive_voice);
        transcriptTextView = (TextView) findViewById(R.id.transcriptTextView);
        responseTextView = (TextView) findViewById(R.id.responseTextView);
        init();
        StringUtils.isBlank("notempty");
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void init() {
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


        if(response.getTextResponse().contains("Send")){

            gC = new getContact();

            String Value = response.getTextResponse();

            String[] array = Value.split(" ");
            //출력
            for (int i = 3; i < array.length; i++) {
                System.out.println(array[i]);
            }
            gC = new getContact();

            String contactName = gC.getPhoneNumber(array[3], appContext);
            String SendMessageType = "";

            for(int i = 4; i<array.length; i++){
                SendMessageType += array[i] +" ";
            }

            SendMessage.SendMessage(appContext, contactName, SendMessageType);

        }
    }

    @Override
    public void onError(final String responseText, final Exception e) {
        Log.e(TAG, "Error: " + responseText, e);
    }
}
