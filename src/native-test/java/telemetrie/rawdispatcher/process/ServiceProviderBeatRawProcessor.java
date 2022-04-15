package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.rawdispatcher.metrics.MetricsProcessor;
import io.spoud.mobi.telemetrie.schema.ServiceProviderBeat;
import io.spoud.mobi.telemetrie.utils.*;
import io.spoud.mobi.telemetrie.utils.Key.Method;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RegisterForReflection
public class ServiceProviderBeatRawProcessor extends ServiceProviderBeat implements ThrottledBeatRawProcessor<ServiceProviderBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("52", ServiceProviderBeatRawProcessor.class);
  }

  private final List<String> NORM_MEASURE_POINTS_NEW = Arrays.asList(
          "mcs-zpb-validate-backend-autocomplete-v2",
          "mcs-zpb-validate-backend-v2",
          "mcs-zpb-create-forderung-v2",
          "mcs-menu-ereignisdaten-click-v2",
          "mcs-menu-forderungen-click-v2",
          "mcs-menu-rueckstellungen-click-v2",
          "mcs-menu-zahlungen-click-v2",
          "mcs-zahlungsvorbereitung-erstelle-zahlung-click-v2",
          "mcs-menu-schadenfalldaten-click-v2",
          "mcs-menu-teilfalldaten-click-v2",
          "mcs-menu-deckungen-click-v2",
          "mcs-synchronize-update-v2",
          "mcs-zahlung-freigeben-mit-speichern-v2",
          "mcs-deckungchooser-save-v2"
  );

  private final List<String> NORM_MEASURE_POINTS_OLD = Arrays.asList(
         "performance-mcs-synchronize-update",
         "performance-mcs-menu-Teilfalldaten-click",
         "performance-mcs-menu-Notizen-click",
         "performance-mcs-menu-Zahlungen-click",
         "performance-mcs-zpb-validate-backend-je-zpb",
         "performance-mcs-menu-Forderungen-click",
         "performance-mcs-menu-Zahlungen-click-je-zpb",
         "performance-mcs-zpb-validate-backend-autocomplete",
         "performance-mcs-zpb-validate-backend-autocomplete-je-zpb",
         "performance-mcs-synchronize-update-je-zpb",
         "performance-mcs-menu-Deckungen-click",
         "performance-mcs-menu-Teilfalldaten-click-je-zpb",
         "performance-mcs-zahlung-freigeben-mit-speichern",
         "performance-mcs-zahlung-freigeben-mit-speichern-je-zpb",
         "performance-mcs-menu-Forderungen-click-je-zpb",
         "performance-mcs-zpb-create-forderung",
         "performance-mcs-zpb-create-forderung-je-zpb",
         "performance-mcs-menu-Ereignisdaten-click",
         "performance-mcs-deckungchooser-save",
         "performance-mcs-zahlungsvorbereitung-erstelle-zahlung-click",
         "performance-mcs-zahlungsvorbereitung-erstelle-zahlung-click-je-zpb",
         "performance-mcs-menu-Schadenfalldaten-click",
         "performance-mcs-menu-Rückstellungen-click"
  );

  @JsonIgnore
  private final transient TaggingProcessor taggingProcessor =
      TaggingProcessor.getInstance("/tag-mapping.csv");

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
  public String extractKey(final SpoudContext<ServiceProviderBeatRawProcessor> sc) {
    if (sc.getKey() == null || sc.getKey().isEmpty()) {
      sc.setKey(
          new Key.Builder()
              .withPart(getStage())
              .withPart(getAppIdProvider())
              .withPart(getFkNameProvider())
              .withPart(getTkNameIdProvider())
              .withPart(getAppIdConsumer())
              .withPart(getFkNameConsumer())
              .withPart(getTkNameIdConsumer())
              .withPart(getNormMeasurePoint())
              .withPart(getRole())
              .withPart(Math.random())
              .withMethod(Method.SHA256HEX)
              .build()
              .toString());
    }
    return sc.getKey();
  }

  public String extractThrottlingKey(final SpoudContext<ServiceProviderBeatRawProcessor> sc) {
    return getStage() + "/" + getNewStage() + "/" + getRole() + "/" + getCallScope() + "/" + getAppIdConsumer() + "@"
            + getFkNameIdConsumer() + "@" + getTkNameIdConsumer() + "/" + getAppIdProvider() + "@" + getFkNameIdProvider() + "@"
            + getTkNameIdProvider() + "@" + getNormMeasurePoint();
  }


  @Override
  @JsonIgnore
  public void preProcess(SpoudContext<ServiceProviderBeatRawProcessor> sc) {
    ThrottledBeatRawProcessor.super.preProcess(sc);
    if (getRole() == null || getRole().isEmpty()) {
      setRole("provider");
    }

    if (getCallScope() == null || getCallScope().isEmpty()) {
      setCallScope("LOCAL");
    }

    if (getCallMode() == null || getCallMode().isEmpty()) {
      setCallScope("SYNC");
    }

    // match and normalize strange component names including uuid's and number
    ComponentNameNormalizer.normalize(sc);
    // make sure the http method is in the measurePoint
    if (getMeasurePoint() != null && getHttpMethod() != null && !getHttpMethod().isEmpty()) {
      if (getMeasurePoint().indexOf('/') > -1) {
        String normMp =
            RestUrlNormalizer.normalize(getMeasurePoint()) + " (" + getHttpMethod() + ")";
        final int iORest = normMp.indexOf("rest/");
        if (iORest > -1) {
          normMp = normMp.substring(iORest + 4);
        }
        setNormMeasurePoint(normMp);
        setMeasurePoint(getMeasurePoint() + " (" + getHttpMethod() + ")");
      }
    } else if (getMeasurePoint() != null) {
      final String normMp = RestUrlNormalizer.normalizeSpaNav(getMeasurePoint());
      setNormMeasurePoint(normMp);
      setMeasurePoint(normMp);
      isFrontend(sc);
      log.debug("got service provider beat sc={}", sc);
    }

    compareFields();

    sc.setType("5011");
    if (getDurationMs() == null || getDurationMs() == 0) {
      setDurationMs(1);
    } else if (getDurationMs() < 0) {
      final RawDataProblem p = new RawDataProblem();
      p.setReason("negative duration");
      sc.getProblems().add(p);
    }
    annotateTags(sc);
  }

  @Override
  @JsonIgnore
  public void process(final SpoudContext<ServiceProviderBeatRawProcessor> sc,
      boolean throttlingEnabled) {
    ThrottledBeatRawProcessor.super.process(sc, throttlingEnabled);
  }

  @JsonIgnore
  public void compareFields() {
    // MT-155: Workaround for events with either of those fields.
    if (getFkNameConsumer() == null && getFkNameIdConsumer() != null) {
      setFkNameConsumer(getFkNameIdConsumer());
    }
    if (getFkNameProvider() == null && getFkNameIdProvider() != null) {
      setFkNameProvider(getFkNameIdProvider());
    }
    if (getFkNameIdConsumer() == null && getFkNameConsumer() != null) {
      setFkNameIdConsumer(getFkNameConsumer());
    }
    if (getFkNameIdProvider() == null && getFkNameProvider() != null) {
      setFkNameIdProvider(getFkNameProvider());
    }
  }

  // SHI-1274: New field to check if measurePoint is frontend or backend.
  @JsonIgnore
  private void isFrontend(final SpoudContext<ServiceProviderBeatRawProcessor> sc){
    if (getTkNameIdProvider() != null && getTkNameIdConsumer() != null) {
      if (getTkNameIdProvider().contains("service") || getTkNameIdConsumer().contains("service")){
        setFrontEnd(false);
      } else {
        setFrontEnd(true);
      }
    }
  }

  // SHI-1497: Remove outliers to seperate topic
  @Override
  public String getTargetTopic(SpoudContext<ServiceProviderBeatRawProcessor> sc, String topicPattern, String samplingTopic, String inaccurateTopic, String spamTopic) {
    if (getDurationMs() >= 1000000) {
      return spamTopic;
    }
    return ThrottledBeatRawProcessor.super.getTargetTopic(sc, topicPattern, samplingTopic, inaccurateTopic, spamTopic);
  }


  @JsonIgnore
  private void annotateTags(final SpoudContext<ServiceProviderBeatRawProcessor> sc) {
    taggingProcessor.processServiceProviderBeat(sc);
  }

  @Override
  @JsonIgnore
  public <T extends RawProcessor<ServiceProviderBeatRawProcessor>> List<SpoudContext<T>> postProcess(final SpoudContext<T> sc) {
    final List<SpoudContext<T>> list = new ArrayList<>();
    if ("lasttest".equalsIgnoreCase(sc.getEvent().getStage())) {
      MetricsProcessor.getInstance().getLoadTest().increment();
      return list;
    }
    if (sc.getTrace() != null && sc.getTrace().get(0) != null && sc.getTrace().get(0).getId() != null) {
      String mtcVersion = sc.getTrace().get(0).getId().toLowerCase();
      // previously done in beat collector, now that everything goes through it (coll & mon) we need to differentiate
      if (mtcVersion.startsWith("mtc-") && getDataProducer().equals("jes-monitoring-service")) {
        sc.setTimestamp(sc.getTimestamp().plus(getDurationMs(), ChronoUnit.MILLIS));
      }
    }
    list.add(sc);
    return list;
  }

  @Override
  @JsonIgnore
  public List<MissingField> getMissingFields() {
    final List<MissingField> missing = new ArrayList<>();
    missing.add(getMissingField("durationMs", getDurationMs()));
    missing.add(getMissingField("eventTime", getDurationMs()));
    missing.add(getMissingField("correlationId", getCorrelationId()));
    missing.add(getMissingField("stage", getStage()));

    missing.add(getMissingField("appIdProvider", getAppIdProvider()));
    missing.add(getMissingField("fkNameProvider", getFkNameProvider()));
    missing.add(getMissingField("tkNameIdProvider", getTkNameIdProvider()));
    missing.add(getMissingField("appIdConsumer", getAppIdConsumer()));
    missing.add(getMissingField("fkNameConsumer", getFkNameConsumer()));
    missing.add(getMissingField("tkNameIdConsumer", getTkNameIdConsumer()));
    missing.add(getMissingField("measurePoint", getMeasurePoint()));
    missing.add(getMissingField("role", getRole()));
    return missing;
  }

  @Override
  @JsonIgnore
  public String getDataProducer() {
    return Optional.ofNullable(getMeta_tknameid()).orElse("unknown");
  }

  @Override
  public boolean isTestFraction(final SpoudContext<ServiceProviderBeatRawProcessor> sc) {
    final String tkNameIdProvider = getTkNameIdProvider();
    final String normMeasurePoint = getNormMeasurePoint();

    try {
      if(((ServiceProviderBeat)(sc.getEvent())).getStage().startsWith("SIM-TEST")){
        return true;
      }
      // TODO: enter following condition when mcs works again: tkNameIdProvider.equals("b2e-portal-rwc") &&
      if ((NORM_MEASURE_POINTS_NEW.contains(normMeasurePoint)) ||
        NORM_MEASURE_POINTS_OLD.contains((normMeasurePoint))) {
        return true;
      }
    } catch (final Exception e) {
      log.warn("invalid normMeasurePoint: {} or tkNameIdProvider: {}", normMeasurePoint, tkNameIdProvider);
    }
    return ThrottledBeatRawProcessor.super.isTestFraction(sc) || extractKey(sc).hashCode() % 1000 == 521;
  }
}
