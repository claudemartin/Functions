package ch.claude_martin.function;

import ch.claude_martin.function.Exceptions.SneakyException;

/** Creates a {@link Fn function } from a risky function. That is one that throws checked exceptions.
 * 
 * {@link #apply(Object)} will throw a {@link SneakyException} with the original checked exception
 * set as its cause.
 * 
 * @author Claude Martin */
@FunctionalInterface
public interface RiskyFn<T, R> extends Fn<T, R> {

  /** Tries to invoke this function but throws a {@link SneakyException} if execution fails. */
  @Override
  public default R apply(final T t) {
    try {
      return this.tryApply(t);
    } catch (final Throwable x) {
      throw SneakyException.of(x);
    }
  };

  public abstract R tryApply(final T t) throws Throwable;

}