import vstts from "./ktAiApiSDK/VSTTS.js";
var mVstts = new vstts();
import dotenv from "dotenv";
dotenv.config({ path: 'ENV.config' });
import fs from "fs";

function onStart() {
    if(mVstts == null){
        mVstts = new vstts();
    }

    var strUrl = "https://" + process.env.HOSTNAME + ":" + process.env.AI_API_HTTP_PORT;
    mVstts.setServiceURL(strUrl);
    mVstts.setAuth(process.env.CLIENT_KEY, process.env.CLIENT_ID, process.env.CLIENT_SECRET);
}

function requestVSTTS(text) {
    onStart();

    var pitch = 100;
    var speed = 100;
    var speaker = 100;
    var volume = 100;
    var language = "ko";
    var encoding = "mp3";
    var emotion = "fear";
    var voiceName = "";
    var channel = 1;
    var sampleRate = 16000;
    var sampleFmt = "S16LE";

    console.log(">> 호출 결과 대기중...");
    var resultJson = mVstts.requestVSTTS(text, pitch, speed, speaker, volume, language, encoding, emotion, voiceName, channel, sampleRate, sampleFmt);
    console.log("\n------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------"); 

    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        try {
            var buff = Buffer.from(resultJson["audioData"], "base64");
            let vsttsfile = "TestFile\\VSTTS_Down.mp3";
            fs.writeFileSync(vsttsfile, buff);
            console.log('CREATE VSTTS AUDIO FILE \"TestFile\\VSTTS_Down.mp3\"');
        } catch(e) {
            console.log(e);
        }
    } else {
        console.log(JSON.stringify(resultJson));
    }
}

requestVSTTS("안녕하세요. 보이스 스튜디오입니다.");