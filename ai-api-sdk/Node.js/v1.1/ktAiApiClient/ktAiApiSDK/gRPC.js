import HttpUtils from "./HttpUtils.js";
var httpUtils = new HttpUtils();
import notifier from "./noti.js";
import grpcConnector from "./gRPCConnector.js";

export default class gRpc {
    VERSION_MAJOR = 1;
    VERSION_MINOR = 0;
    VERSION_BUILD = 0;
    CODE_NAME =  "STTGRPC_" + this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_BUILD;

    TAG = this.CODE_NAME;

    GRPC_STATUS_DISCONNECTED = 0;
    GRPC_STATUS_CONNECTED = 1;
    GRPC_STATUS_STARTED = 2;
    GRPC_STATUS_STOPED = 3;

    mSampleRate = 8000;

    mGrpcStatus = "";

    isRecording = false;
    isSending = false;

    /** ClientKey */
    mClientKey = "";

    /** Timestamp */
    mTimeStamp = "";

    /** Signature */
    mSignature = "";

    /** Requested SttMode for reconnection */
    mRequestSttMode = "";

    /** Requested SamleFmt for reconnection */
    mRequestSampleFmt = "";

    /** Requested SamleRate for reconnection */
    mRequestSampleRate = "";

    /** Requested Channel for reconnection */
    mRequestChannel = "";

    metadata = "";

    constructor() {
        this.mSTTgRPCCallback = null;
        this.mGrpcStatus = this.GRPC_STATUS_DISCONNECTED;
        this.mSslContext = null;
        this.grpcClient = null;
        this.ktSttService = null;
        httpUtils.checkEntryPointProfile();
        // this.mServiceURL = process.env.HOSTNAME + ":" + process.env.AI_API_GRPC_PORT;
    }

    setMetaData(clientKey, clientId, clientSecret){

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

    connectGRPC(){
        this.grpcClient = grpcConnector(this.mServiceURL, this.mClientKey, this.mTimeStamp, this.mSignature);
        this.mGrpcStatus = this.GRPC_STATUS_CONNECTED;
        this.ktSttService = this.grpcClient.ktSttService();
        this.ktSttService.on('error', this.grpcError);
        this.ktSttService.on('data', (data) => {
            this.responseData(data);
        });
        this.ktSttService.on('end', this.grpcEnd);
    }

    reconnectionGRPC() {
        this.grpcEnd();
        this.connectGRPC();
    }

    grpcError(error) {
        var gRpcL = this;
        var errorObj = new Object();
        errorObj.code = 402;
        errorObj.strMsg = '자원 없음 (음성인식 준비 안됨)';

        if(error.toString().indexOf('CODE:') != -1) {
            errorObj.code = error.toString().split('CODE:')[1].split(',')[0];
        }

        if(error.toString().indexOf('MSG:') != -1) {
            errorObj.strMsg = error.toString().split('MSG:')[1];
        }
        else {
            errorObj.strMsg = ' [' + error.toString() + ']';
        }

        if(error.toString().indexOf('CODE0301:') != -1) {
            var strEntryPoint = error.toString().split("entrypoint=")[1];

            if(strEntryPoint) {
                httpUtils.setGrpcEntrypoint(strEntryPoint);
                this.mServiceURL = strEntryPoint;
            }

            errorObj.code = 301;
            errorObj.strMsg = strEntryPoint;
        }

        notifier.emit('onError', errorObj);
    }

    grpcEnd() {
        this.ktSttService = null;
        notifier.emit('onRelease', 'onRelease');
    }

    releaseConnection() {
        this.ktSttService = null;
        notifier.emit('onRelease', 'onRelease');
    }

    startSTT(sttMode, sampleFmt, sampleRate, channel) {
        var msgPayload = "CODE0200:MODE=" + sttMode + ",F=" + sampleFmt + ",R=" + sampleRate + ",C=" + channel;
        var msgType = "sttStartCmd";
        this.sendGrpcData(msgType, msgPayload);
    }
    startSTT2(sttModelcode) {
        var msgPayload = "CODE0200:ModelCode=" + sttModelcode;
        var msgType = "sttStartCmd";
        this.sendGrpcData(msgType, msgPayload);
    }
    startSTT3(sttModelCode, sttMode, sampleFmt, sampleRate, channel) {
        var msgPayload = "CODE0200:sttModelCode=" + sttModelCode + ",MODE=" + sttMode + ",F=" + sampleFmt + ",R=" + sampleRate + ",C=" + channel;
        var msgType = "sttStartCmd";
        this.sendGrpcData(msgType, msgPayload);
    }

    sendAudioData(data) {
        this.ktSttService.write({ pcmData: data });
    }

    stopSTT() {
        var msgPayload = "CODE0200:";
        var msgType = "sttStopCmd";
        this.sendGrpcData(msgType, msgPayload);
    }

    responseData(data) {
        if (data.sttRes === 'signal') {

            if(data.signal.msgType == 'startSendPcmCmd') {
                var returnPayload = data.signal.msgPayload.split(':');

                if(returnPayload[0] == 'CODE0200') {
                    var pcmFormat = returnPayload[1];
                    var format = pcmFormat.split(",")[0].split("F=")[1];
                    var sampleRate = parseInt(pcmFormat.split(",")[1].split("R=")[1]);
                    var ch = parseInt(pcmFormat.split(",")[2].split("C=")[1]);

                    notifier.emit('onReadySTT', sampleRate, ch, format);
                }
                else {
                    var errorObj = new Object();
                    errorObj.code = getCode(Code);
                    errorObj.strMsg = getCodeDescriptor(Code);
                    notifier.emit('onError', errorObj);
                }
            }
            else if(data.signal.msgType == 'stopSendPcmCmd') {
                this.stopSTT();
                notifier.emit('onStopSTT');
                this.mGrpcStatus = this.GRPC_STATUS_STOPED;
            }
            else if(data.signal.msgType == 'sttStopRes') {
                notifier.emit('onStopSTT');
            }
            else if(data.signal.msgType == 'sttStartRes') {
            }

        } else if (data.sttRes === 'sttResult') {
            var resultObj = new Object();
            resultObj.text = data.sttResult.text;
            resultObj.type = data.sttResult.type;
            resultObj.startTime = data.sttResult.startTime;
            resultObj.endTime = data.sttResult.endTime;

            notifier.emit('onSTTResult', resultObj);
        } else {
        }
    }

    async sendGrpcData(msgType, msgPayload) {
        if (this.grpcClient === null) {
            await this.connectGRPC();
        }
        this.ktSttService.write({ signal: { msgType: msgType, msgPayload: msgPayload } });
    }

    getCode(strCode){
        var strCodeValue = strCode.split("CODE")[1];
        var code = parseInt(strCodeValue);
        return code;
    }

    getCodeDescriptor(strCode){
        var strDescriptor = "";
        if(strCode == "CODE0200") {
            strDescriptor = "성공";
        }
        else if(strCode == "CODE0500") {
            strDescriptor = "서버 에러";
        }
        else if(strCode == "CODE0400") {
            strDescriptor = "지원되지 않는 옵션임";
        }
        else if(strCode = "CODE0401") {
            strDescriptor = "권한 없음";
        }
        else if(strCode = "CODE0402") {
            strDescriptor = "자원 없음 (음성인식 준비 안됨)";
        }
        else {
            strDescriptor = "기타 에러";
        }
        return strDescriptor;
    }
}