package io.github.ololx.samples.utils;

import java.util.Objects;
import java.util.function.Function;

/**
 * project deduplication-service-example
 * created 29.05.2023 17:23
 *
 * @author Alexander A. Kropotin
 */
public class SneakyTryCatch {

    public static void sneakyThrow(Throwable throwable) throws RuntimeException {
        Objects.requireNonNull(throwable, "The throwable must be not null");
        throw new RuntimeException(throwable);
    }

    public static <T> T sneakyTry(ExecutableFunc<T> executable) {
       try {
           return executable.execute();
       } catch (Throwable throwable) {
           SneakyTryCatch.sneakyThrow(throwable);
       }

       return null;
    }

    public static void sneakyTry(ExecutableVoid executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            SneakyTryCatch.sneakyThrow(throwable);
        }
    }

    public interface ExecutableVoid {

       void execute() throws Throwable;
    }

    public interface ExecutableFunc<T> {

        T execute() throws Throwable;
    }
}
