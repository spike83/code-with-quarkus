package telemetrie.rawdispatcher.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.CompletableFuture;

public interface ConsumerRecordProcessor<K, V> {
  CompletableFuture process(ConsumerRecord<K, V> record);
}
