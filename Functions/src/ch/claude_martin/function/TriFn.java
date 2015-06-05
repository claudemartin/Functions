package ch.claude_martin.function;


@FunctionalInterface
public interface TriFn<T, U, V, R> {

  public default BiFn<U, V, R> apply1(final T t) {
    return (u, v) -> this.apply3(t, u, v);
  }

  public default Fn<V, R> apply2(final T t, final U u) {
    return v -> this.apply3(t, u, v);
  }

  public abstract R apply3(final T t, final U u, final V v);

  public default Fn3<T, U, V, R> curry() {
    return Functions.curry(this);
  }

}
