package io.github.ololx.samples.checksum;

import io.github.ololx.samples.utils.SneakyTryCatch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * project deduplication-service-example
 * created 22.07.2023 17:47
 *
 * @author Alexander A. Kropotin
 */
public class SHA256Checksum extends AbstractChecksum {

    public SHA256Checksum(byte[] data) {
        super(data);
    }

    @Override
    public byte[] getBytes() {
        return SneakyTryCatch.sneakyTry(() -> {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(this.data);
        });
    }
}

