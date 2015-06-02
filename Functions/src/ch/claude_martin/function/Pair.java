package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This describes a tuple, based on {@link Entry}, that is comparable, iff both elements are
 * comparable. This allows null for key and value but is supposed to be immutable.
 * 
 * @author Claude Martin
 *
 * @param <K>
 *          The type of the first element (key).
 * @param <V>
 *          The type of the second element (value).
 */
public interface Pair<K, V> extends Entry<K, V>, Comparable<Entry<K, V>>, Serializable {
  @SuppressWarnings({ "rawtypes" })
  final static Comparator<Pair> comparator = //
  Comparator.<Pair, Comparable> comparing(e -> (Comparable) e.getKey(),
      Comparator.nullsFirst(Comparator.naturalOrder()))//
      .thenComparing(
          Comparator.comparing(e -> (Comparable) e.getValue(),
              Comparator.nullsFirst(Comparator.naturalOrder())));

  public static <K, V> Pair<K, V> of(final K k, final V v) {

    return new Pair<K, V>() {
      private static final long serialVersionUID = -4728534348015729052L;

      @Override
      public K getKey() {
        return k;
      }

      @Override
      public V getValue() {
        return v;
      }

      @Override
      public boolean equals(final Object o) {
        if (!(o instanceof Entry))
          return false;
        final Entry<?, ?> e = (Entry<?, ?>) o;
        return Objects.equals(k, e.getKey()) && Objects.equals(v, e.getValue());
      }

      @Override
      public int hashCode() {
        return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
      }

      @Override
      public String toString() {
        return k + "=" + v;
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public int compareTo(final Entry<K, V> o) {
        return Objects.compare(this, o, (Comparator) comparator);
      }
    };

  }

  /**
   * Tries to compare two entries. This fails if any element is not comparable, however
   * <tt>null</tt> is allowed. The given entries no not need to implement {@link Comparable}, but
   * the keys and values have to.
   * 
   * @param a
   *          first entry
   * @param b
   *          second entry
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *         equal to, or greater than the second.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static int compare(final Entry a, final Entry b) {
    return Objects.compare(a, b, (Comparator) comparator);
  }

  public static int hash(final Entry<?, ?> e) {
    requireNonNull(e, "e");
    final Object k = e.getKey(), v = e.getValue();
    return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
  }

  public static <K, V> Pair<K, V> zip(final K k, final Function<K, V> f) {
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

  public default <R> R applyTo(final BiFunction<K, V, R> f) {
    requireNonNull(f, "f");
    return f.apply(this.getKey(), this.getValue());
  }

  public default <R> R applyTo(final Function<K, Function<V, R>> f) {
    requireNonNull(f, "f");
    return f.apply(this.getKey()).apply(this.getValue());
  }

  public default <K2, V2> Pair<K2, V2> map(final Function<K, K2> f, final Function<V, V2> g) {
    requireNonNull(f, "f");
    requireNonNull(g, "g");
    return of(f.apply(this.getKey()), g.apply(getValue()));
  }
}