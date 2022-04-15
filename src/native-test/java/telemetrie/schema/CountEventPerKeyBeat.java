package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class CountEventPerKeyBeat {
    private String key;

    protected OffsetDateTime processingTime;

    private Integer count;

    private String samplingRule;

    private String topic;
}
