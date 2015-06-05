package ch.claude_martin.function.tuple;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import ch.claude_martin.function.Functions;

/** This describes a tuple, based on {@link Entry}, that is comparable, iff both elements are
 * comparable. This allows null for key and value but is supposed to be immutable.
 * 
 * @author Claude Martin
 *
 * @param <K>
 *          The type of the first element (key).
 * @param <V>
 *          The type of the second element (value). */
public interface Pair<K, V> extends Entry<K, V>, Comparable<Entry<K, V>>, Serializable {
  @SuppressWarnings({ "rawtypes" })
  final static Comparator<Pair> comparator = //
  Comparator.<Pair, Comparable> comparing(
      e -> (Comparable) e.getKey(),
      Comparator.nullsFirst(Comparator.naturalOrder()))//
      .thenComparing(
          Comparator.comparing(e -> (Comparable) e
              .getValue(), Comparator
              .nullsFirst(Comparator.naturalOrder())));

  @SuppressWarnings("unchecked")
  public static <K, V> Pair<K, V> of(final Entry<? extends K, ? extends V> e) {
    requireNonNull(e, "e");
    if (e instanceof Pair)
      return (Pair<K, V>) e; // ok, because it's immutable.
    return of(e.getKey(), e.getValue());
  }

  public static <K, V> Pair<K, V> of(final K k, final V v) {

    return new PairImpl<>(k, v);

  }


  /** Tries to compare two entries. This fails if any element is not comparable, however
   * <tt>null</tt> is allowed. The given entries no not need to implement {@link Comparable}, but
   * the keys and values have to.
   * 
   * @param a
   *          first entry
   * @param b
   *          second entry
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *         equal to, or greater than the second. */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static int compare(final Entry a, final Entry b) {
    return Objects.compare(a, b, (Comparator) comparator);
  }

  public static int hash(final Entry<?, ?> e) {
    requireNonNull(e, "e");
    final Object k = e.getKey(), v = e.getValue();
    return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
  }

  public static <K, V> Pair<K, V> zip(final K k, final Function<? super K, ? extends V> f) {
    requireNonNull(f, "f");
    return of(k, f.apply(k));
  }

  public default K _1() {
    return this.getKey();
  }

  public default V _2() {
    return this.getValue();
  }

  @Override
  public default V setValue(final V value) {
    throw new UnsupportedOperationException();
  }

  public default Pair<V, K> swap() {
    return Functions.toPair(this.getValue(), this.getKey());
  };

  public default <R> R applyTo(final BiFunction<? super K, ? super V, ? extends R> f) {
    requireNonNull(f, "f");
    return f.apply(this.getKey(), this.getValue());
  }

  public default <R> R applyTo(
      final Function<? super K, ? extends Function<? super V, ? extends R>> f) {
    requireNonNull(f, "f");
    return f.apply(this.getKey()).apply(this.getValue());
  }

  public default <K2, V2> Pair<K2, V2> map(final Function<? super K, ? extends K2> k,
      final Function<? super V, ? extends V2> v) {
    requireNonNull(k, "k");
    requireNonNull(v, "v");
    return of(k.apply(this.getKey()), v.apply(getValue()));
  }

  /** Pair with two elements of the same type. */
  public static interface UniPair<T> extends Pair<T, T> {
    public static <T> UniPair<T> of(final T t1, final T t2) {
      return new PairImpl.UniPairImpl<>(t1, t2);
    }

    public default <R> Pair<R, R> map(final Function<? super T, ? extends R> f) {
      requireNonNull(f, "f");
      return of(f.apply(this.getKey()), f.apply(getValue()));
    }

    public default T applyTo(final BinaryOperator<T> op) {
      requireNonNull(op, "op");
      return op.apply(getKey(), getValue());
    }

    @Override
    public default UniPair<T> swap() {
      return of(getValue(), getKey());
    }
  }
}