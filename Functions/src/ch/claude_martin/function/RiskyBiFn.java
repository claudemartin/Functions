package ch.claude_martin.function;

import ch.claude_martin.function.Exceptions.SneakyException;

/** Creates a {@link BiFn function } from a risky function. That is one that throws checked
 * exceptions.
 * 
 * {@link #apply(Object)} will throw a {@link SneakyException} with the original checked exception
 * set as its cause.
 * 
 * @author Claude Martin */
@FunctionalInterface
public interface RiskyBiFn<T, U, R> extends BiFn<T, U, R> {

  /** Tries to invoke this function but throws a {@link RuntimeException} if execution fails. */
  @Override
  public default R apply2(final T t, final U u) {
    try {
      return this.tryApply2(t, u);
    } catch (final Throwable x) {
      throw SneakyException.of(x);
    }
  };

  public abstract R tryApply2(final T t, final U u) throws Throwable;

}