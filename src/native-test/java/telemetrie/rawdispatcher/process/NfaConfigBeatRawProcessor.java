package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.NfaConfigBeat;
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

@ApplicationScoped
@RegisterForReflection
public class NfaConfigBeatRawProcessor extends NfaConfigBeat implements RawProcessor<NfaConfigBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("68", NfaConfigBeatRawProcessor.class);
  }

  @Getter
  @Setter
  @JsonProperty(access = Access.WRITE_ONLY)
  public String fraction = "";

  @Override
  @JsonIgnore
  public OffsetDateTime extractTimestamp(final OffsetDateTime eventTime) {
    return getEventTime();
  }

  @Override
  @JsonIgnore
  public String extractKey(final SpoudContext<NfaConfigBeatRawProcessor> sc) {
    return new Key.Builder()
        .withPart(getStage())
        .withPart(getTkNameId())
        .withPart(getMeasurePoint())
        .withMethod(Method.SHA256HEX)
        .build()
        .toString();
  }

  @Override
  @JsonIgnore
  public void process(final SpoudContext<NfaConfigBeatRawProcessor> sc, boolean throttlingEnabled) {
    RawProcessor.super.process(sc, throttlingEnabled);
  }

  @Override
  @JsonIgnore
  public List<MissingField> getMissingFields() {
    final List<MissingField> missing = new ArrayList<>();
    missing.add(getMissingField("stage", getStage()));
    missing.add(getMissingField("tkNameId", getTkNameId()));
    missing.add(getMissingField("measurePoint", getMeasurePoint()));
    missing.add(getMissingField("enabled", isEnabled()));

    return missing;
  }

  @Override
  @JsonIgnore
  public String getDataProducer() {
    return null;
  }

  @Override
  @JsonIgnore
  public String getCorrelationId() {
    return null;
  }
}
