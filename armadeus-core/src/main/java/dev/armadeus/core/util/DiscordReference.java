package dev.armadeus.core.util;

import net.dv8tion.jda.api.entities.ISnowflake;

import java.lang.ref.WeakReference;
import java.util.function.LongFunction;

public class DiscordReference<T extends ISnowflake> implements ISnowflake {

    private final LongFunction<T> fallbackProvider;
    private final long id;
    private WeakReference<T> reference;

    public DiscordReference(T referent, LongFunction<T> fallback) {
        this.fallbackProvider = fallback;
        this.reference = new WeakReference<>(referent);
        this.id = referent.getIdLong();
    }

    public T resolve() {
        T referent = reference.get();
        if(referent == null) {
            referent = fallbackProvider.apply(id);
            if(reference == null)
                return null;
            reference = new WeakReference<>(referent);
        }
        return referent;
    }

    @Override
    public int hashCode()
    {
        return resolve().hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj)
    {
        return resolve().equals(obj);
    }

    @Override
    public String toString()
    {
        return resolve().toString();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }
}
