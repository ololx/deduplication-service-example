package io.github.ololx.samples.checksum;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

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
