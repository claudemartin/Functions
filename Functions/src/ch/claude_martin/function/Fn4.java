package ch.claude_martin.function;


/** {@code f : T -> U -> V -> W -> R } */
@FunctionalInterface
public interface Fn4<T, U, V, W, R> extends Fn<T, Fn3<U, V, W, R>> {
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
}