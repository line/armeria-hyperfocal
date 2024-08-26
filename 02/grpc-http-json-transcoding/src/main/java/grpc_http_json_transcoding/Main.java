package grpc_http_json_transcoding;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.grpc.GrpcService;

public final class Main {
    public static void main(String[] args) throws Exception {
        var grpcSerivce = GrpcService
                .builder()
                .addService(new HelloServiceImpl())
                .enableHttpJsonTranscoding(true)
                .build();

        var server = Server
                .builder()
                .http(8080)
                .service(grpcSerivce)
                .service("/docs", new DocService())
                .build();

        server.start().join();
    }
}
