package org.nsu.syspro.parprog.solution;

import org.nsu.syspro.parprog.external.*;

import java.util.HashMap;
import java.util.concurrent.locks.*;

public class CompiledCache {
    private final HashMap<Long, Entry> entries = new HashMap<>();
    private final Lock readLock;
    private final Lock writeLock;

    {
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    public Entry get(MethodID method) {
        readLock.lock();
        try {
            return entries.get(method.id());
        } finally {
            readLock.unlock();
        }
    }

    public void store(Entry entry) {
        writeLock.lock();
        try {
            if (entry.l2) entries.put(entry.compiled.id().id(), entry);
            else entries.putIfAbsent(entry.compiled.id().id(), entry);
        } finally {
            writeLock.unlock();
        }
    }

    public static class Entry {
        public final CompiledMethod compiled;
        public final boolean l2;

        public Entry(CompiledMethod compiled, boolean l2) {
            this.compiled = compiled;
            this.l2 = l2;
        }
    }
}
