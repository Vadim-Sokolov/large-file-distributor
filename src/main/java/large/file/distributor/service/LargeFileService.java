package large.file.distributor.service;

import large.file.distributor.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class LargeFileService {

    private static final Logger logger = LoggerFactory.getLogger(LargeFileService.class);
    private static final String TXT = ".txt";
    private final FileRepository fileRepository;

    public LargeFileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Flux<DataBuffer> getFileByName(String fileName, ServerWebExchange exchange) {
        logger.info("Request from Controller received. Retrieving file {}", fileName);
        var fileNameWithExtension = fileName + TXT;
        return fileRepository.getFileByName(fileNameWithExtension)
                .flatMapMany(path -> {
                    if (path == null) {
                        return Flux.error(new RuntimeException("Path is null"));
                    }
                    exchange.getResponse().getHeaders().add("Content-Disposition", "attachment; filename=\"" + fileNameWithExtension + "\"");
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    exchange.getResponse().getHeaders().forEach((key, value) -> logger.info(key + ": " + value));
                    return DataBufferUtils.read(path, exchange.getResponse().bufferFactory(), 4096);
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnError(throwable -> {
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(
                            ("This is my rifle, this is my gun \nFile not found \n" + throwable.getMessage()).getBytes());
                    exchange.getResponse().writeWith(Mono.just(buffer)).subscribe();
                });
    }
}
