package grpc_http_json_transcoding;

import grpc_http_json_transcoding.Hello.HelloReply;
import grpc_http_json_transcoding.Hello.HelloRequest;
import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        var message = String.format("Hello, %s!", request.getName());
        responseObserver.onNext(
                HelloReply
                        .newBuilder()
                        .setMessage(message)
                        .build());
        responseObserver.onCompleted();
    }
}
