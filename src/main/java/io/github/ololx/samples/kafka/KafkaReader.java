package io.github.ololx.samples.kafka;

/**
 * project deduplication-service-example
 * created 28.05.2023 08:56
 *
 * @author Alexander A. Kropotin
 */
public interface KafkaReader {

    void start();

    void stop() throws Exception;
}
