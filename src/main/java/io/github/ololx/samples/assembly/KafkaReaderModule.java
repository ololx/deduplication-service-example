package io.github.ololx.samples.assembly;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import io.github.ololx.moonshine.measuring.memory.Memory;
import io.github.ololx.samples.Deduplication;
import io.github.ololx.samples.kafka.KafkaReader;
import io.github.ololx.samples.kafka.PartitionReader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class KafkaReaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KafkaReader.class).toProvider(PartitionReaderProvider.class).in(Scopes.SINGLETON);
    }

    static class PartitionReaderProvider implements Provider<PartitionReader> {

        @Inject
        private Deduplication deduplication;

        @Override
        public PartitionReader get() {
            Properties kafkacConsumerProperties = new Properties();
            kafkacConsumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
            kafkacConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "deduplication-service");
            kafkacConsumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            kafkacConsumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            kafkacConsumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(10_000));
            kafkacConsumerProperties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, (int) Memory.ofMegabytes(12).toBytes());

            TopicPartition topicPartition = new TopicPartition("deduplication", 0);

            return new PartitionReader(
                    kafkacConsumerProperties,
                    topicPartition,
                    (record) -> deduplication.apply(record.value())
            );
        }
    }
}
