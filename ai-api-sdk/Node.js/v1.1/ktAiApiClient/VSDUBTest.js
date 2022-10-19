import vsdub from "./ktAiApiSDK/VSDUB.js";
var mVsdub = new vsdub();
import dotenv from "dotenv";
dotenv.config({ path: 'ENV.config' });
import fs from "fs";

function onStart() {
    if(mVsdub == null){
        mVsdub = new vsdub();
    }

    var strUrl = "https://" + process.env.HOSTNAME + ":" + process.env.AI_API_HTTP_PORT;
    mVsdub.setServiceURL(strUrl);
    mVsdub.setAuth(process.env.CLIENT_KEY, process.env.CLIENT_ID, process.env.CLIENT_SECRET);
}

function requestVSDUB(testFile, text) {
    onStart();

    var fileData = fs.readFileSync(testFile);
    var audioData = Buffer.from(fileData, 'binary')

    var pitch = 100;
    var speed = 100;
    var speaker = 100;
    var volume = 100;
    var language = "ko";
    var encoding = "wav";
    var emotion = "neutral";
    var voiceName = "";
    var sampleRate = 16000;
    
    console.log(">> 호출 결과 대기중...");
    var resultJson = mVsdub.requestVSDUB(audioData, text, pitch, speed, speaker, volume, language, encoding, emotion, voiceName, sampleRate);
    console.log("\n------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------"); 

    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        try {
            var buff = Buffer.from(resultJson["audioData"], "base64");
            let vsdubfile = "TestFile\\VSDUB_Down.mp3";
            fs.writeFileSync(vsdubfile, buff);
            console.log('CREATE VSDUB AUDIO FILE \"TestFile\\VSDUB_Down.mp3\"');
        } catch(e) {
            console.log(e);
        }
    } else {
        console.log(JSON.stringify(resultJson));
    }
}

let testFile = "TestFile\\VSDUB.mp3";
requestVSDUB(testFile, "안녕하세요. 보이스 스튜디오 입니다.");