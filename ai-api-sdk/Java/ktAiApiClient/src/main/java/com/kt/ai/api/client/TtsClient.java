package com.kt.ai.api.client;

import kt.gigagenie.ai.api.TTS;
import org.json.JSONObject;

import java.io.*;

public class TtsClient {
    public static void main(String[] args) {
        TTS ttsClient = new TTS();

        String clientID = "client-id";
        String clientKey = "client-key";
        String clientSecret = "client-secret";

        ttsClient.setAuth(clientKey, clientID, clientSecret);

        String text = "안녕하세요 반갑습니다.";
        int pitch = 100;
        int speed = 100;
        int speaker = 1;
        int volume = 100;
        String language = "ko";
        String encoding = "mp3";
        int channel = 1;
        int sampleRate = 16000;
        String sampleFmt = "S16LE";

        try {
            JSONObject resultJson = ttsClient.requestTTS(text, pitch, speed, speaker, volume,
                    language, encoding, channel, sampleRate, sampleFmt);
            int statusCode = resultJson.optInt("statusCode");

            if(statusCode == 200) {

                byte[] audioData = (byte[]) resultJson.opt("audioData");

                String targetFilePath = "./src/main/resources/tts.mp3";
                System.out.println("targetFile:" + targetFilePath);

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(targetFilePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    fos.write(audioData, 0, audioData.length);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("e >> " + e.getMessage());
        }
    }
}
