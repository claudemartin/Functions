package ch.claude_martin.function;

import ch.claude_martin.function.tuple.Triplet;

/** {@code f : T -> U -> V -> R } */
@FunctionalInterface
public interface Fn3<T, U, V, R> extends Fn<T, Fn2<U, V, R>> {

  public static <T, U, V, R> Fn3<T, U, V, R> of(final TriFn<T, U, V, R> f) {
    return Functions.curry(f::apply3);
  }

  public default Fn<V, R> apply2(final T t, final U u) {
    return this.apply(t).apply(u);
  }

  public default R apply3(final T t, final U u, final V v) {
    return this.apply(t).apply(u).apply(v);
  }

  /** Converts {@code Fn3<T,U,V,R>} to {@code Fn<Triplet<T,U,V>,R>} */
  public default Fn<Triplet<T, U, V>, R> uncurry() {
    return Functions.uncurry3(this);
  }

  public default TriFn<T, U, V, R> toTriFn() {
    return this::apply3;
  }

}