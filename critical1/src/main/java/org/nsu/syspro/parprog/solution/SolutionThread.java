package org.nsu.syspro.parprog.solution;

import org.nsu.syspro.parprog.UserThread;
import org.nsu.syspro.parprog.external.*;

import java.util.*;
import java.util.concurrent.*;

public final class SolutionThread extends UserThread {
    private static final int L1_SOFT_BOUND = 50;
    private static final int L2_HARD_BOUND = 1000;
    private static final int L2_SOFT_BOUND = L2_HARD_BOUND / 2;

    private static ExecutorService l1 = Executors.newCachedThreadPool();
    private static ExecutorService l2 = Executors.newSingleThreadExecutor();

    private static CompiledCache cache = new CompiledCache();

    private HashMap<Long, Long> methodStat = new HashMap<>();
    private HashMap<Long, Future<CompiledCache.Entry>> pendingCompilation = new HashMap<>();

    public SolutionThread(
            int compilationThreadBound,
            ExecutionEngine exec,
            CompilationEngine compiler,
            Runnable r) {
        super(compilationThreadBound, exec, compiler, r);
    }

    @Override
    public ExecutionResult executeMethod(MethodID method) {
        CompiledCache.Entry entry = cache.get(method);
        long timesExecuted = methodStat.getOrDefault(method.id(), 0L);
        methodStat.put(method.id(), timesExecuted + 1);

        int level = entry != null ? (entry.l2 ? 2 : 1) : 0;
        if (timesExecuted == L1_SOFT_BOUND && level < 1)
            l1.execute(
                    () -> cache.store(new CompiledCache.Entry(compiler.compile_l1(method), false)));
        if (timesExecuted == L2_SOFT_BOUND && level < 2)
            pendingCompilation.put(
                    method.id(),
                    l2.submit(
                            () -> {
                                CompiledCache.Entry rv =
                                        new CompiledCache.Entry(compiler.compile_l2(method), true);
                                cache.store(rv);
                                return rv;
                            }));
        if (timesExecuted == L2_HARD_BOUND) {
            try {
                Future<CompiledCache.Entry> pending = pendingCompilation.get(method.id());
                if (pending != null) entry = pending.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            CompiledCache.Entry toAssert = cache.get(method);
            if (toAssert == null || !toAssert.l2) throw new AssertionError();
        }

        return entry != null ? exec.execute(entry.compiled) : exec.interpret(method);
    }
}
