package telemetrie.schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@RegisterForReflection
public class HealthStatusBeat {
  private String serverName;
  private String status;
  private String stage;
  private String newStage;
  private int errorCount;
  private String healthStatusType;
  private String tkNameId;
  private String meta_host;
  private OffsetDateTime logCollectorTimestamp;
  private String meta_tknameid;
}
