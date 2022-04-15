package telemetrie.schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@RegisterForReflection
public class AppRuntimeBeat {
  private String tkNameId;
  private String stage;
  private String newStage;
  private OffsetDateTime initTimestamp;
  private String hostName;
  private String osName;
  private String lang;
  private String langSpecVersion;
  private String langVersion;
  private String runtimeEnvironment;
  private String runtimeEnvironmentVersion;
  private String appServer;
  private String stackName;
  private String stackVersion;
  private String meta_host;
  private OffsetDateTime logCollectorTimestamp;
  private String meta_tknameid;
}
