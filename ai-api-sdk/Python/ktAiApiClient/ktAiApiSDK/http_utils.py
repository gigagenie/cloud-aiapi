# -*- coding: utf-8 -*-
import hmac
import hashlib
import requests
import json
import base64
from datetime import datetime
from requests_toolbelt.multipart import encoder
import os
import sys

sys.path.append(os.getcwd())

import os
import sys
import http.client
import urllib, zlib, mimetypes

# Decorator
def constant(func):
    def func_set(self, value):
        raise TypeError

    def func_get(self):
        return func()
    return property(func_get, func_set)

class HttpUtils:
    def TAG(self):
        return "HttpUtils"

    @constant
    def SUPPORT_PROTOCOL_LOG():
        return True

    @constant
    def RESPONSE_STATUS_CODE():
        return "statusCode"

    @constant
    def RESPONSE_ERROR_CODE():
        return "errorCode"

    @constant
    def RESPONSE_RESULT():
        return "result"

    @constant
    def RESPONSE_AUDIO_DATA():
        return "audioData"

    @constant
    def RESULT_STATUS_CODE_200():
        return 200

    @constant
    def RESULT_STATUS_CODE_400():
        return 400

    @constant
    def RESULT_STATUS_CODE_401():
        return 401

    @constant
    def RESULT_STATUS_CODE_500():
        return 500

    @constant
    def RESULT_ERROR_CODE_400():
        return '파라미터 설정 오류'

    @constant
    def RESULT_ERROR_CODE_401():
        return '권한 없음'

    @constant
    def RESULT_ERROR_CODE_500():
        return '시스템 에러'

    @constant
    def REQUEST_PARAMETER_AUTHORIZATION():
        return "Authorization"

    @constant
    def REQUEST_PARAMETER_X_CLIENT_KEY():
        return "x-client-key"

    @constant
    def REQUEST_PARAMETER_X_AUTH_TIMESTAMP():
        return "x-auth-timestamp"

    @constant
    def REQUEST_PARAMETER_X_CLIENT_SIGNATURE():
        return "x-client-signature"

    @constant
    def REQUEST_PARAMETER_CONTENT_TYPE():
        return "Content-type"

    @constant
    def REQUEST_PARAMETER_CONTENT_LENGTH():
        return "Content-length"

    @constant
    def LINE_FEED():
        return "\r\n"

    @constant
    def MULTIPART_BOUNDARY():
        return "9912ef1112228sf8899123f21e8e"

    @constant
    def CHAR_SET():
        return "utf-8"

    @constant
    def DEFAULT_HTTP_CONNECT_TIMEOUT():
        return 30 * 1000

    @constant
    def DEFAULT_HTTP_SO_TIMEOUT():
        return 60 * 1000

    @constant
    def mEntryProfilePath():
        return os.path.join(os.path.dirname(os.path.abspath(__file__)), 'server.config')

    @constant
    def DEFAULT_HOST():
        return 'aiapi.gigagenie.ai'

    @constant
    def DEFAULT_HTTP_POST():
        return 443

    @constant
    def DEFAULT_GRPC_POST():
        return 443

    def __init__(self):
        self.checkEntryPointProfile()

    def getBoundary(self, contentType):
        strBoundary = contentType.split("boundary=")[1].strip()
        return strBoundary

    def getMultipartStream(self, content, boundary):
        strBody = content.split(self.LINE_FEED);
        strTemp = ''
        strList = []

        for text in strBody:
            if text.strip() != '':
                strTemp += text + self.LINE_FEED

        strBody2 = strTemp.split('--' + boundary)

        for text in strBody2:
            if 'form-data' in text:
                strList.append(text)
        return strList


    def readHeader(self, multipart):
        strBody = multipart.split(self.LINE_FEED)
        result = ''
        insertindex = 0
        for  text in strBody:
            if 'Content-' in text:
                if insertindex > 0:
                     result += self.LINE_FEED
                result += text
                insertindex += 1
        return result

    def readBodyData(self, multipart):
        strBody = multipart.split(self.LINE_FEED)
        result = ""
        insertindex = 0
        for text in strBody:
            if 'Content-' in text or text == '':
                continue
            if insertindex > 0:
                result += self.LINE_FEED
            result += text
            insertindex += 1
        return result

    def getTimestamp(self):
        (dt, micro) = datetime.now().strftime('%Y%m%d%H%M%S.%f').split(".")
        dt = "%s%01d" % (dt, int(micro) / 100000)
        return dt

    def makeSignature(self, mTimeStamp, clientId, clientSecret):
        secret_key = bytes(clientSecret, "UTF-8")
        message = bytes(clientId + ":" + mTimeStamp, "UTF-8")
        return hmac.new(secret_key, message, digestmod=hashlib.sha256).hexdigest()

    def requestGet(self, strUrl, headerJson):
        headerJson[self.REQUEST_PARAMETER_CONTENT_TYPE] = "application/json"
        response = requests.get(strUrl, headers=headerJson)

        responseJSON = {}

        if response.status_code == 200:
            responseJSON[self.RESPONSE_STATUS_CODE] = self.RESULT_STATUS_CODE_200
            resultObj = json.loads(response.content.decode('utf8'))
            for key, value in resultObj.items():
                responseJSON[key] = value
        elif response.status_code == 301:
            responseJSON[self.RESPONSE_STATUS_CODE] = response.status_code
            responseJSON[self.RESPONSE_RESULT] = response.content.decode('utf8')
        else:
            responseJSON[self.RESPONSE_STATUS_CODE] = response.status_code
            responseJSON[self.RESPONSE_ERROR_CODE] = response.content.decode('utf8')

        return responseJSON

    def requestPost(self, strUrl, headerJson, mediaMetaData):
        headerJson[self.REQUEST_PARAMETER_CONTENT_TYPE] = "application/json"

        responseJSON = {}

        response = requests.post(strUrl, data=json.dumps(mediaMetaData), headers=headerJson)

        size = response.headers["content-length"]

        if response.status_code == 200:
            if response.headers["content-type"] == "application/octet-stream":
                base64data = base64.b64encode(response.content).decode('utf8')
                responseJSON[self.RESPONSE_STATUS_CODE] = self.RESULT_STATUS_CODE_200
                responseJSON[self.RESPONSE_AUDIO_DATA] = base64data
            else:
                responseJSON[self.RESPONSE_STATUS_CODE] = self.RESULT_STATUS_CODE_500
                responseJSON[self.RESPONSE_ERROR_CODE] = self.RESULT_ERROR_CODE_500

        elif response.status_code == 301:
            responseJSON[self.RESPONSE_STATUS_CODE] = response.status_code
            responseJSON[self.RESPONSE_RESULT] = response.content.decode('utf8')
        else:
            responseJSON[self.RESPONSE_STATUS_CODE] = response.status_code
            responseJSON[self.RESPONSE_ERROR_CODE] = response.content.decode('utf8')

        return json.dumps(responseJSON)



    def requestMultipart(self, strUrl, headerJson, mediaMetaData, mediaData):
        headerJson[self.REQUEST_PARAMETER_CONTENT_TYPE] = "multipart/form-data;boundary=" + self.MULTIPART_BOUNDARY

        multiple_files = [
            ("metadata", ("application/json", json.dumps(mediaMetaData))),
            ("media", ("application/octet-stream", mediaData, "rb"))
        ]

        multipart_encoder = encoder.MultipartEncoder(
            fields=multiple_files,
            boundary=self.MULTIPART_BOUNDARY,
        )

        response = requests.post(strUrl,
                          data=multipart_encoder,
                          headers=headerJson)

        responseJSON = {}

        if response.status_code == 200:
            if "multipart" in response.headers["content-type"]:
                boundary = self.getBoundary(response.headers["content-type"]);

                responseJSON[self.RESPONSE_STATUS_CODE] = self.RESULT_STATUS_CODE_200

                stream = self.getMultipartStream(response.content.decode('utf8'), boundary)

                jsonArray = []
                for text in stream:
                    header = self.readHeader(text)
                    strResult = self.readBodyData(text)
                    jsonArray.append(strResult)

                responseJSON[self.RESPONSE_RESULT] = jsonArray

            else:
                responseJSON[self.RESPONSE_STATUS_CODE] = self.RESULT_STATUS_CODE_500
                responseJSON[self.RESPONSE_ERROR_CODE] = self.RESULT_ERROR_CODE_500

        elif response.status_code == 301:
            responseJSON[self.RESPONSE_STATUS_CODE] = response.status_code
            responseJSON[self.RESPONSE_RESULT] = response.content.decode('utf8')
        else:
            responseJSON[self.RESPONSE_STATUS_CODE] = self.statusCode
            responseJSON[self.RESPONSE_ERROR_CODE] = response.content.decode('utf8')

        return responseJSON

    def checkEntryPointProfile(self):
        if not os.path.exists(self.mEntryProfilePath):
            f = open(self.mEntryProfilePath, mode='w')
            f.write('[server]\n')
            f.write('host: ' + self.DEFAULT_HOST + '\n')
            f.write('http_port: ' + str(self.DEFAULT_HTTP_POST) + '\n')
            f.write('grpc_port: ' + str(self.DEFAULT_GRPC_POST) + '\n')
            f.close()


    def setHttpEntrypoint(self, strEntryPoint):
        hostname = strEntryPoint['entrypoint'].split(':')[0]
        port = strEntryPoint['entrypoint'].split(':')[1]
        f = open(self.mEntryProfilePath, mode='r')
        grpcText = 'grpc_port: ' + str(self.DEFAULT_GRPC_POST) + '\n'
        while True:
            line = f.readline()
            if not line: break
            if 'grpc_port:' in line:
                grpcText = line
                break
        f.close()

        f = open(self.mEntryProfilePath, mode='w')
        f.write('[server]\n')
        f.write('host: ' + hostname + '\n')
        f.write('http_port: ' + port + '\n')
        f.write(grpcText)
        f.close()

        return strEntryPoint['entrypoint']

    def setGrpcEntrypoint(self, strEntryPoint):
        hostname = strEntryPoint.split(':')[0]
        port = strEntryPoint.split(':')[1]

        f = open(self.mEntryProfilePath, mode='r')
        httpText = 'http_port: ' + str(self.DEFAULT_HTTP_POST) + '\n'
        while True:
            line = f.readline()
            if not line: break
            if 'http_port:' in line:
                httpText = line
                break
        f.close()

        f = open(self.mEntryProfilePath, mode='w')
        f.write('[server]\n')
        f.write('host: ' + hostname + '\n')
        f.write(httpText)
        f.write('grpc_port: ' + port + '\n')
        f.close()
        return strEntryPoint















#
#
#
#
#
##
##
