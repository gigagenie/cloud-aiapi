import geniememo from "./ktAiApiSDK/GENIEMEMO.js";
var mGeniememo = new geniememo();
import dotenv from "dotenv";
dotenv.config({ path: 'ENV.config' });
import fs from "fs";

var REQUEST_GENIEMEMO_TEXT_GET_TIME = 2000; // 2sec

function onStart() {
    if(mGeniememo == null){
        mGeniememo = new geniememo();
    }

    var strUrl = "https://" + process.env.HOSTNAME + ":" + process.env.AI_API_HTTP_PORT;
    mGeniememo.setServiceURL(strUrl);
    mGeniememo.setAuth(process.env.CLIENT_KEY, process.env.CLIENT_ID, process.env.CLIENT_SECRET);
}

function requestGENIEMEMOSync(testFile, callKey) {
    onStart();

    var fileData = fs.readFileSync(testFile);
    var audioData = Buffer.from(fileData, 'binary')

    var lastYn = "Y";
    var callIndex = 0;

    console.log(">> 호출 결과 대기중...");
    var resultJson = mGeniememo.requestGENIEMEMO(audioData, callKey, lastYn, callIndex);
    console.log("\n------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------");    
    console.log(JSON.stringify(resultJson));
}

function requestGENIEMEMOAsync(testFile, callKey) {
    onStart();

    var fileData = fs.readFileSync(testFile);
    var audioData = Buffer.from(fileData, 'binary')

    var resultJson = mGeniememo.requestGENIEMEMOASYNC(audioData, callKey);
    console.log("-------------------------------");
    console.log("실행 결과");
    console.log("-------------------------------");
    console.log(JSON.stringify(resultJson));

    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        console.log("\n>> 결과 조회 실행");
        requestGeniememoQuery(callKey);
    }
}

function requestGeniememoQuery(callKey) {
    console.log(">> 결과 조회중... callKey : " + callKey);

    var resultJson = mGeniememo.queryGENIEMEMO(callKey);
    requestGeniememoQueryPrint(resultJson);
}

function requestGeniememoQueryPrint(resultJson) {
    var statusCode = resultJson["statusCode"];
    if(statusCode == 200) {
        var status = resultJson["status"];
        console.log("status => " + status);
        switch(status) {
            case 0 :
                console.log("\n-------------------------------");
                console.log("실행 결과");
                console.log("-------------------------------");
                console.log(JSON.stringify(resultJson));
                break;

            default :
                setTimeout(requestGeniememoQuery, REQUEST_GENIEMEMO_TEXT_GET_TIME, callKey);
                break;
        }
    }
    else{
        console.log("result : " + JSON.stringify(resultJson));
    }
}

let testFile = "TestFile\\GENIEMEMO.wav";
var callKey = "GENIEMEMOTEST-0006";

// geniememo(동기화)
requestGENIEMEMOSync(testFile, callKey);
// geniememo(비동기화)
// requestGENIEMEMOAsync(testFile, callKey);