package ch.claude_martin.function;

import java.util.function.Supplier;

import ch.claude_martin.function.Exceptions.SneakyException;

public interface RiskySupplier<T> extends Supplier<T> {
  public abstract T tryGet() throws Throwable;

  @Override
  public default T get() {
    try {
      return this.tryGet();
    } catch (final Throwable x) {
      throw SneakyException.of(x);
    }
  }
}
