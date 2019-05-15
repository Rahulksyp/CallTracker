package com.rahulfreeforyou.rk.calldemo.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.aykuttasil.callrecord.CallRecord;
import com.rahulfreeforyou.rk.calldemo.CallDetect;

import java.util.Date;

public class CallHelper {

    private static final String TAG = "CallHelper";

    /**
     * Call recorder
     */
    private CallRecord callRecord = null;
    private Context ctx;
    private CallDetect outgoingReceiver;

    public CallHelper(Context ctx) {
        this.ctx = ctx;

        //Initialize the Broadcast receiver
        outgoingReceiver = new CallDetect();
    }

    public class CallingBroadReciever extends BroadcastReceiver {
        private static final String TAG = "CallingBroadReciever";
        private int lastState = TelephonyManager.CALL_STATE_IDLE;
        private Date callStartTime;
        private boolean isIncoming, wasRinging;
        private String savedNumber, number;
        private Context context = null;

        @Override
        public void onReceive(Context context, Intent intent) {

            this.context = context;

            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {

                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
                String info = intent.getExtras().getString("android.intent.extra.NAME");
                String name = intent.getExtras().getString("name");
                callStartTime = new Date();

                Log.e(TAG, "onReceive: " +
                        "Phone Number: " + savedNumber +
                        "info: " + info +
                        "name: " + name +
                        "call start time: " + callStartTime);

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

        protected void onIncomingCallStarted(Context ctx, String number, Date start) {
            Log.e(TAG, "onIncomingCallStarted: ");

        }

        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            Log.e(TAG, "onOutgoingCallStarted: ");

            callRecord.startCallReceiver();
        }

        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.e(TAG, "onIncomingCallEnded: ");
            callRecord.stopCallReceiver();
        }

        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.e(TAG, "onOutgoingCallEnded: ");

            callRecord.stopCallReceiver();
        }

        protected void onMissedCall(Context ctx, String number, Date start) {
            Log.e(TAG, "onMissedCall: ");
        }

        public void onCallStateChanged(Context context, int state, String number) {
            if (lastState == state) {
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = number;
                    onIncomingCallStarted(context, number, callStartTime);
                    wasRinging = true;

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;
                        callStartTime = new Date();

                        onOutgoingCallStarted(context, savedNumber, callStartTime);
                    }

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {

                        onMissedCall(context, savedNumber, callStartTime);
                        wasRinging = true;

                    } else if (isIncoming) {
                        onIncomingCallEnded(context, savedNumber, callStartTime, new Date());

                    } else {

                        onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());

                    }
                    break;


            }

            lastState = state;
        }
    }

    public void start() {

        Log.e(TAG, "start: register receiver");

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);

        // start call recorder
        callRecord = new CallRecord.Builder(ctx)
                .setRecordFileName("RecordFileName")
                .setRecordDirName("CallTrackerRecordFile")
                .setRecordDirPath(Environment.getExternalStorageDirectory().getPath()) // optional & default value
                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setShowSeed(true)
                .build();

        // Start call recording
        callRecord.startCallReceiver();
    }

    /**
     * Stop calls detection.
     */
    public void stop() {

        Log.e(TAG, "stop: unregister receiver & stop call recording");

        // Stop further listening of broadcast
        ctx.unregisterReceiver(outgoingReceiver);
    }
}
