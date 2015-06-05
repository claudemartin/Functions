package ch.claude_martin.function;

import java.util.Map.Entry;
import java.util.function.Function;

/** {@code f : T -> U -> R } */
@FunctionalInterface
public interface Fn2<T, U, R> extends Fn<T, Fn<U, R>> {
  /** Converts {@code Fn2<T,U,R>} to {@code Fn<Entry<T,U>,R>} */
  public default Fn<Entry<? extends T, ? extends U>, R> uncurry() {
    return Functions.uncurry(this);
  }

  public default R apply2(final T t, final U u) {
    return this.apply(t).apply(u);
  }

  /** This only works if R extends {@link Function}. */
  public default BiFn<T, U, R> toBiFn() {
    return this::apply2;
  }

}