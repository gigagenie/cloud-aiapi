package com.kt.ai.aisdk.ui;

import static com.kt.ai.aisdk.ui.MainActivity.TEST_TYPE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import kt.gigagenie.ai.api.STT;

public class Stt2Activity  extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG =  "Stt2Activity";

    // UI Handler
    private static final int MSG_SHOW_PROGRESS = 0;
    private static final int MSG_HIDE_PROGRESS = 1;
    private static final int MSG_SHOW_TOAST_TEXT = 2;
    private static final int MSG_UDATE_TEXT = 3;
    private static final int MSG_REQUEST_STT_TEXT = 4;

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
//    private Spinner mSttModeSpinner = null;
    private Spinner msttmodelcodeSpinner = null;
//    private Spinner mSampleRateSpinner = null;
//    private Spinner mSampleFmtSpinner = null;

    private LinearLayout mEncodingOptLayout = null;
    private LinearLayout mTransactionLayout = null;
    private EditText mTransactioIdEditText = null;

    private int mTestType = 0;
    private int mSttModelcode = 0;

    private String mTartgetFilePath;

    private STT mStt = null;

    private ProgressDialog mProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_stt2);

        mScrollView = (ScrollView) findViewById(R.id.txt_scrollview_ad);

        mNote = (TextView) findViewById(R.id.note_ad);

        mBtnQuery = (Button) findViewById(R.id.sttquery_ad);
        mBtnFile = (Button) findViewById(R.id.sttfile_ad);

        mTransactionLayout = (LinearLayout) findViewById(R.id.transactionLayout_ad);

        mTransactioIdEditText = (EditText) findViewById(R.id.transEditText_ad);

        mEncodingSpinner = (Spinner) findViewById(R.id.encoding_spinner_ad);
        msttmodelcodeSpinner = (Spinner) findViewById(R.id.stt2modelcode_spinner);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mStt == null) {
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
        if (mStt != null) {
            mStt = null;
        }
    }

    @Override
    public void onClick(View v) {
        Message msg;
        switch (v.getId()) {
            case R.id.sttrequest_ad: // request stt

                mNote.setText("");
                mNote.invalidate();
                mTransactioIdEditText.setText("");
                mStt.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
                if (TextUtils.isEmpty(mTartgetFilePath)) {
                    Toast.makeText(this, "Text로 변환할 파일을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                final File file = new File(mTartgetFilePath);
                if (!file.exists()) {
                    Toast.makeText(this, "Text로 변환할 음성을 먼저 녹음해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                String encoding = (String) mEncodingSpinner.getSelectedItem();
                int sttmodelcode = Integer.parseInt((String) msttmodelcodeSpinner.getSelectedItem());
                mSttModelcode = sttmodelcode;

                SendAudioFile(sttmodelcode, encoding);
                break;

            case R.id.sttquery_ad: // request stt
                mStt.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
                String input = mTransactioIdEditText.getText().toString();
                if (input.length() <= 0) {
                    Toast.makeText(this, "transactionId를 입력 후 요청해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                mNote.setText("");
                mNote.invalidate();

                queryStt(input);

                break;

            case R.id.sttfile_ad:

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
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri fileUri = data.getData();
                        String uriPath = fileUri.getPath();

                        String filePath = null;
                        try {
                            if (uriPath.contains("/storage")) {
                                filePath = uriPath.substring(uriPath.indexOf("/storage"));
                            } else {
                                filePath = FilePickUtils.getPath(Stt2Activity.this, fileUri);
                            }
                        } catch (Exception e) {

                        }

                        if (TextUtils.isEmpty(filePath)) {
                            Toast.makeText(Stt2Activity.this, "선택한 파일경로가 인식이 되지 않습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            mTartgetFilePath = filePath;
                            Toast.makeText(Stt2Activity.this, "선택한 파일은 " + filePath + "입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(Stt2Activity.this, "파일이 선택되지 않았어요. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
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

            if (isDestroyed()) return false;

            switch (msg.what) {
                case MSG_SHOW_PROGRESS:
                    String message = (String) msg.obj;
                    showProgress(message);
                    break;
                case MSG_HIDE_PROGRESS:
                    hideProgress();
                    break;
                case MSG_SHOW_TOAST_TEXT:
                    String toastText = (String) msg.obj;
                    Toast.makeText(Stt2Activity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UDATE_TEXT:
                    String text = (String) msg.obj;
                    mNote.append(text);
                    mNote.invalidate();
                    mTransactioIdEditText.setText("");
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    Toast.makeText(Stt2Activity.this, "Text로 변환이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_REQUEST_STT_TEXT:
                    String transactionId = (String) msg.obj;
                    mTransactioIdEditText.setText(transactionId);
                    break;
                default:
                    break;
            }

            return false;
        }
    });


    private void SendAudioFile(final int sttModelecode , final String encoding) {

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

                    inputStream.close();
                    bos.close();

                    JSONObject resultJson = mStt.requestSTT2(uploadAudioByte, sttModelecode, encoding);

                    requestSttResponse(resultJson);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void requestSttResponse(JSONObject resultJson) {

        try {
            int statusCode = resultJson.optInt("statusCode");
            if (statusCode == 200) {

                boolean msgReceived = false;
                StringBuilder sb = new StringBuilder();

                String result = resultJson.optString("result");
                JSONArray resultArray = new JSONArray(result);
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject jsonObject = resultArray.getJSONObject(i);

                    if (!jsonObject.isNull("resultType")) {
                        String resultType = jsonObject.getString("resultType");

                        if (resultType.equalsIgnoreCase("start")) {
                            String transactionId = jsonObject.getString("transactionId");
                            if (mSttModelcode == 4 || mSttModelcode == 5) {
                                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                                mUIHandler.sendMessage(msg);
                                msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "해당 Transaction Id로 조회해주세요.");
                                mUIHandler.sendMessage(msg);
                                msg = mUIHandler.obtainMessage(MSG_REQUEST_STT_TEXT, transactionId);
                                mUIHandler.sendMessage(msg);
                            }
                        } else if (resultType.equalsIgnoreCase("text")) {
                            JSONObject sttResult = jsonObject.getJSONObject("sttResult");
                            String text = sttResult.getString("text");
                            msgReceived = true;
                            sb.append(text).append("\n");

                        } else if (resultType.equalsIgnoreCase("end")) {
                            JSONObject sttInfoJson = jsonObject.getJSONObject("sttInfo");

                        } else if (resultType.equalsIgnoreCase("err")) {
                            String errCode = jsonObject.getString("errCode");
                            String errMsg = "";

                            if (errCode.equalsIgnoreCase("STT000")) {
                                errMsg = "허용 음성데이터 용량초과";
                            } else if (errCode.equalsIgnoreCase("STT001")) {
                                errMsg = "Rest-API Key 사용 정책 설정시 월 API 사용량 한도 초과";
                            } else if (errCode.equalsIgnoreCase("STT002")) {
                                errMsg = "오디오 포맷 판별 실패, Resampling 실패";
                            } else if (errCode.equalsIgnoreCase("STT003")) {
                                errMsg = "비동기식 장문 음성 데이터 포맷 에러";
                            }

                            Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                            mUIHandler.sendMessage(msg);
                            msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, "errCode:" + errCode + ", errMsg:" + errMsg); //MSG_SHOW_TOAST_TEXT
                            mUIHandler.sendMessage(msg);
                        }
                    }
                }

                if (msgReceived) {
                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, sb.toString());
                    mUIHandler.sendMessage(msg);
                }

            } else {

                String errorCode = resultJson.optString("errorCode");

                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, "statusCode:" + statusCode + ", errorCode:" + resultJson.toString());//errorCode + ", STT AD로 변환요청 중 에러가 발생하였습니다.");
                mUIHandler.sendMessage(msg);
            }

        } catch (Exception e) {

        }
    }

    private void queryStt(final String transactionId) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject resultJson = mStt.querySTT(transactionId);
                querySttResponse(resultJson);

            }
        }).start();
    }

    private void querySttResponse(JSONObject resultJson) {
        try {
            int statusCode = resultJson.optInt("statusCode");

            if (statusCode == 200) {

                StringBuilder sb = new StringBuilder();
                String sttStatus = resultJson.getString("sttStatus");

                if (sttStatus.equalsIgnoreCase("processing")) {

                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "요청한 Transaction 은 아직 처리중입니다. 다시 조회해주세요.");
                    mUIHandler.sendMessage(msg);

                } else if (sttStatus.equalsIgnoreCase("completed")) {

                    String sttResult = resultJson.getString("sttResults");
                    JSONArray textArray = new JSONArray(sttResult);
                    for (int i = 0; i < textArray.length(); i++) {
                        JSONObject textJson = textArray.getJSONObject(i);

                        String text = textJson.getString("text");
                        sb.append(text).append("\n");
                    }

                    String sttInfo = resultJson.getString("sttInfo");
                    JSONObject sttInfoJson = new JSONObject(sttInfo);

                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, sb.toString());
                    mUIHandler.sendMessage(msg);

                } else if (sttStatus.equalsIgnoreCase("err")) {

                    Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                    mUIHandler.sendMessage(msg);
                    msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, "sttStatus:err" + ", STT AD로 변환요청 중 에러가 발생하였습니다.");
                    mUIHandler.sendMessage(msg);
                }

            } else {

                String errorCode = resultJson.optString("errorCode");

                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, "statusCode:" + statusCode + ", errorCode:" + errorCode + ", STT AD로 변환요청 중 에러가 발생하였습니다.");
                mUIHandler.sendMessage(msg);
            }

        } catch (Exception e) {

        }
    }



    private void showProgress(String message) {
        mProgress = new ProgressDialog(this);
        mProgress.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //뒤가 흐리지않게
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
