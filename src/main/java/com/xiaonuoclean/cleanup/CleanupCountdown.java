package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.config.CleanConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CleanupCountdown {
    public static final int TICKS_PER_SECOND = 20;

    private CleanConfig config;
    private long remainingTicks;
    private final Set<Integer> sentWarnings = new HashSet<>();

    public CleanupCountdown(CleanConfig config) {
        reset(config);
    }

    public void reset(CleanConfig config) {
        this.config = config.normalize();
        this.remainingTicks = (long) this.config.intervalSeconds() * TICKS_PER_SECOND;
        this.sentWarnings.clear();
    }

    public void tick(Consumer<Event> eventConsumer) {
        remainingTicks--;

        if (remainingTicks <= 0) {
            eventConsumer.accept(new Cleanup());
            reset(config);
            return;
        }

        if (remainingTicks % TICKS_PER_SECOND == 0) {
            int remainingSeconds = remainingSeconds();
            if (config.warningSeconds().contains(remainingSeconds) && sentWarnings.add(remainingSeconds)) {
                eventConsumer.accept(new Warning(remainingSeconds));
            }
        }
    }

    public long remainingTicks() {
        return remainingTicks;
    }

    public int remainingSeconds() {
        return (int) Math.ceil(remainingTicks / (double) TICKS_PER_SECOND);
    }

    public interface Event {
    }

    public record Warning(int remainingSeconds) implements Event {
    }

    public record Cleanup() implements Event {
    }
}
