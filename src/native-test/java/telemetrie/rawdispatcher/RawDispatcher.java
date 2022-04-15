package telemetrie.rawdispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecordMetadata;
import io.spoud.mobi.telemetrie.rawdispatcher.exception.UnprocessableEntityException;
import io.spoud.mobi.telemetrie.rawdispatcher.metrics.MetricsProcessor;
import io.spoud.mobi.telemetrie.rawdispatcher.process.CountEventPerKeyBeatRawProcessor;
import io.spoud.mobi.telemetrie.rawdispatcher.process.RawProcessor;
import io.spoud.mobi.telemetrie.utils.JsonOm;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class RawDispatcher
    implements ControlMessageProcessor<String, SpoudContext<CountEventPerKeyBeatRawProcessor>>,
    IngestlMessageProcessor<String, SpoudContext<ObjectNode[]>>{

  private boolean firstElement = true;
  private static final int BATCH_COUNT_SIZE = 1000;
  public static Map<String, Class<? extends RawProcessor>> processorsRegistry = new HashMap<>();
  private static int callCounter = 0;
  private static AtomicInteger batchCounter = new AtomicInteger(0);
  private static long timestamp = Instant.now().toEpochMilli();
  @Inject
  MetricsProcessor metricsProcessor;
  @Inject
  Configuration configuration;
  @Inject @Channel("destination")
  @OnOverflow(value = Strategy.BUFFER)
  Emitter<SpoudContext<?>> rdEmitter;


  void onStart(@Observes final StartupEvent ev) {
    log.info("Starting up raw-dispatcher");

    if (log.isDebugEnabled()) {
      // pretty print
      try {
        final String json =
            JsonOm.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(configuration);
        log.debug(json);
      } catch (final JsonProcessingException e) {
        log.error("cannot dump config", e);
      }
    }
  }



  @Override
  public CompletableFuture<Void> processControlMessage(
      final IncomingKafkaRecord<String, SpoudContext<CountEventPerKeyBeatRawProcessor>> r){
    if (r == null || r.getPayload() == null) {
      metricsProcessor
          .nullSkipped();
      return CompletableFuture.completedFuture(null);
    }
    r.getPayload().getEvent().process(r.getPayload(), configuration.isThrottling());

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> processIngestMessage(final IncomingKafkaRecord<String, SpoudContext<ObjectNode[]>> r) {
    if(firstElement && configuration.isThrottling()){
      try {
        log.info("first element sleep {}ms", configuration.getInitialDelayMs());
        Thread.sleep(configuration.getInitialDelayMs());
        firstElement = false;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    logCalls();
    if (r == null || r.getPayload() == null) {
      metricsProcessor.nullSkipped();
      return CompletableFuture.completedFuture(null);
    }

    if (!configuration.getEventTypes().contains("*") && !configuration.getEventTypes().contains(r.getPayload().getType())) {
      metricsProcessor.batchSkippedFiltered();
      return CompletableFuture.completedFuture(null);
    }

    final List<CompletableFuture<Void>> results = new ArrayList<>();
    final SpoudContext<ObjectNode[]> value = r.getPayload();
    value.setTopic(r.getTopic());
    value.setPartition(r.getPartition());
    value.setOffset(r.getOffset());

    final Class<? extends RawProcessor> proc = processorsRegistry.get(value.getType());
    if (proc != null) {
      sendEvents(proc, results, value);
    } else {
      log.error(
          "Unknown type ignored type: {}, path: {}, value {}",
          value.getType(),
          value.getPath(),
          value);
    }

    // combine the results into one
    if (results.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    return CompletableFuture.allOf(results.toArray(new CompletableFuture[] {}));
  }

  private static void logCalls() {
    if (++callCounter % BATCH_COUNT_SIZE == 0) {
      batchCounter.incrementAndGet();
      final long newTimestamp = Instant.now().toEpochMilli();
      long time = (newTimestamp - timestamp);
      if(time == 0) time =1;
      log.info(
          (batchCounter.get() * BATCH_COUNT_SIZE)
              + " batches, "
              + BATCH_COUNT_SIZE / (time / 1000.0)
              + " batch/s");
      callCounter = 0;
      timestamp = newTimestamp;
    }
  }

  private <T extends RawProcessor<T>> void sendEvents(
      final Class<T> clazz,
      final List<CompletableFuture<Void>> results,
      final SpoudContext<ObjectNode[]> value) {
    final ObjectNode[] nodes = value.getEvent();

    if(nodes == null){
      log.error("found event batch without events!!");
      SpoudContext<String> sc = new SpoudContext<>();
      sc.setEvent(value.toString());
      sc.setTimestamp(OffsetDateTime.now());
      Message<SpoudContext<String>> m = getMessage(sc, configuration.getSpamTopic());
      results.add(m.ack().toCompletableFuture());
      rdEmitter.send(m);
      return;
    }
    int id = 0;
    for (final ObjectNode node : nodes) {
      final SpoudContext<T> scTemplate = new SpoudContext<>();
      scTemplate.adaptContext(value);
      scTemplate.setSizeInBytes(value.getSizeInBytes() / value.getNumberOfEvents());
      scTemplate.setNumberOfEvents(1);
      scTemplate.setId(value.getId() + "_" + id++);
      scTemplate.addVersioningTrace(value.getType(), "krd", "2");

      String topicPattern = configuration.getKafkaProducerTopicTemplate();
      String samplingTopic = configuration.getSamplingTopic();
      String inaccurateTopic = configuration.getInaccurateTopic();
      String spamTopic = configuration.getSpamTopic();
      try {
        for (final SpoudContext<T> sc : fillSpoudContextFromNode(clazz, node, scTemplate)) {
          String topic = sc.getEvent().getTargetTopic(sc, topicPattern, samplingTopic, inaccurateTopic, spamTopic);
          if (sc.getEvent().getStage() == null
              || !sc.getEvent().getStage().startsWith("SIM-TEST")) {
            Message<SpoudContext<T>> m = getMessage(sc, topic);
            results.add(m.ack().toCompletableFuture());
            rdEmitter.send(m);
          }

          // send event to test topics when it's part of the test fraction
          if (configuration != null
              && configuration.isKafkaProducerTestFraction()
              && sc.getEvent().isTestFraction(sc)
              && sc.getEvent().isValid(sc)) {
            try {
              final SpoudContext<T> scTest = sc.copy();
              scTest.getEvent().setFraction("-test");
              scTest.getEvent().setStage("SPOUD-TEST");
              scTest.getEvent().setNewStage("SPOUD-TEST");
              scTest.setId(sc.getId() + "_test");
              scTest.setKey(sc.getKey() + "_test");
              topic = scTest.getEvent().getTargetTopic(scTest, topicPattern, samplingTopic, inaccurateTopic, spamTopic);
              Message<SpoudContext<T>> m = getMessage(scTest, topic);
              results.add(m.ack().toCompletableFuture());
              rdEmitter.send(m);
            } catch (final Exception e) {
              log.error("error creating test fraction event", e);
              log.error("error creating test fraction event: {}", sc);
            }
          }
        }
      } catch (final ConcurrentModificationException u) {
        System.err.println("ccme");
      } catch (final UnprocessableEntityException u) {
        try {
          // TODO: handle json parse errors
          log.debug("logging spam for", u);
          SpoudContext<String> sc = new SpoudContext<>();
          sc.setEvent(u.toString());
          sc.setTimestamp(OffsetDateTime.now());
          Message<SpoudContext<String>> m = getMessage(sc, configuration.getSpamTopic());
          results.add(m.ack().toCompletableFuture());
          rdEmitter.send(m);
        } catch (final Exception e) {
          // do nothing we just try to document the issue...
          log.debug("exception logging spam", e);
        }
      }
    }
  }

  public <T> Message<SpoudContext<T>> getMessage(SpoudContext<T> sc, String topic){
    Message<SpoudContext<T>> message = Message.of(sc);
    message = message.addMetadata(OutgoingKafkaRecordMetadata.builder().withTopic(topic).withKey(sc.getKey()).withTimestamp(sc.getTimestamp().toInstant()).build());
    return message;
  }

  /**
   * @param <T> Type of RawProcessor
   * @param clazz class of the RawProcessor to be used to deserialize.
   * @param node ObjectNoede of the object to deserialize.
   * @param sc SpoudContext to put the event on.
   * @throws UnprocessableEntityException when json is not parsable
   * @return list of spoud contexts
   */
  public  <T extends RawProcessor<T>> List<SpoudContext<T>> fillSpoudContextFromNode(
      final Class<T> clazz, final ObjectNode node, final SpoudContext<T> sc) throws UnprocessableEntityException {
    try {
      sc.setEvent(JsonOm.getMapper().readerFor(clazz).readValue(node));
      sc.getEvent().preProcess(sc);
      sc.getEvent().process(sc, configuration.isThrottling());
      final List<SpoudContext<T>> scList = sc.getEvent().postProcess(sc);
      metricsProcessor.updateMetrics(sc);
      scList.forEach(s -> s.setKey(s.getEvent().extractKey(s)));
      final List<String> ids = scList.stream().map(SpoudContext::getId).collect(Collectors.toList());
      if (ids.size() != scList.size()) {
        log.error(
            "Something is wrong with id generation. Multiple events have the same id. They will probably be erased.");
      }
      final List<String> keys = scList.stream().map(SpoudContext::getKey).collect(Collectors.toList());
      if (keys.size() != scList.size()) {
        log.error(
            "Something is wrong with key generation. Multiple events have the same key. They will probably be erased.");
      }
      return scList;
    } catch (final InvalidFormatException e) {
      log.error(
          "InvalidFormatException happen when parsing single event to type: {} msg: {} ",
          clazz.getName(),
          e.getOriginalMessage());
      throw new UnprocessableEntityException(
          "JsonProcessingException happen when parsing single event to type", e, node);
    } catch (final JsonProcessingException e) {
      log.error(
          "JsonProcessingException happen when parsing single event to type: {} msg: {}",
          clazz.getName(),
          e.getOriginalMessage());
      throw new UnprocessableEntityException(
          "JsonProcessingException happen when parsing single event to type", e, node);
    } catch (final IOException e) {
      log.error(
          "IOException happen when parsing single event to type: {} msg: {}",
          clazz.getName(),
          e.getMessage());
      throw new UnprocessableEntityException(
          "IOException happen when parsing single event to type", e, node);
    }
  }


}
