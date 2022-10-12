package com.kt.ai.aisdk.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import kt.gigagenie.ai.api.STT;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.kt.ai.aisdk.ui.MainActivity.TEST_TYPE;

public class SttRestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =  "SttRestActivity";

    private static final int MSG_SHOW_PROGRESS              = 0;
    private static final int MSG_HIDE_PROGRESS              = 1;
    private static final int MSG_SHOW_TOAST_TEXT            = 2;
    private static final int MSG_UDATE_TEXT                 = 3;
    private static final int MSG_REQUEST_STT_TEXT           = 4;

    private static final int REQUEST_CODE_MY_PICK = 2000;

    private int mSampleRate = 16000;
    private int mChannel = 1;
    private String mSampleFmt = "S16LE";

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mChannelInCount = AudioFormat.CHANNEL_IN_MONO;
    private int mChannelOutCount = AudioFormat.CHANNEL_OUT_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mInBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelInCount, mAudioFormat) * 4;
    private int mOutBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelOutCount, mAudioFormat);


    /** AudioRecord */
    private AudioRecord mAudioRecord = null;
    private boolean isRecording = false;

    /** AudioTrack */
    private AudioTrack mAudioTrack = null;
    private boolean isPlaying = false;

    private Button mBtnRecord = null;
    private Button mBtnPlay = null;
    private TextView mNote = null;
    private ScrollView mScrollView = null;
    private Button mBtnQuery = null;
    private Button mBtnFile = null;

    private Spinner mEncodingSpinner = null;
    private Spinner mSttModeSpinner = null;
    private Spinner mChannelSpinner = null;
    private Spinner mSampleRateSpinner = null;
    private Spinner mSampleFmtSpinner = null;

    private LinearLayout mEncodingOptLayout = null;
    private LinearLayout mTransactionLayout = null;
    private EditText mTransactioIdEditText = null;

    private int mTestType = 0;
    private int mSttMode = 0;

    private String mTartgetFilePath;

    private STT mStt = null;

    private ProgressDialog mProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_stt_rest);

        mBtnRecord = (Button)findViewById(R.id.sttrecording);
        mBtnPlay = (Button)findViewById(R.id.sttplay);
        mScrollView = (ScrollView)findViewById(R.id.txt_scrollview);

        mNote = (TextView)findViewById(R.id.note);
        mBtnQuery = (Button)findViewById(R.id.sttquery);
        mBtnFile = (Button)findViewById(R.id.sttfile);

        mEncodingOptLayout = (LinearLayout)findViewById(R.id.encodingOptLayout);
        mTransactionLayout = (LinearLayout)findViewById(R.id.transactionLayout);
        mTransactioIdEditText = (EditText) findViewById(R.id.transEditText);

        mEncodingSpinner = (Spinner)findViewById(R.id.encoding_spinner);
        mEncodingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getSelectedItemPosition() == 0){
                    mEncodingOptLayout.setVisibility(View.VISIBLE);
                }else{
                    mEncodingOptLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mChannelSpinner = (Spinner)findViewById(R.id.channel_spinner);
        mChannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChannel = parent.getSelectedItemPosition() + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSampleRateSpinner = (Spinner)findViewById(R.id.sampltRate_spinner);
        mSampleRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSampleRate = Integer.parseInt((String)parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSampleFmtSpinner = (Spinner)findViewById(R.id.sampltFmt_spinner);
        mSampleFmtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSampleFmt = (String)parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSttModeSpinner = (Spinner)findViewById(R.id.sttmode_spinner);
        mSttModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getSelectedItemPosition() == 0){
                    mTransactionLayout.setVisibility(View.INVISIBLE);
                    mBtnQuery.setEnabled(false);
                    mSttMode = 1;
                }else{
                    mTransactionLayout.setVisibility(View.VISIBLE);
                    mBtnQuery.setEnabled(true);
                    mSttMode = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();
        mTestType = intent.getIntExtra(TEST_TYPE, 0);
        if(mTestType == 0){

            mBtnFile.setVisibility(View.GONE);
            mTartgetFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aisdk/record.raw";

        } else if(mTestType == 1){

            mBtnFile.setVisibility(View.VISIBLE);
            mBtnRecord.setVisibility(View.GONE);
            mBtnPlay.setVisibility(View.GONE);

            String[] encodings = getResources().getStringArray(R.array.encoding_array);
            ArrayAdapter<CharSequence> endcodingAdapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    new ArrayList(Arrays.asList(encodings)));
            endcodingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mEncodingSpinner.setAdapter(endcodingAdapter);

            String[] channels = getResources().getStringArray(R.array.channel_array);
            ArrayAdapter<CharSequence> channelAdapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    new ArrayList(Arrays.asList(channels)));
            channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mChannelSpinner.setAdapter(channelAdapter);

            String[] sampleRates = getResources().getStringArray(R.array.samplerate_array);
            ArrayAdapter<CharSequence> sampleRateAdapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    new ArrayList(Arrays.asList(sampleRates)));
            sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSampleRateSpinner.setAdapter(sampleRateAdapter);

            String[] sampleFmts = getResources().getStringArray(R.array.sampleFmt_array);
            ArrayAdapter<CharSequence> sampleFmtAdapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    new ArrayList(Arrays.asList(sampleFmts)));
            sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSampleFmtSpinner.setAdapter(sampleFmtAdapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mStt == null){
            mStt = new STT();

            String strUrl = "https://" + ENV.hostname + ":" + ENV.ai_api_http_port;
            mStt.setServiceURL(strUrl);
            mStt.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgress();
        if(mStt != null){
            mStt = null;
        }
    }

    @Override
    public void onClick(View v) {
        Message msg;
        switch (v.getId()) {
            case R.id.sttrequest:

                mNote.setText("");
                mNote.invalidate();
                mTransactioIdEditText.setText("");

                if (TextUtils.isEmpty(mTartgetFilePath)){
                    Toast.makeText(this, "Text로 변환할 파일을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                final File file = new File(mTartgetFilePath);
                if (!file.exists()) {
                    Log.d(TAG, "File not exist >> " + mTartgetFilePath);
                    Toast.makeText(this, "Text로 변환할 음성을 먼저 녹음해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                String targetLanguage = "ko";

                int    channel = 0;
                int    sampleRate = 0;
                String sampleFmt = "";
                String encoding = (String)mEncodingSpinner.getSelectedItem();

                if (encoding.equalsIgnoreCase("raw")) {
                    channel = mChannel;
                    sampleRate = mSampleRate;
                    sampleFmt = mSampleFmt;
                }

                SendAudioFile(mSttMode, targetLanguage, encoding, channel, sampleRate, sampleFmt);
                break;

            case R.id.sttquery:

                String input = mTransactioIdEditText.getText().toString();
                if(input.length() <= 0){
                    Toast.makeText(this, "transactionId를 입력 후 요청해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                mNote.setText("");
                mNote.invalidate();

                queryStt(input);

                break;

            case R.id.sttfile:

                mNote.setText("");
                mNote.invalidate();

                Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFileIntent.setType("*/*");
                chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
                startActivityForResult(chooseFileIntent, REQUEST_CODE_MY_PICK);
                break;

            case R.id.sttrecording:
                if(isRecording) {
                    stopAudioRecording();
                    isRecording = false;
                    mBtnRecord.setText("start recording");
                    Toast.makeText(this, "stopRecording...", Toast.LENGTH_SHORT).show();
                }else{
                    startAudioRecording();
                    isRecording = true;
                    mBtnRecord.setText("stop recording");
                    Toast.makeText(this, "startRecording...", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sttplay:
                if(isPlaying) {
                    isPlaying = false;
                    mBtnPlay.setText("start play");
                    Toast.makeText(this, "stopPlaying...", Toast.LENGTH_SHORT).show();
                }else{
                    playAudio();
                    isPlaying = true;
                    mBtnPlay.setText("stop play");
                    Toast.makeText(this, "startPlaying...", Toast.LENGTH_SHORT).show();
                }
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
                        Log.d(TAG, "File Path() >> " + uriPath);

                        String filePath = null;
                        try {
                            if (uriPath.contains("/storage")) {
                                filePath = uriPath.substring(uriPath.indexOf("/storage"));
                            } else {
                                filePath = FilePickUtils.getPath(SttRestActivity.this, fileUri);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "get File Path() Error >> " + e.getMessage());
                        }
                        Log.i(TAG, "filePath: " + filePath);
                        if (TextUtils.isEmpty(filePath)) {
                            Toast.makeText(SttRestActivity.this, "선택한 파일경로가 인식이 되지 않습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            mTartgetFilePath = filePath;
                            Toast.makeText(SttRestActivity.this, "선택한 파일은 " + filePath + "입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(SttRestActivity.this, "파일이 선택되지 않았어요. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if(isDestroyed()) return false;

            switch (msg.what) {
                case MSG_SHOW_PROGRESS:
                    String message = (String)msg.obj;
                    showProgress(message);
                    break;
                case MSG_HIDE_PROGRESS:
                    hideProgress();
                    break;
                case MSG_SHOW_TOAST_TEXT:
                    String toastText = (String)msg.obj;
                    Toast.makeText(SttRestActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UDATE_TEXT:
                    String text = (String)msg.obj;
                    mNote.append(text);
                    mNote.invalidate();
                    mTransactioIdEditText.setText("");
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    Toast.makeText(SttRestActivity.this, "Text로 변환이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_REQUEST_STT_TEXT:
                    String transactionId = (String)msg.obj;
                    mTransactioIdEditText.setText(transactionId);
                    break;
                default:
                    break;
            }

            return false;
        }
    });


    private void SendAudioFile(final int sttMode, final String targetLanguage, final String encoding, final int channel, final int sampleRate, final String sampleFmt){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    File file = new File(mTartgetFilePath);

                    byte[] uploadAudioByte = null;

                    FileInputStream inputStream = new FileInputStream(file);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    uploadAudioByte = bos.toByteArray();

                    Log.d(TAG, "audioData length : " + uploadAudioByte.length);

                    inputStream.close();
                    bos.close();

                    JSONObject resultJson = mStt.requestSTT(uploadAudioByte, sttMode, targetLanguage, encoding, channel, sampleRate, sampleFmt);

                    requestSttResponse(resultJson);

                }catch (Exception e){
                    Log.d(TAG, "e >> " + e.getMessage());
                }
            }
        }).start();
    }

    private void requestSttResponse(JSONObject resultJson){

        try {
            int statusCode = resultJson.optInt("statusCode");
            if(statusCode == 200){

                boolean msgReceived = false;
                StringBuilder sb = new StringBuilder();

                String result = resultJson.optString("result");
                JSONArray resultArray = new JSONArray(result);
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject jsonObject = resultArray.getJSONObject(i);
                    Log.d(TAG, "requestSTT Result >> (" + i + ") data:" + jsonObject.toString());
                    if(!jsonObject.isNull("resultType")){
                        String resultType = jsonObject.getString("resultType");

                        if(resultType.equalsIgnoreCase("start")){
                            String transactionId = jsonObject.getString("transactionId");
                            if(mSttMode == 2){
                                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                                mUIHandler.sendMessage(msg);
                                msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "해당 Transaction Id로 조회해주세요.");
                                mUIHandler.sendMessage(msg);
                                msg = mUIHandler.obtainMessage(MSG_REQUEST_STT_TEXT, transactionId);
                                mUIHandler.sendMessage(msg);
                            }
                        }else if(resultType.equalsIgnoreCase("text")){
                            JSONObject sttResult = jsonObject.getJSONObject("sttResult");
                            String text = sttResult.getString("text");
                            msgReceived = true;
                            sb.append(text).append("\n");

                        }else if(resultType.equalsIgnoreCase("end")){
                            JSONObject sttInfoJson = jsonObject.getJSONObject("sttInfo");
                            Log.d(TAG, "sttInfo >> " + sttInfoJson.toString());

                        }else if (resultType.equalsIgnoreCase("err")) {
                            String errCode = jsonObject.getString("errCode");
                            String errMsg = "";
                            Log.d(TAG, "errCode >> " + errCode);
                            if(errCode.equalsIgnoreCase("STT000")){
                                errMsg = "허용 음성데이터 용량초과";
                            }else if(errCode.equalsIgnoreCase("STT001")){
                                errMsg = "Rest-API Key 사용 정책 설정시 월 API 사용량 한도 초과";
                            }else if(errCode.equalsIgnoreCase("STT002")){
                                errMsg = "오디오 포맷 판별 실패, Resampling 실패";
                            }else if(errCode.equalsIgnoreCase("STT003")){
                                errMsg = "비동기식 장문 음성 데이터 포맷 에러";
                            }
                            Log.d(TAG, "onError >> errCode:" + errCode + ", errMsg:" + errMsg);
                            Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                            mUIHandler.sendMessage(msg);
                            msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "errCode:" + errCode + ", errMsg:" + errMsg);
                            mUIHandler.sendMessage(msg);
                        }
                    }
                }

                if(msgReceived) {
                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, sb.toString());
                    mUIHandler.sendMessage(msg);
                }

            }else{
                Log.d(TAG, "onError >> statusCode:" + statusCode);
                String errorCode = resultJson.optString("errorCode");
                Log.d(TAG, "onError >> errorCode:" + errorCode);

                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "statusCode:" + statusCode  + ", errorCode:" + errorCode + ", STT로 변환요청 중 에러가 발생하였습니다.");
                mUIHandler.sendMessage(msg);
            }

        }catch (Exception e){
            Log.d(TAG, "e >> " + e.getMessage());
        }
    }

    private void queryStt(final String transactionId){

        Log.d(TAG, "transactionId : " + transactionId);
        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject resultJson = mStt.querySTT(transactionId);
                querySttResponse(resultJson);

            }
        }).start();
    }

    private void querySttResponse(JSONObject resultJson){
        try {
            int statusCode = resultJson.optInt("statusCode");

            if(statusCode == 200){

                StringBuilder sb = new StringBuilder();
                String sttStatus = resultJson.getString("sttStatus");
                Log.d(TAG, "sttStatus >> " + sttStatus);
                if(sttStatus.equalsIgnoreCase("processing")){

                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "요청한 Transaction 은 아직 처리중입니다. 다시 조회해주세요.");
                    mUIHandler.sendMessage(msg);

                }else if(sttStatus.equalsIgnoreCase("completed")){

                    String sttResult = resultJson.getString("sttResults");
                    JSONArray textArray = new JSONArray(sttResult);
                    for (int i = 0; i < textArray.length(); i++) {
                        JSONObject textJson = textArray.getJSONObject(i);
                        Log.d(TAG, "sttResult >> " + textJson.toString());
                        String text = textJson.getString("text");
                        sb.append(text).append("\n");
                    }

                    String sttInfo = resultJson.getString("sttInfo");
                    JSONObject sttInfoJson = new JSONObject(sttInfo);
                    Log.d(TAG, "sttInfo >> " + sttInfoJson.toString());

                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, sb.toString());
                    mUIHandler.sendMessage(msg);

                }else if (sttStatus.equalsIgnoreCase("err")) {

                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "sttStatus:err" + ", STT로 변환요청 중 에러가 발생하였습니다.");
                    mUIHandler.sendMessage(msg);
                }

            }else{
                Log.d(TAG, "onError >> statusCode:" + statusCode);
                String errorCode = resultJson.optString("errorCode");
                Log.d(TAG, "onError >> errorCode:" + errorCode);

                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "statusCode:" + statusCode  + ", errorCode:" + errorCode + ", STT로 변환요청 중 에러가 발생하였습니다.");
                mUIHandler.sendMessage(msg);
            }

        }catch (Exception e){
            Log.d(TAG, "e >> " + e.getMessage());
        }
    }

    private void startAudioRecording(){
        Log.d(TAG, "startRecording");
        if(isRecording == true) {
            Log.d(TAG, "Audio Recording already started");
            return;
        }

        if(mAudioRecord == null) {
            mAudioRecord =  new AudioRecord(mAudioSource, mSampleRate, mChannelInCount, mAudioFormat, mInBufferSize);
        }

        mAudioRecord.startRecording();
        isRecording = true;
        Thread mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mInBufferSize];
                String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aisdk/record.raw";
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mFilePath);
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                }

                while(isRecording) {
                    int ret = mAudioRecord.read(readData, 0, mInBufferSize);
                    Log.d(TAG, "read bytes is " + ret);
                    try {
                        fos.write(readData, 0, mInBufferSize);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;

                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mRecordThread.start();
    }

    private void stopAudioRecording(){
        if(isRecording == false) {
            Log.d(TAG, "Audio Recording do first");
            return;
        }
        isRecording = false;
    }

    private void playAudio(){
        if(mAudioTrack == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAudioTrack = new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(mAudioFormat)
                                .setSampleRate(mSampleRate)
                                .setChannelMask(mChannelOutCount)
                                .build())
                        .setBufferSizeInBytes(mOutBufferSize)
                        .build();
                mAudioTrack.setPlaybackRate(mSampleRate);
            }else{
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelOutCount, mAudioFormat, mOutBufferSize, AudioTrack.MODE_STREAM);
            }
        }
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
        }else{
            Thread mPlayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(mTartgetFilePath);
                    if (!file.exists()) {
                        Log.d(TAG, "File not exist >> " + mTartgetFilePath);
                        Toast.makeText(SttRestActivity.this, "재생할 음성을 먼저 녹음해주세요.", Toast.LENGTH_SHORT).show();
                        isPlaying = false;
                        mBtnPlay.setText("start play");
                        return;
                    }

                    byte[] writeData = new byte[mOutBufferSize];
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(mTartgetFilePath);
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    DataInputStream dis = new DataInputStream(fis);
                    mAudioTrack.play();

                    while(isPlaying) {
                        try {
                            int ret = dis.read(writeData, 0, mOutBufferSize);
                            if (ret <= 0) {
                                (SttRestActivity.this).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        isPlaying = false;
                                        mBtnPlay.setText("start play");
                                    }
                                });
                                break;
                            }
                            mAudioTrack.write(writeData, 0, ret);
                        }catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    mAudioTrack.stop();
                    mAudioTrack.release();
                    mAudioTrack = null;
                    try {
                        dis.close();
                        fis.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            mPlayThread.start();
        }
    }

    private void showProgress(String message) {
        mProgress = new ProgressDialog(this);
        mProgress.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(message);
        mProgress.setCancelable(true);
        mProgress.show();
    }

    private void hideProgress() {
        if (mProgress != null) {
            mProgress.cancel();
            mProgress = null;
        }
    }

}
