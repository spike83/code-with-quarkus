package telemetrie.schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@RegisterForReflection
public class HostBeat {
  private int ioPerSec;
  private int mips;
  private int mipsAvailable;
  private int javaDelay;
  private String stage;
  private String newStage;
  private String meta_host;
  private OffsetDateTime logCollectorTimestamp;
  private String meta_tknameid;
}
