package ch.claude_martin.function;

import static ch.claude_martin.function.Exceptions.getCause;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.*;

@FunctionalInterface
public interface BiFn<T, U, R> extends BiFunction<T, U, R> {

  public static <T, U, R> BiFn<T, U, R> of(final BiFunction<T, U, R> f) {
    return f::apply;
  }

  public static BiFn<Long, Long, Long> of(final LongBinaryOperator f) {
    return f::applyAsLong;
  }

  public static <T> BiFn<T, T, T> of(final BinaryOperator<T> f) {
    return f::apply;
  }

  public static <T, U> BiFn<T, U, Boolean> of(final BiPredicate<T, U> f) {
    return f::test;
  }

  public static BiFn<Double, Double, Double> of(final DoubleBinaryOperator f) {
    return f::applyAsDouble;
  }

  public static BiFn<Integer, Integer, Integer> of(final IntBinaryOperator f) {
    return f::applyAsInt;
  }

  public static <T, U> BiFn<T, U, Integer> of(final ToIntBiFunction<T, U> f) {
    return f::applyAsInt;
  }

  public static <T, U> BiFn<T, U, Double> of(final ToDoubleBiFunction<T, U> f) {
    return f::applyAsDouble;
  }

  public static <T, U> BiFn<T, U, Long> of(final ToLongBiFunction<T, U> f) {
    return f::applyAsLong;
  }

  public abstract R apply2(final T t, final U u);

  @Override
  public default R apply(final T t, final U u) {
    return this.apply2(t, u);
  }

  public default Fn<U, R> apply1(final T t) {
    return u -> this.apply2(t, u);
  }

  public default R applyPair(final Entry<? extends T, ? extends U> pair) {
    requireNonNull(pair, "pair");
    return this.apply(pair.getKey(), pair.getValue());
  }

  /** Ignores the result. */
  public default BiConsumer<T, U> toVoid() {
    return this::apply;
  }

  public default Fn2<T, U, R> curry() {
    return Functions.curry(this);
  }

  /** Converts {@code Entry<T,U,R>} to {@code Fn<Entry<T,U>,R>} */
  public default Fn<Entry<T, U>, R> uncurry() {
    return Functions.uncurryBi(this);
  }

  @Override
  default <V> BiFn<T, U, V> andThen(final Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (final T t, final U u) -> after.apply(apply(t, u));
  }

  public default BiFn<T, U, R> cached() {
    return Functions.cached(this);
  }

  public default BiFn<T, U, R> cached(final Supplier<Map<Entry<T, U>, R>> supplier) {
    return Functions.cached(this, supplier);
  }

  public default BiFn<T, U, R> sync() {
    return Functions.sync(this);
  }

  public default BiFn<T, U, R> sync(final Lock lock) {
    return Functions.sync(this, lock);
  }

  public default BiFn<T, U, R> sync(final Object mutex) {
    return Functions.sync(this, mutex);
  }

  public default Fn<T, R> set2nd(final U second) {
    return Functions.set2nd(this, second);
  }

  /** Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails. */
  public default BiFn<T, U, R> orElse(final R value) {
    return orElse(value, value);
  }

  /** Returns null if any exception is thrown. */
  public default BiFn<T, U, R> orNull() {
    return this.orElse(null, null);
  }

  public default BiFn<T, U, R> orElse(final R ifNull, final R ifException) {
    return orElseGet(() -> ifNull, t -> ifException);
  }

  public default BiFn<T, U, R> orElseGet(final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    return (t, u) -> {
      try {
        final R result = this.apply2(t, u);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(getCause(e));
      }
    };
  }

  public default BiFn<T, U, Maybe<R>> toMaybe() {
    return (t, u) -> {
      try {
        return Maybe.ofValue(this.apply2(t, u));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public default BiFn<T, U, Optional<R>> toOptional() {
    return (t, u) -> {
      try {
        return Optional.ofNullable(this.apply2(t, u));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  /** Creates a function that will have a name so that stack traces are easier to read. */
  public default BiFn<T, U, R> named(final String name) {
    requireNonNull(name, "name");
    return (t, u) -> {
      try {
        return this.apply2(t, u);
      } catch (final Throwable e) {
        throw new RuntimeException(Exceptions.getMessage(e, name), e);
      }
    };
  }

  public default BiFn<T, U, R> handle(final Consumer<Throwable> handler) {
    requireNonNull(handler, "handler");
    return (t, u) -> {
      try {
        return this.apply2(t, u);
      } catch (final Throwable e) {
        handler.accept(getCause(e));
        throw e;
      }
    };
  }

  public default BiFn<T, U, R> retry() {
    return Exceptions.retry(this);
  }

}
