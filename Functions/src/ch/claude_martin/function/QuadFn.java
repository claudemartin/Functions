package ch.claude_martin.function;

import static ch.claude_martin.function.Exceptions.getCause;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import ch.claude_martin.function.tuple.Quad;

@FunctionalInterface
public interface QuadFn<T, U, V, W, R> {

  public default TriFn<U, V, W, R> apply1(final T t) {
    return (u, v, w) -> this.apply4(t, u, v, w);
  }

  public default BiFn<V, W, R> apply2(final T t, final U u) {
    return (v, w) -> this.apply4(t, u, v, w);
  }

  public default Fn<W, R> apply3(final T t, final U u, final V v) {
    return w -> this.apply4(t, u, v, w);
  }

  public abstract R apply4(final T t, final U u, final V v, final W w);

  public default Fn4<T, U, V, W, R> curry() {
    return Functions.curry(this);
  }

  /** Converts {@code Quad<T,U,V,W,R>} to {@code Fn<Quad<T,U,V,W>,R>} */
  public default Fn<Quad<T, U, V, W>, R> uncurry() {
    return q -> this.apply4(q._1(), q._2(), q._3(), q._4());
  }

  public default QuadFn<T, U, V, W, R> cached() {
    return Functions.cached(this);
  }

  public default TriFn<T, V, W, R> set2nd(final U second) {
    return Functions.set2nd(this, second);
  }

  public default TriFn<T, U, W, R> set3rd(final V third) {
    return Functions.set3rd(this, third);
  }

  public default TriFn<T, U, V, R> set4th(final W fourth) {
    return Functions.set4th(this, fourth);
  }

  public default QuadFn<T, U, V, W, R> sync() {
    return this.sync(new ReentrantLock());
  }

  public default QuadFn<T, U, V, W, R> sync(final Lock lock) {
    requireNonNull(lock, "lock");
    return (t, u, v, w) -> {
      lock.lock();
      try {
        return this.apply4(t, u, v, w);
      } finally {
        lock.unlock();
      }
    };
  }

  public default QuadFn<T, U, V, W, R> sync(final Object mutex) {
    requireNonNull(mutex, "mutex");
    if (mutex instanceof Lock)
      throw new RuntimeException("Mutex implements Lock, but wasn't used as such.");
    return (t, u, v, w) -> {
      synchronized (mutex) {
        return this.apply4(t, u, v, w);
      }
    };
  }

  /** Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails. */
  public default QuadFn<T, U, V, W, R> orElse(final R value) {
    return orElse(value, value);
  }

  /** Returns null if any exception is thrown. */
  public default QuadFn<T, U, V, W, R> orNull() {
    return this.orElse(null, null);
  }

  public default QuadFn<T, U, V, W, R> orElse(final R ifNull, final R ifException) {
    return orElseGet(() -> ifNull, t -> ifException);
  }

  public default QuadFn<T, U, V, W, R> orElseGet(final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    return (t, u, v, w) -> {
      try {
        final R result = this.apply4(t, u, v, w);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(getCause(e));
      }
    };
  }

  public default QuadFn<T, U, V, W, Maybe<R>> toMaybe() {
    return (t, u, v, w) -> {
      try {
        return Maybe.ofValue(this.apply4(t, u, v, w));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public default QuadFn<T, U, V, W, Optional<R>> toOptional() {
    return (t, u, v, w) -> {
      try {
        return Optional.ofNullable(this.apply4(t, u, v, w));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  /** Creates a function that will have a name so that stack traces are easier to read. */
  public default QuadFn<T, U, V, W, R> named(final String name) {
    requireNonNull(name, "name");
    return (t, u, v, w) -> {
      try {
        return this.apply4(t, u, v, w);
      } catch (final Throwable e) {
        throw new RuntimeException(Exceptions.getMessage(e, name), getCause(e));
      }
    };
  }

  public default QuadFn<T, U, V, W, R> handle(final Consumer<Throwable> handler) {
    requireNonNull(handler, "handler");
    return (t, u, v, w) -> {
      try {
        return this.apply4(t, u, v, w);
      } catch (final Throwable e) {
        handler.accept(getCause(e));
        throw e;
      }
    };
  }

  public default QuadFn<T, U, V, W, R> retry() {
    return (t, u, v, w) -> {
      final Thread thread = Thread.currentThread();
      while (true)
        try {
          if (thread.isInterrupted())
            throw new RuntimeException("Thread was interrupted", new InterruptedException());
          return this.apply4(t, u, v, w);
        } catch (final Throwable e) {
        }
    };
  }

}
