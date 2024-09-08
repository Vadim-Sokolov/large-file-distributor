package large.file.distributor.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.nio.file.NoSuchFileException;

@Repository
public class FileRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileRepository.class);

    public Mono<Resource> getFileByName(String fileName) {
        logger.info("Request from Service received. Retrieving file {}", fileName);
        var filePath = "files/" + fileName;
        var resource = new ClassPathResource(filePath);
        if (!resource.exists()) {
            return Mono.error(new NoSuchFileException(filePath));
        }
        return Mono.just(resource);
    }
}
