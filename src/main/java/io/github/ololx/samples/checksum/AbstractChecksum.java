package io.github.ololx.samples.checksum;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * project deduplication-service-example
 * created 27.05.2023 16:02
 *
 * @author Alexander A. Kropotin
 */
public abstract class AbstractChecksum implements Checksum {

    protected final byte[] data;// = new byte[(Long.BYTES * 2) + (Integer.BYTES * 6)];

    AbstractChecksum(UUID id, LocalDate date, LocalDateTime time) {
        ByteBuffer buffer = ByteBuffer.allocate(64 + 64 + (64 * 6));
        buffer.putLong(id.getMostSignificantBits());
        buffer.putLong(id.getLeastSignificantBits());
        buffer.putInt(date.getYear());
        buffer.putInt(date.getMonthValue());
        buffer.putInt(date.getDayOfMonth());
        buffer.putInt(time.getHour());
        buffer.putInt(time.getMinute());
        buffer.putInt(time.getSecond());

        this.data = buffer.array();
    }
}
