from ktAiApiSDK.stt import STT
import time
import json

def main():
    stt_client = STT()

    client_id = "client-id"
    client_key = "client-key"
    client_secret = "client-secret"

    stt_client.setAuth(client_key, client_id, client_secret)

    stt_mode = 2
    target_language = "ko"
    encoding = "mp3"
    channel = 1
    sample_rate = 16000
    sample_fmt = "S16LE"

    file_path = "sample/long.mp3"
    with open(file_path, mode='rb') as file:
        audio_data = file.read()
        result_json = stt_client.requestSTT(audio_data, stt_mode, target_language, encoding, channel, sample_rate, sample_fmt)
        print(result_json)

        result_array = result_json.get("result")
        transaction_id = json.loads(result_array[0]).get("transactionId")
        
        time.sleep(5)

        query_result_json = stt_client.querySTT(transaction_id);
        print(query_result_json);

if __name__ == "__main__":
	main()