package io.github.ololx.samples;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.ololx.samples.assembly.DeduplicationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * project deduplication-service-example
 * created 27.05.2023 12:48
 *
 * @author Alexander A. Kropotin
 */
public class DeduplicationServiceApp {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationServiceApp.class);

    public static void main(String[] args) {
        try {
            Injector injector = createInjector();
            final var deduplication = injector.getInstance(Deduplication.class);
            log.info(":::Deduplication service was started:::");

            deduplication.accept("Hello world");
        }  catch (Exception e) {
            log.error("Initialization exception", e);
            System.exit(1);
        }
    }

    public static Injector createInjector() {
        try {
            return Guice.createInjector(
                    new DeduplicationModule()
            );
        }  catch (CreationException e) {
            throw new RuntimeException("Cannot create Guice injector", e);
        }
    }
}