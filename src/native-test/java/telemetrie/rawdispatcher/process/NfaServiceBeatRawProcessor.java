package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.NfaServiceBeat;
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
public class NfaServiceBeatRawProcessor extends NfaServiceBeat implements RawProcessor<NfaServiceBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("62", NfaServiceBeatRawProcessor.class);
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
  public String extractKey(final SpoudContext<NfaServiceBeatRawProcessor> sc) {
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
  public void preProcess(final SpoudContext<NfaServiceBeatRawProcessor> sc) {
    RawProcessor.super.preProcess(sc);
    // make sure the http method is in the measurePoint
    if (getHttpMethod() != null && !getHttpMethod().isEmpty()) {
      if (getMeasurePoint().indexOf('/') > -1) {
        setMeasurePoint(getMeasurePoint() + " (" + getHttpMethod() + ")");
      }
    }
    if (getPerformanceIndexBaseline() == 0) {
      setPerformanceIndexBaseline(getResponseTimeP95());
      setBaselineSource("NFA-P95");
    } else {
      setBaselineSource("NFA-CONFIG");
    }
  }

  @Override
  @JsonIgnore
  public void process(final SpoudContext<NfaServiceBeatRawProcessor> sc, boolean throttlingEnabled) {
    RawProcessor.super.process(sc, throttlingEnabled);
    // SHI-1275: we don't add the nfa-monitor tag in stream-62, it should be a self-service
    // It will be added and managed on Mobi's side on stream-5011 itself
    // If it has the tags then we will alert with nfa (see @flink) otherwise we do nothing
  }

  @Override
  @JsonIgnore
  public <T extends RawProcessor<NfaServiceBeatRawProcessor>> List<SpoudContext<T>> postProcess(final SpoudContext<T> sc) {
    final List<SpoudContext<T>> list = new ArrayList<>();
    list.add(sc);
    final NfaServiceBeatRawProcessor event = (NfaServiceBeatRawProcessor) sc.getEvent();

    setFraction("-np");
    if (sc.getTrace() != null
        && sc.getTrace().get(0) != null
        && sc.getTrace().get(0).getId() != null) {
      final String mtcVersion = sc.getTrace().get(0).getId().toLowerCase();
      if (mtcVersion.startsWith("mtc-")
          && (mtcVersion.contains("p") || mtcVersion.contains("prod"))) {
        setFraction("");
      }
    }

    if (event.getResource() != null && !event.getResource().isEmpty()) {
      if (!event.getResource().startsWith("/")) {
        event.setResource("/" + event.getResource());
      }
      final SpoudContext<NfaServiceBeatRawProcessor> sc1 =
          (SpoudContext<NfaServiceBeatRawProcessor>) sc.copy();
      list.add((SpoudContext<T>) sc1);
      sc1.getEvent()
          .setMeasurePoint(
              sc1.getEvent().getResource() + " (" + sc1.getEvent().getHttpMethod() + ")");
      sc1.setId(sc1.getId() + "_res");
    }
    return list;
  }

  @Override
  @JsonIgnore
  public List<MissingField> getMissingFields() {
    final List<MissingField> missing = new ArrayList<>();
    missing.add(getMissingField("tkNameId", getTkNameId()));
    missing.add(getMissingField("measurePoint", getMeasurePoint()));
    return missing;
  }

  @Override
  @JsonIgnore
  public String getDataProducer() {
    return Optional.ofNullable(getMeta_tknameid()).orElse("unknown");
  }

  @Override
  @JsonIgnore
  public String getStage() {
    return STAGE_NOT_APPLICABLE;
  }

  @Override
  public void setStage(final String stage) {
    // no stage in this beat
  }

  @Override
  @JsonIgnore
  public String getCorrelationId() {
    return null;
  }
}
