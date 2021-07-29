import os
import sys

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))

from proto import ktaiapi_pb2_grpc
from datetime import datetime
import hmac
import hashlib
from ._config import *
import grpc

CLIENT_ID = None
CLIENT_KEY = None
CLIENT_SECRET = None

HOST = config.get('server', 'host')
PORT = int(config.get('server', 'grpc_port'))

PKGNAME = ''


g_channel = None
g_stub = None

def getMetadata():
    timestamp = datetime.now().strftime('%Y%m%d%H%M%S%f')[:-3]
    message = CLIENT_ID + ':' + timestamp

    signature = hmac.new(CLIENT_SECRET.encode(), message.encode(), hashlib.sha256).hexdigest()
    metadata = [('x-client-key', CLIENT_KEY),
                ('x-auth-timestamp', timestamp),
                ('x-client-signature', signature)]

    return metadata

def credentials(context, callback):
    callback(getMetadata(), None)

def getCredentials():
    sslCred = grpc.ssl_channel_credentials()
    authCred = grpc.metadata_call_credentials(credentials)
    return grpc.composite_channel_credentials(sslCred, authCred)

def log_connectivity_changes(connectivity):
    return

def grpc_conn():
    global g_channel
    global g_stub

    if g_channel is None:
        g_channel = grpc.secure_channel('{}:{}'.format(HOST, PORT), getCredentials())
        g_channel.subscribe(log_connectivity_changes)
    if g_stub is None:
        g_stub = ktaiapi_pb2_grpc.KtAiApiStub(g_channel)

    return g_stub

def grpc_reconn(grpcUrl):
    global g_channel
    global g_stub

    if g_channel is None:
        g_channel = grpc.secure_channel(grpcUrl, getCredentials())
        g_channel.subscribe(log_connectivity_changes)
    if g_stub is None:
        g_stub = ktaiapi_pb2_grpc.KtAiApiStub(g_channel)

    return g_stub

def grpc_disconn():
    global g_channel
    global g_stub

    if g_channel is not None:
        g_channel.unsubscribe(log_connectivity_changes)
        g_channel.close()
    g_channel = None
    g_stub = None
