package io.github.ololx.samples.kafka;

import io.github.ololx.moonshine.stopwatch.SimpleStopwatch;
import io.github.ololx.moonshine.stopwatch.Stopwatch;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * project deduplication-service-example
 * created 28.05.2023 09:27
 *
 * @author Alexander A. Kropotin
 */
public final class PartitionReader implements KafkaReader, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PartitionReader.class);

    private final TopicPartition topicPartition;

    private final Properties properties;

    private final Function<ConsumerRecord<String, String>, CompletableFuture<?>> recordProcessor;

    private KafkaConsumer<String, String> kafkaConsumer;

    private LogScheduledExecutorService executorService;

    private ScheduledFuture<?> pollFuture;

    public PartitionReader(Properties properties, TopicPartition topicPartition, Function<ConsumerRecord<String, String>, CompletableFuture<?>> recordProcessor) {
        this.properties = properties;
        this.topicPartition = topicPartition;
        this.executorService = new LogScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        this.recordProcessor = recordProcessor;
    }

    @Override
    public void start() {
        CompletableFuture.runAsync(
                () -> {
                    log.debug("Creating kafka reader for {}", this.topicPartition);
                    this.kafkaConsumer = new KafkaConsumer<>(this.properties);
                    this.kafkaConsumer.assign(Collections.singleton(this.topicPartition));

                    long committedOffset = kafkaConsumer.position(this.topicPartition);
                    long beginningOffset = kafkaConsumer.beginningOffsets(Collections.singleton(this.topicPartition)).get(topicPartition);
                    if (committedOffset < beginningOffset) {
                        log.warn("Committed offset {} is less than beginning offset {} of {}", committedOffset, beginningOffset, topicPartition);
                        committedOffset = beginningOffset;
                    }

                    log.info("Last committed committedOffset for {} is {}", topicPartition, committedOffset);
                    log.info("Beginning committedOffset for {} is {}", topicPartition, beginningOffset);
                    log.info("End committedOffset for {} is {}", topicPartition, kafkaConsumer.endOffsets(Collections.singleton(this.topicPartition)).get(topicPartition));
                    }, executorService
                )
                .thenRunAsync(
                        () -> {
                            log.info("Schedule polling kafka topic for {}", this.topicPartition);
                            pollFuture = executorService.scheduleWithFixedDelay(
                                    this.poll(() -> {
                                        var messages = kafkaConsumer.poll(Duration.ofMillis(10)).records(topicPartition);
                                        if (messages == null || messages.size() == 0) {
                                            return;
                                        }

                                        messages.forEach(message -> {
                                            this.recordProcessor.apply(message)
                                                    .whenCompleteAsync((any, exception) -> {
                                                        if (exception != null) {
                                                            log.error("Catch exception", exception);
                                                        }

                                                        this.kafkaConsumer.commitAsync();
                                                    }, executorService);
                                        });
                                    }),
                                    0,
                                    10,
                                    TimeUnit.MILLISECONDS
                            );
                            },
                        executorService
                ).join();
    }

    private Runnable poll(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Uncaught exception", e);
            }
        };
    }

    @Override
    public void stop() {
        try {
            this.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        CompletableFuture.runAsync(
                () -> {
                    if (this.pollFuture != null && !this.pollFuture.isDone()) {
                        pollFuture.cancel(true);
                    }
                    if (this.kafkaConsumer != null) {
                        kafkaConsumer.close();
                    }
                    },
                        executorService
                )
                .thenRun(
                        () -> {
                            executorService.shutdownNow();
                            try {
                                this.executorService.awaitTermination(10, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            executorService = null;}
                );
    }

    class LogScheduledExecutorService implements ScheduledExecutorService {

        ScheduledExecutorService scheduledExecutorService;

        LogScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
            this.scheduledExecutorService = scheduledExecutorService;
        }

        @Override
        public void shutdown() {
            this.scheduledExecutorService.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return this.scheduledExecutorService.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return this.scheduledExecutorService.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return this.scheduledExecutorService.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return this.scheduledExecutorService.awaitTermination(timeout, unit);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            Stopwatch stopwatch = new SimpleStopwatch().start();
            return this.scheduledExecutorService.submit(() -> {
                System.out.println("Waited in queue: " + stopwatch.elapsed());
                return task.call();
            });
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return this.scheduledExecutorService.submit(task, result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return this.scheduledExecutorService.submit(task);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return this.scheduledExecutorService.invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return this.scheduledExecutorService.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return this.scheduledExecutorService.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.scheduledExecutorService.invokeAny(tasks);
        }

        @Override
        public void execute(Runnable command) {
            Stopwatch stopwatch = new SimpleStopwatch().start();
            this.scheduledExecutorService.execute(() -> {
                System.out.println("Waited in queue: " + stopwatch.elapsed());
                command.run();
            });
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return this.scheduledExecutorService.schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return this.scheduledExecutorService.schedule(callable, delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return this.scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return this.scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }
}
