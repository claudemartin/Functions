package ch.claude_martin.function.tuple;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;

/** @see Pair */
class PairImpl<K, V> implements Pair<K, V> {

  /** @see UniPair */
  static class UniPairImpl<T> extends PairImpl<T, T> implements UniPair<T> {
    private static final long serialVersionUID = 2419097586198467624L;
    public UniPairImpl(final T k, final T v) {
      super(k, v);
    }
  }

  public PairImpl(final K k, final V v) {
    super();
    this.k = k;
    this.v = v;
  }

  private final K           k;
  private final V           v;
  private static final long serialVersionUID = -4728534348015729052L;

  @Override
  public K getKey() {
    return this.k;
  }

  @Override
  public V getValue() {
    return this.v;
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Entry))
      return false;
    final Entry<?, ?> e = (Entry<?, ?>) o;
    return Objects.equals(this.k, e.getKey()) && Objects.equals(this.v, e.getValue());
  }

  @Override
  public int hashCode() {
    return (this.k == null ? 0 : this.k.hashCode()) ^ (this.v == null ? 0 : this.v.hashCode());
  }

  @Override
  public String toString() {
    return this.k + "=" + this.v;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public int compareTo(final Entry<K, V> o) {
    return Objects.compare(this, o, (Comparator) comparator);
  }
}
