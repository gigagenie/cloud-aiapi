package com.kt.ai.aisdk.client;

import kt.gigagenie.ai.api.STT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class SttAsyncClient {
    public static void main(String[] args) {
        STT sttClient = new STT();
        String clientID = "client-id";
        String clientKey = "client-key";
        String clientSecret = "client-secret";

        sttClient.setAuth(clientKey, clientID, clientSecret);

        int sttMode = 2;
        int channel = 1;
        int sampleRate = 160000;
        String sampleFmt = "S16LE";
        String encoding = "mp3";
        String language = "ko";

        try {
            byte[] audioData = null;
            FileInputStream inputStream = new FileInputStream("/home/dev/Workspace/GitLab/KtAiApiSDK/Java/ktAiApiClient/src/main/resources/long.mp3");
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

            String result = resultJson.optString("result");
            JSONArray resultArray = new JSONArray(result);
            JSONObject jsonObject = resultArray.getJSONObject(0);

            String transactionId = jsonObject.getString("transactionId");
            System.out.println("transactionId:" + transactionId);

            Thread.sleep(10000);

            JSONObject resultQueryJson = sttClient.querySTT(transactionId);
            System.out.println("resultJson:" + resultQueryJson);

        } catch (Exception e) {
            System.out.println("e >> " + e.getMessage());
        }
    }
}
