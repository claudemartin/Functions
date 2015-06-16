package ch.claude_martin.function;

import static ch.claude_martin.function.Exceptions.getCause;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.*;

import ch.claude_martin.function.tuple.Pair;
import ch.claude_martin.function.tuple.Quad;
import ch.claude_martin.function.tuple.Triplet;
import ch.claude_martin.function.tuple.Tuple;

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

  /** Applies all elements of the given tuple. This fails if the given tuple doesn't have the correct
   * arity. */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public default Object applyTuple(final Tuple<?> t) {
    Object f = this;
    for (int i = 0; i < t.arity(); i++)
      f = ((Fn) f).apply(t.get(i));
    return f;
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

  /** This only works if {@code T extends Triplet<A,B,C>}. */
  @SuppressWarnings("unchecked")
  public default <A, B, C> Fn3<A, B, C, R> curry3() {
    return Functions.curry3((Function<Triplet<A, B, C>, R>) this);
  }

  /** This only works if {@code T extends Quad<A,B,C,D>}. */
  @SuppressWarnings("unchecked")
  public default <A, B, C, D> Fn4<A, B, C, D, R> curry4() {
    return Functions.curry4((Function<Quad<A, B, C, D>, R>) this);
  }

  /** This only works if {@code T extends Entry<A,B>}. */
  @SuppressWarnings("unchecked")
  public default <A, B> BiFn<A, B, R> toBiFn() {
    return Functions.toBiFunction2((Function<Entry<A, B>, R>) this);
  }

  /** This only works if {@code T extends Triplet<A,B,C>}. */
  @SuppressWarnings("unchecked")
  public default <A, B, C> TriFn<A, B, C, R> toTriFn() {
    return (s, t, u) -> ((Function<Triplet<A, B, C>, R>) this).apply(Triplet.of(s, t, u));
  }

  /** This only works if {@code T extends Quad<A,B,C,D>}. */
  @SuppressWarnings("unchecked")
  public default <A, B, C, D> QuadFn<A, B, C, D, R> toQuadFn() {
    return (s, t, u, v) -> ((Function<Quad<A, B, C, D>, R>) this).apply(Quad.of(s, t, u, v));
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

  /** Returns value if any exception is thrown or if result is null.
   * 
   * @param value
   *          Value to be used on exception or if result us null
   * @return Function what never fails. */
  public default Fn<T, R> orElse(final R value) {
    return orElse(value, value);
  }

  /** Returns null if any exception is thrown. */
  public default Fn<T, R> orNull() {
    return this.orElse(null, null);
  }

  public default Fn<T, R> orElse(final R ifNull, final R ifException) {
    return orElseGet(() -> ifNull, t -> ifException);
  }

  public default Fn<T, R> orElseGet(final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    return t -> {
      try {
        final R result = this.apply(t);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(getCause(e));
      }
    };
  }

  public default Fn<T, Maybe<R>> toMaybe() {
    return t -> {
      try {
        return Maybe.ofValue(this.apply(t));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public default Fn<T, Optional<R>> toOptional() {
    return t -> {
      try {
        return Optional.ofNullable(this.apply(t));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  /** Creates a function that will have a name so that stack traces are easier to read. */
  public default Fn<T, R> named(final String name) {
    requireNonNull(name, "name");
    return t -> {
      try {
        return this.apply(t);
      } catch (final Throwable e) {
        throw new RuntimeException(Exceptions.getMessage(e, name), e);
      }
    };
  }

  public default Fn<T, R> handle(final Consumer<Throwable> handler) {
    requireNonNull(handler, "handler");
    return t -> {
      try {
        return this.apply(t);
      } catch (final Throwable e) {
        handler.accept(getCause(e));
        throw e;
      }
    };
  }

  public default Fn<T, R> retry() {
    return Exceptions.retry(this);
  }

  public static <T> Fn<T, T> identity() {
    return t -> t;
  }

}
