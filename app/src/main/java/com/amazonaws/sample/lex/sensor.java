package com.amazonaws.sample.lex;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

// ============================================================================
public class sensor implements SensorEventListener{ // public class ufo
    public static final String sensor_type[] = {" ", "TYPE_ACCELEROMETER", "TYPE_MAGNETIC_FIELD", "TYPE_ORIENTATION",
            "TYPE_GYROSCOPE", "TYPE_LIGHT", "TYPE_PRESSURE", "TYPE_TEMPERATURE", "TYPE_PROXIMITY"};
    public static int[] accx = new int[]{0, 0};

    private static long mShakeTime = 0;
    private static final int SHAKE_SKIP_TIME = 500;
    private static final float SHAKE_THRESHOD_GRAVITY = 2.7F;

    public static int acc_flag;

    //-------------------------------------------------------------------------
    public void get_sensor_manager(Context context) { // public static void get_sensor_manager()

        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor AccSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, AccSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("hello","get_sensor_manager");
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("hello",event.sensor.getType() + "");
        switch (event.sensor.getType()) { // switch <2.2>
            case Sensor.TYPE_ACCELEROMETER:

                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                float gravityX = axisX / SensorManager.GRAVITY_EARTH;
                float gravityY = axisY / SensorManager.GRAVITY_EARTH;
                float gravityZ = axisZ / SensorManager.GRAVITY_EARTH;

                float f = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ;

                double squaredD = Math.sqrt(((double) f));
                float gForce = (float) squaredD;

                if (gForce > SHAKE_THRESHOD_GRAVITY) {
                    long currentTime = System.currentTimeMillis();
                    if (mShakeTime + SHAKE_SKIP_TIME > currentTime) {
                        return;
                    }
                    mShakeTime = currentTime;
                    acc_flag = 1;
                    Log.d("hello shake", "onSensorChanged: Shake");
                }
                /*
                        accx[0] = (int) event.values[0];

                if (java.lang.Math.abs(accx[0] - accx[1]) > 20)
                { // if <2.3>
                    //str = t.format("%Y/%m/%d %H:%M:%S  ");
                    //str += "���� = X:" + accx[0] + " :" + accx[1];
                    //Toast.makeText(ufo.ufo_context, str, Toast.LENGTH_SHORT).show();
                    acc_flag = 1;
                } // if <2.3>

                accx[1] = accx[0];*/
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //mTxtMagnetic.setText("�ڱ� = " + ++mMagneticCount + "ȸ : \n  X:" + v[0] + "\n  Y:" + v[1] + "\n  Z:" + v[2]);
                break;

            case Sensor.TYPE_ORIENTATION:
                //mTxtOrient.setText("���� = " + ++mOrientCount + "ȸ : \n  azimuth:" +	v[0] + "\n  pitch:" + v[1] + "\n  roll:" + v[2]);
                break;

            case Sensor.TYPE_LIGHT:
                //mTxtLight.setText("���� = " + ++mLightCount + "ȸ : " + v[0]);
                break;

            case Sensor.TYPE_PRESSURE:
                //mTxtPress.setText("�з� = " + ++mPressCount + "ȸ : " + v[0]);
                break;

            case Sensor.TYPE_TEMPERATURE:
                //tv.append("T1=" + v[0] + "\n");
                break;

            case Sensor.TYPE_PROXIMITY:
                //tv.append("Distance=" + v[0] + "\n");
                break;


        } // switch <2.2>
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

