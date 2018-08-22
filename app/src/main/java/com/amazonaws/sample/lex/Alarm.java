package com.amazonaws.sample.lex;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Alarm{

    //시간 설정을 위한 객체
    Calendar Time;


    public Calendar getTime() {
        return Time;
    }

    public void setTime(Calendar time) {
        Time = time;
    }

    //알람 설정을 위한 객체
    private Intent intent;
    private PendingIntent ServicePending;
    private AlarmManager alarmManager;

    public AlarmManager getAlarmManager() {
        return alarmManager;
    }

    public void setAlarmManager(AlarmManager alarmManager) {
        this.alarmManager = alarmManager;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 mm분 ss초");

    TextView textView;

    DatePickerDialog.OnDateSetListener eDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Time.set(Calendar.YEAR,year);
            Time.set(Calendar.MONTH,monthOfYear);
            Time.set(Calendar.DAY_OF_MONTH,dayOfMonth);

            updateLabel();
        }
    };

    private TimePickerDialog.OnTimeSetListener sTimeSetListener =
            new TimePickerDialog.OnTimeSetListener(){

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    Time.set(Calendar.HOUR_OF_DAY,hourOfDay);
                    Log.d("check","1 = " + hourOfDay);
                    Time.set(Calendar.MINUTE, minute);
                    Log.d("check","2 = " + minute);
                    Time.set(Calendar.SECOND,0);

                    updateLabel();
                }
            };


    private void updateLabel() {
        textView.setText(simpleDateFormat.format(Time.getTime()));
    }

    public void setAlarm(Context context,String todo){

        //테스트용 알람 설정
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DATE, 14);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 47);
        calendar.set(Calendar.SECOND, 0);

        //receiver로 보내기 위한 인텐트
        //intent= new Intent(this,AlarmReceiver.class);
        intent = new Intent("AlarmReceiver");
        intent.putExtra("todo",todo);
        //PendingIntent.getBroadcast(Context context, int requestCod, Intent intent, int flat);


        ServicePending = PendingIntent.getBroadcast(
                context, calendar.toString().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Log.d("setAlarm","Time is " + Time.getTimeInMillis());
        Log.d("setAlarm", "SerVicePending is" + ServicePending);

        //정해진 시간에 알람 설정
        //alarmManager.set(AlarmManager.RTC_WAKEUP,Time.getTimeInMillis(),ServicePending);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),ServicePending);


        Log.d("Response","설정된 시간 : " + Time.getTimeInMillis());
        Log.d("Response","임의의 시간 : " + calendar.getTimeInMillis());
        Log.d("Response","설정된 시간 : " + Time.getTime());
        Log.d("Response","임의의 시간 : " + calendar.getTime());

        Toast.makeText(context,"알람 설정" + calendar.getTime() + "해야할일 : " + todo,Toast.LENGTH_LONG).show();

    }

    void removeAlarm(Context context){
        intent = new Intent("AlarmReceiver");
        //PendingIntent.getBroadcast(Context context, int requestCod, Intent intent, int flag);

        ServicePending = PendingIntent.getBroadcast(
                context,Time.toString().hashCode(),intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Toast.makeText(context,"알람 해제" + Time.getTime(),Toast.LENGTH_SHORT).show();
        alarmManager.cancel(ServicePending);
    }

}