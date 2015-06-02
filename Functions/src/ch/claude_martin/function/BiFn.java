package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface BiFn<T, U, R> extends BiFunction<T, U, R> {

  /** Ignores the result. */
  public default BiConsumer<T, U> toVoid() {
    return this::apply;
  }

  public default Fn<T, Fn<U, R>> curry() {
    return Functions.curry(this);
  }

  public default Fn<Entry<T, U>, R> uncurry() {
    return Functions.uncurry2(this);
  }

  public default BiFn<T, U, R> sneaky() {
    return Exceptions.sneaky(this::apply);
  }

  /**
   * Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails.
   */
  public default BiFn<T, U, R> orElse(final R value) {
    requireNonNull(value, "value");
    return Exceptions.orElse(this::apply, value).andThen((final R r) -> r == null ? value : r);
  }

  /**
   * Returns null if any exception is thrown.
   */
  public default BiFn<T, U, R> orNull() {
    return Exceptions.orNull(this::apply);
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
}
