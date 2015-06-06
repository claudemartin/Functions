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
public interface RiskyTriFn<T, U, V, R> extends TriFn<T, U, V, R> {

  /** Tries to invoke this function but throws a {@link RuntimeException} if execution fails. */
  @Override
  public default R apply3(final T t, final U u, final V v) {
    try {
      return this.tryApply3(t, u, v);
    } catch (final Throwable x) {
      throw SneakyException.of(x);
    }
  };

  public abstract R tryApply3(final T t, final U u, final V v) throws Throwable;


}