package retrylimiter;

import java.util.concurrent.atomic.AtomicInteger;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryConfig;
import com.linecorp.armeria.client.retry.RetryDecision;
import com.linecorp.armeria.client.retry.RetryLimiter;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatusClass;
import com.linecorp.armeria.common.logging.LogLevel;
import com.linecorp.armeria.common.logging.LogWriter;
import com.linecorp.armeria.server.Server;

public final class Main {
    public static void main(String[] args) throws Exception {
        var counter = new AtomicInteger(0);
        var server = Server.builder()
                           .http(8080)
                           .service("/", (ctx, req) -> {
                               if (counter.getAndIncrement() % 2 == 0) {
                                   return HttpResponse.of(500);
                               } else {
                                   return HttpResponse.of(200);
                               }
                           })
                           .build();

        server.start().join();

        var retryRule = RetryRule.of(
                RetryRule.builder()
                         .onStatusClass(HttpStatusClass.SERVER_ERROR)
                         .build(RetryDecision.retry(Backoff.fixed(1000), 1)),
                RetryRule.builder()
                         .onStatusClass(HttpStatusClass.SUCCESS)
                         .build(RetryDecision.noRetry(-1))
        );

        var retryLimiter = RetryLimiter.tokenBased(5, 3);

        var retryConfig = RetryConfig.builder(retryRule)
                                     .retryLimiter(retryLimiter)
                                     .build();

        var client = WebClient.builder("http://127.0.0.1:8080")
                              .decorator(LoggingClient.builder()
                                                      .logWriter(LogWriter.builder()
                                                                          .requestLogLevel(LogLevel.INFO)
                                                                          .successfulResponseLogLevel(
                                                                                  LogLevel.INFO)
                                                                          .failureResponseLogLevel(
                                                                                  LogLevel.WARN)
                                                                          .build())
                                                      .newDecorator())
                              .decorator(RetryingClient.newDecorator(retryConfig))
                              .build();

        try {
            for (var i = 0; i < 3; i++) {
                client.get("/").aggregate().join();
            }
        } finally {
            System.out.println(retryLimiter);
        }
    }
}
