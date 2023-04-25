package com.kt.ai.aisdk.ui;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kt.gigagenie.ai.api.VSTTS;

public class VsttsActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG =  "VsttsActivity";

    // UI Handler
    private static final int MSG_SHOW_PROGRESS              = 0;
    private static final int MSG_HIDE_PROGRESS              = 1;
    private static final int MSG_SHOW_TOAST_TEXT            = 2;
    private static final int MSG_PLAY_TEXT                  = 3;

    private MediaPlayer mediaPlayer = null;
    private boolean isVsttsPlaying = false;
    private Button mBtnVsttsPlay = null;
    private EditText mEditText = null;

    private Spinner mEmotion = null;
    private Spinner mLanguage = null;
    private Spinner mEncoding = null;
    private Spinner mChannel = null;
    private Spinner mSampleRate = null;
    private Spinner mSampleFmt = null;

    private SeekBar mSpeakSeekbar = null;
    private SeekBar mPitchSeekbar = null;
    private SeekBar mSpeedSeekbar = null;
    private SeekBar mVolumeSeekbar = null;

    private TextView mSpeakLevel = null;
    private TextView mPitchLevel = null;
    private TextView mSpeedLevel = null;
    private TextView mVolumeLevel = null;

    private EditText mVoiceName = null;

    private VSTTS mVstts = null;

    private ProgressDialog mProgress = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_vstts);

        mBtnVsttsPlay = (Button)findViewById(R.id.vsttsplay);
        mEditText = (EditText)findViewById(R.id.charEditText);

        mEmotion = (Spinner)findViewById(R.id.emotion_spinner);
        mLanguage = (Spinner)findViewById(R.id.language_spinner);
        mEncoding = (Spinner)findViewById(R.id.encoding_spinner);
        mChannel = (Spinner)findViewById(R.id.Channel_spinner);
        mSampleRate = (Spinner)findViewById(R.id.SampleRate_spinner);
        mSampleFmt = (Spinner)findViewById(R.id.SampleFmt_spinner);

        mSpeakSeekbar = (SeekBar)findViewById(R.id.speaker_seek);
        mPitchSeekbar = (SeekBar)findViewById(R.id.pitch_seek);
        mSpeedSeekbar = (SeekBar)findViewById(R.id.speed_seek);
        mVolumeSeekbar = (SeekBar)findViewById(R.id.volume_seek);

        mSpeakLevel = (TextView)findViewById(R.id.speaker_level);
        mPitchLevel = (TextView)findViewById(R.id.pitch_level);
        mSpeedLevel = (TextView)findViewById(R.id.speed_level);
        mVolumeLevel = (TextView)findViewById(R.id.volume_level);

        mVoiceName = (EditText)findViewById(R.id.voicename_edit);

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

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mVstts == null){
            mVstts = new VSTTS();

            String strUrl = "https://" + ENV.hostname + ":" + ENV.ai_api_http_port;

            mVstts.setServiceURL(strUrl);
            mVstts.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        hideProgress();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if(mVstts != null){
            mVstts = null;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.vsttsrequest: // request vstts
                mVstts.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
                final String text = mEditText.getText().toString();
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(this, "VSTTS로 변환할 문자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                Message msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                int    speaker = Integer.parseInt(mSpeakLevel.getText().toString());
                String voiceName = mVoiceName.getText().toString();
                int    pitch = Integer.parseInt(mPitchLevel.getText().toString());
                int    speed = Integer.parseInt(mSpeedLevel.getText().toString());
                int    volume = Integer.parseInt(mVolumeLevel.getText().toString());
                String emotion = mEmotion.getSelectedItem().toString();
                String language = mLanguage.getSelectedItem().toString();
                String encoding =  mEncoding.getSelectedItem().toString();
                int    channel = mChannel.getSelectedItemPosition() + 1;
                int    sampleRate =  Integer.parseInt(mSampleRate.getSelectedItem().toString());
                String sampleFmt =  mSampleFmt.getSelectedItem().toString();

                sendText(text, speaker, voiceName, pitch, speed, volume, emotion,
                        language, encoding, channel, sampleRate, sampleFmt);

                break;
            case R.id.vsttsplay: // play with vstts file
                if(isVsttsPlaying) {
                    stopAudio();
                    Toast.makeText(this, "stopPlaying...", Toast.LENGTH_SHORT).show();
                }else{
                    playAudio();
                    Toast.makeText(this, "startPlaying...", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * UI Handler 에서 처리할 일이 생길때 호출되는 핸들러
     */
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
                    String text = (String)msg.obj;
                    Toast.makeText(VsttsActivity.this, text, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PLAY_TEXT:
                    if(isVsttsPlaying) {
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


    private void sendText(final String text,
                          final int speaker,
                          final String voiceName,
                          final int pitch,
                          final int speed,
                          final int volume,
                          final String emotion,
                          final String language,
                          final String encoding,
                          final int channel,
                          final int sampleRate,
                          final String sampleFmt) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    JSONObject resultJson = mVstts.requestVSTTS(text, speaker, voiceName, pitch, speed, volume,
                            emotion, language, encoding, channel, sampleRate, sampleFmt);

                    int statusCode = resultJson.optInt("statusCode");

                    if(statusCode == 200){

                        byte[] audioData = (byte[])resultJson.opt("audioData");
//                        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aisdk/vstts.mp3";
                        String mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+ "/vstts.mp3";
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(mFilePath);
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

                        msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "VSTTS로 변환이 완료되었습니다.");
                        mUIHandler.sendMessage(msg);

                        msg = mUIHandler.obtainMessage(MSG_PLAY_TEXT);
                        mUIHandler.sendMessage(msg);

                    }else{
                        String errorCode = resultJson.optString("errorCode");

                        Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                        mUIHandler.sendMessage(msg);
                        msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "statusCode:" + statusCode  + ", errorCode:" + errorCode + ", VSTTS로 변환요청 중 에러가 발생하였습니다.");
                        mUIHandler.sendMessage(msg);
                    }

                }catch (Exception e){

                }
            }
        }).start();
    }

    private void playAudio(){

        String mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+ "/vstts.mp3";
        File file = new File(mFilePath);
        if (!file.exists()) {
            Toast.makeText(this, "VSTTS로 변환을 먼저 수행해주세요.", Toast.LENGTH_SHORT).show();
            isVsttsPlaying = false;
            mBtnVsttsPlay.setText("play audio");
            return;
        }

        try {
            // Release any resources from previous MediaPlayer
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    isVsttsPlaying = false;
                    mBtnVsttsPlay.setText("play audio");
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isVsttsPlaying = false;
                    mBtnVsttsPlay.setText("play audio");
                }
            });

            FileInputStream fis = new FileInputStream(mFilePath);
            FileDescriptor fd = fis.getFD();
            mediaPlayer.setDataSource(fd);

            mediaPlayer.prepare();
            mediaPlayer.start();
            isVsttsPlaying = true;
            mBtnVsttsPlay.setText("stop audio");
        }catch (Exception e){

        }
    }

    private void stopAudio(){
        if(isVsttsPlaying) {
            isVsttsPlaying = false;
            mBtnVsttsPlay.setText("play audio");
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
