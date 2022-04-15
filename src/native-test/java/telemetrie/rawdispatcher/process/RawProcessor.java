package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.utils.MissingField;
import io.spoud.mobi.telemetrie.utils.RawDataProblem;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface provides methods which have to be implemented on the Raw Event type extensions in order
 * to do the postprocessing.
 */
@RegisterForReflection
public interface RawProcessor<T> {

  Logger log = LoggerFactory.getLogger(RawProcessor.class);
  String STAGE_NOT_APPLICABLE = "NOT_APPLICABLE_STAGE";

  AtomicInteger tsPastCounter = new AtomicInteger(0);

  default void preProcess(final SpoudContext<T> sc) {}

  /**
   * Operation which is called just after deserialization.
   *
   * @param sc SpoudContext the object lives in.
   * @param throttlingEnabled
   */
  default void process(final SpoudContext<T> sc, boolean throttlingEnabled) {
    OffsetDateTime timestamp = extractTimestamp(sc.getTimestamp());
    if (timestamp == null) {
      timestamp = sc.getTimestamp();
      final RawDataProblem problem = new RawDataProblem();
      problem.setReason("timestamp_extractor_null");
      sc.getProblems().add(problem);
      if (sc.getTimestamp() == null) {
        sc.setTimestamp(OffsetDateTime.now());
        final RawDataProblem problem2 = new RawDataProblem();
        problem.setReason("timestamp_is_null");
        sc.getProblems().add(problem2);
        log.error("timestamp is null {}", sc);
        timestamp = OffsetDateTime.now();
      }
    }
    checkStage(sc);
    sc.setTimestamp(timestamp.toInstant().atOffset(ZoneOffset.UTC));
    validateTimestamp(sc);
  }

  default void checkStage(final SpoudContext<T> sc) {
    // check stage n/a and define fraction
    String stage = getStage();
    if (stage == null || stage.equals("")) {
      final List<MissingField> missing = new ArrayList<>();
      missing.add(new MissingField("stage"));
      final RawDataProblem p = new RawDataProblem();
      p.setMissingFields(missing);
      p.setReason("missing_fields");
      sc.getProblems().add(p);
      return;
    }
    // TODO: convert to lower case once new names are in place
    stage = stage.toUpperCase();
    setStage(stage);

    if (!stage.equals("n/a") && !stage.equals(STAGE_NOT_APPLICABLE)) {
      if (!Arrays.asList("P", "PROD").contains(stage)) {
        setFraction("-np");
      }
    } else if (stage.equals(STAGE_NOT_APPLICABLE)) {
      // for some beats we have no stage and return "NOT APPLICABLE" there.
      // we do not set fraction or add a problem for such cases.
    }

    setNewStage(convertToNewStage(stage));

  }

  @JsonProperty(access = Access.READ_ONLY)
  default void setNewStage(final String newStage) {}

  default String convertToNewStage(String stage) {
    if(stage == null){
      return "null";
    }
    stage = stage.toLowerCase();
    if ("p".equals(stage) || "prod".equals(stage)) {
      return "prod P"; // Production
    }
    if ("t".equals(stage) || "preprod".equals(stage)) {
      return "preprod T"; // Production-like environment for user acceptance tests and system
    }
                          // integration tests
    if ("z".equals(stage) || "performance".equals(stage)) {
      return "performance Z"; // Large scale integrated performance testing with large amounts of
    }
                              // data. Similar or greater load than production.
    if ("i".equals(stage) || "integration".equals(stage)) {
      return "integration I"; // Conventional release integration testing environment.
    }
    if ("b".equals(stage) || "test".equals(stage)) {
      return "test B"; // System integration testing environment without firewalls
    }
    if ("v".equals(stage) || "timetravel".equals(stage)) {
      return "timetravel V"; // Integration testing that needs to move the system time forward to
    }
                             // test processes like end of year accounts.
    if ("w".equals(stage) || "development".equals(stage)) {
      return "development W"; // First deployment environment for technologies that can not use OCI
    }

    if ("s".equals(stage) || "testlab".equals(stage)) {
      return "testlab S"; // Special closed off Testlab in dev.
    }
                              // (Docker) containers. No integration, for development teams to run
                              // their software.
    return stage;
  }

  /**
   * Extract a key to be used to assign partition.
   *
   * @param sc Spoud context.
   * @return string representation of the key.
   */
  default String extractKey(final SpoudContext<T> sc) {
    return null;
  }

  default void validateTimestamp(final SpoudContext<T> sc) {
    // ist the timestamp in future?
    if (sc.getTimestamp().isAfter(OffsetDateTime.now().plus(5, ChronoUnit.SECONDS))) {
      final RawDataProblem problem = new RawDataProblem();
      problem.setReason("timestamp_in_future");
      sc.getProblems().add(problem);
    } else if (sc.getTimestamp().isBefore(OffsetDateTime.now().minus(5, ChronoUnit.MINUTES))) {
      if(tsPastCounter.incrementAndGet() % 10000 == 0){
        log.warn("10000 timestamps were in past {}", sc.getTimestamp());
        tsPastCounter.set(0);
      }
    }
  }

  /**
   * Extract the timestamp from the event.
   *
   * @param batchEventTime timestamp on the batch event.
   * @return the eventtime of the event.
   */
  default OffsetDateTime extractTimestamp(final OffsetDateTime batchEventTime) {
    return batchEventTime;
  }

  /**
   * Get the missing field for a value.
   *
   * @return true if the element was valid.
   */
  default MissingField getMissingField(final String fieldName, final Object value) {
    if (value == null) {
      return new MissingField(fieldName);
    }
    return null;
  }

  /**
   * Get the missing fields of the event.
   *
   * @return true if the element was valid.
   */
  default List<MissingField> getMissingFields() {
    return Collections.emptyList();
  }

  /**
   * Check if the event is valid against several check which can be triggered.
   *
   * @param sc SpoudContext the object lives in.
   * @return true if the element was valid.
   */
  default boolean isValid(final SpoudContext<T> sc) {
    boolean isValid = true;

    final List<MissingField> missingFields = getMissingFields();
    missingFields.removeAll(Collections.singleton(null));

    if (!missingFields.isEmpty()) {
      final RawDataProblem p = new RawDataProblem();
      p.setMissingFields(missingFields);
      p.setReason("missing_fields");
      sc.getProblems().add(p);
      isValid = false;
    }
    if (!sc.getProblems().isEmpty()) {
      isValid = false;
    }
    return isValid;
  }

  /**
   * Determine the topic to write this event to.
   *
   * @param sc SpoudContext the object lives in.
   * @param topicPattern Pattern string to fill in.
   * @return name of the topic.
   */
  default String getTargetTopic(final SpoudContext<T> sc, String topicPattern, String samplingTopic, String inaccurateTopic, String spamTopic) {
    if (!isValid(sc)) {
      topicPattern = inaccurateTopic;
    }
    return topicPattern
        .replace(":dataGroup", sc.getGroup())
        .replace(":eventType", sc.getType())
        .replace(":fraction", getFraction());
  }

  /**
   * Do things after processing like exposing metrics.
   *
   * @param sc
   */
  default <S extends RawProcessor<T>> List<SpoudContext<S>> postProcess(final SpoudContext<S> sc) {
    return Collections.singletonList(sc);
  }

  default <S extends RawProcessor<T>> String[] getTags(final SpoudContext<S> sc) {
    return new String[] {
      "group", Optional.ofNullable(sc.getGroup()).orElse("none"),
      "type", Optional.ofNullable(sc.getType()).orElse("none"),
      "topic", Optional.ofNullable(sc.getTopic()).orElse("none"),
      "stage", Optional.ofNullable(sc.getEvent().getStage()).orElse("none"),
      "dataProducer", Optional.ofNullable(sc.getEvent().getDataProducer()).orElse("none"),
    };
  }

  /**
   * When a event type has to be split into fractions then here we define the name of the fraction
   * this event belongs to.
   *
   * @return true if the element is selected for test fraction.
   */
  String getFraction();

  void setFraction(String s);

  @JsonIgnore
  default boolean isTestFraction(final SpoudContext<T> sc) {
    try {
      final String correlation = getCorrelationId();
      if (correlation == null || correlation.isEmpty() || correlation.length() < 4 ||
              correlation.equals("emptyString") || correlation.equals("unknown")) {
        return false;
      }
      if("SIM-TEST".equals(getStage()) || "SPOUD-TEST".equals(getStage())){
        return true;
      }
      return Long.parseLong(correlation.substring(correlation.length() - 4), 16) % 1000 == 521;
    } catch (final Exception e) {
      log.warn("invalid correlation id {} {}", getCorrelationId(), e.getLocalizedMessage());
    }
    return false;
  }

  @JsonIgnore
  String getDataProducer();

  /**
   * Retreive the value of the stage. Return "NOT APPLICABLE" if the stage is not applicable.
   *
   * @return
   */
  String getStage();

  /**
   * Set the stage value.
   *
   * @param stage
   */
  void setStage(String stage);

  String getCorrelationId();
}
