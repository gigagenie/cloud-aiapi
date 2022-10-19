import HttpUtils from "./HttpUtils.js";
var httpUtils = new HttpUtils();

export default class geniememo {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "GENIEMEMO_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    URL_GENIEMEMO = "/v2/genieMemo";
    URL_GENIEMEMOASYNC = "/v2/genieMemoAsync";
    URL_GENIEMEMOQUERY = "/v2/genieMemoQuery";

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

    requestGENIEMEMO(audioData, callKey, lastYn, callIndex) {
        try {
            var strUrl1 = null;
            var metadataJsonObject = new Object();
            metadataJsonObject.callKey = callKey;
            metadataJsonObject.callIndex = callIndex
            metadataJsonObject.lastYn = lastYn
            strUrl1 = this.mServiceURL + this.URL_GENIEMEMO;

            var jsonObject = new Object();
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;
            var reponseObj = httpUtils.requestMutipart2(strUrl1, jsonObject, metadataJsonObject, audioData);

            if(reponseObj[httpUtils.RESPONSE_STATUS_CODE] == 301) {
                var entrypointObj = JSON.parse(reponseObj["result"]);
                httpUtils.setHttpEntrypoint(entrypointObj["entrypoint"]);

                this.setServiceURL('https://' + entrypointObj["entrypoint"])
                var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER;
                return httpUtils.requestMutipart2(strUrl1, jsonObject, metadataJsonObject, audioData);
            }
            else {
                return reponseObj;
            }
        }
        catch(e) {
            console.log("error : " + e);
        }

    }

    requestGENIEMEMOASYNC(audioData, callKey) {
        try {
            var strUrl1 = null;
            var metadataJsonObject = new Object();
            metadataJsonObject.callKey = callKey;
            strUrl1 = this.mServiceURL + this.URL_GENIEMEMOASYNC

            var jsonObject = new Object();
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;
            var reponseObj = httpUtils.requestMutipart2(strUrl1, jsonObject, metadataJsonObject, audioData);

            if(reponseObj[httpUtils.RESPONSE_STATUS_CODE] == 301) {
                var entrypointObj = JSON.parse(reponseObj["result"]);
                httpUtils.setHttpEntrypoint(entrypointObj["entrypoint"]);

                this.setServiceURL('https://' + entrypointObj["entrypoint"])
                var strUrl = this.mServiceURL + this.URL_STT_VOICE_RECOGNIZER;
                return httpUtils.requestMutipart2(strUrl1, jsonObject, metadataJsonObject, audioData);
            }
            else {
                return reponseObj;
            }
        }
        catch(e) {
            console.log("error : " + e);
        }

    }    

    queryGENIEMEMO(callKey, callIndex) {
        try {
            var strUrl = this.mServiceURL + this.URL_GENIEMEMOQUERY;
            var metadataJsonObject = new Object();
            metadataJsonObject.callKey = callKey;
            metadataJsonObject.callIndex = callIndex;

            var jsonObject = new Object();
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_KEY] = this.mClientKey;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = this.mTimeStamp;
            jsonObject[httpUtils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = this.mSignature;

            return httpUtils.requestPost(strUrl, jsonObject,metadataJsonObject);
        }
        catch(e) {
            console.log("error : " + e);
        }
    }
}