package io.github.ololx.samples.checksum;

import io.github.ololx.moonshine.bytes.Endianness;
import io.github.ololx.moonshine.bytes.coding.encoders.LongEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.zip.CRC32;

/**
 * project deduplication-service-example
 * created 27.05.2023 16:02
 *
 * @author Alexander A. Kropotin
 */
public class CRC32Checksum extends AbstractChecksum {

    private static final LongEncoder longEncoder = new LongEncoder();

    public CRC32Checksum(byte[] data) {
        super(data);
    }

    @Override
    public byte[] getBytes() {
        CRC32 crc32 = new CRC32();
        crc32.update(this.data);
        long checksum = crc32.getValue();

        return longEncoder.encode(checksum, Endianness.BIG_ENDIAN.byteOrder(Long.BYTES));
    }
}
