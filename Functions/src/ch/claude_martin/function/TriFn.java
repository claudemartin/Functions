package ch.claude_martin.function;

import static ch.claude_martin.function.Exceptions.getCause;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import ch.claude_martin.function.tuple.Triplet;

@FunctionalInterface
public interface TriFn<T, U, V, R> {

  public default BiFn<U, V, R> apply1(final T t) {
    return (u, v) -> this.apply3(t, u, v);
  }

  public default Fn<V, R> apply2(final T t, final U u) {
    return v -> this.apply3(t, u, v);
  }

  public abstract R apply3(final T t, final U u, final V v);

  public default Fn3<T, U, V, R> curry() {
    return Functions.curry(this);
  }

  /** Converts {@code Triplet<T,U,V,R>} to {@code Fn<Triplet<T,U,V>,R>} */
  public default Fn<Triplet<T, U, V>, R> uncurry() {
    return Functions.uncurryTri(this);
  }

  public default TriFn<T, U, V, R> cached() {
    return Functions.cached(this);
  }

  public default TriFn<T, U, V, R> sync() {
    return this.sync(new ReentrantLock());
  }

  public default TriFn<T, U, V, R> sync(final Lock lock) {
    requireNonNull(lock, "lock");
    return (t, u, v) -> {
      lock.lock();
      try {
        return this.apply3(t, u, v);
      } finally {
        lock.unlock();
      }
    };
  }

  public default TriFn<T, U, V, R> sync(final Object mutex) {
    requireNonNull(mutex, "mutex");
    if (mutex instanceof Lock)
      throw new RuntimeException("Mutex implements Lock, but wasn't used as such.");
    return (t, u, v) -> {
      synchronized (mutex) {
        return this.apply3(t, u, v);
      }
    };
  }

  public default BiFn<T, V, R> set2nd(final U second) {
    return Functions.set2nd(this, second);
  }

  public default BiFn<T, U, R> set3rd(final V third) {
    return Functions.set3rd(this, third);
  }

  /** Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails. */
  public default TriFn<T, U, V, R> orElse(final R value) {
    return orElse(value, value);
  }

  /** Returns null if any exception is thrown. */
  public default TriFn<T, U, V, R> orNull() {
    return this.orElse(null, null);
  }

  public default TriFn<T, U, V, R> orElse(final R ifNull, final R ifException) {
    return orElseGet(() -> ifNull, t -> ifException);
  }

  public default TriFn<T, U, V, R> orElseGet(final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    return (t, u, v) -> {
      try {
        final R result = this.apply3(t, u, v);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(getCause(e));
      }
    };
  }

  public default TriFn<T, U, V, Maybe<R>> toMaybe() {
    return (t, u, v) -> {
      try {
        return Maybe.ofValue(this.apply3(t, u, v));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public default TriFn<T, U, V, Optional<R>> toOptional() {
    return (t, u, v) -> {
      try {
        return Optional.ofNullable(this.apply3(t, u, v));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  /** Creates a function that will have a name so that stack traces are easier to read. */
  public default TriFn<T, U, V, R> named(final String name) {
    requireNonNull(name, "name");
    return (t, u, v) -> {
      try {
        return this.apply3(t, u, v);
      } catch (final Throwable e) {
        throw new RuntimeException(Exceptions.getMessage(e, name), e);
      }
    };
  }

  public default TriFn<T, U, V, R> handle(final Consumer<Throwable> handler) {
    requireNonNull(handler, "handler");
    return (t, u, v) -> {
      try {
        return this.apply3(t, u, v);
      } catch (final Throwable e) {
        handler.accept(getCause(e));
        throw e;
      }
    };
  }

  public default TriFn<T, U, V, R> retry() {
    return (t, u, v) -> {
      final Thread thread = Thread.currentThread();
      while (true)
        try {
          if (thread.isInterrupted())
            throw new RuntimeException("Thread was interrupted", new InterruptedException());
          return this.apply3(t, u, v);
        } catch (final Throwable e) {
        }
    };
  }
}
