import stt from "./ktAiApiSDK/STT.js";
var mStt = new stt();
import dotenv from "dotenv";
dotenv.config({ path: 'ENV.config' });
import fs from "fs";

var mTransactionId;
var REQUEST_STT_TEXT_GET_TIME = 2000; // 2sec

function onStart() {
    if(mStt == null){
        mStt = new stt();
    }

    var strUrl = "https://" + process.env.HOSTNAME + ":" + process.env.AI_API_HTTP_PORT;
    mStt.setServiceURL(strUrl);
    mStt.setAuth(process.env.CLIENT_KEY, process.env.CLIENT_ID, process.env.CLIENT_SECRET);
}

function requestSTTSync(testFile, sttMode) {
    onStart();

    var fileData = fs.readFileSync(testFile);
    var audioData = Buffer.from(fileData, 'binary')

    var targetLanguage = "ko";
    var encoding = "mp3";
    var channel = 1;
    var sampleRate = 16000;
    var sampleFmt = "S16LE";

    console.log(">> 호출 결과 대기중...");
    var resultJson = mStt.requestSTT(audioData, encoding, sttMode, targetLanguage, channel, sampleRate, sampleFmt);
    console.log("\n------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------");    
    console.log(JSON.stringify(resultJson));
}

function requestSTTAsync(testFile, sttMode) {
    onStart();

    var fileData = fs.readFileSync(testFile);
    var audioData = Buffer.from(fileData, 'binary')

    var targetLanguage = "ko";
    var encoding = "mp3";
    var channel = 1;
    var sampleRate = 16000;
    var sampleFmt = "S16LE";

    var resultJson = mStt.requestSTT(audioData, encoding, sttMode, targetLanguage, channel, sampleRate, sampleFmt);
    console.log("-------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------");
    console.log(JSON.stringify(resultJson));

    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        var resultArray = resultJson["result"];
        mTransactionId = resultArray[0]["transactionId"];
     
        console.log("\n>> 결과 조회 실행");
        requestSttQuery(mTransactionId);
    }
}

function requestSttQuery(transactionId) {
    console.log(">> 결과 조회중... transactionId : " + transactionId);

    var resultJson = mStt.querySTT(transactionId);
    requestSttQueryPrint(resultJson);
}

function requestSttQueryPrint(resultJson) {
    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        var sttStatus = resultJson["sttStatus"];
        console.log("sttStatus => " + sttStatus.toLowerCase());
        switch(sttStatus.toLowerCase()) {
            case "processing" :
                setTimeout(requestSttQuery, REQUEST_STT_TEXT_GET_TIME, mTransactionId);
                break;

            case "completed" :
                console.log("\n-------------------------------");
                console.log("실행 결과");
                console.log("-------------------------------");
                console.log(JSON.stringify(resultJson));
                break;

            case "err" :
                break;
        }
    }
    else{
        console.log("result : " + JSON.stringify(resultJson));
    }
}

let testFile = "TestFile\\STT.mp3";
// stt(동기화)
requestSTTSync(testFile, 1);
// stt(비동기화)
// requestSTTAsync(testFile, 2);