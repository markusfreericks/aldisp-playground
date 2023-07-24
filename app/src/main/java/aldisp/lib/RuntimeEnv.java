package aldisp.lib;

import java.util.function.Function;

public interface RuntimeEnv {
    class Blocked extends RuntimeException {
        Blocked(String msg){ super(msg); }
    }

    interface HasRuntimeEnv {
        RuntimeEnv getRuntime();
    }

    interface Suspension<T> extends HasRuntimeEnv {
        boolean isAvailable();

        //        T value() throws Blocked;
        T value();
    }

    interface Stream<T> extends Suspension<Cons<T, Stream>> {}

    interface Promise<T> extends HasRuntimeEnv {
        T value();
    }

    // how to create suspensions

    <T> Suspension<T> suspend(Function<RuntimeEnv, Boolean> cond,
                              Function<RuntimeEnv, T> expr,
                              int min, int max);

    <T> Promise<T> delay(Function<RuntimeEnv, T> expr);

    <T> boolean isAvailable(Suspension<T> s);

    long clock();

    interface Cons<A, B>{
        A head();
        B tail();
    }

    // idiotic casting
    static <T> Stream<T> stream(Suspension<Cons<T, Stream<T>>> x){
        return new Stream<T>(){

            @Override
            public boolean isAvailable() {
                return x.isAvailable();
            }

            @Override
            public Cons<T, Stream> value() {
                return new Cons<T, Stream>() {
                    @Override
                    public T head() {
                        return x.value().head();
                    }

                    @Override
                    public Stream tail() {
                        return x.value().tail();
                    }
                };
            }

            @Override
            public RuntimeEnv getRuntime() {
                return x.getRuntime();
            }
        };
    }

    default  <A, B>  Cons<A, B> cons(A a, B b){
        return new Cons<A, B>() {
            @Override
            public A head() {
                return a;
            }

            @Override
            public B tail() {
                return b;
            }
        };
    }
}
