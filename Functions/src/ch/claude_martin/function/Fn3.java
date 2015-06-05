package ch.claude_martin.function;


/** {@code f : T -> U -> V -> R } */
@FunctionalInterface
public interface Fn3<T, U, V, R> extends Fn<T, Fn2<U, V, R>> {
  public default Fn<V, R> apply2(final T t, final U u) {
    return this.apply(t).apply(u);
  }

  public default R apply3(final T t, final U u, final V v) {
    return this.apply(t).apply(u).apply(v);
  }

  public default TriFn<T, U, V, R> toTriFn() {
    return this::apply3;
  }
}