var HttpUtils = require("./HttpUtils");
var httpUtils = new HttpUtils();
const dotenv = require('dotenv');
dotenv.config({ path:  __dirname + '/config/.env.server' });
dotenv.config({ path:  __dirname + '/config/.env.config' });

class stt {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "KT_AI_API_SDK_STT_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    URL_STT_VOICE_RECOGNIZER = "/v2/voiceRecognize";

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

    requestSTT(audioData, sttMode, targetLanguage, encoding, channel, sampleRate, sampleFmt) {
        var resultJson = new Object();

        try {
            if (!this.mServiceURL || !this.mClientKey || !this.mTimeStamp || !this.mSignature) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_401;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_401;
                return resultJson;
            }

            if (!audioData || audioData.length == 0) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            if (sttMode != 1 && sttMode != 2) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            if (!targetLanguage) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            if (!encoding) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

            var metadataJsonObject = new Object();
            metadataJsonObject.encoding = encoding;
            metadataJsonObject.targetLanguage = targetLanguage;
            metadataJsonObject.sttMode = sttMode;

            if (encoding.toLowerCase() == "raw") {
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
        catch(e) {
        }

    }

    querySTT(transactionId) {
        var resultJson = new Object();
        try {
            if (!this.mServiceURL || !this.mClientKey || !this.mTimeStamp || !this.mSignature) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_401;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_401;
                return resultJson;
            }

            if (!transactionId) {
                resultJson[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_400;
                resultJson[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_400;
                return resultJson;
            }

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

module.exports = stt;
