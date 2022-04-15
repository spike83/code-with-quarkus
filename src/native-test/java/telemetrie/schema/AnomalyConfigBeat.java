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
public class AnomalyConfigBeat {

  private OffsetDateTime eventTime;

  private String stage;

  private String tkNameId;

  private String measurePoint;

  private boolean enabled;
}
