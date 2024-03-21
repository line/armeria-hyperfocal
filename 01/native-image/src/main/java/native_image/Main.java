package native_image;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

public final class Main {

    public static void main(String[] args) throws Exception {
        final Server server = Server
                .builder()
                .http(8080)
                .https(8443)
                .tlsSelfSigned()
                .annotatedService(new Object() {
                    @Get("/greet/:name")
                    public String greet(@Param("name") String name) {
                        return "Hello, " + name + '!';
                    }
                })
                .build();

        server.start().join();
    }
}
