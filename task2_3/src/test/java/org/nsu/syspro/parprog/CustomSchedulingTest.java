package org.nsu.syspro.parprog;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nsu.syspro.parprog.base.DefaultFork;
import org.nsu.syspro.parprog.base.DiningTable;
import org.nsu.syspro.parprog.examples.DefaultPhilosopher;
import org.nsu.syspro.parprog.helpers.TestLevels;
import org.nsu.syspro.parprog.interfaces.Fork;
import org.nsu.syspro.parprog.interfaces.Philosopher;

public class CustomSchedulingTest extends TestLevels {

    static final class CustomizedPhilosopher extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            sleepMillis(this.id * 20);
            System.out.println(Thread.currentThread() + " " + this + ": onHungry");
            super.onHungry(left, right);
        }
    }

    static final class CustomizedFork extends DefaultFork {
        @Override
        public void acquire() {
            System.out.println(Thread.currentThread() + " trying to acquire " + this);
            super.acquire();
            System.out.println(Thread.currentThread() + " acquired " + this);
            sleepMillis(100);
        }
    }

    static final class CustomizedTable extends DiningTable<CustomizedPhilosopher, CustomizedFork> {
        public CustomizedTable(int N) {
            super(N);
        }

        @Override
        public CustomizedFork createFork() {
            return new CustomizedFork();
        }

        @Override
        public CustomizedPhilosopher createPhilosopher() {
            return new CustomizedPhilosopher();
        }
    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testDeadlockFreedom(int N) {
        final CustomizedTable table = dine(new CustomizedTable(N), 1);
    }

    private final class OverthinkingTable extends DiningTable<Philosopher, Fork> {
        private boolean overthinkingPhilosopherCreated = false;

        private OverthinkingTable(int N) {
            super(N);
        }

        @Override
        public Fork createFork() {
            return new DefaultFork();
        }

        @Override
        public DefaultPhilosopher createPhilosopher() {
            if (overthinkingPhilosopherCreated) return new DefaultPhilosopher();
            overthinkingPhilosopherCreated = true;
            return new DefaultPhilosopher() {
                @Override
                public void onHungry(Fork left, Fork right) {
                    left.acquire();
                    try {
                        right.acquire();
                        try {
                            sleepSeconds(1);
                        } finally {
                            right.release();
                        }
                    } finally {
                        left.release();
                    }
                }
            };
        }
    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = {4, 5, 7})
    @Timeout(3)
    void testSingleSlow(int N) {
        final DiningTable<Philosopher, Fork> table = new OverthinkingTable(N);
        dine(table, 1);
        assertTrue(table.maxMeals() >= 1000);
    }

    private final class BiTable extends DiningTable<Philosopher, Fork> {
        private BiTable(int N) {
            super(N);
        }

        @Override
        public Fork createFork() {
            return new DefaultFork();
        }

        @Override
        public Philosopher createPhilosopher() {
            return new DefaultPhilosopher() {
                @Override
                public void onHungry(Fork left, Fork right) {
                    sleepMillis((id & 1) == 0 ? 1 : 10);
                    super.onHungry(left, right);
                }
            };
        }
    }

    @EnabledIf("mediumEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testWeakFairness(int N) {
        final BiTable table = dine(new BiTable(N), 1);
        assertTrue(table.minMeals() > 0);
    }
}
