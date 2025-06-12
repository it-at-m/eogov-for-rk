package de.muenchen.oss.eogov.routing.domain.mapper;

import de.cit.xmlns.intelliform._2009.webservices.backend.DepositData;
import de.cit.xmlns.intelliform._2009.webservices.backend.Entry;
import de.muenchen.oss.eogov.routing.domain.model.Attachment;
import de.muenchen.oss.eogov.routing.domain.model.Message;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper
public abstract class MessageMapper {
    @Mapping(source = "attachments", target = "attachments")
    public abstract Message map(DepositData data, List<Attachment> attachments);

    @Mapping(source = "presignedUrl", target = "presignedUrl")
    public abstract Attachment map(de.cit.xmlns.intelliform._2009.webservices.backend.Attachment attachment, String presignedUrl);

    protected Map<String, String> mapKeyValue(final List<Entry> customParameters) {
        return customParameters.stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
