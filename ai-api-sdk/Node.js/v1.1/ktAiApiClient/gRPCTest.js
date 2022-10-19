import gRpc from "./ktAiApiSDK/gRPC.js";
var mgRpc = new gRpc();
import dotenv from "dotenv";
dotenv.config({ path: 'ENV.config' });
import fs from "fs";
import notifier from "./ktAiApiSDK/noti.js";

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
});

notifier.on('onSTTResult', (message) => {
    console.log('Callback Event onSTTResult message ==> ' + JSON.stringify(message));
});

mgRpc.setServiceURL(process.env.HOSTNAME + ":" + process.env.AI_API_GRPC_PORT);
mgRpc.setMetaData(process.env.CLIENT_KEY, process.env.CLIENT_ID, process.env.CLIENT_SECRET);
mgRpc.connectGRPC();

const stdin = process.stdin;
stdin.setRawMode(true);
stdin.resume();
stdin.setEncoding('utf8');

console.log('===================KT AI GRPC TEST================');
console.log('1 : startSTT');
console.log('2 : sendAudioData [example file file/record.pcm]');
console.log('3 : stopSTT');
console.log('4 : releaseConnection');
console.log('0 : reconnectGRPC');
console.log('Ctrl + C : process EXIT');
console.log('PRESS KEY >> ');

function sleep(ms) {
	const wakeUpTime = Date.now() + ms;
	while (Date.now() < wakeUpTime) {}
}

stdin.on('data', (key) => {
	if (key === '\u0003') {
		process.exit();
	} else if (key === '0') {
		console.log('connectGRPC Call >>>>>>>>>>>>>>>>>>>>>>');
		mgRpc.connectGRPC();
	} else if (key === '1') {
		console.log('startSTT Call >>>>>>>>>>>>>>>>>>>>>>>>>');
		mgRpc.startSTT("long", "S16LE", 16000, 1); //sttMode, sampleFmt, sampleRate, channel
		// mgRpc.startSTT2(1); // sttModelcode
		// mgRpc.startSTT3(2, "long", "S16LE", 8000, 1);
	} else if (key === '2') {
		let grpcfile = "TestFile\\GRPC.pcm";
		const readStream =  fs.createReadStream(grpcfile, { highWaterMark: 16000});
		const data = [];

		// data 이벤트: 파일 읽기가 시작되면 실행
		readStream.on('data', (chunk) => {
			data.push(chunk);
			var audioData = Buffer.from(chunk, 'binary');
			mgRpc.sendAudioData(audioData);
			sleep(100);
		})
		readStream.on('end', () => {
			console.log('end: end');
		});
		
		readStream.on('error', (err) => {
			console.log('error:', err);
		})
	} else if (key === '3') {
		console.log('stopSTT Call >>>>>>>>>>>>>>>>>>>>>>>>>>>');
		mgRpc.stopSTT();
	} else if(key === '4') {
        console.log('releaseConnection Call >>>>>>>>>>>>>>>>>>');
        mgRpc.releaseConnection();
    }
});
