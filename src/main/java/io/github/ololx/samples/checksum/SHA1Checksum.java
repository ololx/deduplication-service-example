package io.github.ololx.samples.checksum;

import io.github.ololx.samples.utils.SneakyTryCatch;

import java.security.MessageDigest;

/**
 * project deduplication-service-example
 * created 22.07.2023 17:47
 *
 * @author Alexander A. Kropotin
 */
public class SHA1Checksum extends AbstractChecksum {

    public SHA1Checksum(byte[] data) {
        super(data);
    }

    @Override
    public byte[] getBytes() {
        return SneakyTryCatch.sneakyTry(() -> {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(this.data);
        });
    }
}

