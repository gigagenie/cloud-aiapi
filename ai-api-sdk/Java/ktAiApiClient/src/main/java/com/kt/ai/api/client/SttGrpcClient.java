package com.kt.ai.api.client;

import kt.gigagenie.ai.api.STTgRPC;
import kt.gigagenie.ai.api.STTgRPCCallback;
import java.io.FileInputStream;

public class SttGrpcClient {
    public static void main(String[] args) {
        STTgRPC sttGrpcClient = new STTgRPC();
        String clientID = "client-id";
        String clientKey = "client-key";
        String clientSecret = "client-secret";

        STTgRPCCallback onSTTgRPCCallbackListener = new STTgRPCCallback() {
            @Override
            public void onConnectGRPC() {
                System.out.println("onConnectGRPC");

                String sttMode = "long";
                int channel = 1;
                int sampleRate = 16000;
                String sampleFmt = "S16LE";

                sttGrpcClient.startSTT(sttMode, sampleFmt, sampleRate, channel);
            }

            @Override
            public void onSTTResult(String text, String type, float startTime, float endTime) {
                System.out.println("onSTTResult text: " + text);
                System.out.println("onSTTResult type: " + type);
                System.out.println("onSTTResult startTime: " + startTime);
                System.out.println("onSTTResult endTime: " + endTime);
            }

            @Override
            public void onReadySTT(int sampleRate, int channel, String format) {
                System.out.println("onReadySTT >>> sampleRate:" + sampleRate + ", channel:" + channel + ", format:" + format);

                try {
                    boolean isSending = true;
                    FileInputStream inputStream = new FileInputStream("/home/dev/Workspace/GitLab/KtAiApiSDK/Java/ktAiApiClient/src/main/resources/record.pcm");
                    byte[] buffer = new byte[sampleRate];
                    int bytesRead = 0;

                    while (isSending) {
                        bytesRead = inputStream.read(buffer);

                        if (bytesRead == -1) break;
                        Thread.sleep(500);
                        sttGrpcClient.sendAudioData(buffer);
                    }

                    inputStream.close();

                } catch (Exception e) {

                }
            }

            @Override
            public void onStopSTT() {
                System.out.println("onStopSTT");
            }

            @Override
            public void onRelease() {
                System.out.println("onRelease");
            }

            @Override
            public void onError(int errCode, String errMsg) {
                System.out.println("onError >>> errCode:" + errCode + ", errMsg:" + errMsg);
            }
        };
        sttGrpcClient.setSTTgRPCCallback(onSTTgRPCCallbackListener);
        sttGrpcClient.setMetaData(clientKey, clientID, clientSecret);

        sttGrpcClient.connectGRPC();
        try {
            Thread.sleep(15000);
            sttGrpcClient.releaseConnection();
        } catch (Exception e) {

        }
    }
}
