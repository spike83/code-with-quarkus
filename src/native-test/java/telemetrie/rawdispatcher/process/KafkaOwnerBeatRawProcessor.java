package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.KafkaOwnerBeat;
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
public class KafkaOwnerBeatRawProcessor extends KafkaOwnerBeat implements RawProcessor<KafkaOwnerBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("65", KafkaOwnerBeatRawProcessor.class);
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
  public String extractKey(final SpoudContext<KafkaOwnerBeatRawProcessor> sc) {
    return new Key.Builder()
        .withPart(getCaller())
        .withPart(getGroupId())
        .withPart(getCluster())
        .withMethod(Method.SHA256HEX)
        .build()
        .toString();
  }

  @Override
  @JsonIgnore
  public void process(final SpoudContext<KafkaOwnerBeatRawProcessor> sc, boolean throttlingEnabled) {
    RawProcessor.super.process(sc, throttlingEnabled);
    normalizeStageCaller();
  }

  @Override
  @JsonIgnore
  public List<MissingField> getMissingFields() {
    final List<MissingField> missing = new ArrayList<>();
    missing.add(getMissingField("cluster", getCluster()));
    missing.add(getMissingField("caller", getCaller()));
    missing.add(getMissingField("stageCaller", getStageCaller()));
    return missing;
  }

  @Override
  @JsonIgnore
  public String getDataProducer() {
    return Optional.ofNullable(getMeta_tknameid()).orElse("unknown");
  }

  @JsonIgnore
  public void normalizeStageCaller() {
    final String stageCaller = getStageCaller();
    if (stageCaller != null) {
      setStageCaller(stageCaller.toUpperCase());
    }
  }

  @Override
  public String getStage() {
    return null;
  }

  @Override
  public void setStage(final String stage) {
    // no stage in beat
  }

  @Override
  public void checkStage(final SpoudContext<KafkaOwnerBeatRawProcessor> sc) {
    // no stage in beat
  }

  @Override
  @JsonIgnore
  public String getCorrelationId() {
    return null;
  }
}
