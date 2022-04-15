package telemetrie.rawdispatcher;

import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;

import java.util.concurrent.CompletableFuture;

public interface ControlMessageProcessor<K, V> {
  CompletableFuture processControlMessage(IncomingKafkaRecord<K, V> record);
}
