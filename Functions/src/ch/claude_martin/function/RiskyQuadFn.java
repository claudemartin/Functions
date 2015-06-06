package ch.claude_martin.function;

import ch.claude_martin.function.Exceptions.SneakyException;

/** Creates a {@link TriFn function } from a risky function. That is one that throws checked
 * exceptions.
 * 
 * {@link #apply(Object)} will throw a {@link SneakyException} with the original checked exception
 * set as its cause.
 * 
 * @author Claude Martin */
@FunctionalInterface
public interface RiskyQuadFn<T, U, V, W, R> extends QuadFn<T, U, V, W, R> {

  /** Tries to invoke this function but throws a {@link RuntimeException} if execution fails. */
  @Override
  public default R apply4(final T t, final U u, final V v, final W w) {
    try {
      return this.tryApply4(t, u, v, w);
    } catch (final Throwable x) {
      throw SneakyException.of(x);
    }
  };

  public abstract R tryApply4(final T t, final U u, final V v, final W w) throws Throwable;


}