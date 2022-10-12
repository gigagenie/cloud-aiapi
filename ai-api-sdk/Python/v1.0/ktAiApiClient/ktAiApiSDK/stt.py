from __future__ import absolute_import

import json
import base64
import os
import sys

try:
    # Python 3.x
    from configparser import ConfigParser
except ImportError:
    # Python 2.x
    from ConfigParser import SafeConfigParser as ConfigParser
config = ConfigParser()
config_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '.', 'server.config'))
config.read(config_path)

import http_utils
http_utils = http_utils.HttpUtils()

# Decorator
def constant(func):
    def func_set(self, value):
        raise TypeError

    def func_get(self):
        return func()
    return property(func_get, func_set)

class STT:
    @constant
    def VERSION_MAJOR():
        return 1

    @constant
    def VERSION_MINOR():
        return 0

    @constant
    def VERSION_BUILD():
        return 0

    @constant
    def URL_STT_VOICE_RECOGNIZER():
        return "/v2/voiceRecognize"

    def CODE_NAME(self):
        return "STT" + str(self.VERSION_MAJOR) + "." + str(self.VERSION_MINOR) + "." + str(self.VERSION_BUILD)

    def TAG(self):
        return self.CODE_NAME()

    def __init__(self):
        self.service_url = ""
        self.client_key = ""
        self.timestamp = ""
        self.signature = ""
        strUrl = "https://" + config.get('server', 'host') + ":" + config.get('server', 'http_port')
        self.setServiceURL(strUrl)


    def setServiceURL(self, entrypoint):
        self.service_url = entrypoint

    def setAuth(self, clientKey, clientId, clientSecret):

        if not clientKey or not clientId or not clientSecret:
            return

        self.client_key = clientKey
        self.timestamp = http_utils.getTimestamp()
        self.signature = http_utils.makeSignature(self.timestamp, clientId, clientSecret)


    def requestSTT(self, audioData, sttMode, targetLanguage, encoding, channel, sampleRate, sampleFmt):
        result_json = {}

        try:
            if not self.service_url or not self.client_key or not self.timestamp or not self.signature:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_401
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_401
                return json.dumps(result_json)

            if not audioData or len(audioData) == 0:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            if sttMode != 1 and sttMode != 2:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            if not targetLanguage:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            if not encoding:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            metdata_json_object = {}
            metdata_json_object["encoding"] = encoding
            metdata_json_object["targetLanguage"] = targetLanguage
            metdata_json_object["sttMode"] = sttMode

            if encoding.lower() == "raw":
                if not sampleFmt:
                    result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                    result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                    return json.dumps(result_json)

                encodingOptObject = {}
                encodingOptObject["channel"] = channel
                encodingOptObject["sampleRate"] = sampleRate
                encodingOptObject["sampleFmt"] = sampleFmt

                metdata_json_object["encodingOpt"] = encodingOpt

            strUrl = self.service_url + self.URL_STT_VOICE_RECOGNIZER

            json_object = {}
            json_object[http_utils.REQUEST_PARAMETER_X_CLIENT_KEY] = self.client_key
            json_object[http_utils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = self.timestamp
            json_object[http_utils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = self.signature

            response = http_utils.requestMultipart(strUrl, json_object, metdata_json_object, audioData)


            if response[http_utils.RESPONSE_STATUS_CODE] == 301:
                entrypoint = http_utils.setHttpEntrypoint(json.loads(response[http_utils.RESPONSE_RESULT]))
                self.setServiceURL('https://' + entrypoint)
                strUrl = self.service_url + self.URL_STT_VOICE_RECOGNIZER
                return http_utils.requestMultipart(strUrl, json_object, metdata_json_object, audioData)
            else:
                return response

        except Exception as e:
            return
            
    def querySTT(self, transactionId):
        result_json = {}

        try:
            if not self.service_url or not self.client_key or not self.timestamp or not self.signature:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_401
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_401
                return result_json

            if not transactionId:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return result_json

            strUrl = self.service_url + self.URL_STT_VOICE_RECOGNIZER + "/" + transactionId;

            json_object = {};
            json_object[http_utils.REQUEST_PARAMETER_X_CLIENT_KEY] = self.client_key;
            json_object[http_utils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = self.timestamp;
            json_object[http_utils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = self.signature;

            return http_utils.requestGet(strUrl, json_object);
        except Exception as e:
            return