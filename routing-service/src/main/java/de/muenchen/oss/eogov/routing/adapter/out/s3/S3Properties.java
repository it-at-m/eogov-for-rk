package de.muenchen.oss.eogov.routing.adapter.out.s3;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eogov.s3")
@Getter
@Setter
public class S3Properties {
    @NotNull
    private Duration presignedUrlLifetime;

    public int getPresignedUrlLifetimeMinutes() {
        return (int) this.presignedUrlLifetime.toMinutes();
    }
}
