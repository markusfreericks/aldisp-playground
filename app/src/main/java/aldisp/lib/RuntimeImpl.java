package aldisp.lib;

import java.util.*;
import java.util.function.Function;

public class RuntimeImpl implements RuntimeEnv {

    long clock = 0;
    RuntimeEnv env = this;

    long startTime, endTime;

    boolean fakeClock = false;
    public void enableFakeClock() {
        fakeClock = true;
    }

    // condition not met
    Collection<SuspensionImpl> unavailable = new LinkedList<>();

    // time not met
    Collection<SuspensionImpl> waiting= new LinkedList<>();

    // ok to run now; condition and timing is met
    List<SuspensionImpl> runnable = new LinkedList<>();

    public void run(int numSteps) {

        startTime = System.currentTimeMillis();

        long now = clock();
        long end = now + numSteps;

        while(clock() < end){
            logState();
            int madeAvailable = promoteToAvailable();
            int madeRunnable = promoteToRunnable();
            sortRunnable();
            int computed = 0;
            while(! runnable.isEmpty()) {
                computeNextRunnable();
                computed ++;
            }
            // nur wenn sich in nichts mehr getan hat, muss die zeit erhöht werdem
            int changed = madeAvailable + madeRunnable + computed;
            if(changed == 0) {
                waitForClock();
            } else {
                System.out.println("" + madeAvailable + " made available, " + madeRunnable + " made runnable, " + computed + " computed -> no sleep");
            }
        }

        endTime = System.currentTimeMillis();
        System.out.println("real time elapsed: " + (endTime - startTime) + "ms");
    }

    void waitForClock(){
        if(fakeClock) {
            clock++;
        }
        try {
            System.out.println("sleep 1");
            Thread.sleep(1);
        } catch (InterruptedException e) {
            System.err.println("interrupted");
        }
    }

    private void logState(){

        System.out.println("clock=" + clock() + " u:" + unavailable.size() + " w:" + waiting.size() + " r:" + runnable.size());

    }

    private void computeNextRunnable() {
        // System.out.println("compute!");
        SuspensionImpl s = runnable.get(0);
        s.value = s.expr.apply(this);
        if(s.value == null){
            s.value = SuspensionImpl.NULL_OBJECT;
        }
        runnable.remove(0);
    }

    private void sortRunnable() {
        Collections.sort(runnable, (a, b) -> {
            int d = a.min - b.min;
            if(d == 0){
                d = a.max - b.max;
            }
            return d;
        });
    }

    private int promoteToRunnable() {
        int n =0;
        Iterator<SuspensionImpl> iter = waiting.iterator();
        while(iter.hasNext()){
            SuspensionImpl x = iter.next();
            long now = clock();
            long allowedStart = x.availableSince + x.min;
            boolean tooEarly = allowedStart < now;
            if(! tooEarly) {
                long allowedEnd = x.availableSince + x.max;
                boolean tooLate = now > allowedEnd;
                if (tooLate) {
                    System.err.println("running late job, " + (now - allowedEnd) + "ms late");
                }
                iter.remove();
                n++;
                runnable.add(x);
            }
        }
        return n;
    }

    private int promoteToAvailable() {
        int n=0;
        Iterator<SuspensionImpl> iter = unavailable.iterator();
        while(iter.hasNext()){
            SuspensionImpl x = iter.next();
            if(x.cond.apply(this) == Boolean.TRUE){
                iter.remove();
                x.availableSince = clock();
                n++;
                waiting.add(x);
            }
        }
        return n;
    }


    class SuspensionImpl<T> implements Suspension<T> {

        private static Object NULL_OBJECT = new Object();

        Function<RuntimeEnv, Boolean> cond;
        Function<RuntimeEnv, T> expr;
        T value;

        int min;
        int max;
        long availableSince;

        @Override
        public RuntimeEnv getRuntime() {
            return env;
        }

        @Override
        public boolean isAvailable() {
            return value != null;
        }

        @Override
        public T value() {
            if(value != null){
                if(value == NULL_OBJECT){
                    return null;
                }
                return value;
            }
            throw new Blocked("call to value() of unavailable suspension has been blocked");
        }
    }

    @Override
    public <T> Suspension<T> suspend(Function<RuntimeEnv, Boolean> cond, Function<RuntimeEnv, T> expr, int min, int max) {
        SuspensionImpl<T> r = new SuspensionImpl<>();
        r.cond = cond;
        r.expr = expr;
        r.min = min;
        r.max = max;

        unavailable.add(r);

        return r;
    }

    @Override
    public <T> Promise<T> delay(Function<RuntimeEnv, T> expr) {
        return null;
    }

    @Override
    public <T> boolean isAvailable(Suspension<T> s) {
        return s.value() != null;
    }

    @Override
    public long clock() {
        if(fakeClock) {
            return clock;
        }
        return System.currentTimeMillis();
    }
}
