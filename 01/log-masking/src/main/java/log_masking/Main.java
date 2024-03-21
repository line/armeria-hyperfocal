package log_masking;

import java.util.function.BiFunction;

import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.common.logging.HeadersSanitizer;
import com.linecorp.armeria.common.logging.LogFormatter;
import com.linecorp.armeria.common.logging.LogLevel;
import com.linecorp.armeria.common.logging.LogWriter;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.logging.ContentPreviewingService;
import com.linecorp.armeria.server.logging.LoggingService;

public final class Main {

    public static void main(String[] args) throws Exception {
        final HeadersSanitizer<String> sanitizer =
                HeadersSanitizer.builderForText()
                                .sensitiveHeaders(HttpHeaderNames.AUTHORIZATION)
                                .sensitiveHeaders("x-access-token")
                                .build();

        final BiFunction<RequestContext, Object, String> contentSanitizer =
                (ctx, content) -> content.toString().replaceAll("secret", "****");

        final LogFormatter logFormatter =
                LogFormatter.builderForText()
                            .headersSanitizer(sanitizer)
                            .contentSanitizer(contentSanitizer)
                            .build();

        final LogWriter logWriter =
                LogWriter.builder()
                         .logFormatter(logFormatter)
                         .requestLogLevel(LogLevel.INFO)
                         .successfulResponseLogLevel(LogLevel.INFO)
                         .build();

        final Server server = Server
                .builder()
                .http(8080)
                .https(8443)
                .tlsSelfSigned()
                .service("/", (ctx, req) -> HttpResponse.of("Hello, world! Here's the secret."))
                .decorator(LoggingService.builder()
                                         .logWriter(logWriter)
                                         .newDecorator())
                .decorator(ContentPreviewingService.newDecorator(1024))
                .build();

        server.start().join();
    }
}
