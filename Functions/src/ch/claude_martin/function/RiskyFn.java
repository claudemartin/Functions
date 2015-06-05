package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import ch.claude_martin.function.Exceptions.SneakyException;

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

  /** Converts a checked exception to an unchecked (sneaky) exception. */
  @Override
  public default Fn<T, R> sneaky() {
    return Exceptions.sneaky(this);
  }

  @Override
  public default Fn<T, R> orElse(final R value) {
    requireNonNull(value, "value");
    return Exceptions.orElse(this, value).andThen((final R r) -> r == null ? value : r);
  }

  public default Fn<T, R> orElse(final R ifNull, final R ifException) {
    return Exceptions.orElse(this, ifNull, ifException);
  }

  public default Fn<T, R> orElseGet(final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    return Exceptions.orElseGet(this, ifNull, ifException);
  }

  @Override
  public default Fn<T, R> orNull() {
    return Exceptions.orNull(this);
  }

  public default Fn<T, R> retry() {
    return Exceptions.retry(this);
  }

  public default Fn<T, R> log(final Consumer<Throwable> logger) {
    return Exceptions.log(this, logger);
  }

  public default Fn<T, Maybe<R>> toMaybe(final RiskyFn<T, R> f) {
    return Exceptions.toMaybe(this);
  }

}