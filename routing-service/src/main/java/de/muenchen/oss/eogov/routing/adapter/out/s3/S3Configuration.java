package de.muenchen.oss.eogov.routing.adapter.out.s3;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.integration.s3.properties.S3IntegrationProperties;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class S3Configuration {
    @Bean
    public MinioClient minioClient(final S3IntegrationProperties s3IntegrationProperties) {
        return MinioClient.builder()
                .endpoint(s3IntegrationProperties.getUrl())
                .credentials(s3IntegrationProperties.getAccessKey(), s3IntegrationProperties.getSecretKey())
                .build();
    }

    @Bean
    public PresignedUrlRepository presignedUrlRepository(
            final FileOperationsPresignedUrlInPort fileOperationsPresignedUrlInPort) {
        return new PresignedUrlJavaRepository(fileOperationsPresignedUrlInPort);
    }

    @Bean
    public DocumentStorageFileRepository documentStorageFileRepository(
            final PresignedUrlRepository presignedUrlRepository,
            final S3FileTransferRepository s3FileTransferRepository,
            final FileOperationsInPort fileOperationsInPort) {
        return new DocumentStorageFileJavaRepository(presignedUrlRepository,
                s3FileTransferRepository, fileOperationsInPort);
    }
}
