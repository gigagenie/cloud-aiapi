const fs = require('fs');
var Stream = require('stream').Transform;

var HttpUtils = require("./HttpUtils");
var httpUtils = new HttpUtils();

const dotenv = require('dotenv');
dotenv.config({ path:  __dirname + '/config/.env.server' });
dotenv.config({ path:  __dirname + '/config/.env.config' });


class tts {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "KT_AI_API_SDK_TTS_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    URL_TTS_VOICE_SYNTHESIS = "/v2/voiceSynthesis";

    mServiceURL = "";
    mClientKey = "";
    mTimeStamp = "";
    mSignature = "";

    constructor() {
        httpUtils.checkEntryPointProfile();
        this.mServiceURL = "https://" + process.env.HOSTNAME + ":" + process.env.AI_API_HTTP_PORT;
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
        var resultJson = new Object();

        try {
            if (!this.mServiceURL || !this.mClientKey || !this.mTimeStamp || !this.mSignature) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_401;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_401;
                return resultJson;
            }

            if (!text) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            if (!language) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            if (!encoding) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            if(pitch < 50 || pitch > 150) {
                pitch = -1;
            }

            if(speed < 50 || speed > 150) {
                speed = -1;
            }

            if(volume < 0 || volume > 200) {
                volume = -1;
            }

            var metadataJsonObject = new Object();
            metadataJsonObject.text = text;
            metadataJsonObject.pitch = pitch;
            metadataJsonObject.speed = speed;
            metadataJsonObject.speaker = speaker;
            metadataJsonObject.volume = volume;
            metadataJsonObject.language = language;
            metadataJsonObject.encoding = encoding;

            if (encoding.toLowerCase() == "wav") {
                if (!sampleFmt) {
                    resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                    resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                    return resultJson;
                }

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

module.exports = tts;
