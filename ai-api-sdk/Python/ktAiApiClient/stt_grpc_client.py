import base64
import json
import time
import ktAiApiSDK.gRPC as gRPC


def onError(message):
    print('Call onError = ' + message)
    if message == 'CODE0301':
        gRPC.serviceflag.clear()
        gRPC.releaseConnection()
        print('ReConnection gRpc Server ');
        print("Select Menu (1~4) + Enter: ")


def onResult(message):
    print(message)


def command(request_text=''):
    gRPC.serviceflag.set()
    sttMode = 'long'
    sampleFmt = 'S16LE'
    sampleRate = 16000
    channel = 1

    if request_text == '1':
        print("User Command >>> startSTT")    
        gRPC.startSTT(sttMode, sampleFmt, sampleRate, channel, onError)

    elif request_text == '2':
        print("User Command >>> sendAudioData")
        fileContent = None
        with open('./sample/record.pcm', mode='rb') as file:
            while 1: 
                fileContent = file.read(sampleRate*channel)
                if len(fileContent) == 0:
                    break
                gRPC.sendAudioData(fileContent, onResult)
                time.sleep(0.1)

    elif request_text == '3':
        print("User Command >>> stopSTT")
        gRPC.stopSTT()

    elif request_text == '4':
        print("User Command >>> releaseConnection")
        gRPC.releaseConnection()
        exit()


def main():
    client_id = "client-id"
    client_key = "client-key"
    client_secret = "client-secret"

    gRPC.setMetaData(client_key, client_id, client_secret)
    print('===================KT AI API STT GRPC TEST================');
    print('1 : startSTT');
    print('2 : sendAudioData [example file file/record.pcm]');
    print('3 : stopSTT');
    print('4 : releaseConnection');
    print('Ctrl + C : Exit')

    if not gRPC.grpcThread.is_alive():
        gRPC.start_grpc_thread()
    
    while True:
        try:
            input_key = input("\nSelect Menu (1~4) + Enter: ")
            command(input_key)
        except KeyboardInterrupt:
            break
    

if __name__ == "__main__":
	main()