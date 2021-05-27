const STTgRPC = require('./kt-ai-api-sdk/STTgRPC');
const path = require('path');
const fs = require('fs');

var notifier = require('./kt-ai-api-sdk//noti.js');

notifier.on('onError', (message) => {
    console.log('Callback Event onError message ==> ' + JSON.stringify(message));
    if(message.code == 301) {
        mgRpc.setServiceURL(message.strMsg);
        mgRpc.reconnectionGRPC();
    }
});

notifier.on('onRelease', (message) => {
    console.log('Callback Event onRelease message ==> ' + message)
});

notifier.on('onReadySTT', (sampleRate, ch, format) => {
    console.log('Callback Event onReadySTT message ==> ' + sampleRate + ', ' + ch + ', ' + format);
    gRpcClient.sendAudioData(audioData);
});

notifier.on('onStopSTT', (message) => {
    console.log('Callback Event onStop message ==> ' + message);
});

notifier.on('onSTTResult', (message) => {
    console.log('Callback Event onSTTResult message ==> ' + JSON.stringify(message));
});

const gRpcClient = new STTgRPC();

const clientID = "client-id";
const clientKey = "client-key";
const clientSecret = "client-secret";

gRpcClient.setMetaData(clientKey, clientID, clientSecret);

gRpcClient.connectGRPC();
gRpcClient.startSTT("long", "S16LE", 16000, 1);

pcmPath = path.join(__dirname, "/sample/record.pcm");
var fileData = fs.readFileSync(pcmPath);
var audioData = Buffer.from(fileData, 'binary');

setTimeout(() => {
    gRpcClient.releaseConnection();
    process.exit();
}, 3000);