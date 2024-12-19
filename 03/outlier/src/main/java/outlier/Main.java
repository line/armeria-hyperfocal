package outlier;

import java.util.List;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.ClientRequestContext;
import com.linecorp.armeria.client.ClientRequestContextCaptor;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.common.outlier.OutlierDetection;
import com.linecorp.armeria.common.outlier.OutlierRule;
import com.linecorp.armeria.server.Server;

public final class Main {

    public static void main(String[] args) throws Exception {
        final Server server = Server
                .builder()
                .http(8080)
                .service("/", (ctx, req) -> {
                    if (req.headers().contains("fail")) {
                        return HttpResponse.of(500);
                    }

                    return HttpResponse.of("Hello, world!");
                })
                .build();

        server.start().join();

        final ClientFactoryBuilder builder = ClientFactory.builder();
        builder.maxNumEventLoopsPerEndpoint(2);

        final OutlierRule rule = OutlierRule.builder().onServerError().onException().build();
        final OutlierDetection outlierDetection = OutlierDetection.builder(rule)
                                                                  .build();
        builder.connectionOutlierDetection(outlierDetection);
        final ClientFactory factory = builder.build();

        final WebClient webClient = WebClient.builder("http://127.0.0.1:8080").factory(factory)
                                             .build();

        try (ClientRequestContextCaptor captor = Clients.newContextCaptor()) {
            for (int i = 0; i < 30; i++) {
                if (i % 2 == 0) {
                    webClient.execute(RequestHeaders.of(HttpMethod.GET, "/"));
                } else {
                    webClient.execute(RequestHeaders.of(HttpMethod.GET, "/", "fail", "true"));
                    Thread.sleep(100);
                }
            }
            final List<ClientRequestContext> ctxs = captor.getAll();
            for (int i = 0; i < ctxs.size(); i++) {
                System.out.println("Request " + i + ": " + ctxs.get(i).localAddress().getPort());
            }
        }
    }
}
