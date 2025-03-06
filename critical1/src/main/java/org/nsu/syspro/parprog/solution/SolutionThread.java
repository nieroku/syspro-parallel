package org.nsu.syspro.parprog.solution;

import org.nsu.syspro.parprog.UserThread;
import org.nsu.syspro.parprog.external.*;

import java.util.*;
import java.util.concurrent.*;

public final class SolutionThread extends UserThread {
    private static ExecutorService l1 = Executors.newCachedThreadPool();
    private static ExecutorService l2 = Executors.newSingleThreadExecutor();

    private static CompiledCache cache = new CompiledCache();

    private HashMap<Long, Long> methodStat = new HashMap<>();

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
        if (timesExecuted == 5000 && level < 1)
            l1.execute(
                    () -> cache.store(new CompiledCache.Entry(compiler.compile_l1(method), false)));
        if (timesExecuted == 50000 && level < 2)
            l2.execute(
                    () -> cache.store(new CompiledCache.Entry(compiler.compile_l2(method), true)));

        return entry != null ? exec.execute(entry.compiled) : exec.interpret(method);
    }
}
