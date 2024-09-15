package large.file.distributor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class FileStreamingE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getFileByNameE2ETest() {
        // GIVEN
        var fileName = "alpha";

        // WHEN
        webTestClient.get()
                .uri("/download/{id}", fileName)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Content-Type", "application/octet-stream")
                .expectHeader().valueMatches("Content-Disposition", "attachment; filename=\"alpha.txt\"")
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] fileBytes = response.getResponseBody();

                    // THEN
                    assertNotNull(fileBytes, "The file content should not be null");

                    final int CHUNK_SIZE = 4096;
                    for (int i = 0; i < fileBytes.length; i += CHUNK_SIZE) {
                        int end = Math.min(fileBytes.length, i + CHUNK_SIZE);
                        byte[] chunk = Arrays.copyOfRange(fileBytes, i, end);
                        if (i + CHUNK_SIZE > fileBytes.length) {
                            assertTrue(chunk.length <= CHUNK_SIZE, "Last chunk size should be less than or equal to CHUNK_SIZE");
                        } else {
                            assertEquals(CHUNK_SIZE, chunk.length, "Chunk size mismatch at index " + i);
                        }
                    }
                });
    }
}
