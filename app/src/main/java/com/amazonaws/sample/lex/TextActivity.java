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
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.lex.interactionkit.InteractionClient;
import com.amazonaws.mobileconnectors.lex.interactionkit.Response;
import com.amazonaws.mobileconnectors.lex.interactionkit.continuations.LexServiceContinuation;
import com.amazonaws.mobileconnectors.lex.interactionkit.listeners.AudioPlaybackListener;
import com.amazonaws.mobileconnectors.lex.interactionkit.listeners.InteractionListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexrts.model.DialogState;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.util.StringUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class TextActivity extends Activity {
    private static final String TAG = "TextActivity";
    private EditText userTextInput;
    private Context appContext;
    private InteractionClient lexInteractionClient;
    private boolean inConversation;
    private LexServiceContinuation convContinuation;
    private int file_count = 0;

    private String responseTodo;
    private String responseYear;
    private String responseMonth;
    private String responseDay;
    private String responseTime;

    private AmazonPollyPresigningClient client;
    private Uri notificationVoIce;

    AlarmManager alarmManager;
    Alarm al;

    getContact gC;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        init();
        StringUtils.isBlank("notempty");

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Initializes the application.
     */
    private void init() {
        al = new Alarm();
        Log.d(TAG, "Initializing text component: ");
        appContext = getApplicationContext();
        userTextInput = (EditText) findViewById(R.id.userInputEditText);

        // Set text edit listener.
        userTextInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    textEntered();
                    return true;
                }
                return false;
            }
        });

        initializeLexSDK();
        startNewConversation();
    }

    /**
     * Initializes Lex client.
     */
    private void initializeLexSDK() {
        Log.d(TAG, "Lex Client");
        // Cognito Identity Broker is the credentials provider.
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider(
                appContext.getResources().getString(R.string.identity_id_test),
                Regions.fromName(appContext.getResources().getString(R.string.cognito_region)));

        // Create Lex interaction client.
        lexInteractionClient = new InteractionClient(getApplicationContext(),
                credentialsProvider,
                Regions.fromName(appContext.getResources().getString(R.string.lex_region)),
                appContext.getResources().getString(R.string.bot_name),
                appContext.getResources().getString(R.string.bot_alias));
        lexInteractionClient.setAudioPlaybackListener(audioPlaybackListener);
        lexInteractionClient.setInteractionListener(interactionListener);
    }

    /**
     * Read user text input.
     */
    private void textEntered() {
        // showToast("Text input not implemented");
        String text = userTextInput.getText().toString();
        if (!inConversation) {
            Log.d(TAG, " -- New conversation started");
            startNewConversation();
            addMessage(new TextMessage(text, "tx", getCurrentTimeStamp()));
            lexInteractionClient.textInForTextOut(text, null);

            /*
            if(text.equals("turn on the camera")) {
                showToast("Good");
                Intent textIntent = new Intent(appContext, cameraActivity.class);
                startActivity(textIntent);
            }
            */

            inConversation = true;
        } else {
            Log.d(TAG, " -- Responding with text: " + text);
            addMessage(new TextMessage(text, "tx", getCurrentTimeStamp()));
            convContinuation.continueWithTextInForTextOut(text);
        }

        clearTextInput();
    }

    /**
     * Pass user input to Lex client.
     *
     * @param continuation
     */
    private void readUserText(final LexServiceContinuation continuation) {
        convContinuation = continuation;
        inConversation = true;
    }

    /**
     * Clears the current conversation history and closes the current request.
     */
    private void startNewConversation() {
        Log.d(TAG, "Starting new conversation");
        Conversation.clear();
        inConversation = false;
        clearTextInput();
    }

    /**
     * Clear text input field.
     */
    private void clearTextInput() {
        userTextInput.setText("");
    }

    /**
     * Show the text message on the screen.
     *
     * @param message
     */
    private void addMessage(final TextMessage message) {
        Conversation.add(message);
        final MessagesListAdapter listAdapter = new MessagesListAdapter(getApplicationContext());
        final ListView messagesListView = (ListView) findViewById(R.id.conversationListView);
        messagesListView.setDivider(null);
        messagesListView.setAdapter(listAdapter);
        messagesListView.setSelection(listAdapter.getCount() - 1);
    }

    /**
     * Current time stamp.
     *
     * @return
     */
    private String getCurrentTimeStamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    final InteractionListener interactionListener = new InteractionListener() {
        @Override
        public void onReadyForFulfillment(final Response response) {
            Log.d(TAG, "Transaction completed successfully");
            addMessage(new TextMessage(response.getTextResponse(), "rx", getCurrentTimeStamp()));

            inConversation = false;
        }

        @Override
        public void promptUserToRespond(final Response response,
                final LexServiceContinuation continuation) {

            addMessage(new TextMessage(response.getTextResponse(), "rx", getCurrentTimeStamp()));
            //addMessage(new TextMessage(response.getTextResponse(), "rx", getCurrentTimeStamp()));

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
            else if(response.getTextResponse().contains("Ok. I will turn Camera On")) {
                showToast("Good");
                Intent textIntent = new Intent(appContext, cameraActivity.class);
                startActivity(textIntent);
            }
            else if(response.getTextResponse().contains("correct??")){

                String Value = response.getTextResponse();

                Log.d("Response","Response Todo : " +Value );
                String[] array = Value.split("-");
                responseTodo = array[0];
                responseYear = array[1];
                responseMonth = array[2];
                responseDay = array[3];
                responseTime = array[4];

                Calendar ct = Calendar.getInstance();

                Log.d("Response Todo","Response Todo : " +responseTodo );
                Log.d("Response Date","Response Year : " +responseYear );
                Log.d("Response Date","Response Month : " +responseMonth );
                Log.d("Response Date","Response Day : " +responseDay );
                Log.d("Response Time","Response Time : " +responseTime );


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
            }
            readUserText(continuation);

        }

        @Override
        public void onInteractionError(final Response response, final Exception e) {
            if (response != null) {
                if (DialogState.Failed.toString().equals(response.getDialogState())) {
                    addMessage(new TextMessage(response.getTextResponse(), "rx",
                            getCurrentTimeStamp()));
                    inConversation = false;
                } else {
                    addMessage(new TextMessage("Please retry", "rx", getCurrentTimeStamp()));
                }
            } else {
                showToast("Error: " + e.getMessage());
                Log.e(TAG, "Interaction error", e);
                inConversation = false;
            }
        }
    };

    /**
     * Implementing {@link AudioPlaybackListener}.
     */
    final AudioPlaybackListener audioPlaybackListener = new AudioPlaybackListener() {
        @Override
        public void onAudioPlaybackStarted() {
            Log.d(TAG, " -- Audio playback started");
        }

        @Override
        public void onAudioPlayBackCompleted() {
            Log.d(TAG, " -- Audio playback ended");
        }

        @Override
        public void onAudioPlaybackError(Exception e) {
            Log.d(TAG, " -- Audio playback error", e);
        }
    };

    /**
     * Show a toast.
     *
     * @param message - Message text for the toast.
     */
    private void showToast(final String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        Log.d(TAG, message);
    }
}
