package io.github.ololx.samples.checksum;

import java.util.concurrent.CompletableFuture;

/**
 * A registry interface for managing checksum data associated with keys.
 *
 * project deduplication-service-example
 * created 27.05.2023 12:48
 *
 * @author Alexander A. Kropotin
 */
public interface ChecksumRegistry {

    /**
     * Retrieves the value associated with the specified checksum asynchronously.
     *
     * @param checksum The checksum to retrieve the value for.
     * @return A CompletableFuture that completes with the associated value,
     * or null if no value is found.
     */
    CompletableFuture<byte[]> get(Checksum checksum);

    /**
     * Associates the specified value with the specified checksum asynchronously.
     *
     * @param checksum   The checksum to associate the value with.
     * @param value The value to be associated with the checksum.
     * @return A CompletableFuture that completes when the operation is finished.
     */
    CompletableFuture<Void> put(Checksum checksum, byte[] value);

    /**
     * Deletes the value associated with the specified checksum asynchronously.
     *
     * @param checksum The checksum to delete the value for.
     * @return A CompletableFuture that completes when the operation is finished.
     */
    CompletableFuture<Void> delete(Checksum checksum);
}
