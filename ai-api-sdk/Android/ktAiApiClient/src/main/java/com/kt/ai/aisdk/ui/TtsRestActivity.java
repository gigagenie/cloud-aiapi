package com.kt.ai.aisdk.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import kt.gigagenie.ai.api.TTS;

import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TtsRestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG =  "TtsRestActivity";

    private static final int MSG_SHOW_PROGRESS              = 0;
    private static final int MSG_HIDE_PROGRESS              = 1;
    private static final int MSG_SHOW_TOAST_TEXT            = 2;
    private static final int MSG_PLAY_TEXT                  = 3;

    private MediaPlayer mediaPlayer = null;
    private boolean isTtsPlaying = false;
    private Button mBtnTtsPlay = null;
    private EditText mEditText = null;

    private Spinner mSpeakSpinner = null;

    private SeekBar mPitchSeekbar = null;
    private SeekBar mSpeedSeekbar = null;
    private SeekBar mVolumeSeekbar = null;

    private TextView mPitchLevel = null;
    private TextView mSpeedLevel = null;
    private TextView mVolumeLevel = null;

    private TTS mTts = null;

    private ProgressDialog mProgress = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_tts_rest);

        mBtnTtsPlay = (Button)findViewById(R.id.ttsplay);
        mEditText = (EditText)findViewById(R.id.charEditText);

        mSpeakSpinner = (Spinner)findViewById(R.id.speaker_spinner);

        mPitchSeekbar = (SeekBar)findViewById(R.id.pitch_seek);
        mSpeedSeekbar = (SeekBar)findViewById(R.id.speed_seek);
        mVolumeSeekbar = (SeekBar)findViewById(R.id.volume_seek);

        mPitchLevel = (TextView)findViewById(R.id.pitch_level);
        mSpeedLevel = (TextView)findViewById(R.id.speed_level);
        mVolumeLevel = (TextView)findViewById(R.id.volume_level);

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

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mTts == null){
            mTts = new TTS();

            String strUrl = "https://" + ENV.hostname + ":" + ENV.ai_api_http_port;

            mTts.setServiceURL(strUrl);
            mTts.setAuth(ENV.client_key, ENV.client_id, ENV.client_secret);
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

        if(mTts != null){
            mTts = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ttsrequest:
                final String text = mEditText.getText().toString();
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(this, "TTS로 변환할 문자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                Message msg = mUIHandler.obtainMessage(MSG_SHOW_PROGRESS, "처리중입니다.");
                mUIHandler.sendMessage(msg);

                int    speaker = mSpeakSpinner.getSelectedItemPosition() + 1;
                int    pitch = Integer.parseInt(mPitchLevel.getText().toString());
                int    speed = Integer.parseInt(mSpeedLevel.getText().toString());
                int    volume = Integer.parseInt(mVolumeLevel.getText().toString());
                String language = "ko";
                String encoding = "mp3";
                int    channel = 1;
                int    sampleRate = 16000;
                String sampleFmt = "S16LE";

                sendText(text, pitch, speed, speaker, volume,
                        language, encoding, channel, sampleRate, sampleFmt);

                break;
            case R.id.ttsplay:
                if(isTtsPlaying) {
                    stopAudio();
                    Toast.makeText(this, "stopPlaying...", Toast.LENGTH_SHORT).show();
                }else{
                    playAudio();
                    Toast.makeText(this, "startPlaying...", Toast.LENGTH_SHORT).show();
                }

                break;
        }
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
                    String text = (String)msg.obj;
                    Toast.makeText(TtsRestActivity.this, text, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PLAY_TEXT:
                    if(isTtsPlaying) {
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
                          final int pitch,
                          final int speed,
                          final int speaker,
                          final int volume,
                          final String language,
                          final String encoding,
                          final int channel,
                          final int sampleRate,
                          final String sampleFmt) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    JSONObject resultJson = mTts.requestTTS(text, pitch, speed, speaker, volume,
                            language, encoding, channel, sampleRate, sampleFmt);

                    int statusCode = resultJson.optInt("statusCode");

                    if(statusCode == 200){

                        byte[] audioData = (byte[])resultJson.opt("audioData");
                        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aisdk/tts.mp3";
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(mFilePath);
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, "write bytes is " + audioData.length);

                        try {
                            fos.write(audioData, 0, audioData.length);
                            fos.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                        Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                        mUIHandler.sendMessage(msg);

                        msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "TTS로 변환이 완료되었습니다.");
                        mUIHandler.sendMessage(msg);

                        msg = mUIHandler.obtainMessage(MSG_PLAY_TEXT);
                        mUIHandler.sendMessage(msg);

                    }else{
                        Log.d(TAG, "onError >> statusCode:" + statusCode);
                        String errorCode = resultJson.optString("errorCode");
                        Log.d(TAG, "onError >> errorCode:" + errorCode);

                        Message msg = mUIHandler.obtainMessage(MSG_HIDE_PROGRESS);
                        mUIHandler.sendMessage(msg);
                        msg = mUIHandler.obtainMessage(MSG_SHOW_TOAST_TEXT, "statusCode:" + statusCode  + ", errorCode:" + errorCode + ", TTS로 변환요청 중 에러가 발생하였습니다.");
                        mUIHandler.sendMessage(msg);
                    }

                }catch (Exception e){
                    Log.d(TAG, "e >> " + e.getMessage());
                }
            }
        }).start();
    }

    private void playAudio(){

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aisdk/tts.mp3";
        File file = new File(mFilePath);
        if (!file.exists()) {
            Log.d(TAG, "File not exist >> " + mFilePath);
            Toast.makeText(this, "TTS로 변환을 먼저 수행해주세요.", Toast.LENGTH_SHORT).show();
            isTtsPlaying = false;
            mBtnTtsPlay.setText("play audio");
            return;
        }

        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    isTtsPlaying = false;
                    mBtnTtsPlay.setText("play audio");
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isTtsPlaying = false;
                    mBtnTtsPlay.setText("play audio");
                }
            });

            FileInputStream fis = new FileInputStream(mFilePath);
            FileDescriptor fd = fis.getFD();
            mediaPlayer.setDataSource(fd);

            mediaPlayer.prepare();
            mediaPlayer.start();
            isTtsPlaying = true;
            mBtnTtsPlay.setText("stop audio");
        }catch (Exception e){
            Log.d(TAG, "e >> " + e.getMessage());
        }
    }

    private void stopAudio(){
        if(isTtsPlaying) {
            isTtsPlaying = false;
            mBtnTtsPlay.setText("play audio");
            if(mediaPlayer != null) {
                mediaPlayer.stop();
            }
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
