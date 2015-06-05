package ch.claude_martin.function;

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

}
