import json
import os
import http_utils

http_utils = http_utils.HttpUtils()

try:
    # Python 3.x
    from configparser import ConfigParser
except ImportError:
    # Python 2.x
    from ConfigParser import SafeConfigParser as ConfigParser
config = ConfigParser()
config_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '.', 'server.config'))
config.read(config_path)

# Decorator
def constant(func):
    def func_set(self, value):
        raise TypeError

    def func_get(self):
        return func()
    return property(func_get, func_set)

class TTS:
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
    def URL_TTS_VOICE_SYNTHESIS():
        return "/v2/voiceSynthesis"

    def CODE_NAME(self):
        return "TTS" + str(self.VERSION_MAJOR) + "." + str(self.VERSION_MINOR) + "." + str(self.VERSION_BUILD)

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


    def requestTTS(self, text, pitch, speed, speaker, volume, language, encoding, channel, sampleRate, sampleFmt):

        result_json = {}

        try:
            if not self.service_url or not self.client_key or not self.timestamp or not self.signature:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_401
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_401
                return json.dumps(result_json)

            if not text:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            if not language:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            if not encoding:
                result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                return json.dumps(result_json)

            if pitch < 50 or pitch > 150:
                pitch = -1

            if speed < 50 or speed > 150:
                speed = -1

            if volume < 0 or volume > 200:
                volume = -1

            metdata_json_object = {}
            metdata_json_object["text"] = text
            metdata_json_object["pitch"] = pitch
            metdata_json_object["speed"] = speed
            metdata_json_object["speaker"] = speaker
            metdata_json_object["volume"] = volume
            metdata_json_object["language"] = language
            metdata_json_object["encoding"] = encoding

            if encoding.lower() == "wav":
                if not sampleFmt:
                    result_json[http_utils.RESPONSE_STATUS_CODE] = http_utils.RESULT_STATUS_CODE_400
                    result_json[http_utils.RESPONSE_ERROR_CODE] = http_utils.RESULT_ERROR_CODE_400
                    return json.dumps(result_json)
                encodingOptObject = {}
                encodingOptObject["channel"] = channel
                encodingOptObject["sampleRate"] = sampleRate
                encodingOptObject["sampleFmt"] = sampleFmt

                metdata_json_object["encodingOpt"] = encodingOptObject


            strUrl = self.service_url + self.URL_TTS_VOICE_SYNTHESIS

            json_object = {}
            json_object[http_utils.REQUEST_PARAMETER_X_CLIENT_KEY] = self.client_key
            json_object[http_utils.REQUEST_PARAMETER_X_AUTH_TIMESTAMP] = self.timestamp
            json_object[http_utils.REQUEST_PARAMETER_X_CLIENT_SIGNATURE] = self.signature

            response = http_utils.requestPost(strUrl, json_object, metdata_json_object)

            responseObj = json.loads(response)

            if responseObj[http_utils.RESPONSE_STATUS_CODE] == 301:
                entrypoint = http_utils.setHttpEntrypoint(json.loads(responseObj[http_utils.RESPONSE_RESULT]))
                self.setServiceURL('https://' + entrypoint)
                strUrl = self.service_url + self.URL_TTS_VOICE_SYNTHESIS
                return http_utils.requestPost(strUrl, json_object, metdata_json_object)
            else:
                return response

        except Exception as e:
            return
