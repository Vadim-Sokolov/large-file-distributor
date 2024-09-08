package large.file.distributor.controller;

import large.file.distributor.service.LargeFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/download")
public class LargeFileController {

    private static final Logger logger = LoggerFactory.getLogger(LargeFileController.class);
    private final LargeFileService fileService;

    public LargeFileController(LargeFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<DataBuffer> getFileById(@PathVariable String fileName, ServerWebExchange exchange) {
        logger.info("Request from User received. Retrieving file {}", fileName);
        var result = fileService.getFileByName(fileName, exchange);
        logger.info("File {} retrieved. Sending to User. :P ", fileName);
        return result;
    }
}
