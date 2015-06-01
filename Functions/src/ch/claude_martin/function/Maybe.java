package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This holds a value (which might be null) or it holds an exception of unknown type.
 * 
 * Do not confuse this with {@link Optional}. The methods seem similar, but are different.
 * 
 * @author Claude Martin
 *
 * @param <T>
 */
public final class Maybe<T> {

  private final Object object;
  private final boolean hasValue;

  private Maybe(final Object value, final boolean hasValue) {
    this.object = value;
    this.hasValue = hasValue;
  }

  public static <T> Maybe<T> ofOptional(final Optional<T> opt) {
    try {
      return new Maybe<>(opt.get(), true);
    } catch (final Throwable t) {
      return new Maybe<>(t, false);
    }
  }

  public static <T> Maybe<T> ofValue(final T value) {
    return new Maybe<>(value, true);
  }

  public static <T> Maybe<T> ofException(final Throwable throwable) {
    requireNonNull(throwable, "throwable");
    return new Maybe<>(throwable, false);
  }

  @SuppressWarnings("unchecked")
  public T get() throws Throwable {
    if (!this.hasValue)
      throw (Throwable) this.object;
    return (T) this.object;
  }

  @SuppressWarnings("unchecked")
  public T orElse(final T other) {
    return this.hasValue ? (T) this.object : other;
  }

  @SuppressWarnings("unchecked")
  public T orElseGet(final Supplier<? extends T> other) {
    return this.hasValue ? (T) this.object : other.get();
  }

  @SuppressWarnings("unchecked")
  public void ifPresent(final Consumer<? super T> consumer) {
    requireNonNull(consumer, "consumer");
    if (this.hasValue)
      consumer.accept((T) this.object);
  }

  @SuppressWarnings("unchecked")
  public void ifElse(final Consumer<? super T> _if, final Consumer<? super Throwable> _else) {
    requireNonNull(_if, "_if");
    requireNonNull(_else, "_else");
    if (this.hasValue)
      _if.accept((T) this.object);
    else
      _else.accept((Throwable) this.object);
  }

  @SuppressWarnings("unchecked")
  public <U> Maybe<U> map(final Function<? super T, ? extends U> mapper) {
    requireNonNull(mapper, "mapper");
    if (!this.hasValue)
      return (Maybe<U>) this;
    try {
      return new Maybe<>(mapper.apply((T) this.object), true);
    } catch (final Throwable t) {
      return new Maybe<>(t, false);
    }
  }

  @SuppressWarnings("unchecked")
  public <U> Maybe<U> flatMap(final Function<? super T, Maybe<U>> mapper) {
    requireNonNull(mapper, "mapper");
    if (!this.hasValue)
      return (Maybe<U>) this;
    try {
      return mapper.apply((T) this.object);
    } catch (final Throwable t) {
      return new Maybe<>(t, false);
    }
  }

  public boolean isPresent() {
    return this.hasValue;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;

    if (!(obj instanceof Maybe))
      return false;

    final Maybe<?> other = (Maybe<?>) obj;
    return this.hasValue == other.hasValue && Objects.equals(this.object, other.object);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.object);
  }

  @Override
  public String toString() {
    return String.format("Maybe[%s]", this.object);
  }

  @SuppressWarnings("unchecked")
  public Optional<T> toOptional() {
    if (this.hasValue)
      return Optional.ofNullable((T) this.object);
    return Optional.empty();
  }

}
