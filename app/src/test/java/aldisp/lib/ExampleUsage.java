package aldisp.lib;

import aldisp.lib.RuntimeEnv.*;
import org.junit.Test;

public class ExampleUsage {

    <T> Stream<T> merge(Stream<T> a, Stream<T> b){
        return RuntimeEnv.stream( // kommt der java-compiler nicht von selbst drauf
                    a.getRuntime()
                        .suspend(r -> a.isAvailable() || b.isAvailable(),
                                 r -> a.isAvailable()
                                      ? r.cons(a.value().head(), merge(b, a.value().tail()))
                                      : r.cons(b.value().head(), merge(a, b.value().tail())),
                                0, 0));
    }

    Stream<String> emitAt(RuntimeEnv env, String a, int n, int count) {
        return RuntimeEnv.stream(
                env.suspend(
                    r -> true,
                    r -> r.cons(a + count, emitAt(r, a, n, count+1)),
                    n, n));
    }

    void printStream(Stream<String> xs){
        xs.getRuntime().suspend(
                r -> xs.isAvailable(),
                r -> {
                    System.out.println("[" + r.clock() + "] " + xs.value().head());
                    printStream(xs.value().tail());
                    return null;
                },
                0,0);
    }

    @Test
    public void testMerge(){

        RuntimeImpl env = new RuntimeImpl();

//        Stream<String> a = emitAt(env, "a 2", 2);
//        Stream<String> b = emitAt(env, "b 10", 10);

        Stream<String> a = emitAt(env, "A", 2, 0);
        Stream<String> b = emitAt(env, "B", 10, 0);

        Stream<String> m = merge(a, b);
        printStream(m);

        env.run(100); // run 100 clicks

    }

}
