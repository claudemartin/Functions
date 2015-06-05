package ch.claude_martin.function;

import java.util.Map.Entry;

/** {@code f : T -> U -> R } */
@FunctionalInterface
public interface Fn2<T, U, R> extends Fn<T, Fn<U, R>> {

  public static <T, U, R> Fn2<T, U, R> of(final BiFn<T, U, R> f) {
    return Functions.curry(f::apply2);
  }

  /** Converts {@code Fn2<T,U,R>} to {@code Fn<Entry<T,U>,R>} */
  public default Fn<Entry<T, U>, R> uncurry() {
    return Functions.uncurry2(this);
  }

  public default R apply2(final T t, final U u) {
    return this.apply(t).apply(u);
  }

  @Override
  public default BiFn<T, U, R> toBiFn() {
    return this::apply2;
  }

}