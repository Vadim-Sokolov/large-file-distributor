package large.file.distributor.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenAccessProtectedEndpointWithoutAuth_thenUnauthorized() {
        webTestClient.get()
                .uri("/download/{id}", "alpha")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void whenAuthenticated_thenAccessProtectedEndpoint() {
        webTestClient.get()
                .uri("/download/{id}", "alpha")
                .exchange()
                .expectStatus().isOk();
    }
}
