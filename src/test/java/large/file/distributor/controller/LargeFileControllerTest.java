package large.file.distributor.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LargeFileControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getFileByIdTest() {
        webTestClient.get()
                .uri("/download/{id}", "alpha")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DataBuffer.class)
                .consumeWith(response -> {
                    var dataBuffers = response.getResponseBody();
                    Assertions.assertNotNull(dataBuffers);
                    Assertions.assertEquals(4, dataBuffers.size());
                    var buffer1 = dataBufferToString(dataBuffers.get(0));
                    var buffer2 = dataBufferToString(dataBuffers.get(1));
                    assertEquals(4096, buffer1.length());
                    assertEquals(4096, buffer2.length());
                });
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void fileNotFound_Test() {
        webTestClient.get()
                .uri("/download/{id}", "meta")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.contains("File not found");
                    assert responseBody.contains("files/meta.txt");
                });
    }

    private String dataBufferToString(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
