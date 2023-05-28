package io.github.ololx.samples.checksum;

import java.util.Objects;

/**
 * project deduplication-service-example
 * created 27.05.2023 16:02
 *
 * @author Alexander A. Kropotin
 */
public abstract class AbstractChecksum implements Checksum {

    protected final byte[] data;

    AbstractChecksum(byte[] data) {
        this.data = Objects.requireNonNull(data);
    }
}
