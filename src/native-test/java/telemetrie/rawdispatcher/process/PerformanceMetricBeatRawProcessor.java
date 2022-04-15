package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.PerformanceMetricBeat;
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
public class PerformanceMetricBeatRawProcessor extends PerformanceMetricBeat
    implements RawProcessor<PerformanceMetricBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("66", PerformanceMetricBeatRawProcessor.class);
  }

  @Getter
  @JsonProperty(access = Access.WRITE_ONLY)
  public OffsetDateTime eventTime;

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
  public String extractKey(final SpoudContext<PerformanceMetricBeatRawProcessor> sc) {
    return new Key.Builder()
        .withPart(getStage())
        .withPart(getUserSessionId())
        .withPart(getTkNameId())
        .withPart(getMeasurePoint())
        .withMethod(Method.SHA256HEX)
        .build()
        .toString();
  }

  @Override
  @JsonIgnore
  public void process(final SpoudContext<PerformanceMetricBeatRawProcessor> sc,
      boolean throttlingEnabled) {
    RawProcessor.super.process(sc, throttlingEnabled);
  }

  @Override
  @JsonIgnore
  public List<MissingField> getMissingFields() {
    final List<MissingField> missing = new ArrayList<>();
    missing.add(getMissingField("metricName", getMetricName()));
    missing.add(getMissingField("metricValue", getValue()));
    return missing;
  }

  @Override
  @JsonIgnore
  public String getDataProducer() {
    return Optional.ofNullable(getMeta_tknameid()).orElse("unknown");
  }
}