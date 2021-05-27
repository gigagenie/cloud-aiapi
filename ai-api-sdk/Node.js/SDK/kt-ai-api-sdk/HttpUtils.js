var dateFormat = require("dateformat");
var crypto = require("crypto");
const express = require("express");
const app = express();
const bodyParser = require('body-parser');
const FormData = require('form-data');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
var request = require('sync-request');


class HttpUtils {
    static TAG = "HttpUtils";
    static SUPPORT_PROTOCOL_LOG = true;

    RESPONSE_STATUS_CODE =  "statusCode";
    RESPONSE_ERROR_CODE =  "errorCode";

    static RESPONSE_RESULT =  "result";
    static RESPONSE_AUDIO_DATA =  "audioData";

    RESULT_STATUS_CODE_200 =  200;
    RESULT_STATUS_CODE_301 =  301;
    RESULT_STATUS_CODE_400 =  400;
    RESULT_STATUS_CODE_401 =  401;
    RESULT_STATUS_CODE_500 =  500;

    RESULT_ERROR_CODE_400 =  "파라미터 설정 오류";
    RESULT_ERROR_CODE_401 =  "권한 없음";
    RESULT_ERROR_CODE_500 =  "시스템 에러";

    static REQUEST_PARAMETER_AUTHORIZATION = "Authorization";
    REQUEST_PARAMETER_X_CLIENT_KEY = "x-client-key";
    REQUEST_PARAMETER_X_AUTH_TIMESTAMP = "x-auth-timestamp";
    REQUEST_PARAMETER_X_CLIENT_SIGNATURE = "x-client-signature";

    static REQUEST_PARAMETER_CONTENT_TYPE = "Content-type";
    static REQUEST_PARAMETER_CONTENT_LENGTH = "Content-length";

    static LINE_FEED = "\r\n";
    static MULTIPART_BOUNDARY = "9912ef1112228sf8899123f21e8e";
    static CHAR_SET = "utf-8";

    static DEFAULT_ENTRYPOINT = "dev.gigagenie.ai";
    static DEFAULT_HTTP_PORT = 40086;
    static DEFAULT_GRPC_PORT = 40085;

    static DEFAULT_HTTP_CONNECT_TIMEOUT = 30 * 1000;
    static DEFAULT_HTTP_SO_TIMEOUT = 60 * 1000;

    mSSLSocketFactory = null;
    mHostnameVerifier = null;

    static DEFAULT_ENTRYPOINT = "dev.gigagenie.ai";
    static DEFAULT_HTTP_PORT = 40086;
    static DEFAULT_GRPC_PORT = 40085;

    static mEntryProfilePath = path.join(__dirname, "config/.env.server");

    mCurrent = 0;

    static getBoundary(contentType) {
        var strBoundary = contentType.split("boundary=")[1].trim();
        return strBoundary;
    }

    static getMultipartStream(content, boundary) {
        var strBody = content.split(HttpUtils.LINE_FEED);
        var strTemp = "";
        var strList = new Array();

        for (var i = 0; i < strBody.length; i++) {
            var text = strBody[i];
            if (text.trim() != "") {
                strTemp += text + HttpUtils.LINE_FEED;
            }
        }

        var strBody2 = strTemp.split("--" + boundary);

        for (var i = 0; i < strBody2.length; i++) {
            var text = strBody2[i];
            if (text.search("form-data") > -1) {
                strList.push(text);
            }
        }
        return strList;
    }

    static readHeader(multipart) {
        var strBody = multipart.split(HttpUtils.LINE_FEED);
        var result = "";
        var insertindex = 0;
        for (var i = 0; i < strBody.length; i++) {
            var text = strBody[i];
            if (text.search("Content-") > -1) {
                if(insertindex > 0) result += HttpUtils.LINE_FEED;
                result += text;
                insertindex++;
            }
        }

        return result;
    }

    static readBodyData(multipart) {
        var strBody = multipart.split(HttpUtils.LINE_FEED);
        var result = "";
        var insertindex = 0;
        for (var i = 0; i < strBody.length; i++) {
            var text = strBody[i];
            if (text.search("Content-") > -1 || !text) {
                continue;
            }
            if(insertindex > 0) result += HttpUtils.LINE_FEED;
            result += text;
            insertindex++;
        }

        return result;
    }

    getTimestamp() {
        var now = new Date();
        return dateFormat(now, "yyyymmddHHMMssl");
    }

    makeSignature(mTimeStamp, clientId, clientSecret) {
        var signature = crypto.createHmac("sha256", clientSecret).update(clientId + ":" + mTimeStamp).digest("hex");
        return signature;
    }

    requestGet(strUrl, headerJson) {
        var httpUtils = this;
        var options = {
            headers: headerJson
        };

        var response = request("GET", strUrl, options);

        var responseJSON = new Object();

        if(response.statusCode == httpUtils.RESULT_STATUS_CODE_200) {
            responseJSON[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_200;
            Object.assign(responseJSON, JSON.parse(response.getBody('utf8')));
        }
        else if(response.statusCode == httpUtils.RESULT_STATUS_CODE_301) {

        }
        else {
            responseJSON[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_500;
            responseJSON[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_500;
        }

        return responseJSON;
    }

    requestPost(strUrl, headerJson, mediaMetaData) {
        var httpUtils = this;

        headerJson[HttpUtils.REQUEST_PARAMETER_CONTENT_TYPE] = "application/json";

        var options = {
            headers: headerJson,
            body: JSON.stringify(mediaMetaData),
            followRedirects : false
        };

        var responseJSON = new Object();

        var response = request("POST", strUrl, options);
        var size = response.headers["content-length"];

        if(response.statusCode == httpUtils.RESULT_STATUS_CODE_200) {
            if(response.headers["content-type"] == "application/octet-stream") {
                var base64data = response.getBody().toString("base64");

                responseJSON[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_200;
                responseJSON[HttpUtils.RESPONSE_AUDIO_DATA] = base64data;
            }
            else {
                responseJSON[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_500;
                responseJSON[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_500;
            }
        }
        else if(response.statusCode == httpUtils.RESULT_STATUS_CODE_301) {
            responseJSON[httpUtils.RESPONSE_STATUS_CODE] = response.statusCode;
            responseJSON[HttpUtils.RESPONSE_RESULT] = response.body.toString('utf8');
        }
        else {
            responseJSON[httpUtils.RESPONSE_STATUS_CODE] = response.statusCode;
            responseJSON[httpUtils.RESPONSE_ERROR_CODE] = response.body.toString('utf8');
        }

        return responseJSON;
    }

    requestMutipart2(strUrl, headerJson, mediaMetaData, mediaData) {
        var httpUtils = this;
        headerJson[HttpUtils.REQUEST_PARAMETER_CONTENT_TYPE] = "multipart/form-data;boundary=" + HttpUtils.MULTIPART_BOUNDARY;

        var bodyData = "";

        bodyData += "--" + HttpUtils.MULTIPART_BOUNDARY + HttpUtils.LINE_FEED;
        bodyData += "Content-Disposition: form-data; name=\"metadata\"" + HttpUtils.LINE_FEED;
        bodyData += "Content-Type: application/json; charset=" + HttpUtils.CHAR_SET + HttpUtils.LINE_FEED;
        bodyData += HttpUtils.LINE_FEED;
        bodyData += JSON.stringify(mediaMetaData) + HttpUtils.LINE_FEED;

        bodyData += "--" + HttpUtils.MULTIPART_BOUNDARY + HttpUtils.LINE_FEED;
        bodyData += "Content-Disposition: form-data; name=\"media\"" + HttpUtils.LINE_FEED;
        bodyData += "Content-Type: application/octet-stream" + HttpUtils.LINE_FEED;
        bodyData += "Content-Transfer-Encoding: binary" + HttpUtils.LINE_FEED;
        bodyData += HttpUtils.LINE_FEED;

        var payload = Buffer.concat([
            Buffer.from(bodyData, "utf8"),
            mediaData,
            Buffer.from(HttpUtils.LINE_FEED + "--" + HttpUtils.MULTIPART_BOUNDARY + "--" + HttpUtils.LINE_FEED, "utf8"),
        ]);

        var options = {
            headers: headerJson,
            body: payload,
            followRedirects : false
        };

        var response = request("POST", strUrl, options);
        var responseJSON = new Object();

        if(response.statusCode == httpUtils.RESULT_STATUS_CODE_200) {
            if(response.headers["content-type"].search("multipart") > -1) {
                var boundary = HttpUtils.getBoundary(response.headers["content-type"]);
                responseJSON[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_200;

                var stream = HttpUtils.getMultipartStream(response.getBody('utf8'), boundary);

                var jsonArray = new Array();
                for (var j = 0; j < stream.length; j++) {
                    var header = HttpUtils.readHeader(stream[j]);
                    var strResult = HttpUtils.readBodyData(stream[j]);

                    jsonArray.push(JSON.parse(strResult));
                }

                responseJSON[HttpUtils.RESPONSE_RESULT] = jsonArray;
            }
            else {
                responseJSON[httpUtils.RESPONSE_STATUS_CODE] = httpUtils.RESULT_STATUS_CODE_500;
                responseJSON[httpUtils.RESPONSE_ERROR_CODE] = httpUtils.RESULT_ERROR_CODE_500;
            }
        }
        else if(response.statusCode == httpUtils.RESULT_STATUS_CODE_301) {
            responseJSON[httpUtils.RESPONSE_STATUS_CODE] = response.statusCode;
            responseJSON[HttpUtils.RESPONSE_RESULT] = response.body.toString('utf8');
        }
        else {
            responseJSON[httpUtils.RESPONSE_STATUS_CODE] = response.statusCode;
            responseJSON[httpUtils.RESPONSE_ERROR_CODE] = response.body.toString('utf8');
        }

        return responseJSON;
    }

    checkEntryPointProfile() {
        try {
            var exists = fs.existsSync(HttpUtils.mEntryProfilePath);
            if (!exists) {
                var entryPointText = 'HOSTNAME=' + HttpUtils.DEFAULT_ENTRYPOINT + '\n';
                entryPointText += 'AI_API_HTTP_PORT=' + HttpUtils.DEFAULT_HTTP_PORT + '\n';
                entryPointText += 'AI_API_GRPC_PORT=' + HttpUtils.DEFAULT_GRPC_PORT + '\n';
                fs.writeFileSync(HttpUtils.mEntryProfilePath, entryPointText, 'utf8');
            }
        } catch (e){
        }
    }

    setHttpEntrypoint(strEntryPoint) {
        var hostname = strEntryPoint.split(":")[0];
        var port = strEntryPoint.split(":")[1];
        var grpcOrigText = 'AI_API_GRPC_PORT=' + HttpUtils.DEFAULT_GRPC_PORT;
        try {
            var entryPointFileText = fs.readFileSync(HttpUtils.mEntryProfilePath, 'utf8');
            var entryPointTextArray = entryPointFileText.split('\n');
            for(var i=0;i<entryPointTextArray.length;i++) {
                if (entryPointTextArray[i].search("AI_API_GRPC_PORT") > -1) {
                    grpcOrigText = entryPointTextArray[i];
                }
            }

            var entryPointText = 'HOSTNAME=' + hostname + '\n';
            entryPointText += 'AI_API_HTTP_PORT=' + port + '\n';
            entryPointText += grpcOrigText + '\n';

            fs.writeFileSync(HttpUtils.mEntryProfilePath, entryPointText, 'utf8');
        } catch(e) {
        }
    }

    setGrpcEntrypoint(strEntryPoint) {
        var hostname = strEntryPoint.split(":")[0];
        var port = strEntryPoint.split(":")[1];
        var httpOrigText = 'AI_API_HTTP_PORT=' + HttpUtils.DEFAULT_HTTP_PORT;

        try {
            var entryPointFileText = fs.readFileSync(HttpUtils.mEntryProfilePath, 'utf8');
            var entryPointTextArray = entryPointFileText.split('\n');
            for(var i=0;i<entryPointTextArray.length;i++) {
                if (entryPointTextArray[i].search("AI_API_HTTP_PORT") > -1) {
                    httpOrigText = entryPointTextArray[i];
                }
            }

            var entryPointText = 'HOSTNAME=' + hostname + '\n';
            entryPointText += httpOrigText + '\n';
            entryPointText += 'AI_API_GRPC_PORT=' + port + '\n';

            fs.writeFileSync(HttpUtils.mEntryProfilePath, entryPointText, 'utf8');
        } catch(e) {
        }
    }
}

module.exports = HttpUtils;
