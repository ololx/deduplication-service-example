package io.github.ololx.samples.checksum;

import io.github.ololx.moonshine.stopwatch.SimpleStopwatch;
import io.github.ololx.moonshine.stopwatch.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * project deduplication-service-example
 * created 22.07.2023 18:00
 *
 * @author Alexander A. Kropotin
 */
public class ChecksumPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(ChecksumPerformanceTest.class);

    @Test
    public void getBytes_checksum_measure() {
        Stopwatch stopwatch = new SimpleStopwatch();
        var testData = IntStream.range(0, 10_000_000).mapToObj(index -> generateRandomData()).toList();

        stopwatch.start();
        testData.forEach(data -> new CRC32Checksum(data).getBytes());
        log.info("CRC time = {}", stopwatch.elapsed());

        stopwatch.reset().start();
        testData.forEach(data -> new MD5Checksum(data).getBytes());
        log.info("MD5 time = {}", stopwatch.elapsed());

        stopwatch.reset().start();
        testData.forEach(data -> new SHA1Checksum(data).getBytes());
        log.info("SHA-1 time = {}", stopwatch.elapsed());

        stopwatch.reset().start();
        testData.forEach(data -> new SHA256Checksum(data).getBytes());
        log.info("SHA-256 time = {}", stopwatch.elapsed());

        stopwatch.reset().start();
        testData.forEach(data -> new SHA512Checksum(data).getBytes());
        log.info("SHA-512 time = {}", stopwatch.elapsed());
    }

    private byte[] generateRandomData() {
        return (UUID.randomUUID().toString() + LocalDateTime.now().toString()).getBytes();
    }
}
