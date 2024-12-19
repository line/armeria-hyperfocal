package outlier;

import java.util.List;

import com.linecorp.armeria.client.ClientRequestContext;
import com.linecorp.armeria.client.ClientRequestContextCaptor;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.RetryConfig;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.Server;

public final class Main {

    public static void main(String[] args) throws Exception {
        final Server server = Server
                .builder()
                .http(8080)
                .service("/", (ctx, req) -> {
                    if (req.headers().contains("fail")) {
                        return HttpResponse.of(ResponseHeaders.of(500));
                    }

                    return HttpResponse.of("Hello, world!");
                })
                .build();

        server.start().join();
        final WebClient webClient =
                WebClient.builder("http://127.0.0.1:8080")
                         .decorator(LoggingClient.newDecorator())
                         .decorator(RetryingClient.builder(RetryConfig.builder(RetryRule.failsafe())
                                                                      .maxTotalAttempts(3).build())
                                                  .newDecorator())
                         .decorator(LoggingClient.builder().newDecorator())
                         .build();

        try (ClientRequestContextCaptor captor = Clients.newContextCaptor()) {
            webClient.execute(RequestHeaders.of(HttpMethod.GET, "/", "fail", "true")).aggregate().join();
            final List<ClientRequestContext> ctxs = captor.getAll();
            for (int i = 0; i < ctxs.size(); i++) {
                System.out.println("Request " + i + ": " + ctxs.get(i).localAddress().getPort());
            }
        }
    }
}
