package io.github.ololx.samples;

import com.google.inject.Inject;
import io.github.ololx.samples.checksum.CRC32Checksum;
import io.github.ololx.samples.checksum.ChecksumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * project deduplication-service-example
 * created 27.05.2023 16:49
 *
 * @author Alexander A. Kropotin
 */
public class Deduplication implements Function<String, CompletableFuture<Void>> {

    private final static Logger log = LoggerFactory.getLogger(Deduplication.class);

    private final ChecksumRegistry checksumRegistry;

    @Inject
    public Deduplication(ChecksumRegistry checksumRegistry) {
        this.checksumRegistry = Objects.requireNonNull(checksumRegistry);
    }

    @Override
    public CompletableFuture<Void> apply(String message) {
        log.info("Receive message: {}", message);
        var checksum = new CRC32Checksum(message.getBytes());

        return this.checksumRegistry.get(new CRC32Checksum(message.getBytes()))
                .thenAcceptAsync(result -> {
                    log.info("Is duplicate? {}", result != null);
                    this.checksumRegistry.put(checksum, message.getBytes());
                });
    }
}
