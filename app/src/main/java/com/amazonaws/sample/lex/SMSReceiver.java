package com.amazonaws.sample.lex;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class SMSReceiver extends BroadcastReceiver{
    private static final String TAG = "PollyDemo";
    public String str;
    public String readSMS;
    public String contactPerson;
    public String pNum;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 수신되었을 때 호출되는 콜백 메서드
        // 매개변수 intent의 액션에 방송의 '종류'가 들어있고
        //         필드에는 '추가정보' 가 들어 있습니다.

        // SMS 메시지를 파싱합니다.
        Bundle bundle = intent.getExtras();
        str = ""; // 출력할 문자열 저장
        readSMS = "";
        contactPerson = "";
        pNum = "";
        if (bundle != null) { // 수신된 내용이 있으면
            // 실제 메세지는 Object타입의 배열에 PDU 형식으로 저장됨

            Object [] pdus = (Object[])bundle.get("pdus");

            SmsMessage [] msgs = new SmsMessage[pdus.length];

            for (int i = 0; i < msgs.length; i++) {
                // PDU 포맷으로 되어 있는 메시지를 복원합니다.
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += msgs[i].getOriginatingAddress() + "에게 문자왔음, " + msgs[i].getMessageBody().toString() +"\n";
               // contents += msgs[i].getMessageBody().toString() ;
                readSMS += msgs[i].getMessageBody().toString();
                pNum += msgs[i].getOriginatingAddress();
            }

            Toast.makeText(context, str, Toast.LENGTH_LONG).show();

            intent = new Intent(context, smsActivity.class);
            intent.putExtra("smsMessage",readSMS);
            intent.putExtra("contactPerson",pNum);
            context.startActivity(intent);

        }

    } // end of onReceive

}
