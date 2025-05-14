package de.muenchen.oss.eogov.routing.adapter.out.s3;

import de.muenchen.oss.eogov.routing.application.port.out.PresignedUrlOutPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.properties.S3IntegrationProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("eogovS3Adapter")
@RequiredArgsConstructor
@Slf4j
public class S3Adapter implements PresignedUrlOutPort {
    public static final int PART_SIZE = 5 * 1024 * 1024;
    private final DocumentStorageFileRepository fileRepository;
    private final PresignedUrlRepository presignedUrlRepository;
    private final MinioClient minioClient;
    final S3IntegrationProperties s3IntegrationProperties;
    private final S3Properties s3Properties;

    @Override
    public String createFileAndCreatePresignedUrl(final String path, final InputStream content) {
        this.putObject(path, content);
        return this.getPresignedUrl(path);
    }

    protected String getPresignedUrl(final String key) {
        log.debug("S3: Creating presigned URL for object {}", key);
        try {
            return this.presignedUrlRepository.getPresignedUrlGetFile(key, s3Properties.getPresignedUrlLifetimeMinutes());
        } catch (DocumentStorageClientErrorException | DocumentStorageServerErrorException | DocumentStorageException e) {
            throw new RuntimeException(e);
        }
    }

    protected void putObject(final String key, final InputStream content) {
        log.debug("S3: Putting object to {}", key);
        final PutObjectArgs request = PutObjectArgs.builder()
                .bucket(s3IntegrationProperties.getBucketName())
                .object(key)
                .stream(content, -1, PART_SIZE)
                .build();
        try {
            minioClient.putObject(request);
            log.info("S3: Put object to {}", key);
        } catch (final MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
