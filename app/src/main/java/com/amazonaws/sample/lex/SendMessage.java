package com.amazonaws.sample.lex;

import android.content.Context;
import android.telephony.SmsManager;

import android.widget.Toast;

public class SendMessage  {

    public static void SendMessage(Context context, String number, String Contents) {
        sendSMS(number, Contents);
        Toast.makeText(context, number+"\n"+Contents, Toast.LENGTH_SHORT).show();
    }

    private static void sendSMS(String phoneNumber, String message)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
