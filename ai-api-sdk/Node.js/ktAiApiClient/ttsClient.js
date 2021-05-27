const TTS = require('./kt-ai-api-sdk/TTS');
const fs = require('fs');
const path = require("path");

var ttsClient = new TTS();

const clientID = "client-id";
const clientKey = "client-key";
const clientSecret = "client-secret";

ttsClient.setAuth(clientKey, clientID, clientSecret);

const pitch = -1;
const speed = -1;
const speaker = 2;
const volume = -1;
const language = "ko";
const encoding = "mp3";
const channel = 1;
const sampleRate = 16000;
const sampleFmt = "S16LE";
const text = "안녕하세요 반갑습니다."

var resultJson = ttsClient.requestTTS(text, pitch, speed, speaker, volume, language, encoding, channel, sampleRate, sampleFmt);
const statusCode = resultJson["statusCode"];
if (statusCode == 200) {
    const filePath = "tts.mp3"
    try {
        var buff = Buffer.from(resultJson["audioData"], "base64");
        let ttsfile = path.join(__dirname, filePath);
        fs.writeFileSync(ttsfile, buff);
    } catch(err) {
        console.log(err);
    }
} else {
    console.log(resultJson);
}