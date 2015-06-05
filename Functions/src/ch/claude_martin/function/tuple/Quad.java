package ch.claude_martin.function.tuple;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import ch.claude_martin.function.Fn3;
import ch.claude_martin.function.QuadFn;

public interface Quad<A, B, C, D> extends Tuple<Quad<A, B, C, D>>, Serializable {

  @SuppressWarnings({ "rawtypes" })
  final static Comparator<Quad> COMPARATOR = //
  Comparator
  .<Quad, Comparable> comparing(
      e -> (Comparable) e._1(), NULLS_FIRST)
      .thenComparing(
          Comparator.comparing(e -> (Comparable) e._2(),
              NULLS_FIRST))
              .thenComparing(
                  Comparator.comparing(e -> (Comparable) e._3(),
                      NULLS_FIRST))
                      .thenComparing(
                          Comparator.comparing(e -> (Comparable) e._4(),
                              NULLS_FIRST));

  public A _1();

  public B _2();

  public C _3();

  public D _4();

  @Override
  public default int arity() {
    return 4;
  }

  @Override
  public default Object get(final int index) {
    switch (index) {
    case 0:
      return _1();
    case 1:
      return _2();
    case 2:
      return _3();
    case 3:
      return _4();
    }
    throw new IndexOutOfBoundsException();
  }

  @Override
  public default Object[] toArray() {
    return new Object[] { _1(), _2(), _3(), _4() };
  }

  public static <A, B, C, D> Quad<A, B, C, D> of(final A a, final B b, final C c, final D d) {
    return new QuadImpl<>(a, b, c, d);
  }

  /** Tries to compare two triplets. This fails if any element is not comparable, however
   * <tt>null</tt> is allowed. The given triplets no not need to implement {@link Comparable}, but
   * the keys and values have to.
   * 
   * @param a
   *          first entry
   * @param b
   *          second entry
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *         equal to, or greater than the second. */
  @SuppressWarnings({ "rawtypes" })
  public static int compare(final Quad a, final Quad b) {
    return Objects.compare(a, b, COMPARATOR);
  }

  public static int hash(final Entry<?, ?> e) {
    requireNonNull(e, "e");
    final Object k = e.getKey(), v = e.getValue();
    return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
  }

  public static <T, U, V, R> Quad<T, U, V, R> zip(final T t, final U u, final V v,
      final Fn3<? super T, ? super U, ? super V, ? extends R> f) {
    requireNonNull(f, "f");
    return of(t, u, v, f.apply3(t, u, v));
  }

  public static <T, U, V, R> Quad<T, U, V, R> zip(
      final T t,
      final U u,
      final V v,
      final Function<? super T, ? extends Function<? super U, ? extends Function<? super V, ? extends R>>> f) {
    requireNonNull(f, "f");
    return of(t, u, v, f.apply(t).apply(u).apply(v));
  }

  public default <R> R applyTo(
      final Function<? super A, ? extends Function<? super B, ? extends Function<? super C, ? extends R>>> f) {
    requireNonNull(f, "f");
    return f.apply(this._1()).apply(this._2()).apply(this._3());
  }

  public default <R> R applyTo(final QuadFn<A, B, C, D, R> f) {
    requireNonNull(f, "f");
    return f.apply4(this._1(), this._2(), this._3(), this._4());
  }

  /** Pair with two elements of the same type. */
  public static interface UniQuad<T> extends Quad<T, T, T, T> {
    public static <T> UniQuad<T> of(final T t1, final T t2, final T t3, final T t4) {
      return new QuadImpl.UniQuadImpl<>(t1, t2, t3, t4);
    }

    public default <R> UniQuad<R> map(final Function<? super T, ? extends R> f) {
      requireNonNull(f, "f");
      return of(f.apply(this._1()), f.apply(_2()), f.apply(_3()), f.apply(_4()));
    }
  }
}
