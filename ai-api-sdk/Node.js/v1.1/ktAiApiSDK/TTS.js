import HttpUtils from "./HttpUtils.js";
var httpUtils = new HttpUtils();

export default class tts {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "TTS_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    URL_TTS_VOICE_SYNTHESIS = "/v2/voiceSynthesis";

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

    requestTTS(text, pitch, speed, speaker, volume, language, encoding, channel, sampleRate, sampleFmt) {
        try {
            var metadataJsonObject = new Object();
            metadataJsonObject.text = text;
            metadataJsonObject.pitch = pitch;
            metadataJsonObject.speed = speed;
            metadataJsonObject.speaker = speaker;
            metadataJsonObject.volume = volume;
            metadataJsonObject.language = language;
            metadataJsonObject.encoding = encoding;

            if (encoding.toLowerCase() == "wav") {
                var encodingOptObject = new Object();
                encodingOptObject.channel = channel;
                encodingOptObject.sampleRate = sampleRate;
                encodingOptObject.sampleFmt = sampleFmt;

                metadataJsonObject.encodingOpt = encodingOptObject;
            }

            var strUrl = this.mServiceURL + this.URL_TTS_VOICE_SYNTHESIS;

            var jsonObject = new Object();
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;

            var reponseObj = httpUtils.requestPost(strUrl, jsonObject, metadataJsonObject);

            if(reponseObj[httpUtils.RESPONSE_STATUS_CODE] == 301) {
                var entrypointObj = JSON.parse(reponseObj["result"]);
                httpUtils.setHttpEntrypoint(entrypointObj["entrypoint"]);

                this.setServiceURL('https://' + entrypointObj["entrypoint"])
                var strUrl = this.mServiceURL + this.URL_TTS_VOICE_SYNTHESIS;
                return httpUtils.requestPost(strUrl, jsonObject, metadataJsonObject);
            }
            else {
                return reponseObj;
            }
        }
        catch(e) {
        }
    }
}