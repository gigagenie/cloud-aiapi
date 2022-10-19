import HttpUtils from "./HttpUtils.js";
var httpUtils = new HttpUtils();

export default class vsdub {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "VSDUB_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    URL_VSDUB_VOICE_SYNTHESIS = "/v2/voicestudio/voiceDubbing";

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

    requestVSDUB(audioData, text, pitch, speed, speaker, volume, language, encoding, emotion, voiceName, sampleRate) {
        try {
            var metadataJsonObject = new Object();
            metadataJsonObject.text = text;
            metadataJsonObject.pitch = pitch;
            metadataJsonObject.speed = speed;
            metadataJsonObject.speaker = speaker;
            metadataJsonObject.volume = volume;
            metadataJsonObject.language = language;
            metadataJsonObject.emotion = emotion;
            metadataJsonObject.voiceName = voiceName;
            metadataJsonObject.encoding = encoding;
            metadataJsonObject.sampleRate = sampleRate;

            var strUrl = this.mServiceURL + this.URL_VSDUB_VOICE_SYNTHESIS;

            var jsonObject = new Object();
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;

            var reponseObj = httpUtils.requestMutipart2(strUrl, jsonObject, metadataJsonObject, audioData);

            if(reponseObj[httpUtils.RESPONSE_STATUS_CODE] == 301) {
                var entrypointObj = JSON.parse(reponseObj["result"]);
                httpUtils.setHttpEntrypoint(entrypointObj["entrypoint"]);

                this.setServiceURL('https://' + entrypointObj["entrypoint"])
                var strUrl = this.mServiceURL + this.URL_VSDUB_VOICE_SYNTHESIS;
                return httpUtils.requestMutipart2(strUrl, jsonObject, metadataJsonObject, audioData);
            }
            else {
                return reponseObj;
            }
        }
        catch(e) {
        }
    }
}