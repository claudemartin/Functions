package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import ch.claude_martin.function.Exceptions.SneakyException;

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

  @Override
  public default BiFn<T, U, R> sneaky() {
    return Exceptions.sneaky(this);
  }

  @Override
  public default BiFn<T, U, R> orElse(final R value) {
    requireNonNull(value, "value");
    return Exceptions.orElse(this, value).andThen((final R r) -> r == null ? value : r);
  }

  public default BiFn<T, U, R> orElse(final R ifNull, final R ifException) {
    return Exceptions.orElse(this, ifNull, ifException);
  }

  public default BiFn<T, U, R> orElseGet(final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    return Exceptions.orElseGet(this, ifNull, ifException);
  }

  @Override
  public default BiFn<T, U, R> orNull() {
    return Exceptions.orNull(this);
  }

  public default BiFn<T, U, R> log(final Consumer<Throwable> logger) {
    return Exceptions.log(this, logger);
  }

  public default BiFn<T, U, Maybe<R>> toMaybe(final RiskyBiFn<T, U, R> f) {
    return Exceptions.toMaybe(this);
  }

  public default BiFn<T, U, R> named(final String name) {
    return Exceptions.named(this, name);
  }

}