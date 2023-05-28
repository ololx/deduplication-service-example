package io.github.ololx.samples;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.ololx.samples.assembly.DeduplicationModule;
import io.github.ololx.samples.kafka.PartitionReader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * project deduplication-service-example
 * created 27.05.2023 12:48
 *
 * @author Alexander A. Kropotin
 */
public class DeduplicationServiceApp {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationServiceApp.class);

    public static void main(String[] args) {
        try {
            Injector injector = createInjector();
            final var deduplication = injector.getInstance(Deduplication.class);
            log.info(":::Deduplication service was started:::");

            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "deduplication-service");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(10_000));
            props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                    String.valueOf(12 * 1024 * 1024));


            var reader = new PartitionReader(
                    props,
                    new TopicPartition("deduplication", 0),
                    (record) -> deduplication.apply(record.value())
            );
            reader.start();
        }  catch (Exception e) {
            log.error("Initialization exception", e);
            System.exit(1);
        }
    }

    public static Injector createInjector() {
        try {
            return Guice.createInjector(
                    new DeduplicationModule()
            );
        }  catch (CreationException e) {
            throw new RuntimeException("Cannot create Guice injector", e);
        }
    }
}