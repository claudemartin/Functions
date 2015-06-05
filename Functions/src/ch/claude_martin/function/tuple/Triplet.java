package ch.claude_martin.function.tuple;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import ch.claude_martin.function.tuple.TripletImpl.UniTripletImpl;

public interface Triplet<A, B, C> extends Tuple<Triplet<A, B, C>>, Serializable {
  @SuppressWarnings({ "rawtypes" })
  final static Comparator<Triplet> COMPARATOR = //
  Comparator
  .<Triplet, Comparable> comparing(
      e -> (Comparable) e._1(),
      Comparator.nullsFirst(Comparator
          .naturalOrder()))
          .thenComparing(
              Comparator.comparing(
                  e -> (Comparable) e._2(),
                  Comparator.nullsFirst(Comparator
                      .naturalOrder())))
                      .thenComparing(
                          Comparator.comparing(
                              e -> (Comparable) e._3(),
                              Comparator.nullsFirst(Comparator
                                  .naturalOrder())));

  public A _1();

  public B _2();

  public C _3();

  @Override
  public default int arity() {
    return 3;
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
    }
    throw new IndexOutOfBoundsException();
  }

  @Override
  public default Object[] toArray() {
    return new Object[] { _1(), _2(), _3() };
  }

  public static <A, B, C> Triplet<A, B, C> of(final A a, final B b, final C c) {
    return new TripletImpl<>(a, b, c);
  }

  public static <T> Triplet<T, T, T> uniform(final T a, final T b, final T c) {
    return new UniTripletImpl<>(a, b, c);
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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static int compare(final Triplet a, final Triplet b) {
    return Objects.compare(a, b, COMPARATOR);
  }

  public static int hash(final Entry<?, ?> e) {
    requireNonNull(e, "e");
    final Object k = e.getKey(), v = e.getValue();
    return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
  }

  public static <T, U, R> Triplet<T, U, R> zip(final T t, final U u,
      final BiFunction<? super T, ? super U, ? extends R> f) {
    requireNonNull(f, "f");
    return of(t, u, f.apply(t, u));
  }

  public static <T, U, R> Triplet<T, U, R> zip(final T t, final U u,
      final Function<? super T, ? extends Function<? super U, ? extends R>> f) {
    requireNonNull(f, "f");
    return of(t, u, f.apply(t).apply(u));
  }

  public default <R> R applyTo(
      final Function<? super A, ? extends Function<? super B, ? extends Function<? super C, ? extends R>>> f) {
    requireNonNull(f, "f");
    return f.apply(this._1()).apply(this._2()).apply(this._3());
  }

  /** Pair with two elements of the same type. */
  public static interface UniTriplet<T> extends Triplet<T, T, T> {
    public static <T> UniTriplet<T> of(final T t1, final T t2, final T t3) {
      return new TripletImpl.UniTripletImpl<>(t1, t2, t3);
    }
    public default <R> UniTriplet<R> map(final Function<? super T, ? extends R> f) {
      requireNonNull(f, "f");
      return of(f.apply(this._1()), f.apply(_2()), f.apply(_3()));
    }
  }
}
