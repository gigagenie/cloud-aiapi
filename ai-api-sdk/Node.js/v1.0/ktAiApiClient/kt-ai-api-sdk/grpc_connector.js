const grpc = require('grpc');
const httpUtils = require(__dirname + '/HttpUtils');

var metaClientKey = null;
var metaTimeStamp = null;
var metaSignature = null;

function grpc_connector(serviceURL, clientKey, timeStamp, signature) {
    metaClientKey = clientKey;
    metaTimeStamp = timeStamp;
    metaSignature = signature;
    sslCredential = grpc.credentials.createSsl();
    authCredential = grpc.credentials.createFromMetadataGenerator(_setMetadata);
    credentials = grpc.credentials.combineChannelCredentials(sslCredential, authCredential);
    proto = grpc.load(__dirname + '/protos/ktaiapi.proto').kt.gigagenie.ai.api;
    return new proto.KtAiApi(serviceURL, credentials);
}

function _setMetadata(params, callback) {
    const metadata = new grpc.Metadata();
    metadata.add('x-client-key', metaClientKey);
    metadata.add('x-auth-timestamp', metaTimeStamp);
    metadata.add('x-client-signature', metaSignature);
    callback(null, metadata);
};

module.exports.grpc_connector = grpc_connector;
