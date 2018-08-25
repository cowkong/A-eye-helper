package com.amazonaws.sample.lex.CALL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

//import java.util.logging.Handler;

public class CallReceiver extends BroadcastReceiver {
    static final String TAG = "CallReceiver";
    final String phone_number="";
    private static String mLastState;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG,"onReceive()");

        /**
         * http://mmarvick.github.io/blog/blog/lollipop-multiple-broadcastreceiver-call-state/
         * 2번 호출되는 문제 해결
         */
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(mLastState)) {
            return;

        } else {
            mLastState = state;

        }

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            final String phone_number = PhoneNumberUtils.formatNumber(incomingNumber);
//            Toast.makeText(context, phone_number, Toast.LENGTH_LONG).show();
            intent = new Intent(context, GetCall.class);
            //serviceIntent.putExtra(CallingService.EXTRA_CALL_NUMBER, phone_number);
            intent.putExtra("pNum",phone_number);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}