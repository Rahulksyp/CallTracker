package com.rahulfreeforyou.rk.calldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallDetect extends BroadcastReceiver {

    private static int callStateIdle = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean incoming;
    private static String savedNumber;
    private String number;
    private SimpleDateFormat sdf;

    MediaRecorder recorder;
    TelephonyManager telManager;
    boolean recordStarted;
    Context context;
    File audiofile = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        RecordingProcess(intent);

//        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
//
//            Toast.makeText(context, "Call Stated", Toast.LENGTH_SHORT).show();
//        }else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE)){
//
//            Toast.makeText(context, "Call Ended", Toast.LENGTH_SHORT).show();
//        }

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

            incoming = false;

//            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//
//            Toast.makeText(context, ""+phoneNumber  , Toast.LENGTH_SHORT).show();

        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);


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
    }



    private void onCallStateChanged(Context context, int state, String number) {

        if (callStateIdle == state) {
            return;
        }
        switch (state) {

            case TelephonyManager.CALL_STATE_RINGING:
                incoming = true;
                callStartTime = new Date();
                savedNumber = number;
                Toast.makeText(context, "Incoming Call Ringing", Toast.LENGTH_SHORT).show();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:


                if (callStateIdle != TelephonyManager.CALL_STATE_RINGING) {
                    incoming = false;
                    callStartTime = new Date();
                    Toast.makeText(context, "Outgoing Call Started", Toast.LENGTH_SHORT).show();
                    System.out.println("gandmra" + savedNumber);

                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:

                if (callStateIdle == TelephonyManager.CALL_STATE_RINGING) {
                    Toast.makeText(context, "Ringing but no pickup" + savedNumber + " Call time " + callStartTime + " Date " + new Date(), Toast.LENGTH_SHORT).show();

                    sdf = new SimpleDateFormat("EEE-dd-MM-yyy hh:mm:ss");
                    String strDate = sdf.format(callStartTime);
                    MainActivity.time.setText("Date & Time :- " + strDate);
                    MainActivity.name.setText("Status :- " + "Missed Call");
                    System.out.println("GandMra " + strDate);
                    MainActivity.num.setText("Num :- " + savedNumber);

                } else if (incoming) {

                    Toast.makeText(context, "Incoming " + savedNumber + " Call time " + callStartTime, Toast.LENGTH_SHORT).show();

                    sdf = new SimpleDateFormat("EEE-dd-MM-yyy hh:mm:ss");
                    String strDate = sdf.format(callStartTime);
                    MainActivity.time.setText("Date & Time :- " + strDate);
                    MainActivity.name.setText("Status :- " + "Incoming Call");
                    MainActivity.num.setText("Num :- " + savedNumber);

                } else {

                    Toast.makeText(context, "outgoing " + savedNumber + " Call time " + callStartTime + " Date " + new Date(), Toast.LENGTH_SHORT).show();

                    sdf = new SimpleDateFormat("EEE-dd-MM-yyy hh:mm:ss");
                    String strDate = sdf.format(callStartTime);
                    MainActivity.time.setText("Date & Time :- " + strDate);
                    System.out.println("GandMra " + strDate);
                    MainActivity.num.setText("Num :- " + savedNumber);
                    MainActivity.name.setText("Status :- " + "Outgoing Call");

                }

                break;
        }
        callStateIdle = state;
    }


    private void RecordingProcess(Intent intent) {

        try {
            File sampleDir = Environment.getExternalStorageDirectory();
            try {
                audiofile = File.createTempFile("sound"+System.currentTimeMillis(), ".3gp", sampleDir);

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            recordStarted = true;
            telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING: {
                        recorder.start();

                        break;
                    }
                    case TelephonyManager.CALL_STATE_OFFHOOK: {
                        recorder.start();


                        break;
                    }
                    case TelephonyManager.CALL_STATE_IDLE: {
                        if (recordStarted) {
                            recorder.stop();
                            recordStarted = false;
                        }
                        break;
                    }
                    default: { }
                }
            } catch (Exception ex) {

            }
        }
    };





}
