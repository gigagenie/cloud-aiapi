from ktAiApiSDK.tts import TTS
import base64
import json


def main():
    tts_client = TTS()

    client_id = "client-id"
    client_key = "client-key"
    client_secret = "client-secret"

    tts_client.setAuth(client_key, client_id, client_secret)

    text = "안녕하세요 반갑습니다"
    pitch = -1
    speed = -1
    speaker = 1
    volume = -1
    language = "ko"
    encoding = "mp3"
    channel = 1
    sample_rate = 16000
    sample_fmt = "S16LE"

    result_json = tts_client.requestTTS(text, pitch, speed, speaker, volume, language, encoding, channel, sample_rate, sample_fmt)
    result_json_dic = json.loads(result_json)
    print(result_json_dic)

    if result_json_dic["statusCode"] == 200:
        print(result_json_dic)
        try:
            with open('tts.mp3', 'wb') as f:
                f.write(base64.b64decode(result_json_dic["audioData"].encode('utf8')))
        except Exception as e:
            print("error = " + e)


if __name__ == "__main__":
	main()