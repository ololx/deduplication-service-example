package io.github.ololx.samples.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * project deduplication-service-example
 * created 28.05.2023 09:27
 *
 * @author Alexander A. Kropotin
 */
public final class PartitionReader implements KafkaReader, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PartitionReader.class);

    private final ScheduledExecutorService executorService;

    private final TopicPartition topicPartition;

    private final Properties properties;

    private final Function<ConsumerRecord<String, String>, CompletableFuture<?>> recordProcessor;

    private KafkaConsumer<String, String> kafkaConsumer;

    private ScheduledFuture<?> pollFuture;

    public PartitionReader(Properties properties, TopicPartition topicPartition, Function<ConsumerRecord<String, String>, CompletableFuture<?>> recordProcessor) {
        this.properties = properties;
        this.topicPartition = topicPartition;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.recordProcessor = recordProcessor;
    }

    @Override
    public void start() {
        CompletableFuture.runAsync(
                () -> {
                    log.debug("Creating kafka reader for {}", this.topicPartition);
                    this.kafkaConsumer = new KafkaConsumer<>(this.properties);
                    this.kafkaConsumer.assign(Collections.singleton(this.topicPartition));

                    long committedOffset = kafkaConsumer.position(this.topicPartition);
                    long beginningOffset = kafkaConsumer.beginningOffsets(Collections.singleton(this.topicPartition)).get(topicPartition);
                    if (committedOffset < beginningOffset) {
                        log.warn("Committed offset {} is less than beginning offset {} of {}", committedOffset, beginningOffset, topicPartition);
                        committedOffset = beginningOffset;
                    }

                    log.info("Last committed committedOffset for {} is {}", topicPartition, committedOffset);
                    log.info("Beginning committedOffset for {} is {}", topicPartition, beginningOffset);
                    log.info("End committedOffset for {} is {}", topicPartition, kafkaConsumer.endOffsets(Collections.singleton(this.topicPartition)).get(topicPartition));
                    }, executorService
                )
                .thenRunAsync(
                        () -> {
                            log.info("Schedule polling kafka topic for {}", this.topicPartition);
                            pollFuture = executorService.scheduleWithFixedDelay(
                                    this.poll(() -> {
                                        var messages = kafkaConsumer.poll(Duration.ofMillis(10)).records(topicPartition);
                                        if (messages == null || messages.size() == 0) {
                                            return;
                                        }

                                        messages.forEach(message -> {
                                            this.recordProcessor.apply(message)
                                                    .whenCompleteAsync((any, exception) -> {
                                                        if (exception != null) {
                                                            log.error("Catch exception", exception);
                                                        }

                                                        this.kafkaConsumer.commitAsync();
                                                    }, executorService);
                                        });
                                    }),
                                    0,
                                    10,
                                    TimeUnit.MILLISECONDS
                            );
                            },
                        executorService
                ).join();
    }

    private Runnable poll(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Uncaught exception", e);
            }
        };
    }

    @Override
    public void stop() {
        try {
            this.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {

    }
}
