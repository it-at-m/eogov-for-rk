package de.muenchen.oss.dbs.fk.config;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("dbs")
@Data
@Validated
public class DbsProperties {
    @NotNull
    private List<String> internalAttachments = List.of();
    @NotNull
    private List<String> skipAttachments = List.of();
}
