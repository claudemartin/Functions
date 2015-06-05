package ch.claude_martin.function;

import ch.claude_martin.function.tuple.Quad;

/** {@code f : T -> U -> V -> W -> R } */
@FunctionalInterface
public interface Fn4<T, U, V, W, R> extends Fn<T, Fn3<U, V, W, R>> {
  public static <T, U, V, W, R> Fn4<T, U, V, W, R> of(final QuadFn<T, U, V, W, R> f) {
    return Functions.curry(f::apply4);
  }

  public default Fn2<V, W, R> apply2(final T t, final U u) {
    return this.apply(t).apply(u);
  }

  public default Fn<W, R> apply3(final T t, final U u, final V v) {
    return this.apply(t).apply(u).apply(v);
  }

  public default R apply4(final T t, final U u, final V v, final W w) {
    return this.apply(t).apply(u).apply(v).apply(w);
  }

  public default QuadFn<T, U, V, W, R> toQuadFn() {
    return this::apply4;
  }

  /** Converts {@code Fn4<T,U,V,W,R>} to {@code Fn<Quad<T,U,V,W>,R>} */
  public default Fn<Quad<T, U, V, W>, R> uncurry() {
    return Functions.uncurry4(this);
  }
}