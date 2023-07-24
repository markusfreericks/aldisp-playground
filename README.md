# Whats this all about?

The programming language ALDiSP was developed in the 1990s as a vehicle to succinctly describe real-time algorithms
usign anasynchronous functional paradigm. (In contrast, implementation was usually done imperatively, or in 
synchrononous functional style).

ALDiSP combined lots of ideas, one of them was unique: the *suspension* construct. Suspensions describe 
*call-by-availability* driven computation, in the same way that promises (the Scheme *delay* construct) 
describe *call by need*.

- A promise is a value that will be transparently evaluated when it is needed
- A suspension is an expression (or statement) that will be evaluated when a precondition is met (such as, an input value is present)

A list that is build up by promises is a (lazy) *stream*; a list that is build by suspensions is a *pipe*.

The suspension construct is coupled with a timing directive, where an "execution window" can be defined for a suspension, e.g.

```
suspend expr until cond within 0ms, 2ms
```

so when ```cond``` becomes true, the ```expr``` has to be executed within 2ms.


To transparently support Promises, the evaluator needs an *automatic forcing* mechanisms, i.e. if a primitive function 
needs the value of a promise, the promise is forced (evaluated and then cached).

To transparently support Suspensions, the evaluator needs to *automatically block*, i.e. if a 
primitive function needs the value of a suspension, it is suspended itself until the suspension's value is available.

In full ALDiSP, this is combined with automatic mapping of scalar functions to vectors, maps, streams, and pipes.

# The schedular problem

An interpreter/evaluator runtime for ALDiSP needs a scheduler for suspensions in its runtime; this is not a good idea
for "hard realtime" algorithms that need worst-case execution time guarantees. We had an idea how to solve it: the 
compiler has to determine whether an ALDiSP program has an effectively static schedule, and compile the schedule 
into the generated program. It can do so by simulating all execution traces, and determine worst-case schedules. 
This way it can prove whether the timing requirements defined by the suspensions can be fulfilled (and it can show 
where they won't). This "simulation" could be done by writing a partial evaluator.

# So what happened to ALDiSP

Lots of work went into a compiler for ALDiSP; the resulting piece of software (written in SML/NJ) was much too slow 
for any real use (work on the compiler ended in 1994, the biggest machine we had was a 40Mhz Sun workstation with a 
whopping 64Mb of RAM). We tried to prove the workability of the approach, and managed to trace a few very simple 
programs, but the compiler was buggy and never fully implemented the ALDISP language.

At least I got a PhD thesis out of this.

# So this here is ...?

I recently was contacted by my PhD advisor, Prof. Alois Knoll, to see whether any new work can be done with ALDiSP.

To re-engage with the language, I wanted to refresh myself on the central concept of suspensions, by writing a 
suspension runtime im Java (since that is the language I use daily at work).
