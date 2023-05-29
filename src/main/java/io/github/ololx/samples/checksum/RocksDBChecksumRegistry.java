package io.github.ololx.samples.checksum;

import io.github.ololx.samples.utils.SneakyTryCatch;
import org.rocksdb.*;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * project deduplication-service-example
 * created 27.05.2023 14:04
 *
 * @author Alexander A. Kropotin
 */
public class RocksDBChecksumRegistry implements ChecksumRegistry, AutoCloseable {

    private final RocksDB rocksDB;

    public RocksDBChecksumRegistry() throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true)
                .setCompactionStyle(CompactionStyle.UNIVERSAL)
                .setCompressionType(CompressionType.SNAPPY_COMPRESSION)
                .setTtl(Duration.ofDays(30).toSeconds());
        rocksDB = RocksDB.open(options, new File("./rocksdb").getPath());
    }

    @Override
    public CompletableFuture<byte[]> get(Checksum checksum) {
        return CompletableFuture.supplyAsync(() -> {
            return SneakyTryCatch.sneakyTry(() -> rocksDB.get(checksum.getBytes()));
        });
    }

    @Override
    public CompletableFuture<Void> put(Checksum checksum, byte[] value) {
        return CompletableFuture.runAsync(() -> {
            SneakyTryCatch.sneakyTry(() -> rocksDB.put(checksum.getBytes(), value));
        });
    }

    @Override
    public CompletableFuture<Void> delete(Checksum checksum) {
        return CompletableFuture.runAsync(() -> {
            SneakyTryCatch.sneakyTry(() -> rocksDB.delete(checksum.getBytes()));
        });
    }

    @Override
    public void close() {
        rocksDB.close();
    }
}
