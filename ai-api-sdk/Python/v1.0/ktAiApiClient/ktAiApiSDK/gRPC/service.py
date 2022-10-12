from __future__ import print_function
from __future__ import absolute_import

import os
import sys

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
_root_path = os.path.dirname(os.path.abspath(os.path.dirname(__file__))) # ktAiApiSDK/grpc/service.py
sys.path.insert(0, _root_path) 
_proto_path = os.path.abspath(os.path.join(_root_path, 'proto')) # ktAiApiSDK/proto/service.py
sys.path.insert(0, _proto_path) 

import grpc
import ktaiapi_pb2
import gRPC
import gRPC.grpc_channel as grpc_channel

import json
import logging
import threading
from ._config import *

import time
import http_utils

logger = logging.getLogger()

http_utils = http_utils.HttpUtils() 

serviceflag = threading.Event()
gen_event = threading.Event()

message = ktaiapi_pb2.reqKtStt()
g_msgType = None
g_msgPayload = None
g_msgPcmData = None
g_msgKind = None

GRPC_STATUS_DISCONNECTED       = 0
GRPC_STATUS_CONNECTED          = 1
GRPC_STATUS_STARTED            = 2
GRPC_STATUS_STOPED             = 3

mGrpcStatus = None

gStub = None

grpcThread = threading.Thread()

onErrorCallback = None
onResultCallback = None

def sendMessage(msgType='', msgPayload=''):
    global g_msgKind
    global g_msgType
    global g_msgPayload

    g_msgKind = 'text'
    g_msgType = msgType
    g_msgPayload = msgPayload
    if msgType != '':
        gen_event.set()

def sendAudioData(msgPcmData='', callback=''):
    global g_msgKind
    global g_msgPcmData
    global onResultCallback

    g_msgKind = 'binary'
    g_msgPcmData = msgPcmData
    if msgPcmData != '':
        onResultCallback = callback
        gen_event.set()

def startSTT(sttMode, sampleFmt, sampleRate, channel, callback):
    global onErrorCallback
    msgType = 'sttStartCmd'
    msgPayload = 'CODE0200:MODE=' + sttMode + ',F=' + sampleFmt + ',R=' + str(sampleRate) + ',C=' + str(channel)
    sendMessage(msgType, msgPayload)
    onErrorCallback = callback

def stopSTT():
    msgType = 'sttStopCmd'
    msgPayload = 'CODE0200:'
    sendMessage(msgType, msgPayload)

def setMetaData(clientKey, clientId, clientSecret):
    grpc_channel.CLIENT_ID = clientId
    grpc_channel.CLIENT_KEY = clientKey
    grpc_channel.CLIENT_SECRET = clientSecret

def setServiceURL(entrypoint):
    grpc_channel.HOST = entrypoint.split(':')[0]
    grpc_channel.PORT = int(entrypoint.split(':')[1])

def _generate_request():
    while True:
        gen_event.wait()
        if g_msgKind == 'text':
            message.signal.msgType = g_msgType
            message.signal.msgPayload = g_msgPayload
            yield message
        elif g_msgKind == 'binary':
            message.pcmData = g_msgPcmData
            yield message
        gen_event.clear()

def releaseConnection():
    global gStub
    grpc_channel.grpc_disconn()
    gStub = None


def grpc_request():
    global gStub
    global g_msgType
    global g_msgPayload
    global onErrorCallback
    global onResultCallback
    requests = gStub.ktSttService(_generate_request())
    for responses in requests:
        if responses.HasField("signal"):
            mType = responses.signal.msgType
            mPayload = responses.signal.msgPayload
            if mType == 'sttStartRes':
                pass
            elif mType == 'startSendPcmCmd':
                Code = mPayload.split(':')[0]
                if Code == 'CODE0200':
                    PcmFormat = mPayload.split(':')[1]
                    format = PcmFormat.split(',')[0].split('F=')[1];
                    rate = PcmFormat.split(',')[1].split('R=')[1];
                    channel = PcmFormat.split(',')[2].split('C=')[1];
                gen_event.clear()
            elif mType == 'stopSendPcmCmd':
                pass
            elif mType == 'sttStopRes':
                pass

            elif mType == 'sttEvent':
                pass
            elif mType == 'errInfo':
                errorCode = mPayload.split(':')[0]

                if errorCode == 'CODE0301':
                    strEntrypoint = mPayload.split('entrypoint=')[1]
                    strEntryPoint = http_utils.setGrpcEntrypoint(strEntrypoint)
                    grpc_channel.HOST = strEntryPoint.split(':')[0]
                    grpc_channel.PORT = int(strEntryPoint.split(':')[1])

                    if onErrorCallback is not None:
                        onErrorCallback(errorCode)
                gen_event.clear()

            if onResultCallback is not None:
                returnData = 'msgType:' + mType + ',msgPayload:' +mPayload
                onResultCallback(returnData)

        elif responses.HasField("sttResult"):
            text = responses.sttResult.text
            type = responses.sttResult.type
            startTime = ''
            endTime = ''
            if type == 'full':
                startTime = responses.sttResult.startTime
                endTime = responses.sttResult.endTime

            returnData = 'sttResult:text='+ text +', type='+ type + ', startTime=' + str(startTime) + ', endTime' + str(endTime);

            if onResultCallback is not None:
                onResultCallback(returnData)

            gen_event.clear()

        else: # error
            break

def connectGRPC():
    global gStub

    while True:
        try:
            serviceflag.wait()
            gStub = grpc_channel.grpc_conn()
            grpc_request()
            serviceflag.clear()
        except grpc.RpcError as rpc_error:
            if rpc_error.code() == grpc.StatusCode.UNAVAILABLE:
                logger.debug('The service is currently unavailable.')
            elif rpc_error.code() == grpc.StatusCode.CANCELLED:
                logger.debug('Channel closed!')
            elif rpc_error.code() == grpc.StatusCode.UNKNOWN:
                logger.debug('write after end')
            else:
                raise rpc_error
            serviceflag.clear()
        except Exception as e:
            logger.error('Error: ' + str(e))
        else:
            logger.error('UNKNOWN ERROR. retry grpc service.')

def start_grpc_thread():
    global grpcThread
    grpcThread = threading.Thread(target=connectGRPC)
    grpcThread.daemon = True
    grpcThread.start()
