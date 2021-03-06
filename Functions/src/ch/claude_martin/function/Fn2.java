package ch.claude_martin.function;

import java.util.Map.Entry;

import ch.claude_martin.function.tuple.Pair;
import ch.claude_martin.function.tuple.Tuple;

/** {@code f : T -> U -> R } */
@FunctionalInterface
public interface Fn2<T, U, R> extends Fn<T, Fn<U, R>> {
  /** @see BiFn#curry() */
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

  @SuppressWarnings("unchecked")
  @Override
  public default R applyTuple(final Tuple<?> t) {
    if (t instanceof Pair)
      return ((Pair<T, U>) t).applyTo(this);
    throw new IllegalArgumentException();
  }

  @Override
  public default BiFn<T, U, R> toBiFn() {
    return this::apply2;
  }

  public default Fn<T, R> set2nd(final U second) {
    return first -> this.apply(first).apply(second);
  }

}