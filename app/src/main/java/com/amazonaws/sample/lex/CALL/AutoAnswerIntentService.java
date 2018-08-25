package com.amazonaws.sample.lex.CALL;

import java.lang.reflect.Method;

import com.amazonaws.sample.lex.sensor;
import com.android.internal.telephony.ITelephony;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

// ============================================================================
public class AutoAnswerIntentService extends IntentService
{

    public AutoAnswerIntentService()
    {
        super("AutoAnswerIntentService");
    }

    // ------------------------------------------------------------------------
    @Override
    protected void onHandleIntent(Intent intent)
    { // protected void onHandleIntent(Intent intent)
        Context context = getBaseContext();
        sensor s = new sensor();
        // Let the phone ring for a set delay
        try
        {
            Thread.sleep(2000);
            s.acc_flag = 0;
            s.get_sensor_manager(context);
        }
        catch (InterruptedException e)
        {
            // We don't really care
        }
        Log.d("hello","before " + sensor.acc_flag);
        while(sensor.acc_flag == 0)
            ;
        Log.d("hello","after " + sensor.acc_flag);
        // Make sure the phone is still ringing
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING)
        {
            return;
        }

        // Answer the phone
        try
        {
            answerPhoneAidl(context);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("AutoAnswer","Error trying to answer using telephony service.  Falling back to headset.");
            answerPhoneHeadsethook(context);
        }

        return;
    } // protected void onHandleIntent(Intent intent)

    // ------------------------------------------------------------------------
    private void answerPhoneHeadsethook(Context context)
    { // private void answerPhoneHeadsethook(Context context)
        // Simulate a press of the headset button to pick up the call
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        // froyo and beyond trigger on buttonUp instead of buttonDown
        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    } // private void answerPhoneHeadsethook(Context context)

    // ------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void answerPhoneAidl(Context context) throws Exception
    { // private void answerPhoneAidl(Context context) throws Exception
        // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Class c = Class.forName(tm.getClass().getName());
        Method m = c.getDeclaredMethod("getITelephony");
        m.setAccessible(true);
        ITelephony telephonyService;
        telephonyService = (ITelephony)m.invoke(tm);

        // Silence the ringer and answer the call!
        telephonyService.silenceRinger();
        telephonyService.answerRingingCall();
    } // private void answerPhoneAidl(Context context) throws Exception
}
