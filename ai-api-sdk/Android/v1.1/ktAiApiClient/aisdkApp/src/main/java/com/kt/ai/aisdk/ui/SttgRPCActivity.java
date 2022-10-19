package com.kt.ai.aisdk.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import kt.gigagenie.ai.api.STTgRPC;
import kt.gigagenie.ai.api.STTgRPCCallback;


public class SttgRPCActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =  "SttgRPCActivity";

    // UI Handler
    private static final int MSG_SHOW_TOAST_TEXT            = 0;
    private static final int MSG_UDATE_TEXT                 = 1;
    private static final int MSG_UPDATE_STATUS              = 2;
    private static final int MSG_REQUEST_CONNECTED          = 3;
    private static final int MSG_REQUEST_DISCONNECTED       = 4;
    private static final int MSG_REQUEST_STOP_STT           = 5;
    private static final int MSG_REQUEST_STOPED_STT         = 6;
    private static final int MSG_REQUEST_START_RECORDING    = 7;
    private static final int MSG_REQUEST_STOP_RECORDING     = 8;
    private static final int MSG_REQUEST_START_AUDIO_SEND   = 9;

    private static final int RECORDING_MODE                 = 1;
    private static final int AUDIO_FILE_SEND_MODE           = 2;

    private static final int UPDATE_PARTIAL                 = 1;
    private static final int UPDATE_FULL                    = 2;

    private static final int REQUEST_CODE_MY_PICK = 2000;

    private boolean isRecording = false;
    private Button mBtnConnect = null;
    private Button mBtnDisconnect = null;
    private Button mBtnRecord = null;
    private Button mBtnAudioSend = null;
    private Button mBtnSelectFile = null;

    private Spinner mGrpcModeSpinner = null;
    private Spinner mSampleRateSpinner = null;
    private Spinner mSttmodelcodeGrpcSpinner = null;

    private int mSampleRate = 8000;
    private String mGrpcMode = "long";
    private int mTestMode = RECORDING_MODE;
    private String mTartgetFilePath;

    private ScrollView mScrollView = null;
    private TextView mNote = null;
    private TextView mStatus = null;

    private STTgRPC mSttgRPC = null;

    private String mExistText = "";


    private STTgRPCCallback onSTTgRPCCallbackListener = new STTgRPCCallback() {
        @Override
        public void onConnectGRPC() {

            Message msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "GRPC Connected...");
            mUIHandler.sendMessage(msg);

            msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "Connected");
            mUIHandler.sendMessage(msg);

            msg = mUIHandler.obtainMessage(MSG_REQUEST_CONNECTED);
            mUIHandler.sendMessage(msg);
        }

        @Override
        public void onSTTResult(String text, String type, float startTime, float endTime) {

            StringBuilder sb = new StringBuilder();
            sb.append(text);
            if(type.equalsIgnoreCase("full")){
                sb.append("\n");
                Message msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, UPDATE_FULL, 0, sb.toString());
                mUIHandler.sendMessage(msg);
            }else{
                Message msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, UPDATE_PARTIAL, 0, sb.toString());
                mUIHandler.sendMessage(msg);
            }
        }

        @Override
        public void onReadySTT(int sampleRate, int channel, String format) {
            if(mTestMode == RECORDING_MODE) {
                Message msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "onReadySTT");
                mUIHandler.sendMessage(msg);

                msg = mUIHandler.obtainMessage(MSG_REQUEST_START_RECORDING);
                mUIHandler.sendMessage(msg);
            }else{
                Message msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "** File Sending **");
                mUIHandler.sendMessage(msg);

                msg = mUIHandler.obtainMessage(MSG_REQUEST_START_AUDIO_SEND);
                mUIHandler.sendMessage(msg);
            }
        }

        @Override
        public void onStopSTT() {
            Message msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "Connected");
            mUIHandler.sendMessage(msg);
            msg = mUIHandler.obtainMessage(MSG_REQUEST_STOPED_STT);
            mUIHandler.sendMessage(msg);

        }

        @Override
        public void onStartRecord() {
            Message msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "** onRecording **");
            mUIHandler.sendMessage(msg);
            msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "startRecording...");
            mUIHandler.sendMessage(msg);
        }

        @Override
        public void onStopRecord() {
            Message msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "Connected");
            mUIHandler.sendMessage(msg);
            msg = mUIHandler.obtainMessage(MSG_REQUEST_STOP_RECORDING);
            mUIHandler.sendMessage(msg);
            msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "stopRecording...");
            mUIHandler.sendMessage(msg);
        }

        @Override
        public void onRelease() {
            Message msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "GRPC Disconnected...");
            mUIHandler.sendMessage(msg);
            msg = mUIHandler.obtainMessage(MSG_UPDATE_STATUS, "Disconnected");
            mUIHandler.sendMessage(msg);
            msg = mUIHandler.obtainMessage(MSG_REQUEST_DISCONNECTED);
            mUIHandler.sendMessage(msg);
        }

        @Override
        public void onError(int errCode, String errMsg) {
            Message msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "errCode:" + errCode + ", errMsg:" + errMsg);
            mUIHandler.sendMessage(msg);
            gRPCDisconnect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_stt_grpc);

        mBtnConnect = (Button)findViewById(R.id.grpcConnect);
        mBtnDisconnect = (Button)findViewById(R.id.grpcDisconnect);
        mBtnRecord = (Button)findViewById(R.id.grpcRecording);
        mBtnAudioSend = (Button)findViewById(R.id.grpcAudioSend);
        mBtnSelectFile = (Button)findViewById(R.id.grpcFile);
        mScrollView = (ScrollView)findViewById(R.id.txt_scrollview);
        mNote = (TextView)findViewById(R.id.note);
        mStatus = (TextView)findViewById(R.id.grpc_status);

        mSttmodelcodeGrpcSpinner = (Spinner)findViewById(R.id.sttmodelcode_grpc_spinner);

        mGrpcModeSpinner = (Spinner)findViewById(R.id.grpcMode_spinner);
        mGrpcModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mGrpcMode = (String)parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSampleRateSpinner = (Spinner)findViewById(R.id.grpcSampleRate_spinner);
        mSampleRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSampleRate = Integer.parseInt((String)parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mSttgRPC == null){
            Log.d(TAG, "onStart(): mSttgRPC is null");
            mSttgRPC = new STTgRPC(SttgRPCActivity.this);
            mSttgRPC.setSTTgRPCCallback(onSTTgRPCCallbackListener);
            mSttgRPC.setServiceURL(ENV.hostname + ":" + ENV.ai_api_grpc_port);
            mSttgRPC.setMetaData(ENV.client_key, ENV.client_id, ENV.client_secret);
        } else {
            Log.e(TAG, "onStart(): mSttgRPC is not null");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSttgRPC != null){
            gRPCDisconnect();
            mSttgRPC = null;
        }
    }

    @Override
    public void onClick(View v) {
        Message msg;
        switch (v.getId()) {
            case R.id.grpcConnect:

                mNote.setText("");
                mNote.invalidate();
                mExistText = "";

                gRPCConnect();
                break;

            case R.id.grpcDisconnect:

                mNote.setText("");
                mNote.invalidate();
                mExistText = "";

                gRPCDisconnect();
                break;

            case R.id.grpcRecording:
                if(isRecording) {
                    stopAudioRecording();
                }else{
                    mNote.setText("");
                    mNote.invalidate();
                    mExistText = "";

                    gRPCStart(mGrpcMode, "S16LE", mSampleRate, 1);

                    mTestMode = RECORDING_MODE;
                }
                break;
            case R.id.grpcAudioSend:

                mNote.setText("");
                mNote.invalidate();
                mExistText = "";

                if (TextUtils.isEmpty(mTartgetFilePath)){
                    Toast.makeText(this, "Text로 변환할 파일을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                gRPCStart(mGrpcMode, "S16LE", mSampleRate, 1);
                mBtnRecord.setEnabled(false);
                mBtnAudioSend.setEnabled(false);
                mBtnDisconnect.setEnabled(false);
                mBtnSelectFile.setEnabled(false);
                mTestMode = AUDIO_FILE_SEND_MODE;
                Toast.makeText(this, "startSendAudio...", Toast.LENGTH_SHORT).show();
                break;

            case R.id.grpcFile:

                mNote.setText("");
                mNote.invalidate();

                Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFileIntent.setType("*/*");
                chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
                startActivityForResult(chooseFileIntent, REQUEST_CODE_MY_PICK);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_MY_PICK:
                if (resultCode == RESULT_OK ) {
                    if (data != null) {
                        Uri fileUri = data.getData();
                        String uriPath = fileUri.getPath();

                        String filePath = null;
                        try {
                            if (uriPath.contains("/storage")) {
                                filePath = uriPath.substring(uriPath.indexOf("/storage"));
                            } else {
                                filePath = FilePickUtils.getPath(SttgRPCActivity.this, fileUri);
                            }
                        } catch (Exception e) {

                        }

                        if (TextUtils.isEmpty(filePath)) {
                            Toast.makeText(SttgRPCActivity.this, "선택한 파일경로가 인식이 되지 않습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            mTartgetFilePath = filePath;
                            Toast.makeText(SttgRPCActivity.this, "선택한 파일은 " + filePath + "입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(SttgRPCActivity.this, "파일이 선택되지 않았어요. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * UI Handler 에서 처리할 일이 생길때 호출되는 핸들러
     */
    private Handler mUIHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if(isDestroyed()) return false;

            switch (msg.what) {
                case MSG_SHOW_TOAST_TEXT:
                    String toastText = (String)msg.obj;
                    Toast.makeText(SttgRPCActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UDATE_TEXT:
                    String text = (String)msg.obj;
                    int type = msg.arg1;
                    if(type == UPDATE_FULL){
                        mNote.setText(mExistText);
                        mNote.append(text);
                        mNote.invalidate();
                        mExistText = mNote.getText().toString();
                    }else{
                        mNote.setText(mExistText);
                        mNote.append(text);
                        mNote.invalidate();
                    }
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    break;
                case MSG_UPDATE_STATUS:
                    String status = (String)msg.obj;
                    mStatus.setText(status);
                    break;
                case MSG_REQUEST_CONNECTED:
                    mBtnConnect.setEnabled(false);
                    mBtnDisconnect.setEnabled(true);
                    mBtnRecord.setEnabled(true);
                    mBtnAudioSend.setEnabled(true);
                    mBtnSelectFile.setEnabled(true);
                    break;
                case MSG_REQUEST_DISCONNECTED:
                    mBtnConnect.setEnabled(true);
                    mBtnDisconnect.setEnabled(false);
                    mBtnRecord.setEnabled(false);
                    mBtnAudioSend.setEnabled(false);
                    mBtnSelectFile.setEnabled(false);
                    break;
                case MSG_REQUEST_STOP_STT:
                    gRPCStop();
                    break;
                case MSG_REQUEST_STOPED_STT:
                    mBtnDisconnect.setEnabled(true);
                    mBtnRecord.setEnabled(true);
                    mBtnAudioSend.setEnabled(true);
                    mBtnSelectFile.setEnabled(true);
                    break;
                case MSG_REQUEST_START_RECORDING:
                    isRecording = true;
                    mBtnRecord.setText("stop recording");
                    mBtnDisconnect.setEnabled(false);
                    mBtnAudioSend.setEnabled(false);
                    mBtnSelectFile.setEnabled(false);
                    startAudioRecording();
                    break;
                case MSG_REQUEST_STOP_RECORDING:
                    isRecording = false;
                    mBtnRecord.setText("start recording");
                    mBtnAudioSend.setEnabled(true);
                    mBtnSelectFile.setEnabled(true);
                    break;
                case MSG_REQUEST_START_AUDIO_SEND:
                    sendAudioFile(mTartgetFilePath);
                    break;
                default:
                    break;
            }

            return false;
        }
    });

    private void gRPCConnect(){
        Log.d(TAG, "gRPCConnect()");
        mSttgRPC.setMetaData(ENV.client_key, ENV.client_id, ENV.client_secret);
        mSttgRPC.connectGRPC();
    }

    private void gRPCDisconnect(){
        mSttgRPC.releaseConnection();
    }

    private void gRPCStop(){
        mSttgRPC.stopSTT();
    }

    private void gRPCStart(final String sttMode, final String sampleFmt, final int sampleRate, final int channel){
        mSttgRPC.startSTT(sttMode, sampleFmt, sampleRate, channel);
    }


    private void startAudioRecording(){
        mSttgRPC.startRecording();
    }

    private void stopAudioRecording(){
        mSttgRPC.stopRecording();
    }

    private void sendAudioFile(final String mFilePath){
        mSttgRPC.sendAudioFile(mFilePath);
    }

}
