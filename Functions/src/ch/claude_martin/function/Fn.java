package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Functional interface like {@link Function} but with shorter name and some more methods.
 * 
 * @author Claude martin
 * @param <T>
 *          the type of the input to the function
 * @param <R>
 *          the type of the result of the function
 * @see Function
 */
@FunctionalInterface
public interface Fn<T, R> extends Function<T, R> {

  /** Shorter synonym for {@link #apply(Object)}. */
  public default R a(final T t) {
    return this.apply(t);
  }

  /** Ignores the result. */
  public default Consumer<T> toVoid(final Function<T, R> f) {
    return f::apply;
  }

  /** This only works if R extends {@link Function}. */
  @SuppressWarnings("unchecked")
  public default <U> Fn<Entry<T, U>, R> uncurry() {
    return Functions.uncurry((Function<T, Function<U, R>>) this);
  }

  /** This only works if {@code T extends Entry<A,B>}. */
  @SuppressWarnings("unchecked")
  public default <A, B> Fn<A, Fn<B, R>> curry() {
    return Functions.curry((Function<Entry<A, B>, R>) this);
  }

  /** This only works if R extends {@link Function}. */
  @SuppressWarnings("unchecked")
  public default <U> BiFunction<T, U, R> toBiFunction() {
    return Functions.toBiFunction((Function<T, Function<U, R>>) this);
  }

  /** This only works if {@code T extends Entry<A,B>}. */
  @SuppressWarnings("unchecked")
  public default <A, B> BiFunction<A, B, R> toBiFunction2() {
    return Functions.toBiFunction2((Function<Entry<A, B>, R>) this);
  }

  public default Fn<T, R> sneaky() {
    return Exceptions.sneaky(this::apply);
  }

  /**
   * Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails.
   */
  public default Fn<T, R> orElse(final R value) {
    requireNonNull(value, "value");
    return Exceptions.orElse(this::apply, value).andThen((final R r) -> r == null ? value : r);
  }

  /**
   * Returns null if any exception is thrown.
   */
  public default Fn<T, R> orNull() {
    return Exceptions.orNull(this::apply);
  }

  @Override
  public default <V> Fn<T, V> andThen(final Function<? super R, ? extends V> after) {
    return (final T t) -> after.apply(apply(t));
  }

  @Override
  public default <V> Fn<V, R> compose(final Function<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return (final V v) -> apply(before.apply(v));
  }

  public default <V, W> Fn<W, R> compose(final Function<? super V, ? extends T> f,
      final Function<? super W, ? extends V> g) {
    return Functions.compose(this, f, g);
  }

  public default <V, W, X> Fn<X, R> compose(final Function<? super V, ? extends T> f,
      final Function<? super W, ? extends V> g, final Function<? super X, ? extends W> i) {
    return Functions.compose(this, f, g, i);
  }

  public default Fn<T, R> cached() {
    return Functions.cached(this);
  }

  public default Fn<T, R> sync() {
    return Functions.sync(this);
  }

  public default Fn<T, R> sync(final Lock lock) {
    return Functions.sync(this, lock);
  }

  public default Fn<T, R> sync(final Object mutex) {
    return Functions.sync(this, mutex);
  }

  public default Fn<T, R> nonNull() {
    return Functions.nonNull(this);
  }

  public static <T> Fn<T, T> identity() {
    return t -> t;
  }

}
