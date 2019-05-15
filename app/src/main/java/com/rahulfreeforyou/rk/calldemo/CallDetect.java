package com.rahulfreeforyou.rk.calldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.aykuttasil.callrecord.CallRecord;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.RequiresApi;

public class CallDetect extends BroadcastReceiver {

    private static Date callStartTime;
    private static boolean incoming, wasRinging;
    private static String savedNumber,info,name;
    private String number;
    private SimpleDateFormat sdf;
    private static final String TAG = "CallingBroadReciever";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;


        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {

            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            info = intent.getExtras().getString("android.intent.extra.NAME");
            name = intent.getExtras().getString("name");
            callStartTime = new Date();


        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            checkState(stateStr);
        }
    }

    private void checkState(String stateStr) {
        int state = 0;

        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            state = TelephonyManager.CALL_STATE_IDLE;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            state = TelephonyManager.CALL_STATE_OFFHOOK;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            state = TelephonyManager.CALL_STATE_RINGING;
        }


        onCallStateChanged(context, state, number);
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                incoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                wasRinging = true;

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    incoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedCall(context, savedNumber, callStartTime);
                    wasRinging = true;

                } else if (incoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());

                } else {

                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());

                }
                break;

        }


        lastState = state;
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Incoming Call :)", Toast.LENGTH_SHORT).show();

    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "OutGoing Call Started :)", Toast.LENGTH_SHORT).show();
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Toast.makeText(ctx, "Incoming Call Ended :)", Toast.LENGTH_SHORT).show();

        sdf = new SimpleDateFormat("EEE-dd-MM-yyy hh:mm:ss");
        String strDate = sdf.format(callStartTime);
        MainActivity.time.setText("Date & Time :- " + strDate);
        MainActivity.name.setText("Status :- " + "Incoming");
        MainActivity.num.setText("Num :- " + savedNumber);

    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {

        Toast.makeText(ctx, "OutGoing Call Ended :)", Toast.LENGTH_SHORT).show();

        sdf = new SimpleDateFormat("EEE-dd-MM-yyy hh:mm:ss");
        String strDate = sdf.format(callStartTime);
        MainActivity.time.setText("Date & Time :- " + strDate);
        MainActivity.name.setText("Status :- " + "Outgoing");
        MainActivity.num.setText("Num :- " + savedNumber);

    }

    protected void onMissedCall(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Missed Call :)", Toast.LENGTH_SHORT).show();

        sdf = new SimpleDateFormat("EEE-dd-MM-yyy hh:mm:ss");
        String strDate = sdf.format(callStartTime);
        MainActivity.time.setText("Date & Time :- " + strDate);
        MainActivity.name.setText("Status :- " + "Missedcall");
        MainActivity.num.setText("Num :- " + savedNumber);

    }


}
