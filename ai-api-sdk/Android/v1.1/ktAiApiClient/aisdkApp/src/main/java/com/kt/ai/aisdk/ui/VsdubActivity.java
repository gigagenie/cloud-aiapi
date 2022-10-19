package com.kt.ai.aisdk.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kt.gigagenie.ai.api.VSDUB;

public class VsdubActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VsdubActivity";

    // UI Handler
    private static final int MSG_SHOW_PROGRESS = 0;
    private static final int MSG_HIDE_PROGRESS = 1;
    private static final int MSG_SHOW_TOAST_TEXT = 2;
    private static final int MSG_PLAY_TEXT = 3;
    private static final int MSG_UDATE_TEXT = 3;
    private static final int MSG_REQUEST_VSDUB_TEXT = 4;

    private static final int REQUEST_CODE_MY_PICK = 2000;

    private MediaPlayer mediaPlayer = null;
    private boolean isVsdubPlaying = false;
    private Button mBtnVsdubPlay = null;
    private EditText mEditText = null;

    private int SampleRate = 16000;
    private int Channel = 1;
    private String SampleFmt = "S16LE";

    //    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mChannelInCount = AudioFormat.CHANNEL_IN_MONO;
    private int mChannelOutCount = AudioFormat.CHANNEL_OUT_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
//    private int mInBufferSize = AudioRecord.getMinBufferSize(SampleRate, mChannelInCount, mAudioFormat) * 4;
//    private int mOutBufferSize = AudioTrack.getMinBufferSize(SampleRate, mChannelOutCount, mAudioFormat);

    /** AudioRecord */
//    private AudioRecord mAudioRecord = null;
//    private boolean isRecording = false;

    /** AudioTrack */
    private AudioTrack mAudioTrack = null;
    private boolean isPlaying = false;

    //    private Button mBtnRecord = null;
    private Button mBtnPlay = null;
    private TextView mNote = null;
    private ScrollView mScrollView = null;
    //    private Button mBtnQuery = null;
    private Button mBtnRequest = null;
    private Button mBtnFile = null;


    private Spinner mEmotion = null;
    private Spinner mLanguage = null;
    private Spinner mEncoding = null;
    private Spinner mSampleRate = null;

    private SeekBar mSpeakSeekbar = null;
    private SeekBar mPitchSeekbar = null;
    private SeekBar mSpeedSeekbar = null;
    private SeekBar mVolumeSeekbar = null;

    private TextView mSpeakLevel = null;
    private TextView mPitchLevel = null;
    private TextView mSpeedLevel = null;
    private TextView mVolumeLevel = null;

    private LinearLayout mEncodingOptLayout = null;
    private LinearLayout mTransactionLayout = null;
    private EditText mTransactioIdEditText = null;

    private EditText mVoiceName = null;

    private int mTestType = 0;

    private String mTartgetFilePath = null;

    private VSDUB mVsdub = null;

    private ProgressDialog mProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_vsdub);

        mBtnPlay = (Button) findViewById(R.id.vsdubplay);
        mScrollView = (ScrollView) findViewById(R.id.txt_scrollview);

        mBtnVsdubPlay = (Button)findViewById(R.id.vsdubplay);
        mEditText = (EditText) findViewById(R.id.charEditText);

        mBtnRequest = (Button) findViewById(R.id.vsdubrequest);
        mBtnFile = (Button) findViewById(R.id.vsdubfile);

        mEncodingOptLayout = (LinearLayout) findViewById(R.id.encodingOptLayout);
        mTransactionLayout = (LinearLayout) findViewById(R.id.transactionLayout);
        mTransactioIdEditText = (EditText) findViewById(R.id.transEditText);

        mEmotion = (Spinner)findViewById(R.id.emotion_spinner);
        mLanguage = (Spinner)findViewById(R.id.language_spinner);
        mEncoding = (Spinner)findViewById(R.id.encoding_spinner);
        mSampleRate = (Spinner)findViewById(R.id.SampleRate_spinner);

        mSpeakSeekbar = (SeekBar)findViewById(R.id.speaker_seek);
        mPitchSeekbar = (SeekBar)findViewById(R.id.pitch_seek);
        mSpeedSeekbar = (SeekBar)findViewById(R.id.speed_seek);
        mVolumeSeekbar = (SeekBar)findViewById(R.id.volume_seek);

        mSpeakLevel = (TextView)findViewById(R.id.speaker_level);
        mPitchLevel = (TextView)findViewById(R.id.pitch_level);
        mSpeedLevel = (TextView)findViewById(R.id.speed_level);
        mVolumeLevel = (TextView)findViewById(R.id.volume_level);

        mVoiceName = (EditText)findViewById(R.id.voicename_edit);

        mEncoding = (Spinner) findViewById(R.id.encoding_spinner);
        mEncoding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItemPosition() == 1) {
                    mEncodingOptLayout.setVisibility(View.VISIBLE);
                } else {
                    mEncodingOptLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpeakSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int level = progress + 100;
                mSpeakLevel.setText("" + level);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mPitchSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int level = progress + 50;
                mPitchLevel.setText("" + level);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSpeedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int level = progress + 50;
                mSpeedLevel.setText("" + level);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mVolumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int level = progress + 50;
                mVolumeLevel.setText("" + level);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSampleRate = (Spinner) findViewById(R.id.sampleRate_spinner);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVsdub == null) {
            mVsdub = new VSDUB();

            String strUrl = "https://" + ENV.hostname + ":" + ENV.ai_api_http_port;
            mVsdub.setServiceURL(strUrl);
            mVsdub.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgress();
        if (mVsdub != null) {
            mVsdub = null;
        }
    }

    @Override
    public void onClick(View v) {
        Message msg;
        switch (v.getId()) {
            case R.id.vsdubrequest: // request vsdub
                mVsdub.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);

                final String text = mEditText.getText().toString();
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(this, "VSDUB로 변환할 문자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                mNote.setText("");
                mNote.invalidate();
                mTransactioIdEditText.setText("");

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

                int    speaker = Integer.parseInt(mSpeakLevel.getText().toString());
                String voiceName = mVoiceName.getText().toString();
                int    pitch = Integer.parseInt(mPitchLevel.getText().toString());
                int    speed = Integer.parseInt(mSpeedLevel.getText().toString());
                int    volume = Integer.parseInt(mVolumeLevel.getText().toString());
                String emotion = mEmotion.getSelectedItem().toString();
                String language = mLanguage.getSelectedItem().toString();
                String encoding =  mEncoding.getSelectedItem().toString();
                int    sampleRate =  Integer.parseInt(mSampleRate.getSelectedItem().toString());

                SendAudioFile(text, speaker, voiceName, pitch, speed, volume, emotion, language, encoding, sampleRate);
                break;


            case R.id.vsdubfile:


                Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFileIntent.setType("*/*");
                chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
                startActivityForResult(chooseFileIntent, REQUEST_CODE_MY_PICK);
                break;

            case R.id.vsdubplay: // play with vsdub file
                if (isPlaying) {
                    isPlaying = false;
                    mBtnPlay.setText("start play");
                    Toast.makeText(this, "stopPlaying...", Toast.LENGTH_SHORT).show();
                } else {
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
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri fileUri = data.getData();
                        String uriPath = fileUri.getPath();

                        String filePath = null;
                        try {
                            if (uriPath.contains("/storage")) {
                                filePath = uriPath.substring(uriPath.indexOf("/storage"));
                            } else {
                                filePath = FilePickUtils.getPath(VsdubActivity.this, fileUri);
                            }
                        } catch (Exception e) {

                        }

                        if (TextUtils.isEmpty(filePath)) {
                            Toast.makeText(VsdubActivity.this, "선택한 파일경로가 인식이 되지 않습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            mTartgetFilePath = filePath;
                            Toast.makeText(VsdubActivity.this, "선택한 파일은 " + filePath + "입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(VsdubActivity.this, "파일이 선택되지 않았어요. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(VsdubActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PLAY_TEXT:
                    if(isVsdubPlaying) {
                        stopAudio();
                    }
                    playAudio();
                    break;
                default:
                    break;
            }

            return false;
        }
    });


    private void SendAudioFile(final String text, final int speaker, final String voiceName, final int pitch, final int speed,
                               final int volume, final String emotion, final String language, final String encoding, final int sampleRate) {

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

                    JSONObject resultJson = mVsdub.requestVSDUB(text, speaker, voiceName, pitch, speed, volume, emotion, language, encoding, sampleRate, uploadAudioByte);

                    int statusCode = resultJson.optInt("statusCode");

                    if(statusCode == 200){

                        byte[] audioData = (byte[])resultJson.opt("audioData");

                        String targetFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+ "/vsdub.mp3";

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(targetFilePath);
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            fos.write(audioData, 0, audioData.length);
                            fos.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                        Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                        mUIHandler.sendMessage(msg);

                        msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "VSDUB로 변환이 완료되었습니다.");
                        mUIHandler.sendMessage(msg);

                        msg = mUIHandler.obtainMessage(MSG_PLAY_TEXT);
                        mUIHandler.sendMessage(msg);

                    }else{
                        String errorCode = resultJson.optString("errorCode");

                        Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                        mUIHandler.sendMessage(msg);
                        msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "statusCode:" + statusCode  + ", errorCode:" + errorCode + ", VSDUB로 변환요청 중 에러가 발생하였습니다.");
                        mUIHandler.sendMessage(msg);
                    }

                }catch (Exception e){

                }
            }
        }).start();
    }


    private void playAudio(){

        String mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+ "/vsdub.mp3";
        File file = new File(mFilePath);
        if (!file.exists()) {
            Toast.makeText(this, "VSDUB로 변환을 먼저 수행해주세요.", Toast.LENGTH_SHORT).show();
            isVsdubPlaying = false;
            mBtnVsdubPlay.setText("play audio");
            return;
        }

        try {
            // Release any resources from previous MediaPlayer
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    isVsdubPlaying = false;
                    mBtnVsdubPlay.setText("play audio");
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isVsdubPlaying = false;
                    mBtnVsdubPlay.setText("play audio");
                }
            });

            FileInputStream fis = new FileInputStream(mFilePath);
            FileDescriptor fd = fis.getFD();
            mediaPlayer.setDataSource(fd);

            mediaPlayer.prepare();
            mediaPlayer.start();
            isVsdubPlaying = true;
            mBtnVsdubPlay.setText("stop audio");
        }catch (Exception e){

        }
    }

    private void stopAudio(){
        if(isVsdubPlaying) {
            isVsdubPlaying = false;
            mBtnVsdubPlay.setText("play audio");
            if(mediaPlayer != null) {
                mediaPlayer.stop();
            }
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