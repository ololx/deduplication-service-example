package io.github.ololx.samples.assembly;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.github.ololx.samples.Deduplication;
import io.github.ololx.samples.checksum.ChecksumRegistry;
import io.github.ololx.samples.checksum.RocksDBChecksumRegistry;

/**
 * project deduplication-service-example
 * created 27.05.2023 18:40
 *
 * @author Alexander A. Kropotin
 */
public class DeduplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Deduplication.class).in(Scopes.SINGLETON);
        bind(ChecksumRegistry.class).to(RocksDBChecksumRegistry.class).in(Scopes.SINGLETON);
    }
}
