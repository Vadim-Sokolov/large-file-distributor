package large.file.distributor.service;

import large.file.distributor.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LargeFileServiceTest {

    private final FileRepository fileRepository = new FileRepository();
    private final LargeFileService largeFileService = new LargeFileService(fileRepository);

    private ServerWebExchange exchange;

    @BeforeEach
    public void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
    }

    @Test
    void getFileByNameTest_Success() {

        // GIVEN
        var fileName = "alpha";
        var dataBufferList = new ArrayList<DataBuffer>();

        // WHEN
        var actual = largeFileService.getFileByName(fileName, exchange);

        // THEN
        Object ArrayList;
        StepVerifier.create(actual)
                .recordWith(() -> dataBufferList)
                .expectNextCount(3)  // Adjust this number based on expected chunk count
                .thenConsumeWhile(dataBuffer -> {
                    int chunkSize = dataBuffer.readableByteCount();
                    assert chunkSize <= 4096;
                    return true;
                })
                .expectComplete()
                .verify();

        assertEquals("attachment; filename=\"alpha.txt\"",
                exchange.getResponse().getHeaders().getFirst("Content-Disposition"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM,
                exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    public void getFileByNameTest_Error() {

        // GIVEN
        var fileName = "testFile";
        var filePath = "files/testFile.txt";

        // WHEN
        Flux<DataBuffer> result = largeFileService.getFileByName(fileName, exchange);

        // THEN
        StepVerifier.create(result)
                .consumeErrorWith(throwable -> {
                    assert throwable instanceof NoSuchFileException;
                    assert throwable.getMessage().contains(filePath);
                })
                .verify();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, exchange.getResponse().getHeaders().getContentType());
    }
}