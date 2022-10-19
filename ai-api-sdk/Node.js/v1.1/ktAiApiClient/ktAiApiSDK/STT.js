import HttpUtils from "./HttpUtils.js";
var httpUtils = new HttpUtils();

export default class stt {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "STT_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    URL_STT_VOICE_RECOGNIZER = "/v2/voiceRecognize";
    URL_STT_VOICE_RECOGNIZER2 = "/v2/voiceRecognize2";

    mServiceURL = "";
    mClientKey = "";
    mTimeStamp = "";
    mSignature = "";

    constructor() {
        httpUtils.checkEntryPointProfile();
    }

    setAuth(clientKey, clientId, clientSecret) {
        if (!clientKey || !clientId || !clientSecret) {
              return;
        }

        this.mClientKey = clientKey;
        this.mTimeStamp = httpUtils.getTimestamp();
        this.mSignature = httpUtils.makeSignature(this.mTimeStamp, clientId, clientSecret);
    }

    setServiceURL(entrypoint) {
        this.mServiceURL = entrypoint;
    }

    requestSTT(audioData, encoding, sttMode, targetLanguage, channel, sampleRate, sampleFmt) {
        var metadataJsonObject = new Object();
        metadataJsonObject.encoding = encoding;
        metadataJsonObject.targetLanguage = targetLanguage;
        metadataJsonObject.sttMode = sttMode;

        if (encoding.toLowerCase() == "raw") {
            var encodingOptObject = new Object();
            encodingOptObject.channel = channel;
            encodingOptObject.sampleRate = sampleRate;
            encodingOptObject.sampleFmt = sampleFmt;
            metadataJsonObject.encodingOpt = encodingOptObject;
        }

        var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER;

        var jsonObject = new Object();
        jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
        jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
        jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;

        var reponseObj = httpUtils.requestMutipart2(strUrl, jsonObject, metadataJsonObject, audioData);

        if(reponseObj[httpUtils.RESPONSE_STATUS_CODE] == 301) {
            var entrypointObj = JSON.parse(reponseObj["result"]);
            httpUtils.setHttpEntrypoint(entrypointObj["entrypoint"]);

            this.setServiceURL('https://' + entrypointObj["entrypoint"])
            var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER;
            return httpUtils.requestMutipart2(strUrl, jsonObject, metadataJsonObject, audioData);
        }
        else {
            return reponseObj;
        }
    }

    requestSTT2(audioData, encoding, sttModelCode) {
        var metadataJsonObject = new Object();
        metadataJsonObject.encoding = encoding;
        metadataJsonObject.sttModelCode = sttModelCode;

        var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER2;

        var jsonObject = new Object();
        jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
        jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
        jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;

        var reponseObj = httpUtils.requestMutipart2(strUrl, jsonObject, metadataJsonObject, audioData);

        if(reponseObj[httpUtils.RESPONSE_STATUS_CODE] == 301) {
            var entrypointObj = JSON.parse(reponseObj["result"]);
            httpUtils.setHttpEntrypoint(entrypointObj["entrypoint"]);

            this.setServiceURL('https://' + entrypointObj["entrypoint"])
            var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER2;
            return httpUtils.requestMutipart2(strUrl, jsonObject, metadataJsonObject, audioData);
        }
        else {
            return reponseObj;
        }
    }

    querySTT(transactionId) {
        try {
            var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER + "/" + transactionId;

            var jsonObject = new Object();
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;

            return httpUtils.requestGet(strUrl, jsonObject);
        }
        catch(e) {
        }
    }
}