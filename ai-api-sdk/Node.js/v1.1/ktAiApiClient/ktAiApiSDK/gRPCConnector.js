import * as url from 'url';
const __filename = url.fileURLToPath(import.meta.url);
const __dirname = url.fileURLToPath(new URL('.', import.meta.url));
var PROTO_PATH = __dirname + '/protos/ktaiapi.proto';
// import parseArgs from "minimist";
import grpc from "@grpc/grpc-js";
import protoLoader from "@grpc/proto-loader";
var packageDefinition = protoLoader.loadSync(PROTO_PATH, {keepCase: true, longs: String, enums: String, defaults: true, oneofs: true});

var metaClientKey = null;
var metaTimeStamp = null;
var metaSignature = null;
var sslCredential = null;
var authCredential = null;
var credentials = null;
var proto = null;

// proto = grpc.loadPackageDefinition(packageDefinition).kt.gigagenie.ai.api;

export default function grpc_connector(serviceURL, clientKey, timeStamp, signature) {
    metaClientKey = clientKey;
    metaTimeStamp = timeStamp;
    metaSignature = signature;

    sslCredential = grpc.credentials.createSsl();
    authCredential = grpc.credentials.createFromMetadataGenerator(metaCallback);
    credentials = grpc.credentials.combineChannelCredentials(sslCredential, authCredential);
    proto = grpc.loadPackageDefinition(packageDefinition).kt.gigagenie.ai.api;
    return new proto.KtAiApi(serviceURL, credentials);
}

const metaCallback = (_params, callback) => {
    const metadata = new grpc.Metadata();
    metadata.add('x-client-key', metaClientKey);
    metadata.add('x-auth-timestamp', metaTimeStamp);
    metadata.add('x-client-signature', metaSignature);
    callback(null, metadata);
}