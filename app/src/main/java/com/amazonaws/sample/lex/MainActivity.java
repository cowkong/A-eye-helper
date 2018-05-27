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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_RECORDING_PERMISSIONS_RESULT = 75;
    private Button textDemoButton;
    private Button speechDemoButton;


    static final int SMS_RECEIVE_PERMISSON=1;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        smsPermission();
    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(true);
    }

    /**
     * Initializes the application.
     */
    private void init() {
        Log.e(TAG, "Initializing app");

        textDemoButton = (Button) findViewById(R.id.button_select_text);
        speechDemoButton = (Button) findViewById(R.id.button_select_voice);
        textDemoButton.setOnClickListener(this);
        speechDemoButton.setOnClickListener(this);

        // Starting with Marshmallow we need to explicitly ask if we can record audio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                speechDemoButton.setEnabled(true);
            } else {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORDING_PERMISSIONS_RESULT);
            }
        } else {
            speechDemoButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORDING_PERMISSIONS_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "LexSample will not be able to use the voice feature", Toast.LENGTH_SHORT).show();

                // Disable the button
                speechDemoButton.setEnabled(false);
            } else {
                speechDemoButton.setEnabled(true);
            }
        }
    }

    /**
     * On-click listener for buttons text and voice buttons.
     *
     * @param v {@link View}, instance of the button component.
     */
    @Override
    public void onClick(final View v) {
        switch ((v.getId())) {
            case R.id.button_select_text:
                Intent textIntent = new Intent(this, TextActivity.class);
                startActivity(textIntent);
                break;
            case R.id.button_select_voice:
                Intent voiceIntent = new Intent(this, InteractiveVoiceActivity.class);
                startActivity(voiceIntent);

                break;
        }
    }
    //sms 권한 확인
    public void smsPermission(){
        int permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);

        if(permissonCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "SMS 수신권한 있음", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "SMS 수신권한 없음", Toast.LENGTH_SHORT).show();

            //권한설정 dialog에서 거부를 누르면
            //ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
            //단, 사용자가 "Don't ask again"을 체크한 경우
            //거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                //이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                Toast.makeText(getApplicationContext(), "SMS권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.RECEIVE_SMS}, SMS_RECEIVE_PERMISSON);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.RECEIVE_SMS}, SMS_RECEIVE_PERMISSON);
            }
        }
    }



}
