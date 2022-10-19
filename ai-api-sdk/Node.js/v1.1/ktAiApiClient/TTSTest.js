import tts from "./ktAiApiSDK/TTS.js";
var mTts = new tts();
import dotenv from "dotenv";
dotenv.config({ path: 'ENV.config' });
import fs from "fs";

function onStart() {
    if(mTts == null){
        mTts = new tts();
    }

    var strUrl = "https://" + process.env.HOSTNAME + ":" + process.env.AI_API_HTTP_PORT;
    mTts.setServiceURL(strUrl);
    mTts.setAuth(process.env.CLIENT_KEY, process.env.CLIENT_ID, process.env.CLIENT_SECRET);
}

function requestTTS(text) {
    onStart();

    var pitch = -1;
    var speed = -1;
    var speaker = 1;
    var volume = -1;
    var language = "ko";
    var encoding = "wav";
    var channel = 1;
    var sampleRate = 16000;
    var sampleFmt = "S16LE";

    console.log(">> 호출 결과 대기중...");
    var resultJson = mTts.requestTTS(text, pitch, speed, speaker, volume, language, encoding, channel, sampleRate, sampleFmt);
    console.log("\n------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------"); 

    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        try {
            var buff = Buffer.from(resultJson["audioData"], "base64");
            let ttsfile = "TestFile\\TTS_Down.mp3";
            fs.writeFileSync(ttsfile, buff);
            console.log('CREATE TTS AUDIO FILE \"TestFile\\TTS_Down.mp3\"');
        } catch(e) {
            console.log(e);
        }
    } else {
        console.log(JSON.stringify(resultJson));
    }
}

requestTTS("안녕하세요.");