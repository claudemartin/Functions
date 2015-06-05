package ch.claude_martin.function;

import ch.claude_martin.function.tuple.Quad;

@FunctionalInterface
public interface QuadFn<T, U, V, W, R> {

  public default TriFn<U, V, W, R> apply1(final T t) {
    return (u, v, w) -> this.apply4(t, u, v, w);
  }

  public default BiFn<V, W, R> apply2(final T t, final U u) {
    return (v, w) -> this.apply4(t, u, v, w);
  }

  public default Fn<W, R> apply3(final T t, final U u, final V v) {
    return w -> this.apply4(t, u, v, w);
  }

  public abstract R apply4(final T t, final U u, final V v, final W w);

  public default Fn4<T, U, V, W, R> curry() {
    return Functions.curry(this);
  }

  /** Converts {@code Quad<T,U,V,W,R>} to {@code Fn<Quad<T,U,V,W>,R>} */
  public default Fn<Quad<T, U, V, W>, R> uncurry() {
    return q -> this.apply4(q._1(), q._2(), q._3(), q._4());
  }

  public default QuadFn<T, U, V, W, R> cached() {
    return Functions.cached(this);
  }

  public default TriFn<T, V, W, R> set2nd(final U second) {
    return Functions.set2nd(this, second);
  }

  public default TriFn<T, U, W, R> set3rd(final V third) {
    return Functions.set3rd(this, third);
  }

  public default TriFn<T, U, V, R> set4th(final W fourth) {
    return Functions.set4th(this, fourth);
  }

}
