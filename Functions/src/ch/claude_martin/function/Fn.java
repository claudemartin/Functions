package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.*;

/** Functional interface like {@link Function} but with shorter name and some more methods.
 * 
 * @author Claude martin
 * @param <T>
 *          the type of the input to the function
 * @param <R>
 *          the type of the result of the function
 * @see Function */
@FunctionalInterface
public interface Fn<T, R> extends Function<T, R> {

  public static <T, R> Fn<T, R> of(final Function<T, R> f) {
    return f::apply;
  }

  public static <T> Fn<Integer, T> of(final IntFunction<T> f) {
    return f::apply;
  }

  public static Fn<Integer, Long> of(final IntToLongFunction f) {
    return f::applyAsLong;
  }

  public static Fn<Integer, Double> of(final IntToDoubleFunction f) {
    return f::applyAsDouble;
  }

  public static <T> Fn<Double, T> of(final DoubleFunction<T> f) {
    return f::apply;
  }

  public static Fn<Double, Long> of(final DoubleToLongFunction f) {
    return f::applyAsLong;
  }

  public static Fn<Double, Integer> of(final DoubleToIntFunction f) {
    return f::applyAsInt;
  }

  public static <T> Fn<Long, T> of(final LongFunction<T> f) {
    return f::apply;
  }

  public static Fn<Long, Integer> of(final LongToIntFunction f) {
    return f::applyAsInt;
  }

  public static Fn<Long, Double> of(final LongToDoubleFunction f) {
    return f::applyAsDouble;
  }

  public static <T> Fn<T, Long> of(final ToLongFunction<T> f) {
    return f::applyAsLong;
  }

  public static <T> Fn<T, Double> of(final ToDoubleFunction<T> f) {
    return f::applyAsDouble;
  }

  public static <T> Fn<T, T> of(final UnaryOperator<T> f) {
    return f::apply;
  }

  public static Fn<Integer, Integer> of(final IntUnaryOperator f) {
    return f::applyAsInt;
  }

  public static Fn<Long, Long> of(final LongUnaryOperator f) {
    return f::applyAsLong;
  }

  public static Fn<Double, Double> of(final DoubleUnaryOperator f) {
    return f::applyAsDouble;
  }

  public static <T> Fn<T, Boolean> of(final Predicate<T> f) {
    return f::test;
  }

  /** Shorter synonym for {@link #apply(Object)}. */
  public default R a(final T t) {
    return this.apply(t);
  }

  /** Ignores the result. */
  public default Consumer<T> toVoid() {
    return this::apply;
  }

  /** This only works if {@code T extends Entry<A,B>}. */
  @SuppressWarnings("unchecked")
  public default <A, B> Fn2<A, B, R> curry() {
    return Functions.curry((Function<Entry<A, B>, R>) this);
  }

  /** This only works if {@code T extends Entry<A,B>}. */
  @SuppressWarnings("unchecked")
  public default <A, B> BiFunction<A, B, R> toBiFunction2() {
    return Functions.toBiFunction2((Function<Entry<A, B>, R>) this);
  }

  public default Fn<T, R> sneaky() {
    return this;
  }

  /** Creates a function that will have a name so that stack traces are easier to read. This must be
   * used after any other exceptions-related modifications of the function. */
  public default Fn<T, R> named(final String name) {
    return Exceptions.named(this, name);
  }

  /** Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails. */
  public default Fn<T, R> orElse(final R value) {
    requireNonNull(value, "value");
    return Exceptions.orElse(this::apply, value).andThen((final R r) -> r == null ? value : r);
  }

  /** Returns null if any exception is thrown. */
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

  public default Fn<T, R> cached(final Supplier<Map<T, R>> supplier) {
    return Functions.cached(this, supplier);
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

  /** Applies t to this and returns a pair of t and the result.
   * <p>
   * Can be used to map to pairs: <br/>
   * {@code coll.stream().map(f::paired);  */
  public default Pair<T, R> paired(final T t) {
    return Pair.of(t, this.apply(t));
  }

  public static <T> Fn<T, T> identity() {
    return t -> t;
  }


}
