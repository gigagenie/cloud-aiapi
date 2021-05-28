package com.kt.ai.aisdk.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =  "MainActivity";

    public static String TEST_TYPE       = "TEST_TYPE";

    private String[] REQUIRED_PERMISSIONS  = {  Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.RECORD_AUDIO };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dataDirectory = new File(rootPath+"/aisdk/");
        if(!dataDirectory.exists()) {
            if(dataDirectory.mkdirs()) {
                Log.i(TAG, "dataDirectory make success");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart >> ");
        checkPermit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.STTOnHTTP01:
                {
                    Intent intent = new Intent(this, SttRestActivity.class);
                    intent.putExtra(TEST_TYPE, 0);
                    startActivity(intent);
                }
                break;
            case R.id.STTOnHTTP02:
                {
                    Intent intent = new Intent(this, SttRestActivity.class);
                    intent.putExtra(TEST_TYPE, 1);
                    startActivity(intent);
                }
                break;
            case R.id.STTgRPC01:
                startActivity( new Intent(this, SttgRpcActivity.class) );
                break;
            case R.id.TTSOnHTTP01:
                startActivity( new Intent(this, TtsRestActivity.class) );
                break;
        }
    }

    private void checkPermit() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permit = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int permitWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permitRecord = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            if (permit != PackageManager.PERMISSION_GRANTED  || permitWrite != PackageManager.PERMISSION_GRANTED || permitRecord != PackageManager.PERMISSION_GRANTED )  {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.require_permission));
                    builder.setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,0);
                                }
                            });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,0);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult >> ");
        boolean permit = true;
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                permit = false;
            }
        }
        if (!permit) {
            Log.d(TAG, "no permit >> ");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.denied_permission_finish));
                builder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.denied_permission_finish));
                builder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                builder.show();
            }
        }
    }
}
