package native_image;

import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void test() throws Exception {
        // This is not really a test,
        // but this will make the native-image-agent generate the native image configuration.
        Main.main(new String[0]);
    }
}
