const STT = require('./kt-ai-api-sdk/STT');
const fs = require('fs');
const path = require("path");

var sttClient = new STT();

const clientID = "client-id";
const clientKey = "client-key";
const clientSecret = "client-secret";

sttClient.setAuth(clientKey, clientID, clientSecret);

var sttMode = 1;
var targetLanguage = "ko";
var encoding = "mp3";
var channel = 1;
var sampleRate = 16000;
var sampleFmt = "S16LE";

let audioFile = path.join(__dirname, "/sample/short.mp3");

var fileData = fs.readFileSync(audioFile);
var audioData = Buffer.from(fileData, 'binary');

var resultJson = sttClient.requestSTT(audioData, sttMode, targetLanguage, encoding, channel, sampleRate, sampleFmt);
console.log(resultJson);