package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.DB2QueryBeat;
import io.spoud.mobi.telemetrie.utils.Key;
import io.spoud.mobi.telemetrie.utils.Key.Method;
import io.spoud.mobi.telemetrie.utils.MissingField;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RegisterForReflection
public class DB2QueryBeatRawProcessor extends DB2QueryBeat implements RawProcessor<DB2QueryBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("67", DB2QueryBeatRawProcessor.class);
  }

  @Getter
  @Setter
  @JsonProperty(access = Access.WRITE_ONLY)
  public String fraction = "";

  @Override
  @JsonIgnore
  public OffsetDateTime extractTimestamp(final OffsetDateTime batchEventTime) {
    return getEventTime();
  }

  @Override
  @JsonIgnore
  public String extractKey(final SpoudContext<DB2QueryBeatRawProcessor> sc) {
    return new Key.Builder()
        .withPart(getStage())
        .withPart(getApplicationName())
        .withPart(getTechUser())
        .withPart(getMeta_tknameid())
        .withMethod(Method.SHA256HEX)
        .build()
        .toString();
  }

  @Override
  @JsonIgnore
  public void process(final SpoudContext<DB2QueryBeatRawProcessor> sc, boolean throttlingEnabled) {
    RawProcessor.super.process(sc, throttlingEnabled);
  }

  @Override
  @JsonIgnore
  public List<MissingField> getMissingFields() {
    final List<MissingField> missing = new ArrayList<>();
    missing.add(getMissingField("stage", getStage()));
    missing.add(getMissingField("applicationName", getApplicationName()));
    missing.add(getMissingField("techUser", getTechUser()));
    return missing;
  }

  @Override
  @JsonIgnore
  public String getDataProducer() {
    return Optional.ofNullable(getMeta_tknameid()).orElse("unknown");
  }

  @Override
  @JsonIgnore
  public String getCorrelationId() {
    return null;
  }

}
