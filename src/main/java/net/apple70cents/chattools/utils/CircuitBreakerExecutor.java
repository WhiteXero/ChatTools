package net.apple70cents.chattools.utils;

import java.util.function.Supplier;

public class CircuitBreakerExecutor {
    private final Runnable task;
    private Supplier<Integer> maxLimitPerSecond = () -> Integer.MAX_VALUE;
    private Runnable failsafeFunction;
    private Supplier<Boolean> failsafeJudgement = () -> true;
    private final Object lock = new Object();
    private volatile long currentSecond;
    private int requestCount;

    private CircuitBreakerExecutor(Runnable task) {
        this.task = task;
    }

    public static CircuitBreakerExecutor of(Runnable task) {
        return new CircuitBreakerExecutor(task);
    }

    public CircuitBreakerExecutor setMaxLimitPerSecond(Supplier<Integer> max) {
        this.maxLimitPerSecond = max;
        return this;
    }

    public CircuitBreakerExecutor setFailsafeFunction(Runnable failsafeFunction) {
        this.failsafeFunction = failsafeFunction;
        return this;
    }

    public CircuitBreakerExecutor setFailsafeJudgement(Supplier<Boolean> failsafeJudgement) {
        this.failsafeJudgement = failsafeJudgement;
        return this;
    }

    public void run() {
        if (!failsafeJudgement.get()) {
            return;
        }

        boolean shouldBreak = false;
        synchronized (lock) {
            long now = System.currentTimeMillis() / 1000;
            if (now != currentSecond) {
                currentSecond = now;
                requestCount = 0;
            }
            if (++requestCount > maxLimitPerSecond.get()) {
                shouldBreak = true;
            }
        }

        if (shouldBreak) {
            if (failsafeFunction != null) {
                failsafeFunction.run();
            }
            return;
        }

        task.run();
    }
}