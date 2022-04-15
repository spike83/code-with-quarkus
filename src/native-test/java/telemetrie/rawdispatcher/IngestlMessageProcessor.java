package telemetrie.rawdispatcher;

import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;

import java.util.concurrent.CompletableFuture;

public interface IngestlMessageProcessor<K, V> {
  CompletableFuture<Void> processIngestMessage(IncomingKafkaRecord<K, V> record);
}
