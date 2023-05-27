package io.github.ololx.samples;

import com.google.inject.Inject;
import io.github.ololx.samples.checksum.CRC32Checksum;
import io.github.ololx.samples.checksum.ChecksumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * project deduplication-service-example
 * created 27.05.2023 16:49
 *
 * @author Alexander A. Kropotin
 */
public class Deduplication implements Consumer<String> {

    private final static Logger log = LoggerFactory.getLogger(Deduplication.class);

    private final ChecksumRegistry checksumRegistry;

    @Inject
    public Deduplication(ChecksumRegistry checksumRegistry) {
        this.checksumRegistry = Objects.requireNonNull(checksumRegistry);
    }

    @Override
    public void accept(String message) {
        log.info("Receive message: {}", message);
        var checksum = new CRC32Checksum(UUID.randomUUID(), LocalDate.now(), LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        this.checksumRegistry.get(checksum)
                .thenApplyAsync(result -> {
                    log.info("Is duplicate? {}", result != null);
                    return result != null;
                })
                .thenAcceptAsync(result -> {
                    this.checksumRegistry.put(checksum, message.getBytes());
                })
                .join();
    }
}
