package com.kt.ai.aisdk.client;

import kt.gigagenie.ai.api.STT;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

public class SttSyncClient {
    public static void main(String[] args) {
        STT sttClient = new STT();
        String clientID = "client-id";
        String clientKey = "client-key";
        String clientSecret = "client-secret";

        sttClient.setAuth(clientKey, clientID, clientSecret);

        int sttMode = 1;
        int channel = 1;
        int sampleRate = 160000;
        String sampleFmt = "S16LE";
        String encoding = "mp3";
        String language = "ko";

        try {
            byte[] audioData = null;
            FileInputStream inputStream = new FileInputStream("/home/dev/Workspace/GitLab/KtAiApiSDK/Java/ktAiApiClient/src/main/resources/short.mp3");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            audioData = bos.toByteArray();
            inputStream.close();
            bos.close();

            JSONObject resultJson = sttClient.requestSTT(audioData, sttMode, language, encoding, channel, sampleRate, sampleFmt);
            System.out.println("resultJson:" + resultJson);

        } catch (Exception e) {
            System.out.println("e >> " + e.getMessage());
        }
    }
}
