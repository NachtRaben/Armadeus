package com.nachtraben.core.util;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TimedCache<K, V> {
    private static final long DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis(15);

    private long timeout;
    private TimeoutPolicy policy;
    private HashMap<K, TimedValue<V>> data;
    private Timer timer;

    public TimedCache() {
        this(DEFAULT_TIMEOUT);
    }

    public TimedCache(long timeout) {
        this(timeout, TimeoutPolicy.ACCESS);
    }

    public TimedCache(long timeout, TimeoutPolicy policy) {
        this.timeout = timeout;
        this.policy = policy;
        this.data = new HashMap<>();
        this.timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanup();
            }
        }, timeout, timeout);
    }

    public void put(K key, V value) {
        if (policy.equals(TimeoutPolicy.ACCESS))
            data.put(key, new TimedValue<>(value).setExpiration(System.currentTimeMillis() + timeout));
        else
            data.put(key, new TimedValue<>(value));
    }

    public V get(K key) {
        TimedValue<V> value = data.get(key);
        if(value != null) {
            if (policy.equals(TimeoutPolicy.ACCESS))
                value.setExpiration(System.currentTimeMillis() + timeout);
            return value.value;
        }
        return null;
    }

    public boolean hasKey(K key) {
        return data.containsKey(key) && !data.get(key).isExpired();
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mapper) {
        return data.computeIfAbsent(key, value -> {
            if(policy.equals(TimeoutPolicy.ACCESS))
                return new TimedValue<V>(mapper.apply(key)).setExpiration(System.currentTimeMillis() + timeout);
            else
                return new TimedValue<>(mapper.apply(key));
        }).value;
    }

    public Set<Map.Entry<K, TimedValue<V>>> entrySet() {
        return data.entrySet();
    }

    private void cleanup() {
        data.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private class TimedValue<T> {
        T value;
        long expire;

        TimedValue(T value) {
            this.value = value;
            expire = System.currentTimeMillis();
        }

        TimedValue<T> setExpiration(long expire) {
            this.expire = expire;
            return this;
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= expire;
        }

    }

    public enum TimeoutPolicy {
        CREATION,
        ACCESS
    }
}


