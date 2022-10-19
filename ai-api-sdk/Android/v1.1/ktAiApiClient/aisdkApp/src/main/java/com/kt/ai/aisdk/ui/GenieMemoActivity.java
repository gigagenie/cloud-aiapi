package com.kt.ai.aisdk.ui;

import static com.kt.ai.aisdk.ui.MainActivity.TEST_TYPE;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;

//import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.media.AudioAttributes;
import android.media.AudioFormat;
//import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
//import android.os.Build;
import android.os.Bundle;
//import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
//import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import kt.gigagenie.ai.api.GenieMemo;

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


public class GenieMemoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "GenieMemoActivity";

    // UI Handler
    private static final int MSG_SHOW_PROGRESS = 0;
    private static final int MSG_HIDE_PROGRESS = 1;
    private static final int MSG_SHOW_TOAST_TEXT = 2;
    private static final int MSG_UDATE_TEXT = 3;
    private static final int MSG_REQUEST_STT_TEXT = 4;

    private static final int REQUEST_CODE_MY_PICK = 2000;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mChannelInCount = AudioFormat.CHANNEL_IN_MONO;
    private int mChannelOutCount = AudioFormat.CHANNEL_OUT_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

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

    private Spinner mCallindexSpinner = null;
    private Spinner mlastYNSpinner = null;
    private Spinner mSelectSpinner = null;

    private LinearLayout mselectOptLayout = null;
    private LinearLayout mCallkeyLayout = null;
    private EditText mCallkeyEditText = null;

    private int mTestType = 0;
//    private int mGenieMemoMode = 0;

    private String mTartgetFilePath;

    private GenieMemo mGenieMemo = null;

    private ProgressDialog mProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_geniememo);


        mScrollView = (ScrollView) findViewById(R.id.txt_scrollview_g);

        mNote = (TextView) findViewById(R.id.note_g);
        mBtnQuery = (Button) findViewById(R.id.genieMemoquery);
        mBtnFile = (Button) findViewById(R.id.genieMemofile);

        mselectOptLayout = (LinearLayout) findViewById(R.id.selectOptLayout_g);
        mCallkeyLayout = (LinearLayout) findViewById(R.id.callkeyLayout);
        mCallkeyEditText = findViewById(R.id.callKeyText);

        mCallindexSpinner = (Spinner) findViewById(R.id.callindex_spinner);

        mlastYNSpinner = (Spinner) findViewById(R.id.lastYN_spinner_g);
        mSelectSpinner = (Spinner) findViewById(R.id.select_spinner_g);
        mSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItemPosition() == 0) {
                    mCallindexSpinner.setEnabled(true);
                    mlastYNSpinner.setEnabled(true);


                } else {
                    mCallindexSpinner.setEnabled(false);
                    mlastYNSpinner.setEnabled(false);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//
//


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGenieMemo == null) {
            mGenieMemo = new GenieMemo();

            String strUrl = "https://" + ENV.hostname + ":" + ENV.ai_api_http_port;
            mGenieMemo.setServiceURL(strUrl);
            mGenieMemo.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgress();
        if (mGenieMemo != null) {
            mGenieMemo = null;
        }
    }

    @Override
    public void onClick(View v) {
        Message msg;
        switch (v.getId()) {
            case R.id.genieMemorequest: // request stt

                mNote.setText("");
                mNote.invalidate();
                mGenieMemo.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
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

                String lastYN = (String) mlastYNSpinner.getSelectedItem();
                int callindex = Integer.parseInt((String) mCallindexSpinner.getSelectedItem());
                String selectgenie = (String) mSelectSpinner.getSelectedItem();

                String Callkey =  mCallkeyEditText.getText().toString();

                SendAudioFile( selectgenie, lastYN, callindex, Callkey);
                break;

            case R.id.genieMemoquery: // request geniememo
                mGenieMemo.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
                String input = mCallkeyEditText.getText().toString();
                if (input.length() <= 0) {
                    Toast.makeText(this, "callkey를 입력 후 요청해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                mNote.setText("");
                mNote.invalidate();

                queryGenieMemo(input);

                break;

            case R.id.genieMemofile:

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
                                filePath = FilePickUtils.getPath(GenieMemoActivity.this, fileUri);
                            }
                        } catch (Exception e) {

                        }

                        if (TextUtils.isEmpty(filePath)) {
                            Toast.makeText(GenieMemoActivity.this, "선택한 파일경로가 인식이 되지 않습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            mTartgetFilePath = filePath;
                            Toast.makeText(GenieMemoActivity.this, "선택한 파일은 " + filePath + "입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(GenieMemoActivity.this, "파일이 선택되지 않았어요. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(GenieMemoActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UDATE_TEXT:
                    String text = (String) msg.obj;
                    mNote.append(text);
                    mNote.invalidate();
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    Toast.makeText(GenieMemoActivity.this, "Text로 변환이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_REQUEST_STT_TEXT:
                    String callkey = (String) msg.obj;
                    mCallkeyEditText.setText(callkey);
                    break;
                default:
                    break;
            }

            return false;
        }
    });


    private void SendAudioFile(final String select, final String lastYN,final int callindex,final String callkey){

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

                    JSONObject resultJson;

                    // 동기
                    if (select.equals("GenieMemo")) {
                        resultJson = mGenieMemo.requestGenieMemo(uploadAudioByte, callkey, lastYN, callindex);
                    }
                    // 비동기
                    else {
                        resultJson = mGenieMemo.requestGenieMemoAsync(uploadAudioByte, callkey);
                    }
                    requestGenieMemoResponse(resultJson);

                } catch (Exception e) {

                }
            }
        }).start();
    }

    private void requestGenieMemoResponse(JSONObject resultJson) {

        try {
            int statusCode = resultJson.optInt("statusCode");
            if (statusCode == 200) {

                StringBuilder sb = new StringBuilder();

                String result = resultJson.optString("result");
                JSONArray resultArray = new JSONArray(result);

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject jsonObject = resultArray.getJSONObject(i);
                    if (!jsonObject.isNull("dataList")){
                        String aa = jsonObject.getString("dataList");
                        sb.append(aa).append("\n");
                    }
                }
                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, sb.toString());
                mUIHandler.sendMessage(msg);

            } else {
                String errorCode = resultJson.optString("errorCode");
                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, "statusCode:" + statusCode + ", error:" + resultJson.toString() + ", GenieMemo 요청 중 에러가 발생하였습니다.");
                mUIHandler.sendMessage(msg);
            }
        } catch (Exception e) {

        }
    }

    private void queryGenieMemo(final String callkey) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject resultJson = mGenieMemo.queryGenieMemo(callkey);
                queryGenieMemoResponse(resultJson);

            }
        }).start();
    }

    private void queryGenieMemoResponse(JSONObject resultJson) {
                Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                mUIHandler.sendMessage(msg);
                msg = mUIHandler.obtainMessage(MSG_UDATE_TEXT, resultJson.toString());
                mUIHandler.sendMessage(msg);
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
