package max_num_event_loop;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.ClientRequestContextCaptor;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.CommonPools;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;

import io.netty.channel.EventLoopGroup;

public final class Main {

    public static void main(String[] args) throws Exception {
        final Server server = Server
                .builder()
                .http(8080)
                .service("/", (ctx, req) -> HttpResponse.of("Hello, world!"))
                .build();

        server.start().join();

        final ClientFactoryBuilder builder = ClientFactory.builder();
        final EventLoopGroup eventLoopGroup = CommonPools.workerGroup();
        builder.workerGroup(eventLoopGroup, false);
        builder.maxNumEventLoopsPerEndpoint(5);
        final ClientFactory factory = builder.build();

        final WebClient webClient = WebClient.builder("http://127.0.0.1:8080")
                                             .factory(factory)
                                             .build();

        try (ClientRequestContextCaptor captor = Clients.newContextCaptor()) {
            for (int i = 0; i < 10; i++) {
                webClient.get("/").aggregate().join();
            }
            captor.getAll().forEach(ctx -> {
                System.err.println("eventLoop: " + ctx.eventLoop().withoutContext());
            });
        }

    }
}
