package telemetrie.rawdispatcher.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.spoud.mobi.telemetrie.rawdispatcher.process.RawProcessor;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@ApplicationScoped
public class MetricsProcessor {

  private final Counter nullSkippedCounter = Metrics.counter("kafka-raw-dispatcher_null-skipped");
  private final Counter batchSkippedFilteredCounter =
      Metrics.counter("kafka-raw-dispatcher_batch-skipped-filtered");
  @Getter
  private final Counter loadTest =
      Metrics.counter("kafka-raw-dispatcher_loadtest_event");

  private static MetricsProcessor instance;
  public static MetricsProcessor getInstance(){
    if(instance == null){
      instance = new MetricsProcessor();
    }
    return instance;
  }

  private MetricsProcessor() {
    new ClassLoaderMetrics().bindTo(Metrics.globalRegistry);
    new JvmMemoryMetrics().bindTo(Metrics.globalRegistry);
    new JvmGcMetrics().bindTo(Metrics.globalRegistry);
    new ProcessorMetrics().bindTo(Metrics.globalRegistry);
    new JvmThreadMetrics().bindTo(Metrics.globalRegistry);
    new ProcessorMetrics().bindTo(Metrics.globalRegistry);
  }

  public <T extends RawProcessor> void updateMetrics(final SpoudContext<T> sc) {
    // count events per type
    Metrics.counter("kafka-raw-dispatcher_events_pertype", sc.getEvent().getTags(sc))
        .increment(sc.getNumberOfEvents());
    // count size
    Metrics.counter("kafka-raw-dispatcher_events_bytesize", sc.getEvent().getTags(sc))
        .increment(sc.getSizeInBytes());

    // histogram of time drifts
    if (sc.getTimestamp() != null) {
      Metrics.summary("kafka-raw-dispatcher_timedrift_distribution", sc.getEvent().getTags(sc))
          .record(OffsetDateTime.now().until(sc.getTimestamp(), ChronoUnit.MILLIS));
    }

    if (!sc.getProblems().isEmpty()) {
      sc.getProblems()
          .forEach(
              p -> {
                Metrics.counter(
                        "kafka-raw-dispatcher_events_problem_" + p.reason,
                        sc.getEvent().getTags(sc))
                    .increment();
              });
    }
  }

  public void updateRawMetrics(final String type) {}

  public void nullSkipped() {
    nullSkippedCounter.increment();
  }

  public void batchSkippedFiltered() {
    batchSkippedFilteredCounter.increment();
  }
}
